package com.bo.playav.net

import java.nio.ByteBuffer

interface OnReceiveMessageListener {
    fun onReceive(data: ByteBuffer?)
}