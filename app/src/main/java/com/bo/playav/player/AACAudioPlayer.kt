package com.bo.playav.player

import android.media.*
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.annotation.RequiresApi
import com.bo.playav.audio.AECUtils
import com.bo.playav.config.AudioConfigInfo
import com.bo.playav.net.OnReceiveFrameListener
import com.bo.playav.utils.YUVUtil
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class AACAudioPlayer: Runnable, OnReceiveFrameListener{

    private lateinit var codec:MediaCodec
    private lateinit var audioTrack: AudioTrack
    private val running = AtomicBoolean(false)
    private val playerThread = HandlerThread("aac audio player")
    private lateinit var playerHandler: Handler
    var audioSessionId: Int? = null

    companion object {
        val TAG: String = "AAC Audio Player"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun start() {

        val format = AudioConfigInfo.buildAudioFormat()

        val byteArray = ByteArray(2)

        // csd-0:是MediaCodec的一个参数，不是AAC的标准。不需要 adts heaer，mediacodec配置一次即可
        // codec specific data
        // AAC Profile 5bits | sample rate 4bits | channel count 4bits | other 3bits |
        // profile = 0x02, sample rate = 0x04, channel count = 0x02 , other = 0x00
        //    uint8_t csd[2];
        byteArray[0] = ((0x02 shl 3) or (0x04 shr 1)).toByte()
        byteArray[1] = (((0x04 shl 7) and 0x80) or (0x02 shl 3)).toByte()
        val csd_0 = ByteBuffer.wrap(byteArray)

        format.setByteBuffer("csd-0", csd_0);
        format.setInteger(MediaFormat.KEY_IS_ADTS, 1)

        val bufferSize = AudioTrack.getMinBufferSize(AudioConfigInfo.SAMPLE_RATE,
            AudioConfigInfo.CHANNEL_LAYOUT, AudioFormat.ENCODING_PCM_16BIT) * 2
        try {
            //init codec info
            codec = MediaCodec.createDecoderByType(AudioConfigInfo.AAC_TYPE)
            codec.configure(format, null, null, 0)

            //init AudioTrack
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setChannelMask(AudioConfigInfo.CHANNEL_LAYOUT)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(AudioConfigInfo.SAMPLE_RATE)
                .build()

            val builder = AudioTrack.Builder()
                .setAudioAttributes(audioAttributes)
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSize)
                .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)

            audioSessionId?.let {
                builder.setSessionId(it)
                //开启回声消除
                AECUtils.initAEC(it)
            }

            audioTrack = builder.build()

            Thread(this).start()

            //init audio track thread

        } catch (ex: IllegalArgumentException) {
            Log.e(TAG, "init audio decoder failed cause of ${ex.message}")
        }
    }

    fun stop() {
        running.set(false)
    }

    override fun run() {
        running.set(true)
        codec.start()

        playerThread.start()
        playerHandler = Handler(playerThread.looper)
        playerHandler.post {
            audioTrack.play()
        }

        val bufferInfo = MediaCodec.BufferInfo()
        while (running.get()) {
            try {
                val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 1000)

                if (outputIndex >= 0) {
                    val buffer = codec.getOutputBuffer(outputIndex)
                    buffer?.let {
                        //send to audio track
                        playerHandler.post {
                            audioTrack.write(it, it.remaining(), AudioTrack.WRITE_BLOCKING)
                        }
                    }
                    codec.releaseOutputBuffer(outputIndex, false)
                }
            } catch (ex:IllegalStateException) {
                Log.e(TAG, "decode failed case of: ${ex.message}")
            }
        }
        codec.stop()
        codec.release()
        playerHandler.post {
            audioTrack.stop()
            audioTrack.release()
        }

        playerThread.interrupt()
    }

    override fun onReceiveFrame(data: ByteBuffer?) {
        data?.apply {
            val index = codec.dequeueInputBuffer(1000)
            if (index >= 0) {
                var size = data.remaining()
                val byteArray = ByteArray(size)
                data.get(byteArray)

//                YUVUtil.writeBytes(byteArray, "out2.aac")

//                size -= 7
                val buffer = codec.getInputBuffer(index)
//                buffer?.put(byteArray, 7, size)
                buffer?.put(byteArray)
                codec.queueInputBuffer(index,0, size, 0,0)
            }
        }
    }
}