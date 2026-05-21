package com.nlespam

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class NleSpamApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "NleSpam Spam Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Persistent notification while BLE spam is active"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "nlespam_spam_channel"
    }
}
