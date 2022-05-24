package com.bo.playav.recorder

import java.nio.ByteBuffer

interface OnPCMDataAvaliableListener {
    fun onPCMAvaliable(data: ByteBuffer, length:Int)
}