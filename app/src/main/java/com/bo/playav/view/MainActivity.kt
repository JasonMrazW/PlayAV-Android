package com.bo.playav.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bo.playav.R
import com.bo.playav.databinding.ActivityMainBinding

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


}