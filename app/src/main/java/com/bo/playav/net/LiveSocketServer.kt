package com.bo.playav.net

import android.util.Log
import com.bo.playav.encoder.OnDataEncodedListener
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class LiveSocketServer(port:Int): WebSocketServer(InetSocketAddress(port)), OnDataEncodedListener {
    var client: WebSocket? = null
    private var listener:OnReceiveMessageListener? = null

    fun setOnReceiveMessageListener(l: OnReceiveMessageListener) {
        listener = l
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        Log.d("socket", "connect new client!!")

        client = conn
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        client = null
    }

    override fun onMessage(conn: WebSocket?, message: String?) {

    }

    override fun onMessage(conn: WebSocket?, message: ByteBuffer?) {
        //收到对方发来的消息
        Log.d("socket", "receive.. ${message?.remaining()}")
        listener?.onReceive(message)
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        ex?.message?.let { Log.d("socket", "hhhh $it") }
    }

    override fun onStart() {
        Log.d("socket", "listening..")
    }

    override fun onDataEncoded(data: ByteBuffer) {
        client?.send(data)
        //Log.d("socket", "send  ${data.remaining()}")
    }
}