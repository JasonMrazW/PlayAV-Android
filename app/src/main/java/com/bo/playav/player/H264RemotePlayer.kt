package com.bo.playav.player

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import com.bo.playav.net.OnReceiveFrameListener
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class H264RemotePlayer() : HandlerThread("h264 player"), OnReceiveFrameListener {

    lateinit var codec:MediaCodec
    val running:AtomicBoolean = AtomicBoolean(false)
    private var handler: Handler? = null

    fun start(surface: Surface, width:Int = 1920, height:Int = 1080) {
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,width, height)
        try {
            codec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            codec.configure(format, surface, null, 0)
        } catch (ex:Exception) {
            Log.d("player", "init decoder failed: ${ex.message}")
        }
        codec.start()
        start()

        handler = Handler(looper)
    }


    override fun run() {
        running.set(true)
        super.run()
    }

    fun stopPlaying() {
        running.set(false)
        codec.stop()
        interrupt()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onReceiveFrame(message: ByteBuffer?) {
        handler?.post {
            message?.apply {
                try {
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

                    var outputIndex = codec.dequeueOutputBuffer(bufferInfo, 1000)
                    while (outputIndex >= 0) {
                        codec.releaseOutputBuffer(outputIndex, true)
                        outputIndex = codec.dequeueOutputBuffer(bufferInfo, 1000)
                    }
                } catch (ex: MediaCodec.CodecException) {
                    Log.e("player", "codec error: ${ex.diagnosticInfo}")
                    Log.e("player", "codec error: ${ex.errorCode}")
                }

            }
        }
    }
}