package com.nlespam.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import com.nlespam.NleSpamApp
import com.nlespam.MainActivity
import com.nlespam.models.GlobalSignals

class SpamForegroundService : Service() {
    private var notificationManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                GlobalSignals.sendStop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_UPDATE_PROGRESS -> {
                val packets = intent.getLongExtra(EXTRA_PACKETS, 0L)
                val route = intent.getStringExtra(EXTRA_ROUTE)
                notificationManager?.notify(NOTIFICATION_ID, createNotification(packets, route))
                return START_NOT_STICKY
            }
        }

        try {
            val route = intent?.getStringExtra(EXTRA_ROUTE)
            val notification = createNotification(0L, route)
            if (Build.VERSION.SDK_INT >= 34) {
                var serviceType = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                if (Build.VERSION.SDK_INT >= 37) { // Android 17+
                    serviceType = serviceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                } else if (Build.VERSION.SDK_INT >= 31) {
                    // Fallback or early check for connectedDevice
                    serviceType = serviceType or ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                }
                
                startForeground(
                    NOTIFICATION_ID, 
                    notification, 
                    serviceType
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun createNotification(packetsSent: Long, route: String? = null): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (route != null) putExtra(EXTRA_ROUTE, route)
        }
        val openPending = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, SpamForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, NleSpamApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setContentTitle("NleSpam Active")
            .setContentText("$packetsSent packets sent")
            .setContentIntent(openPending)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPending)
            .setOngoing(true)
            .setSilent(true)

        // Live Notification progress (Android 16 / API 36)
        if (Build.VERSION.SDK_INT >= 36) {
            builder.setProgress(0, 0, true) // indeterminate spinner
        }

        return builder.build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.nlespam.STOP_SPAM"
        const val ACTION_UPDATE_PROGRESS = "com.nlespam.UPDATE_PROGRESS"
        const val EXTRA_PACKETS = "extra_packets"
        const val EXTRA_ROUTE = "extra_route"
    }
}
