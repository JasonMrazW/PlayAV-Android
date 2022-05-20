package com.bo.playav.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.Camera
import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.bo.playav.utils.YUVUtil
import com.hjq.permissions.XXPermissions
import java.lang.Exception
import kotlin.properties.Delegates

class CameraWrapper(private val preview: SurfaceView, private val rotate: Boolean = true) {

    private lateinit var camera: Camera
    private lateinit var byteBuffer: ByteArray
    private var previewFrameListener: OnPreviewFrameListener? = null
    private var width by Delegates.notNull<Int>()
    private var height by Delegates.notNull<Int>()
    private var isRunning = false

    init {
        preview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                startPreview(holder)
                isRunning = true
            }

            override fun surfaceChanged(holder: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
                if (holder.surface == null) {
                    return
                }

                camera.apply {
                    stopPreview()

                    startPreview(holder)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                isRunning = false
            }

        })
    }

    private fun createCamera() {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
            camera?.let {
                val size = it.parameters.previewSize
                byteBuffer = ByteArray(size.width * size.height * 3/2)
                width = size.width
                height = size.height
            }

        } catch (e: Exception) {
            Log.e(Companion.TAG, "open front camera failed. cause of ${e.message}")
        }
    }

    private fun startPreview(holder: SurfaceHolder) {
        camera?.apply {
            //setDisplayOrientation(90)
            if (rotate) {
                setCameraDisplayOrientation(preview.context, Camera.CameraInfo.CAMERA_FACING_BACK)
            }
            addCallbackBuffer(byteBuffer)
            parameters.setPreviewFpsRange(30000,30000)
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            parameters.setPreviewFormat(ImageFormat.NV21)
            setPreviewCallbackWithBuffer(previewCallback)
            setPreviewDisplay(holder)
            startPreview()
        }

    }

    fun setCameraDisplayOrientation(context: Context, cameraId:Int) {
        val info =  Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        val rotation = (context as Activity).windowManager.defaultDisplay.rotation
        var degree = 0
        when (rotation) {
            Surface.ROTATION_0 -> degree = 0
            Surface.ROTATION_90 -> degree = 90
            Surface.ROTATION_180 -> degree = 180
            Surface.ROTATION_270 -> degree = 270
        }
        var displayDegree = 0
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayDegree = (info.orientation + degree)% 360
            displayDegree = (360 - displayDegree) % 360
        } else {
            displayDegree = (info.orientation - degree + 360) % 360
        }
        camera.setDisplayOrientation(displayDegree)
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

    fun release() {
        camera.release()
    }

    fun setOnPreviewFrameListener(listener: OnPreviewFrameListener) {
        previewFrameListener = listener
    }

    private val previewCallback = object : Camera.PreviewCallback {
        override fun onPreviewFrame(p0: ByteArray?, p1: Camera?) {
            p0?.apply {
                //convert NV21 to I420
                //rotate by cpu
                if (!isRunning)return
                var tmp = YUVUtil.convertNV21ToI420(this, width, height)
                if (rotate) {
                    tmp = YUVUtil.rotate90(tmp, width, height)
                }

                previewFrameListener?.onPreviewFrame(tmp, p1)
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