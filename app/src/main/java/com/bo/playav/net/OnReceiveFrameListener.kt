package com.bo.playav.net

import java.nio.ByteBuffer

interface OnReceiveFrameListener {
    fun onReceiveFrame(data: ByteBuffer?)

    companion object {
        const val VIDEO:Byte = 0
        const val AUDIO:Byte = 1
    }
}