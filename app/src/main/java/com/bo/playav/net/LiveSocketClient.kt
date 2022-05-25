package com.bo.playav.net

import android.util.Log
import com.bo.playav.encoder.OnDataEncodedListener
import com.bo.playav.net.OnReceiveFrameListener.Companion.AUDIO
import com.bo.playav.net.OnReceiveFrameListener.Companion.VIDEO
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.nio.ByteBuffer

class LiveSocketClient(uri:URI):WebSocketClient(uri), OnDataEncodedListener {
    var videoFrameListener:OnReceiveFrameListener? = null
    var audioFrameListener:OnReceiveFrameListener? = null

    override fun onOpen(handshakedata: ServerHandshake?) {
    }

    override fun onMessage(message: String?) {
        Log.d("player", "receive: message")
    }

    override fun onMessage(data: ByteBuffer?) {

        data?.let {
            val type = data.get(0)
            val byteArray = ByteArray(it.remaining()-1)
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
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {

    }

    override fun onError(ex: Exception?) {
        Log.e("player", "error: ${ex?.message}")
        ex?.apply {
            for (trace in stackTrace) {
                Log.e("player", "        $trace.methodName")
            }
        }
    }

    override fun onVideoDataEncoded(data: ByteBuffer) {
        //send data to server
        if (isOpen) {
            val sendData = ByteBuffer.allocate(data.remaining() + 1)
            sendData.put(VIDEO)
            sendData.put(data)
            sendData.flip()
            send(sendData)
        }
    }

    override fun onAudioDataEncoded(data: ByteArray) {
        if (isOpen) {
            val sendData = ByteBuffer.allocate(data.size + 1)
            sendData.put(AUDIO)
            sendData.put(data)
            sendData.flip()
            send(sendData)
        }
    }
}