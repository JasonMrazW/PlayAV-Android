package com.bo.playav.view

import android.content.Intent
import android.media.projection.MediaProjectionManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bo.playav.databinding.ActivityScreenRecorderBinding
import com.bo.playav.service.ScreenRecorderService

class ScreenRecorderActivity : AppCompatActivity() {

    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->
        run {
            startRecorder(result.resultCode, result.data)
        }
    }

    private lateinit var binding: ActivityScreenRecorderBinding

    private fun startRecorder(resultCode:Int, resultData: Intent?) {
        val intent = Intent(this, ScreenRecorderService::class.java)
        intent.putExtra("data", resultData)
        intent.putExtra("resultCode", resultCode)
        ContextCompat.startForegroundService(this, intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreenRecorderBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    fun start(view: View) {
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = projectionManager.createScreenCaptureIntent()
        startForResult.launch(intent)
    }

    fun stop(view: View) {
        val intent = Intent(this, ScreenRecorderService::class.java)
        stopService(intent)
    }
}