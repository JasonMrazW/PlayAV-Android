package com.bo.playav.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import com.bo.playav.toHex
import java.util.concurrent.atomic.AtomicBoolean

class H264SurfaceVideoEncoder : Runnable{
    private val TAG: String = "encoder"
    private lateinit var surface: Surface
    private lateinit var codec: MediaCodec
    private var running: AtomicBoolean = AtomicBoolean(false)


    fun start():Surface {
        codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        val format: MediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
        1080, 1920)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 30)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 2000*1000)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)

        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        surface = codec.createInputSurface()
        running.set(true)
        Thread(this).start()
        return surface
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