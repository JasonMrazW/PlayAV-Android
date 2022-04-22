package com.bo.playav.view

import android.Manifest
import android.hardware.Camera
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import com.bo.playav.databinding.ActivityCameraRecorderBinding
import com.bo.playav.encoder.H264FrameVideoEncoder
import com.bo.playav.toHex
import com.hjq.permissions.XXPermissions
import java.lang.Exception

class CameraRecorderActivity : AppCompatActivity() {

    private val TAG: String = "CameraRecorderActivity"
    private lateinit var binding: ActivityCameraRecorderBinding
    private var camera:android.hardware.Camera? = null
    private var byteBuffer: ByteArray? = null
    private var encoder:H264FrameVideoEncoder? = null

    private val callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(p0: SurfaceHolder) {
            startPreview(p0)
        }

        override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            if (holder.surface == null) {
                return
            }

            camera?.apply {
                stopPreview()

                startPreview(holder)
            }
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            camera?.release()
        }
    }

    private val previewCallback = object : Camera.PreviewCallback {
        override fun onPreviewFrame(p0: ByteArray?, p1: Camera?) {


            camera?.apply {
                if (encoder == null) {
                    encoder = H264FrameVideoEncoder()
                    encoder?.start(parameters.previewSize.width, parameters.previewSize.height)
                }
                p0?.apply {
                    encoder?.encodeFrame(this)
                }
                addCallbackBuffer(byteBuffer)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraRecorderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.preview.holder.addCallback(callback)
        if (XXPermissions.isGranted(this, Manifest.permission.CAMERA )) {
            createCamera()
        } else {
            checkPermission()
        }

        Log.e(TAG, "xxxx")
    }

    fun createCamera() {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT)
            camera?.let {
                val size = it.parameters.previewSize
                byteBuffer = ByteArray(size.width * size.height * 3/2)
            }

        } catch (e:Exception) {
            Log.e(TAG, "open front camera failed. cause of ${e.message}")
        }
    }

    fun startPreview(holder: SurfaceHolder) {
        camera?.apply {
            setDisplayOrientation(90)
            addCallbackBuffer(byteBuffer)
            setPreviewCallbackWithBuffer(previewCallback)
            setPreviewDisplay(holder)
            startPreview()
        }
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            XXPermissions.with(this)
                .permission(Manifest.permission.CAMERA)
                .request { permissions, all -> {
                    createCamera()
                } }
        }
    }
}