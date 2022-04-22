package com.bo.playav.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bo.playav.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun playH264(view: android.view.View) {
        startActivity(Intent(this, H264PlayerActivity::class.java))
    }
    fun playScreenRecorder(view: android.view.View) {
        startActivity(Intent(this, ScreenRecorderActivity::class.java))
    }


}