package com.bo.playav.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import com.bo.playav.databinding.ActivityVideoConnectionBinding

/**
 * 视频通话
 */
class VideoConnectionActivity : AppCompatActivity() {
    private lateinit var binding:ActivityVideoConnectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVideoConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    /**
     * 自己摄像头预览画面
     */
    private val selfPreviewCallback = object: SurfaceHolder.Callback {
        override fun surfaceCreated(p0: SurfaceHolder) {
            TODO("Not yet implemented")
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            TODO("Not yet implemented")
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            TODO("Not yet implemented")
        }

    }

    private val peerPreviewCallback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(p0: SurfaceHolder) {
            TODO("Not yet implemented")
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
            TODO("Not yet implemented")
        }

        override fun surfaceDestroyed(p0: SurfaceHolder) {
            TODO("Not yet implemented")
        }
    }
}