package com.bo.playav.player

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import com.bo.playav.net.LiveSocketClient
import com.bo.playav.net.OnReceiveMessageListener
import com.bo.playav.toHex
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Handler
import kotlin.experimental.and

class H264RemotePlayer : Runnable , OnReceiveMessageListener {

    lateinit var codec:MediaCodec
    val running:AtomicBoolean = AtomicBoolean(false)

    fun start(surface: Surface, width:Int = 1920, height:Int = 1080) {
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,width, height)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
        try {
            codec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            codec.configure(format, surface, null, 0)
        } catch (ex:Exception) {
            Log.d("player", "init decoder failed: ${ex.message}")
        }
        //Thread(this).start()
        codec.start()
    }


    override fun run() {
        running.set(true)
    }

    fun stop() {
        running.set(false)
        codec.stop()
    }

    override fun onReceive(message: ByteBuffer?) {
        message?.apply {
            val index = codec.dequeueInputBuffer(1000)
            if (index >= 0) {
                val inputBuffer = codec.getInputBuffer(index)
                val size = this.remaining()
                val byteArray = ByteArray(size)
                this.get(byteArray)

                //analysis input frame data
                var offset = 4;
                if (byteArray.get(2).toInt() == 0x01) {
                    offset = 3;
                }
                val type = byteArray.get(offset).toInt() and 0x1F
                //Log.d("player", "type: $type")

                inputBuffer?.put(byteArray, 0, size)
                codec.queueInputBuffer(index,0, size, 0, 0)
            }

            val bufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
            val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 1000)
            while (outputIndex >= 0) {
                codec.releaseOutputBuffer(outputIndex, true)
            }
        }
    }
}