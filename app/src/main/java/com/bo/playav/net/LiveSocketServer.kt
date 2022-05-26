package com.bo.playav.net

import android.util.Log
import com.bo.playav.encoder.OnDataEncodedListener
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import kotlin.properties.Delegates
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

class LiveSocketServer(port:Int): WebSocketServer(InetSocketAddress(port)), OnDataEncodedListener {
    var clientProxy: WebsocketAudioVideoProxy? = null

    var videoFrameListener:OnReceiveFrameListener? = null
        set(value)  {
            field = value
            clientProxy?.videoFrameListener = value
        }

    var audioFrameListener:OnReceiveFrameListener? = null
        set(value) {
            field = value
            clientProxy?.audioFrameListener = value
        }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        Log.d("socket", "connect new client!!")

        conn?.let {
            clientProxy = WebsocketAudioVideoProxy(it)
            clientProxy?.videoFrameListener = videoFrameListener
            clientProxy?.audioFrameListener = audioFrameListener
        }
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        clientProxy = null
    }

    override fun onMessage(conn: WebSocket?, message: String?) {

    }

    override fun onMessage(conn: WebSocket?, data: ByteBuffer?) {
        //收到对方发来的消息
        Log.d("socket", "receive.. ${data?.remaining()}")
        data?.let {
            clientProxy?.onReceiveAudioVideoFrame(it)
        }
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        ex?.message?.let { Log.d("socket", "error: $it") }
    }

    override fun onStart() {
        Log.d("socket", "listening..")
    }

    override fun onVideoDataEncoded(data: ByteBuffer) {
        //send data to server
        clientProxy?.sendVideoData(data)
    }

    override fun onAudioDataEncoded(data: ByteArray) {
        clientProxy?.sendAudioData(data)
    }
}