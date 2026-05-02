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
import android.media.AudioAttributes
import android.net.Uri
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
        createNotificationChannel(context, notificationManager)

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Darul Ummah Shadwell"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Prayer reminder"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val playAzan = intent.getBooleanExtra(EXTRA_PLAY_AZAN, false)
        val launchIntent = Intent(context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, if (playAzan) AZAN_CHANNEL_ID else CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .apply {
                if (playAzan && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    setSound(azanSoundUri(context))
                }
            }
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel(
        context: Context,
        notificationManager: NotificationManager,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
        if (existingChannel == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Prayer reminders",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Darul Ummah Shadwell prayer and event reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val existingAzanChannel = notificationManager.getNotificationChannel(AZAN_CHANNEL_ID)
        if (existingAzanChannel != null) return

        val azanAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val azanChannel = NotificationChannel(
            AZAN_CHANNEL_ID,
            "Azan at salah start",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Plays azan when each salah begins"
            setSound(azanSoundUri(context), azanAttributes)
        }
        notificationManager.createNotificationChannel(azanChannel)
    }

    private fun azanSoundUri(context: Context): Uri {
        return Uri.parse("android.resource://${context.packageName}/${R.raw.azaan}")
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_PLAY_AZAN = "play_azan"
        private const val CHANNEL_ID = "darul_ummah_prayer_reminders"
        private const val AZAN_CHANNEL_ID = "darul_ummah_azan"
    }
}
