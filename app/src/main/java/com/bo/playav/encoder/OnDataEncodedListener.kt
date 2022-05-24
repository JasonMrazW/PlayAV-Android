package com.bo.playav.encoder

import java.nio.ByteBuffer

interface OnDataEncodedListener {
    /**
     * 视频数据编码好
     */
    fun onVideoDataEncoded(data: ByteBuffer)

    /**
     * 音频数据编码好
     */
    fun onAudioDataEncoded(data: ByteBuffer)
}