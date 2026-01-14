package com.example.maptracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.maptracker.R

class TrackerService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Când serviciul pornește, trebuie să arătăm o notificare
        startForeground(1, createNotification())
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channelId = "location_channel"
        val channelName = "Location Tracking"

        // Creăm canalul de notificări (obligatoriu pentru Android 8+)
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("MapTracker")
            .setContentText("Înregistrare traseu activă...")
            .setSmallIcon(R.mipmap.ic_launcher) // Iconița aplicației
            .build()
    }
}