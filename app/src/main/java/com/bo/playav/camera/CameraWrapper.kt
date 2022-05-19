package com.bo.playav.camera

import android.Manifest
import android.content.Context
import android.hardware.Camera
import android.os.Build
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.hjq.permissions.XXPermissions
import java.lang.Exception

class CameraWrapper(private val preview: SurfaceView) {

    private lateinit var camera: Camera
    private lateinit var byteBuffer: ByteArray
    private var previewFrameListener: OnPreviewFrameListener? = null

    init {
        preview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                startPreview(holder)
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

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                camera?.release()
            }

        })
    }

    private fun createCamera() {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
            camera?.let {
                val size = it.parameters.previewSize
                byteBuffer = ByteArray(size.width * size.height * 3/2)
            }

        } catch (e: Exception) {
            Log.e(Companion.TAG, "open front camera failed. cause of ${e.message}")
        }
    }

    private fun startPreview(holder: SurfaceHolder) {
        camera?.apply {
            setDisplayOrientation(90)
            addCallbackBuffer(byteBuffer)
            parameters.setPreviewFpsRange(30000,30000)
//            for (i  in parameters.supportedPreviewFpsRange) {
//                for (j in i) {
//                    Log.d("fps", "range: $j")
//                }
//                Log.d("fps", "======")
//            }
            setPreviewCallbackWithBuffer(previewCallback)
            setPreviewDisplay(holder)
            startPreview()
        }

    }


    fun start(context: Context) {
        if (XXPermissions.isGranted(context, Manifest.permission.CAMERA )) {
            createCamera()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                XXPermissions.with(context)
                    .permission(Manifest.permission.CAMERA)
                    .request { permissions, all ->
                        run {
                            createCamera()
                        }
                    }
            }
        }
    }

    fun setOnPreviewFrameListener(listener: OnPreviewFrameListener) {
        previewFrameListener = listener
    }

    private val previewCallback = object : Camera.PreviewCallback {
        override fun onPreviewFrame(p0: ByteArray?, p1: Camera?) {
            Log.d("camerawrapper", "frame has bean render")

            p0?.apply {
                previewFrameListener?.onPreviewFrame(this, p1)
            }

            p1?.apply {
                addCallbackBuffer(byteBuffer)
            }
        }
    }

    companion object {
        private const val TAG = "CameraWrapper"
    }


    interface OnPreviewFrameListener {
        fun onPreviewFrame(data: ByteArray, camera:Camera?)
    }
}