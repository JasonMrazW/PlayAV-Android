package com.bo.playav.net

import android.net.Uri
import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.nio.ByteBuffer

class LiveSocketClient(uri:URI):WebSocketClient(uri) {
    var messageListener:OnSocketClientListener ? = null

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

    }

    fun setReceiveListener(listener: OnSocketClientListener) {
        messageListener = listener
    }

    interface OnSocketClientListener {
        fun onReceive(message: ByteBuffer?)
    }
}