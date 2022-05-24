package com.bo.playav.encoder

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import com.bo.playav.config.AudioConfigInfo
import com.bo.playav.recorder.OnPCMDataAvaliableListener
import com.bo.playav.toHex
import com.bo.playav.utils.YUVUtil
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates

class AACAudioEncoder : Runnable, OnPCMDataAvaliableListener{

    private lateinit var codec:MediaCodec
    private val running = AtomicBoolean(false)

    private var listener: OnDataEncodedListener? = null
    private var pts by Delegates.notNull<Long>()

    companion object {
        private const val TAG = "AACAudioEncoder"
    }

    fun start(bufferSize:Int) {
        codec = MediaCodec.createEncoderByType(AudioConfigInfo.AAC_TYPE)

        val format = MediaFormat.createAudioFormat(AudioConfigInfo.AAC_TYPE, AudioConfigInfo.SAMPLE_RATE,
            AudioConfigInfo.CHANNEL_COUNT)
        try {
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            format.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioConfigInfo.CHANNEL_LAYOUT)
            format.setInteger(MediaFormat.KEY_BIT_RATE, AudioConfigInfo.AUDIO_BITRATE)
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2)
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, AudioConfigInfo.SAMPLE_RATE)
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, bufferSize * 2)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            Thread(this).start()
        } catch (ex: IllegalArgumentException) {
            Log.e(TAG, "init audio encoder failed cause of ${ex.message}")
        }

    }

    fun stop() {
        running.set(false)
    }

    fun setOnDataEncodedListener(l: OnDataEncodedListener) {
        listener = l
    }

    override fun run() {
        running.set(true)
        codec.start()
        val bufferInfo = MediaCodec.BufferInfo()
        while (running.get()) {
            //send data to codec
            val index = codec.dequeueOutputBuffer(bufferInfo, 1000)
            if (index >= 0) {
                val data = codec.getOutputBuffer(index)

                data?.let {
                    //to local file
                    val aacLength = it.remaining()
                    val aacHeaderLength = 7
                    val frameSize = aacLength + aacHeaderLength
                    val byteArray = ByteArray(frameSize)
                    //add header
                    addADTStoPacket(byteArray, 2, 4, 2, frameSize)
                    //add aac body
                    it.get(byteArray, 7, aacLength)

                    YUVUtil.writeBytes(byteArray, "out.aac")

                    listener?.onAudioDataEncoded(it)

                    it.clear()
                }
                codec.releaseOutputBuffer(index, false)
            }
        }
        codec.stop()
    }

    /**
     * 添加ADTS头
     *
     * @param packet
     * @param packetLen
     */
    private fun addADTStoPacket(packet: ByteArray, profile:Int, frequence:Int, channelLayout:Int, packetLen: Int) {
        val profile = profile // AAC LC
        val freqIdx = frequence // 44100KHz
        val chanCfg = channelLayout // STERO

        // fill in ADTS data
        packet[0] = 0xFF.toByte()
        packet[1] = 0xF9.toByte()
        packet[2] = ((profile - 1 shl 6) + (freqIdx shl 2) + (chanCfg shr 2)).toByte()
        packet[3] = ((chanCfg and 3 shl 6) + (packetLen shr 11)).toByte()
        packet[4] = (packetLen and 0x7FF shr 3).toByte()
        packet[5] = ((packetLen and 7 shl 5) + 0x1F).toByte()
        packet[6] = 0xFC.toByte()
    }

    override fun onPCMAvaliable(data: ByteBuffer, length:Int) {
        var offset = 0

        while (offset < length) {
            val index = codec.dequeueInputBuffer(1000)
            if (index >= 0) {
                val buffer = codec.getInputBuffer(index)
                buffer?.let {
                    var bufferLength = 0
                    if ((offset + it.remaining()) < length) {
                        bufferLength = it.remaining()
                    } else {
                        bufferLength = length - offset
                    }
                    val tmp = ByteArray(bufferLength)
                    data.get(tmp)
                    it.put(tmp)
                    pts = (System.nanoTime()) / 1000L
                    codec.queueInputBuffer(index, 0, bufferLength, pts, 0)

                    offset += bufferLength
                }

            }
        }
    }
}