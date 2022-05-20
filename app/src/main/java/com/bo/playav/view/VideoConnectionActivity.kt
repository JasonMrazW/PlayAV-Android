package com.bo.playav.view

import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import com.bo.playav.camera.CameraWrapper
import com.bo.playav.camera.PeerInfo
import com.bo.playav.databinding.ActivityVideoConnectionBinding
import com.bo.playav.encoder.H264FrameVideoEncoder
import com.bo.playav.net.LiveSocketServer
import com.bo.playav.player.H264RemotePlayer
import com.hjq.permissions.XXPermissions

import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission


/**
 * 视频通话
 */
class VideoConnectionActivity : AppCompatActivity() {
    private lateinit var binding:ActivityVideoConnectionBinding
    private lateinit var cameraWrapper: CameraWrapper
    private var encoder: H264FrameVideoEncoder? = null
    private var remotePlayer: H264RemotePlayer? = null

    private var socketServer: LiveSocketServer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVideoConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //对端数据渲染
        remotePlayer = H264RemotePlayer()
        binding.preview.holder.addCallback(peerPreviewCallback)

        cameraWrapper = CameraWrapper(binding.selfPreview)
        cameraWrapper.setOnPreviewFrameListener(selfFrameListener)
        cameraWrapper.start(this)

        checkPermission()
    }

    private fun checkPermission() {
        val context = this
        XXPermissions.with(this) // 不适配 Android 11 可以这样写
            //.permission(Permission.Group.STORAGE)
            // 适配 Android 11 需要这样写，这里无需再写 Permission.Group.STORAGE
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: List<String>, all: Boolean) {
                    if (all) {
                        Toast.makeText(context, "获取存储权限成功", Toast.LENGTH_SHORT)
                    }
                }

                override fun onDenied(permissions: List<String>, never: Boolean) {
                    if (never) {
                        Toast.makeText(context, "被永久拒绝授权，请手动授予存储权限", Toast.LENGTH_SHORT)
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(context, permissions)
                    } else {
                        Toast.makeText(context, "获取存储权限失败，请手动授予存储权限", Toast.LENGTH_SHORT)
                    }
                }
            })
    }

    private val peerPreviewCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(p0: SurfaceHolder) {
            remotePlayer?.start(p0.surface, 1280, 720)
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            remotePlayer?.stop()
            socketServer?.stop()
        }
    }

    private val selfFrameListener = object : CameraWrapper.OnPreviewFrameListener {
        override fun onPreviewFrame(data: ByteArray, camera: Camera?) {
            if (socketServer == null) {
                socketServer = LiveSocketServer(PeerInfo.PORT)
                remotePlayer?.let {
                    socketServer?.setOnReceiveMessageListener(it)
                }

                socketServer?.start()
                camera?.apply {
                    encoder = H264FrameVideoEncoder()
                    encoder?.setOnDataEncodedListener(socketServer!!)
                    encoder?.start(parameters.previewSize.height, parameters.previewSize.width)
                    Log.d("camera preview", "${parameters.previewSize.width} + ${parameters.previewSize.height}")
                }
            }

            //encode data
            encoder?.encodeFrame(data)
        }
    }
}