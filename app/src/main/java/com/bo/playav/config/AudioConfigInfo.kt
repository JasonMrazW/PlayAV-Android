package com.bo.playav.config

import android.media.*

class AudioConfigInfo {
    companion object {
        const val AAC_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC
        const val SAMPLE_RATE = 44100
        const val CHANNEL_COUNT = 2
        const val CHANNEL_LAYOUT = AudioFormat.CHANNEL_IN_STEREO
        const val FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val SOURCE = MediaRecorder.AudioSource.MIC
        const val AUDIO_BITRATE = 200_000
    }
}