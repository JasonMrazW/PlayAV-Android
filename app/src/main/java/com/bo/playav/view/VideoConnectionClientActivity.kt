package com.bo.playav.view

import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import com.bo.playav.camera.CameraWrapper
import com.bo.playav.camera.PeerInfo
import com.bo.playav.databinding.ActivityVideoConnectionClientBinding
import com.bo.playav.encoder.H264FrameVideoEncoder
import com.bo.playav.net.LiveSocketClient
import com.bo.playav.player.H264RemotePlayer
import java.net.URI

class VideoConnectionClientActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVideoConnectionClientBinding
    private lateinit var cameraWrapper: CameraWrapper
    private var encoder: H264FrameVideoEncoder? = null
    private lateinit var remotePlayer: H264RemotePlayer

    private lateinit var socketClient: LiveSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVideoConnectionClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //对端数据渲染
        remotePlayer = H264RemotePlayer()
        binding.preview.holder.addCallback(peerPreviewCallback)

        cameraWrapper = CameraWrapper(binding.selfPreview)
        cameraWrapper.setOnPreviewFrameListener(selfFrameListener)
        cameraWrapper.start(this)
    }

    fun callServer(view: android.view.View) {
        socketClient = LiveSocketClient(URI(PeerInfo.ADDRESS))
        socketClient.setReceiveListener(remotePlayer)
        encoder?.setOnDataEncodedListener(socketClient)
        socketClient.connect()
    }

    private val peerPreviewCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(p0: SurfaceHolder) {
            remotePlayer.start(p0.surface, 1080, 1920)
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            remotePlayer.stop()
            socketClient.close(1000)
        }
    }

    private val selfFrameListener = object : CameraWrapper.OnPreviewFrameListener {
        override fun onPreviewFrame(data: ByteArray, camera: Camera?) {
            if (encoder == null) {
                camera?.apply {
                    encoder = H264FrameVideoEncoder()
                    encoder?.start(parameters.previewSize.width, parameters.previewSize.height)
                }
            }

            //encode data
            encoder?.encodeFrame(data)
        }
    }


}