package com.bo.playav.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import com.bo.playav.toHex
import java.util.concurrent.atomic.AtomicBoolean

class H264FrameVideoEncoder: Runnable {

    private val TAG: String = "h264FrameEncoder"
    private lateinit var codec:MediaCodec
    private var running = AtomicBoolean(false)
    private var frameIndex = 0
    private val frameInterval = 1000*1000L/30 //一帧的时长，单位：微秒

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
        running.set(true)
        Thread(this).start()
    }

    fun encodeFrame(data: ByteArray) {
        val inputIndex = codec.dequeueInputBuffer(1000)
        if (inputIndex >=0) {
            val byteBuffer = codec.getInputBuffer(inputIndex)
            byteBuffer?.apply {
                clear()
                put(data)
            }
            codec.queueInputBuffer(inputIndex, 0, data.size, frameInterval*frameIndex,0)
            frameIndex++
        }
    }

    override fun run() {

        val mediaInfo:MediaCodec.BufferInfo = MediaCodec.BufferInfo()

        while (running.get()) {
            val outputIndex = codec.dequeueOutputBuffer(mediaInfo, 1000)
            if (outputIndex >= 0) {
                codec.getOutputBuffer(outputIndex)?.let {
                    val byteArray = ByteArray(it.remaining())
                    it.get(byteArray)
                    Log.d(TAG, "${byteArray.toHex()})")
                }
                codec.releaseOutputBuffer(outputIndex, false)
            }
        }
        codec.stop()
    }
}