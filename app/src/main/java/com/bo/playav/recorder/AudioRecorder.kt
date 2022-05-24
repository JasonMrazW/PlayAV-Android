package com.bo.playav.recorder

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.RECORDSTATE_RECORDING
import android.util.Log
import com.bo.playav.config.AudioConfigInfo.Companion.CHANNEL_COUNT
import com.bo.playav.config.AudioConfigInfo.Companion.CHANNEL_LAYOUT
import com.bo.playav.config.AudioConfigInfo.Companion.FORMAT
import com.bo.playav.config.AudioConfigInfo.Companion.SAMPLE_RATE
import com.bo.playav.config.AudioConfigInfo.Companion.SOURCE
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class AudioRecorder : Runnable{
    private lateinit var recorder:AudioRecord
    var bufferSize = 0
    private val running = AtomicBoolean(false)
    private lateinit var audioBuffer: ByteBuffer
    var listener: OnPCMDataAvaliableListener? = null

    @SuppressLint("MissingPermission")
    fun start() {
        if (running.get()) {
            stop()
        }

        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_LAYOUT, FORMAT)*4
        recorder = AudioRecord(SOURCE, SAMPLE_RATE,CHANNEL_LAYOUT, FORMAT, bufferSize)

        audioBuffer = ByteBuffer.allocateDirect(bufferSize)
        running.set(true)
        Thread(this).start()
    }

    override fun run() {
        recorder.startRecording()
        while (running.get()) {
            if (recorder.recordingState != RECORDSTATE_RECORDING) {
                continue
            }
            audioBuffer.clear()
            val length = recorder.read(audioBuffer,  bufferSize)
            Log.d("AudioRecorder", "read one frame: $length")
            if (length > 0) {
                //send to encoder
                listener?.onPCMAvaliable(audioBuffer, length)
            } else {
                break
            }
        }
        recorder.stop()
        recorder.release()
    }

    fun stop() {
        running.set(false)
    }

    /**
     * 查找可用的音频录制器
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    private fun findAudioRecord(): AudioRecord? {
        val samplingRates = intArrayOf(44100, 22050, 11025, 8000)
        val audioFormats = intArrayOf(
            AudioFormat.ENCODING_PCM_16BIT,
            AudioFormat.ENCODING_PCM_8BIT
        )
        val channelConfigs = intArrayOf(
            AudioFormat.CHANNEL_IN_STEREO,
            AudioFormat.CHANNEL_IN_MONO
        )
        for (rate in samplingRates) {
            for (format in audioFormats) {
                for (config in channelConfigs) {
                    try {
                        val bufferSize = AudioRecord.getMinBufferSize(rate, config, format)
                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            val recorder =
                                AudioRecord(SOURCE, rate, config, format, bufferSize * 4)
                            if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                                Log.d("AudioRecorder", "find... $rate, $config, $format, $bufferSize")
                                return recorder
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("AudioRecorder", "Init AudioRecord Error." + Log.getStackTraceString(e))
                    }
                }
            }
        }
        return null
    }
}