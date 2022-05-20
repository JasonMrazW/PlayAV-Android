package com.bo.playav.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bo.playav.databinding.ActivityMainBinding
import com.bo.playav.view.communication.VideoConnectionActivity
import com.bo.playav.view.communication.VideoConnectionClientActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun playH264(view: android.view.View) {
        startActivity(Intent(this, H264PlayerActivity::class.java))
    }
    fun playScreenRecorder(view: android.view.View) {
        startActivity(Intent(this, ScreenRecorderActivity::class.java))
    }

    fun playCameraRecorder(view: android.view.View) {
        startActivity(Intent(this, CameraRecorderActivity::class.java))
    }

    fun playVideoConnectionRecorder(view: android.view.View) {
        startActivity(Intent(this, VideoConnectionActivity::class.java))
    }
    fun playVideoConnectionClientRecorder(view: android.view.View) {
        startActivity(Intent(this, VideoConnectionClientActivity::class.java))
    }

}