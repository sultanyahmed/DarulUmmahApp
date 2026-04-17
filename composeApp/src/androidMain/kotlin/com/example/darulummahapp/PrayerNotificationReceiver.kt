package com.example.darulummahapp

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

class PrayerNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Darul Ummah Shadwell"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Prayer reminder"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val launchIntent = Intent(context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
        if (existingChannel != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Prayer reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Darul Ummah Shadwell prayer and event reminders"
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
        private const val CHANNEL_ID = "darul_ummah_prayer_reminders"
    }
}
