package com.bo.playav.net

import org.java_websocket.WebSocket
import java.nio.ByteBuffer

class WebsocketAudioVideoProxy(private val client: WebSocket) {
    var videoFrameListener:OnReceiveFrameListener? = null
    var audioFrameListener:OnReceiveFrameListener? = null

    /**
     * 收到外部传递的音视频数据
     */
    fun onReceiveAudioVideoFrame(data:ByteBuffer) {
        if (isNALUData(data)) {
            videoFrameListener?.onReceiveFrame(data)
        } else {
            audioFrameListener?.onReceiveFrame(data)
        }
    }

    fun isNALUData(source: ByteBuffer): Boolean {
        var i = 0;
        val a_value = (0x00).toByte()
        val b_value = (0x01).toByte()
        //start code：00 00 00 01 or 00 00 01
        return (source[i] == a_value && source[i+1] == a_value && source[i+2] == a_value
                && source[i+3] == b_value) ||
                (source[i] == a_value && source[i+1] == a_value && source[i+2] == b_value)
    }

    /**
     * 向外发送视频数据
     */
    fun sendVideoData(data: ByteBuffer) {
        sendData(data, VIDEO)
    }

    /**
     * 向外发送音频数据
     */
    fun sendAudioData(data: ByteArray) {
        client.let {
            if (it.isOpen) {
                it.send(data)
            }
        }
    }

    private fun sendData(data: ByteBuffer, type:Byte) {
        if (client.isOpen) {
            client.send(data)
        }
    }

    companion object {
        const val VIDEO:Byte = 0
        const val AUDIO:Byte = 1
    }
}