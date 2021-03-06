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
        val builder: Notification.Builder = Notification.Builder(this.applicationContext) //????????????Notification?????????
        val nfIntent = Intent(this, ScreenRecorderActivity::class.java) //???????????????????????????????????????????????????
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // ??????PendingIntent
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    this.resources,
                    R.mipmap.ic_launcher
                )
            ) // ??????????????????????????????(?????????)
            .setContentTitle("ScreenRecorder") // ??????????????????????????????
            .setSmallIcon(R.mipmap.ic_launcher) // ??????????????????????????????
            .setContentText("is running......") // ?????????????????????
            .setWhen(System.currentTimeMillis()) // ??????????????????????????????

        /*????????????Android 8.0?????????*/
        //??????notification??????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id")
        }
        //????????????notification??????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "notification_id",
                "notification_name",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        val notification: Notification = builder.build() // ??????????????????Notification
        notification.defaults = Notification.DEFAULT_SOUND //????????????????????????
        startForeground(110, notification)
    }
}