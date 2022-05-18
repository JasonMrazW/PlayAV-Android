package com.bo.playav.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import com.bo.playav.databinding.ActivityH264PlayerBinding
import com.bo.playav.net.LiveSocketClient
import com.bo.playav.player.H264LocalPlayer
import com.bo.playav.player.H264RemotePlayer
import java.net.URI

class H264PlayerActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity2"
    private lateinit var binding: ActivityH264PlayerBinding
    private lateinit var liveSocketClient:LiveSocketClient
    private lateinit var h264Player: H264RemotePlayer

    private val callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            h264Player.start(holder.surface)
        }

        override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            h264Player.stop()
            liveSocketClient.close(1000)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityH264PlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.preview.holder.addCallback(callback)
        h264Player = H264RemotePlayer()
        liveSocketClient = LiveSocketClient(URI("ws://192.168.0.155:9015"))
        liveSocketClient.setReceiveListener(h264Player)
        liveSocketClient.connect()
    }
}