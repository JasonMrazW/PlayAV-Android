package com.bo.playav.net

import android.util.Log
import com.bo.playav.encoder.OnDataEncodedListener
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.nio.ByteBuffer

class LiveSocketClient(uri:URI):WebSocketClient(uri), OnDataEncodedListener {
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

    override fun onOpen(handshakedata: ServerHandshake?) {
        clientProxy = WebsocketAudioVideoProxy(this)
    }

    override fun onMessage(message: String?) {
        Log.d("player", "receive: message")
    }

    override fun onMessage(data: ByteBuffer?) {
        data?.let {
            clientProxy?.onReceiveAudioVideoFrame(it)
            clientProxy?.videoFrameListener = videoFrameListener
            clientProxy?.audioFrameListener = audioFrameListener
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        clientProxy = null
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
        clientProxy?.sendVideoData(data)
    }

    override fun onAudioDataEncoded(data: ByteArray) {
        clientProxy?.sendAudioData(data)
    }
}