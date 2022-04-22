package com.bo.playav.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.util.concurrent.atomic.AtomicBoolean

class H264VideoEncoder : Runnable{
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

    val hexChars = "0123456789abcdef".toCharArray()

    fun ByteArray.toHex4(): String {
        val hex = CharArray(2 * this.size)
        this.forEachIndexed { i, byte ->
            val unsigned = 0xff and byte.toInt()
            hex[2 * i] = hexChars[unsigned / 16]
            hex[2 * i + 1] = hexChars[unsigned % 16]
        }

        return hex.joinToString("")
    }

    fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

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