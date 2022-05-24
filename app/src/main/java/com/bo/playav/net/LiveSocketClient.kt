package com.bo.playav.net

import android.util.Log
import com.bo.playav.encoder.OnDataEncodedListener
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.nio.ByteBuffer

class LiveSocketClient(uri:URI):WebSocketClient(uri), OnDataEncodedListener {
    var messageListener:OnReceiveMessageListener ? = null

    override fun onOpen(handshakedata: ServerHandshake?) {
    }

    override fun onMessage(message: String?) {
        Log.d("player", "receive: message")
    }

    override fun onMessage(data: ByteBuffer?) {
        Log.d("player", "receive: ${data?.get(0)} " +
                " ${data?.get(1)}" +
                " ${data?.get(2)}" +
                " ${data?.get(3)}" +
                " ${data?.get(4)}" +
                " ${data?.get(5)}" + "size: ${data?.remaining()}")
        messageListener?.onReceive(data)
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {

    }

    override fun onError(ex: Exception?) {
        Log.d("player", "send 222error: ${ex?.message}")
    }

    fun setReceiveListener(listener: OnReceiveMessageListener) {
        messageListener = listener
    }

    override fun onVideoDataEncoded(data: ByteBuffer) {
        //send data to server
        if (isOpen) {
            Log.d("player", "send data: ${data?.get(0)} " +
                    " ${data?.get(1)}" +
                    " ${data?.get(2)}" +
                    " ${data?.get(3)}" +
                    " ${data?.get(4)}" +
                    " ${data?.get(5)}" + "size: ${data?.remaining()}")
            send(data)
        }
    }

    override fun onAudioDataEncoded(data: ByteBuffer) {
        TODO("Not yet implemented")
    }
}