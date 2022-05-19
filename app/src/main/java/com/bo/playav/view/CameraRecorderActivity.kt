package com.bo.playav.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bo.playav.camera.CameraWrapper
import com.bo.playav.databinding.ActivityCameraRecorderBinding
import com.bo.playav.encoder.H264FrameVideoEncoder

class CameraRecorderActivity : AppCompatActivity() {

    private val TAG: String = "CameraRecorderActivity"
    private lateinit var binding: ActivityCameraRecorderBinding
    private var encoder:H264FrameVideoEncoder? = null
    private lateinit var cameraWrapper:CameraWrapper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraRecorderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraWrapper = CameraWrapper(binding.preview)

        //start
        cameraWrapper.start(this)
    }
}