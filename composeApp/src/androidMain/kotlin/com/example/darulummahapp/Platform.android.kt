package com.example.darulummahapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

internal object AndroidAppContext {
    var applicationContext: Context? = null
}

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun currentMinuteOfDay(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
}

actual fun currentSecondOfDay(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 +
        calendar.get(Calendar.MINUTE) * 60 +
        calendar.get(Calendar.SECOND)
}

actual fun currentIsoDayOfWeek(): Int {
    val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    return ((dayOfWeek + 5) % 7) + 1
}

actual fun updateNotificationSchedules(
    preferences: NotificationPreferences,
    timetable: PrayerTimetable,
    isoDayOfWeek: Int,
) {
    val context = AndroidAppContext.applicationContext ?: return
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    cancelScheduledNotifications(context, alarmManager)

    val prayerEvents = timetable.dailyPrayerTimes.mapIndexed { index, prayer ->
        val isFridayZuhr = isoDayOfWeek == 5 && prayer.name == "Zuhr"
        val name = if (isFridayZuhr) "Jum'ah" else prayer.name
        val time = if (isFridayZuhr) timetable.jumahTime.salaatTime else prayer.jamaahTime
        val minuteOfDay = if (isFridayZuhr) timeToMinuteOfDay(time) else prayer.minuteOfDay
        ScheduledPrayer(index, name, time, minuteOfDay)
    }

    if (preferences.prayerReminders) {
        prayerEvents.forEach { prayer ->
            PRAYER_ALERT_OFFSETS.forEach { offsetMinutes ->
                scheduleNotification(
                    context = context,
                    alarmManager = alarmManager,
                    requestCode = countdownRequestCode(prayer.index, offsetMinutes),
                    triggerAtMillis = nextPrayerReminderTriggerMillis(
                        prayerMinuteOfDay = prayer.minuteOfDay,
                        offsetMinutes = offsetMinutes,
                    ),
                    title = "${prayer.name} in $offsetMinutes minutes",
                    message = "Jama'ah is at ${prayer.time}.",
                )
            }
        }
    }

    if (preferences.classesAndEvents) {
        scheduleNotification(
            context = context,
            alarmManager = alarmManager,
            requestCode = COMMUNITY_NOTIFICATION_REQUEST_CODE,
            triggerAtMillis = nextTriggerMillis(9 * 60),
            title = "Darul Ummah Shadwell",
            message = "Check class updates in the app.",
        )
    }
}

actual fun loadNotificationPreferences(): NotificationPreferences {
    val context = AndroidAppContext.applicationContext ?: return defaultNotificationPreferences
    val preferences = context.getSharedPreferences(NOTIFICATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
    return NotificationPreferences(
        prayerReminders = preferences.getBoolean("prayerReminders", true),
        countdownAlerts = preferences.getBoolean("countdownAlerts", false),
        classesAndEvents = preferences.getBoolean("classesAndEvents", false),
    )
}

actual fun saveNotificationPreferences(preferences: NotificationPreferences) {
    val context = AndroidAppContext.applicationContext ?: return
    context.getSharedPreferences(NOTIFICATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean("prayerReminders", preferences.prayerReminders)
        .putBoolean("countdownAlerts", preferences.countdownAlerts)
        .putBoolean("classesAndEvents", preferences.classesAndEvents)
        .apply()
}

actual suspend fun fetchDarulUmmahHomeHtml(): String = withContext(Dispatchers.IO) {
    val connection = URL("http://www.darulummah.org.uk/").openConnection() as HttpURLConnection
    try {
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.requestMethod = "GET"
        connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

private data class ScheduledPrayer(
    val index: Int,
    val name: String,
    val time: String,
    val minuteOfDay: Int,
)

private fun scheduleNotification(
    context: Context,
    alarmManager: AlarmManager,
    requestCode: Int,
    triggerAtMillis: Long,
    title: String,
    message: String,
) {
    val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
        putExtra(PrayerNotificationReceiver.EXTRA_NOTIFICATION_ID, requestCode)
        putExtra(PrayerNotificationReceiver.EXTRA_TITLE, title)
        putExtra(PrayerNotificationReceiver.EXTRA_MESSAGE, message)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
}

private fun cancelScheduledNotifications(
    context: Context,
    alarmManager: AlarmManager,
) {
    val requestCodes = buildList {
        addAll(LEGACY_PRAYER_NOTIFICATION_REQUEST_CODE until LEGACY_PRAYER_NOTIFICATION_REQUEST_CODE + 5)
        repeat(5) { index ->
            PRAYER_ALERT_OFFSETS.forEach { offsetMinutes ->
                add(countdownRequestCode(index, offsetMinutes))
            }
        }
        add(COMMUNITY_NOTIFICATION_REQUEST_CODE)
    }
    requestCodes.forEach { requestCode ->
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, PrayerNotificationReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}

private fun nextTriggerMillis(minuteOfDay: Int): Long {
    val normalizedMinuteOfDay = ((minuteOfDay % (24 * 60)) + (24 * 60)) % (24 * 60)
    val now = Calendar.getInstance()
    val trigger = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, normalizedMinuteOfDay / 60)
        set(Calendar.MINUTE, normalizedMinuteOfDay % 60)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (!after(now)) {
            add(Calendar.DATE, 1)
        }
    }
    return trigger.timeInMillis
}

private fun nextPrayerReminderTriggerMillis(
    prayerMinuteOfDay: Int,
    offsetMinutes: Int,
): Long {
    val reminderMinuteOfDay = prayerMinuteOfDay - offsetMinutes
    return nextTriggerMillis(reminderMinuteOfDay)
}

private fun timeToMinuteOfDay(time: String): Int {
    val parts = time.split(":")
    return parts[0].toInt() * 60 + parts[1].toInt()
}

private fun countdownRequestCode(
    prayerIndex: Int,
    offsetMinutes: Int,
): Int {
    return COUNTDOWN_NOTIFICATION_REQUEST_CODE + prayerIndex * 100 + offsetMinutes
}

private const val COUNTDOWN_NOTIFICATION_REQUEST_CODE = 20_000
private const val LEGACY_PRAYER_NOTIFICATION_REQUEST_CODE = 10_000
private const val COMMUNITY_NOTIFICATION_REQUEST_CODE = 30_000
private const val NOTIFICATION_PREFERENCES_NAME = "darul_ummah_notification_preferences"
private val PRAYER_ALERT_OFFSETS = listOf(30, 10)
