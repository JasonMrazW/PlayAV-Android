package com.bo.playav.service

import android.content.Intent
import android.os.IBinder

import android.os.Build

import android.app.*

import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import com.bo.playav.R
import com.bo.playav.encoder.H264SurfaceVideoEncoder
import com.bo.playav.net.LiveSocketServer

import com.bo.playav.view.MainActivity
import com.bo.playav.view.ScreenRecorderActivity


class ScreenRecorderService : Service() {

    private lateinit var projection:MediaProjection
    private lateinit var encoder: H264SurfaceVideoEncoder
    private lateinit var socketServer: LiveSocketServer

    override fun onBind(intent: Intent): IBinder {
        //do nothing
        throw UnsupportedOperationException("Not yet implemented");
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { it ->
            val data = it.getParcelableExtra<Intent>("data")
            val resultCode = it.getIntExtra("resultCode", -1)
            val projectionManager =
                getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            data?.let {
                //init encoder
                createNotificationChannel()
                projection = projectionManager.getMediaProjection(resultCode, it)
                encoder = H264SurfaceVideoEncoder()
                encoder.setCodecType(MediaFormat.MIMETYPE_VIDEO_AVC)
                socketServer = LiveSocketServer(9015)
                encoder.setOnDataEncodedListener(socketServer)
                socketServer.start()

                val destSurface = encoder.start()

                val virtualDisplay = projection.createVirtualDisplay(
                    "my screen recorder",
                    1080,1980,2,DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    destSurface, null, null
                )
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    override fun onDestroy() {
        super.onDestroy()
        projection.stop()
        encoder.stop()
        socketServer.stop(1000)
    }

    private fun createNotificationChannel() {
        val builder: Notification.Builder = Notification.Builder(this.applicationContext) //获取一个Notification构造器
        val nfIntent = Intent(this, ScreenRecorderActivity::class.java) //点击后跳转的界面，可以设置跳转数据
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    this.resources,
                    R.mipmap.ic_launcher
                )
            ) // 设置下拉列表中的图标(大图标)
            .setContentTitle("ScreenRecorder") // 设置下拉列表里的标题
            .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
            .setContentText("is running......") // 设置上下文内容
            .setWhen(System.currentTimeMillis()) // 设置该通知发生的时间

        /*以下是对Android 8.0的适配*/
        //普通notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id")
        }
        //前台服务notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "notification_id",
                "notification_name",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notification: Notification = builder.build() // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND //设置为默认的声音
        startForeground(110, notification)
    }
}