package com.bo.playav.net

import org.java_websocket.WebSocket
import java.nio.ByteBuffer

class WebsocketAudioVideoProxy(private val client: WebSocket) {
    var videoFrameListener:OnReceiveFrameListener? = null
    var audioFrameListener:OnReceiveFrameListener? = null
    var age:Int = 0

    /**
     * 收到外部传递的音视频数据
     */
    fun onReceiveAudioVideoFrame(data:ByteBuffer) {
        val type = data.get(0)
        val byteArray = ByteArray(data.remaining()-1)
        data.position(1)
        data.get(byteArray)

        val byteBuffer = ByteBuffer.allocate(byteArray.size)
        byteBuffer.put(byteArray)
        byteBuffer.flip()

        if (type == VIDEO) {
            videoFrameListener?.onReceiveFrame(byteBuffer)
        } else {
            audioFrameListener?.onReceiveFrame(byteBuffer)
        }
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
        val sendData = ByteBuffer.allocate(data.size + 1)
        sendData.put(AUDIO)
        sendData.put(data)
        sendData.flip()
        client.let {
            if (it.isOpen) {
                it.send(sendData)
            }
        }
    }

    private fun sendData(data: ByteBuffer, type:Byte) {
        if (client.isOpen) {
            val sendData = ByteBuffer.allocate(data.remaining() + 1)
            sendData.put(type)
            sendData.put(data)
            sendData.flip()
            client.send(sendData)
        }
    }

    companion object {
        const val VIDEO:Byte = 0
        const val AUDIO:Byte = 1
    }
}