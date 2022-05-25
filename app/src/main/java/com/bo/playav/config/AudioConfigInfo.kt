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
        const val PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC

        fun buildAudioFormat():MediaFormat {
            val format = MediaFormat.createAudioFormat(AAC_TYPE, SAMPLE_RATE, CHANNEL_COUNT)
                format.setInteger(MediaFormat.KEY_AAC_PROFILE, PROFILE)
                format.setInteger(MediaFormat.KEY_CHANNEL_MASK, CHANNEL_LAYOUT)
                format.setInteger(MediaFormat.KEY_BIT_RATE, AUDIO_BITRATE)
                format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, CHANNEL_COUNT)
                format.setInteger(MediaFormat.KEY_SAMPLE_RATE, SAMPLE_RATE)
            return format
        }
    }
}