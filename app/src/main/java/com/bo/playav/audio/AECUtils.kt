package com.bo.playav.audio

import android.media.audiofx.AcousticEchoCanceler
import android.util.Log

class AECUtils {
    companion object {
        /**
         * 判断当前设备是否支持AcousticEchoCanceler
         */
        fun isAcousticEchoCancelerAvailable(): Boolean {
            return AcousticEchoCanceler.isAvailable()
        }

        fun initAEC(sessionId: Int) {
            if (isAcousticEchoCancelerAvailable()) {
                val acousticEchoCanceler = AcousticEchoCanceler.create(sessionId)
                if (acousticEchoCanceler == null) {
                    Log.d("AEC", "create AcousticEchoCanceler failed.")
                } else {
                    acousticEchoCanceler.setEnabled(true)
                }
            }
        }
    }
}