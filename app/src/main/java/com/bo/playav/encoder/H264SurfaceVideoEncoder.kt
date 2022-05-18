package com.bo.playav.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import com.bo.playav.toHex
import org.java_websocket.util.ByteBufferUtils
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.experimental.and

class H264SurfaceVideoEncoder : Runnable{
    private val TAG: String = "encoder"
    private lateinit var surface: Surface
    private lateinit var codec: MediaCodec
    private var running: AtomicBoolean = AtomicBoolean(false)

    private var listener:OnDataEncodedListener? = null
    private lateinit var sps_pps_buffer: ByteArray

    fun start():Surface {
        codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        val format: MediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
        1080, 1920)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 2000*1000)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)

        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        surface = codec.createInputSurface()
        running.set(true)
        Thread(this).start()
        return surface
    }

    fun setOnDataEncodedListener(listener:OnDataEncodedListener) {
        this.listener = listener
    }


    //thread not safe
    fun stop() {
        running.set(false)
    }


    override fun run() {
        codec.start()
        val mediaInfo:MediaCodec.BufferInfo = MediaCodec.BufferInfo()
        while (running.get()) {
            val outputIndex = codec.dequeueOutputBuffer(mediaInfo, 1000)
            if (outputIndex >= 0) {
                codec.getOutputBuffer(outputIndex)?.let {
                    var offset = 4
                    if (it.get(2).toInt() == 0x01) {
                        offset = 3
                    }

                    //sps
                    var type = it.get(offset).toInt() and  0x1F
                    Log.d("encoder", "type: $type")
                    Log.d("player", "send: ${it.get(0)} " +
                            " ${it.get(1)}" +
                            " ${it.get(2)}" +
                            " ${it.get(3)}" +
                            " ${it.get(4)}" +
                            " ${it.get(5)}")
                    if (type == 7) {
                        sps_pps_buffer = ByteArray(it.remaining())
                        it.get(sps_pps_buffer)
                    } else if (type == 5) {
                        val ret = ByteBuffer.allocate(sps_pps_buffer.size)
                        ret.put(sps_pps_buffer, 0, sps_pps_buffer.size)
                        ret.flip()
                        listener?.onDataEncoded(ret)
                        listener?.onDataEncoded(it)
                    } else {
                        listener?.onDataEncoded(it)
                    }
                }
                codec.releaseOutputBuffer(outputIndex, false)
            }
        }
        codec.stop()
    }

    interface OnDataEncodedListener {
        fun onDataEncoded(data: ByteBuffer)
    }
}