package com.bo.playav.encoder

import java.nio.ByteBuffer

interface OnDataEncodedListener {
    fun onDataEncoded(data: ByteBuffer)
}