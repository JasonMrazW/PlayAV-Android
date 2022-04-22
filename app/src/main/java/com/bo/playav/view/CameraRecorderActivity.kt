package com.bo.playav.view

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import com.bo.playav.databinding.ActivityCameraRecorderBinding
import com.hjq.permissions.XXPermissions
import java.lang.Exception

class CameraRecorderActivity : AppCompatActivity() {

    private val TAG: String = "CameraRecorderActivity"
    private lateinit var binding: ActivityCameraRecorderBinding
    private var camera:android.hardware.Camera? = null

    private val callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(p0: SurfaceHolder) {
            camera?.apply {
                setPreviewDisplay(p0)
                startPreview()
            }
        }

        override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            if (holder.surface == null) {
                return
            }

            camera?.apply {
                stopPreview()

                setPreviewDisplay(holder)
                startPreview()
            }
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            camera?.release()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraRecorderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.preview.holder.addCallback(callback)
        if (XXPermissions.isGranted(this, Manifest.permission.CAMERA )) {
            initCamera()
        } else {
            checkPermission()
        }

    }

    fun initCamera() {
        try {
            camera = android.hardware.Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT)
        } catch (e:Exception) {
            Log.e(TAG, "open front camera failed. cause of ${e.message}")
        }
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            XXPermissions.with(this)
                .permission(Manifest.permission.CAMERA)
                .request { permissions, all -> {
                    initCamera()
                } }
        }
    }
}