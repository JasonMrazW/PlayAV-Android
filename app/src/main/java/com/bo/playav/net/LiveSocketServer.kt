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
    var videoFrameListener:OnReceiveFrameListener? = null
    var audioFrameListener:OnReceiveFrameListener? = null

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        Log.d("socket", "connect new client!!")

        client = conn
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        client = null
    }

    override fun onMessage(conn: WebSocket?, message: String?) {

    }

    override fun onMessage(conn: WebSocket?, data: ByteBuffer?) {
        //收到对方发来的消息
        Log.d("socket", "receive.. ${data?.remaining()}")
        data?.let {
            val type = data.get(0)
            val byteArray = ByteArray(it.remaining()-1)
            data.position(1)
            data.get(byteArray)

            val byteBuffer = ByteBuffer.allocate(byteArray.size)
            byteBuffer.put(byteArray)
            byteBuffer.flip()

            if (type == OnReceiveFrameListener.VIDEO) {
                videoFrameListener?.onReceiveFrame(byteBuffer)
            } else {
                audioFrameListener?.onReceiveFrame(byteBuffer)
            }
        }
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        ex?.message?.let { Log.d("socket", "hhhh $it") }
    }

    override fun onStart() {
        Log.d("socket", "listening..")
    }

    override fun onVideoDataEncoded(data: ByteBuffer) {
        //send data to server
        val sendData = ByteBuffer.allocate(data.remaining() + 1)
        sendData.put(OnReceiveFrameListener.VIDEO)
        sendData.put(data)
        sendData.flip()
        client?.let {
            if (it.isOpen) {
                it.send(sendData)
            }
        }
    }

    override fun onAudioDataEncoded(data: ByteArray) {
        val sendData = ByteBuffer.allocate(data.size + 1)
        sendData.put(OnReceiveFrameListener.AUDIO)
        sendData.put(data)
        sendData.flip()
        client?.let {
            if (it.isOpen) {
                it.send(sendData)
            }
        }
    }
}