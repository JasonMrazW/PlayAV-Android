package com.bo.playav.net

import android.util.Log
import com.bo.playav.encoder.H264SurfaceVideoEncoder
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress
import java.nio.ByteBuffer

class LiveSocketServer(port:Int): WebSocketServer(InetSocketAddress(port)), H264SurfaceVideoEncoder.OnDataEncodedListener {
    var client: WebSocket? = null


    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        Log.d("socket", "connect new client!!")

        client = conn
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        client = null
    }

    override fun onMessage(conn: WebSocket?, message: String?) {

    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        ex?.message?.let { Log.d("socket", it) }
    }

    override fun onStart() {
        Log.d("socket", "listening..")
    }

    override fun onDataEncoded(data: ByteBuffer) {

//        Log.d("socket", "data ${data.remaining()}")

        client?.send(data)
//        Log.d("player", "send: ${data.get(0)} " +
//                " ${data.get(1)}" +
//                " ${data.get(2)}" +
//                " ${data.get(3)}" +
//                " ${data.get(4)}" +
//                " ${data.get(5)}")
    }
}