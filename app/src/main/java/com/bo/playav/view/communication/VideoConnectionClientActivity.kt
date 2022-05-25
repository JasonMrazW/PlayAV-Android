package com.bo.playav.view.communication

import android.hardware.Camera
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bo.playav.camera.CameraWrapper
import com.bo.playav.camera.PeerInfo
import com.bo.playav.databinding.ActivityVideoConnectionClientBinding
import com.bo.playav.encoder.AACAudioEncoder
import com.bo.playav.encoder.H264FrameVideoEncoder
import com.bo.playav.net.LiveSocketClient
import com.bo.playav.player.AACAudioPlayer
import com.bo.playav.player.H264RemotePlayer
import com.bo.playav.recorder.AudioRecorder
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import java.net.URI

class VideoConnectionClientActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoConnectionClientBinding
    private lateinit var cameraWrapper: CameraWrapper
    private var encoder: H264FrameVideoEncoder? = null
    private lateinit var remotePlayer: H264RemotePlayer

    private var audioEncoder: AACAudioEncoder? = null
    private lateinit var audioRecorder: AudioRecorder
    private var audioPlayer: AACAudioPlayer? = null

    private var socketClient: LiveSocketClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVideoConnectionClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //对端数据渲染
        remotePlayer = H264RemotePlayer()
        binding.preview.holder.addCallback(peerPreviewCallback)

        cameraWrapper = CameraWrapper(binding.selfPreview, false)
        cameraWrapper.setOnPreviewFrameListener(selfFrameListener)
        cameraWrapper.start(this)

        audioRecorder = AudioRecorder()

        checkPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraWrapper.release()
    }

    private fun checkPermission() {
        val context = this
        XXPermissions.with(this) // 不适配 Android 11 可以这样写
            //.permission(Permission.Group.STORAGE)
            // 适配 Android 11 需要这样写，这里无需再写 Permission.Group.STORAGE
            .permission(Permission.MANAGE_EXTERNAL_STORAGE)
            .permission(Permission.RECORD_AUDIO)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun callServer(view: android.view.View) {
        socketClient = LiveSocketClient(URI(PeerInfo.ADDRESS))
        //socket -> video player
        socketClient?.videoFrameListener = remotePlayer
        //socket -> aac audio player
        audioPlayer = AACAudioPlayer()
        audioPlayer?.start()
        socketClient?.audioFrameListener = audioPlayer

        //encoder -> socket
        encoder?.setOnDataEncodedListener(socketClient!!)
        socketClient?.connect()
    }

    private val peerPreviewCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(p0: SurfaceHolder) {
            remotePlayer.start(p0.surface, 1920, 1080)
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            remotePlayer.stopPlaying()
            socketClient?.close(1000)
            encoder?.stop()
            encoder = null

            audioEncoder?.stop()
            audioRecorder.stop()
            audioPlayer?.stop()
        }
    }

    private val selfFrameListener = object : CameraWrapper.OnPreviewFrameListener {
        override fun onPreviewFrame(data: ByteArray, camera: Camera?) {
            if (encoder == null) {
                camera?.apply {
                    encoder = H264FrameVideoEncoder()
                    encoder?.start(parameters.previewSize.width, parameters.previewSize.height)
                    Log.d("camera preview", "${parameters.previewSize.width} + ${parameters.previewSize.height}")

                    audioEncoder = AACAudioEncoder()
                    //encoder -> socket
                    audioEncoder?.listener = socketClient

                    //recoder -> encoder
                    audioRecorder.listener = audioEncoder

                    //start
                    audioEncoder?.start(audioRecorder.bufferSize)

                    audioRecorder.start()
                }
            }

            //encode data
            encoder?.encodeFrame(data)
        }
    }


}