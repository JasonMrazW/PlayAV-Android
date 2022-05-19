package com.bo.playav.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class H264FrameVideoEncoder: Runnable {

    private val TAG: String = "h264FrameEncoder"
    private lateinit var codec:MediaCodec
    private var running = AtomicBoolean(false)
    private var frameIndex = 0
    private val frameInterval = 1000*1000L/30 //一帧的时长，单位：微秒
    private var listener:OnDataEncodedListener? = null
    private lateinit var sps_pps_buffer: ByteArray
    private var codecType:String = MediaFormat.MIMETYPE_VIDEO_AVC

    private val AVC_I_TYPE = 5
    private val AVC_SPS_TYPE = 7
    private val HEVC_I_TYPE = 19
    private val HEVC_VPS_TYPE = 32

    fun start(width: Int, height: Int) {
        codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        val format: MediaFormat = MediaFormat.createVideoFormat(
            MediaFormat.MIMETYPE_VIDEO_AVC,
            width, height)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 2000*1000)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)

        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()
        Thread(this).start()
    }

    fun encodeFrame(data: ByteArray) {
        if (!running.get()) return
        try {
            val inputIndex = codec.dequeueInputBuffer(1000_000)

            if (inputIndex >=0) {
                val byteBuffer = codec.getInputBuffer(inputIndex)
                byteBuffer?.apply {
                    clear()
                    put(data)
                }
                codec.queueInputBuffer(inputIndex, 0, data.size, frameInterval*frameIndex,0)
                frameIndex++
            }
        }catch (ex:RuntimeException) {
            Log.e("encoder", "${ex.message}")
        }
    }

    fun setOnDataEncodedListener(listener:OnDataEncodedListener) {
        this.listener = listener
    }

    override fun run() {
        running.set(true)
        val mediaInfo:MediaCodec.BufferInfo = MediaCodec.BufferInfo()
        while (running.get()) {
            try {
            val outputIndex = codec.dequeueOutputBuffer(mediaInfo, 1000)
            if (outputIndex >= 0) {
                codec.getOutputBuffer(outputIndex)?.let {
                    var offset = 4
                    if (it.get(2).toInt() == 0x01) {
                        offset = 3
                    }

                    var type:Int
                    when (codecType) {
                        MediaFormat.MIMETYPE_VIDEO_HEVC -> {
                            //sps
                            type = (it.get(offset).toInt() and  0x7E) shr  1
                            //vps
                            if (type == HEVC_VPS_TYPE) {
                                sps_pps_buffer = ByteArray(it.remaining())
                                it.get(sps_pps_buffer)
                            } else if (type == HEVC_I_TYPE) {
                                val ret = ByteBuffer.allocate(sps_pps_buffer.size)
                                ret.put(sps_pps_buffer, 0, sps_pps_buffer.size)
                                ret.flip()
                                listener?.onDataEncoded(ret)
                                listener?.onDataEncoded(it)
                            } else {
                                listener?.onDataEncoded(it)
                            }
                        }
                        MediaFormat.MIMETYPE_VIDEO_AVC -> {
                            //sps
                            var type = it.get(offset).toInt() and  0x1F
                            if (type == AVC_SPS_TYPE) {
                                sps_pps_buffer = ByteArray(it.remaining())
                                it.get(sps_pps_buffer)
                            } else if (type == AVC_I_TYPE) {
                                Log.d("player", "send data: I Frame encoded")
                                val ret = ByteBuffer.allocate(sps_pps_buffer.size)
                                ret.put(sps_pps_buffer, 0, sps_pps_buffer.size)
                                ret.flip()
                                listener?.onDataEncoded(ret)
                                listener?.onDataEncoded(it)
                            } else {
                                listener?.onDataEncoded(it)
                            }
                        }
                        else -> {}
                    }
                }
                codec.releaseOutputBuffer(outputIndex, false)
            }
            }catch (ex:RuntimeException) {
                Log.e("encoder", "xxxx:${ex.message}")
            }
        }
        codec.stop()
    }
}