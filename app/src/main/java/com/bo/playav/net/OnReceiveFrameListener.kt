package com.bo.playav.net

import java.nio.ByteBuffer

interface OnReceiveFrameListener {
    fun onReceiveFrame(data: ByteBuffer?)
}