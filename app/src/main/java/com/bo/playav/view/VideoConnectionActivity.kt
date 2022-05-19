package com.bo.playav.view

import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import com.bo.playav.camera.CameraWrapper
import com.bo.playav.camera.PeerInfo
import com.bo.playav.databinding.ActivityVideoConnectionBinding
import com.bo.playav.encoder.H264FrameVideoEncoder
import com.bo.playav.net.LiveSocketServer
import com.bo.playav.player.H264RemotePlayer

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
    }

    private val peerPreviewCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(p0: SurfaceHolder) {
            remotePlayer?.start(p0.surface, 1280, 720)
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            remotePlayer?.stop()
            socketServer?.stop(1000)
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
                    encoder?.start(parameters.previewSize.width, parameters.previewSize.height)
                    Log.d("camera preview", "${parameters.previewSize.width} + ${parameters.previewSize.height}")
                }
            }

            //encode data
            encoder?.encodeFrame(data)
        }
    }
}