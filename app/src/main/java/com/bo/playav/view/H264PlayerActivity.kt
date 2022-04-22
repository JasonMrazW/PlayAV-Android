package com.bo.playav.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.bo.playav.R
import com.bo.playav.player.H264Player

class H264PlayerActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity2"
    private lateinit var preview: SurfaceView
    private lateinit var h264Player: H264Player

    private val callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            h264Player.start(holder.surface, "video/out.h264")
        }

        override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            h264Player.stop()
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_h264_player)

        preview = findViewById<SurfaceView>(R.id.preview)
        preview.holder.addCallback(callback)
        h264Player = H264Player(assets)
    }
}