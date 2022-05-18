package com.bo.playav.player

import android.content.res.AssetManager
import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.util.concurrent.atomic.AtomicBoolean

class H264LocalPlayer(var assets: AssetManager) : Runnable{
    private val TAG: String = "H264Player"
    lateinit var surface: Surface
    lateinit var codec: MediaCodec
    lateinit var filePath: String

    var running:AtomicBoolean = AtomicBoolean(false)

    fun start(surface: Surface, path: String) {
        this.surface = surface
        this.filePath = path

        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 1080, 1920)
        codec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        codec.configure(format, surface, null, 0)

        Thread(this).start()
    }

    fun stop() {
        running.set(false)
    }


    override fun run() {
        running.set(true)
        codec.start()
        val bytes = readFrames(filePath)

        var currentFrameIndex = 0;
        var nextFrameIndex = 0;
        val mediaInfo:MediaCodec.BufferInfo = MediaCodec.BufferInfo()

        while(running.get()) {
            nextFrameIndex = nextFrameStart(bytes, currentFrameIndex+2)
            Log.d(TAG, "$nextFrameIndex")
            if (nextFrameIndex < 0) {
                break
            }

            val inputIndex = codec.dequeueInputBuffer(1000);
            if (inputIndex >= 0) {
                val byteBuffer = codec.getInputBuffer(inputIndex)
                val length = nextFrameIndex - currentFrameIndex
                byteBuffer?.put(bytes, currentFrameIndex, length)
                codec.queueInputBuffer(inputIndex, 0 , length, 0, 0)
                currentFrameIndex = nextFrameIndex
            }


            val outputIndex = codec.dequeueOutputBuffer(mediaInfo, 1000)
            if (outputIndex >= 0) {
                codec.releaseOutputBuffer(outputIndex, true)
            }
            Thread.sleep(30)
        }

        codec.stop()
    }

    fun readFrames(filePath:String):ByteArray {
        val inputStream = this.assets.open(filePath)
        val ret:ByteArray = inputStream.readBytes()
        inputStream.close()
        return ret
    }

    fun nextFrameStart(source: ByteArray, start: Int): Int {
        var i = start;
        val a_value = (0x00).toByte()
        val b_value = (0x01).toByte()
        while (i <= source.size - 4) {
            if ((source[i] == a_value && source[i+1] == a_value && source[i+2] == a_value
                        && source[i+3] == b_value) ||
                (source[i] == a_value && source[i+1] == a_value && source[i+2] == b_value)) {
                return i
            }
            i++
        }

        return  -1
    }
}