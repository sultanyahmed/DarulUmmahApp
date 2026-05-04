package com.example.darulummahapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.absoluteValue

internal object AndroidAppContext {
    var applicationContext: Context? = null
}

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun currentMinuteOfDay(): Int {
    val calendar = mosqueCalendar()
    return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
}

actual fun currentSecondOfDay(): Int {
    val calendar = mosqueCalendar()
    return calendar.get(Calendar.HOUR_OF_DAY) * 60 * 60 +
        calendar.get(Calendar.MINUTE) * 60 +
        calendar.get(Calendar.SECOND)
}

actual fun currentIsoDayOfWeek(): Int {
    val dayOfWeek = mosqueCalendar().get(Calendar.DAY_OF_WEEK)
    return ((dayOfWeek + 5) % 7) + 1
}

actual fun currentDateTimeComponents(): DateTimeComponents {
    val calendar = mosqueCalendar()
    return DateTimeComponents(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH) + 1,
        day = calendar.get(Calendar.DAY_OF_MONTH),
        hour = calendar.get(Calendar.HOUR_OF_DAY),
        minute = calendar.get(Calendar.MINUTE),
        second = calendar.get(Calendar.SECOND),
    )
}

private fun mosqueCalendar(): Calendar {
    return Calendar.getInstance(TimeZone.getTimeZone(MOSQUE_TIME_ZONE_ID))
}

actual fun updateNotificationSchedules(
    preferences: NotificationPreferences,
    timetable: PrayerTimetable,
    isoDayOfWeek: Int,
    announcements: List<Announcement>,
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

    if (preferences.azanAtSalahStart) {
        timetable.dailyPrayerTimes.forEachIndexed { index, prayer ->
            scheduleNotification(
                context = context,
                alarmManager = alarmManager,
                requestCode = azanRequestCode(index),
                triggerAtMillis = nextTriggerMillis(timeToMinuteOfDay(prayer.beginsTime)),
                title = "${prayer.name} has begun",
                message = "Salah begins at ${prayer.beginsTime}.",
                playAzan = true,
            )
        }
    }

    if (preferences.classesAndEvents) {
        classSchedule.forEachIndexed { index, session ->
            val day = session.reminderIsoDayOfWeek
            val minute = session.reminderMinuteOfDay
            if (day != null && minute != null) {
                scheduleNotification(
                    context = context,
                    alarmManager = alarmManager,
                    requestCode = classReminderRequestCode(index),
                    triggerAtMillis = nextWeeklyReminderTriggerMillis(
                        isoDayOfWeek = day,
                        minuteOfDay = minute,
                        offsetMinutes = CLASS_ALERT_OFFSET_MINUTES,
                    ),
                    title = "${session.title} in $CLASS_ALERT_OFFSET_MINUTES minutes",
                    message = "Starts at ${session.time}.",
                )
            }
        }
        val announcementRequestCodes = announcements.mapNotNull { announcement ->
            val triggerAtMillis = announcementReminderTriggerMillis(
                date = announcement.startDate,
                time = announcement.startTime,
            ) ?: return@mapNotNull null
            val requestCode = announcementReminderRequestCode(announcement.id)
            scheduleNotification(
                context = context,
                alarmManager = alarmManager,
                requestCode = requestCode,
                triggerAtMillis = triggerAtMillis,
                title = "${announcement.title} in 1 hour",
                message = "Starts at ${announcement.startTime}.",
            )
            requestCode
        }
        saveScheduledAnnouncementRequestCodes(context, announcementRequestCodes)
    } else {
        saveScheduledAnnouncementRequestCodes(context, emptyList())
    }
}

actual fun loadNotificationPreferences(): NotificationPreferences {
    val context = AndroidAppContext.applicationContext ?: return defaultNotificationPreferences
    val preferences = context.getSharedPreferences(NOTIFICATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
    return NotificationPreferences(
        prayerReminders = preferences.getBoolean("prayerReminders", true),
        azanAtSalahStart = preferences.getBoolean("azanAtSalahStart", false),
        countdownAlerts = preferences.getBoolean("countdownAlerts", false),
        classesAndEvents = preferences.getBoolean("classesAndEvents", true),
    )
}

actual fun saveNotificationPreferences(preferences: NotificationPreferences) {
    val context = AndroidAppContext.applicationContext ?: return
    context.getSharedPreferences(NOTIFICATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean("prayerReminders", preferences.prayerReminders)
        .putBoolean("azanAtSalahStart", preferences.azanAtSalahStart)
        .putBoolean("countdownAlerts", preferences.countdownAlerts)
        .putBoolean("classesAndEvents", preferences.classesAndEvents)
        .apply()
}

actual fun loadDarkModePreference(): Boolean? {
    val context = AndroidAppContext.applicationContext ?: return null
    val preferences = context.getSharedPreferences(NOTIFICATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
    return if (preferences.contains(DARK_MODE_PREFERENCE_KEY)) {
        preferences.getBoolean(DARK_MODE_PREFERENCE_KEY, false)
    } else {
        null
    }
}

actual fun saveDarkModePreference(enabled: Boolean) {
    val context = AndroidAppContext.applicationContext ?: return
    context.getSharedPreferences(NOTIFICATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(DARK_MODE_PREFERENCE_KEY, enabled)
        .apply()
}

actual fun openPhoneNumber(phoneNumber: String) {
    openIntent(
        Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber.filter { it.isDigit() || it == '+' }}")),
    )
}

actual fun openEmailAddress(emailAddress: String) {
    openIntent(
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$emailAddress")),
    )
}

actual fun openMapDirections(address: String) {
    val encodedAddress = Uri.encode(address)
    val mapIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:0,0?q=$encodedAddress"),
    )
    openIntent(Intent.createChooser(mapIntent, "Open directions"))
}

actual fun openExternalUrl(url: String) {
    openIntent(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

actual suspend fun fetchDarulUmmahHomeHtml(): String {
    return fetchUrlString("http://www.darulummah.org.uk/")
}

actual suspend fun fetchDarulUmmahPrayerTimetableHtml(): String {
    return fetchUrlString("http://www.darulummah.org.uk/mosque/prayer-timetable")
}

actual suspend fun fetchDarulUmmahYouTubeFeedXml(): String {
    return fetchUrlString("https://www.youtube.com/feeds/videos.xml?channel_id=$DarulUmmahYouTubeChannelId")
}

private suspend fun fetchUrlString(urlString: String): String = withContext(Dispatchers.IO) {
    val connection = URL(urlString).openConnection() as HttpURLConnection
    try {
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.requestMethod = "GET"
        connection.inputStream.bufferedReader().use { it.readText() }
    } finally {
        connection.disconnect()
    }
}

private fun openIntent(intent: Intent) {
    val context = AndroidAppContext.applicationContext ?: return
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching {
        context.startActivity(intent)
    }.recoverCatching {
        if (it is ActivityNotFoundException) {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, intent.data).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallbackIntent)
        } else {
            throw it
        }
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
    playAzan: Boolean = false,
) {
    val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
        putExtra(PrayerNotificationReceiver.EXTRA_NOTIFICATION_ID, requestCode)
        putExtra(PrayerNotificationReceiver.EXTRA_TITLE, title)
        putExtra(PrayerNotificationReceiver.EXTRA_MESSAGE, message)
        putExtra(PrayerNotificationReceiver.EXTRA_PLAY_AZAN, playAzan)
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
            add(azanRequestCode(index))
            PRAYER_ALERT_OFFSETS.forEach { offsetMinutes ->
                add(countdownRequestCode(index, offsetMinutes))
            }
        }
        repeat(classSchedule.size) { index ->
            add(classReminderRequestCode(index))
        }
        addAll(loadScheduledAnnouncementRequestCodes(context))
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

private fun nextWeeklyReminderTriggerMillis(
    isoDayOfWeek: Int,
    minuteOfDay: Int,
    offsetMinutes: Int,
): Long {
    val reminderMinuteOfDay = minuteOfDay - offsetMinutes
    val normalizedMinuteOfDay = ((reminderMinuteOfDay % (24 * 60)) + (24 * 60)) % (24 * 60)
    val now = Calendar.getInstance()
    val trigger = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, normalizedMinuteOfDay / 60)
        set(Calendar.MINUTE, normalizedMinuteOfDay % 60)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        val targetCalendarDay = ((isoDayOfWeek % 7) + 1)
        set(Calendar.DAY_OF_WEEK, targetCalendarDay)
        if (!after(now)) {
            add(Calendar.WEEK_OF_YEAR, 1)
        }
    }
    return trigger.timeInMillis
}

private fun announcementReminderTriggerMillis(
    date: String,
    time: String,
): Long? {
    val dateParts = date.split("/")
    val timeParts = time.split(":")
    if (dateParts.size != 3 || timeParts.size != 2) return null
    val day = dateParts[0].toIntOrNull() ?: return null
    val month = dateParts[1].toIntOrNull() ?: return null
    val year = dateParts[2].toIntOrNull() ?: return null
    val hour = timeParts[0].toIntOrNull() ?: return null
    val minute = timeParts[1].toIntOrNull() ?: return null
    val trigger = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, day)
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.MINUTE, -ANNOUNCEMENT_ALERT_OFFSET_MINUTES)
    }
    return trigger.timeInMillis.takeIf { it > System.currentTimeMillis() }
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

private fun azanRequestCode(prayerIndex: Int): Int {
    return AZAN_NOTIFICATION_REQUEST_CODE + prayerIndex
}

private fun classReminderRequestCode(classIndex: Int): Int {
    return CLASS_NOTIFICATION_REQUEST_CODE + classIndex
}

private fun announcementReminderRequestCode(announcementId: String): Int {
    return ANNOUNCEMENT_NOTIFICATION_REQUEST_CODE + (announcementId.hashCode().absoluteValue % 10_000)
}

private fun saveScheduledAnnouncementRequestCodes(context: Context, requestCodes: List<Int>) {
    context.getSharedPreferences(NOTIFICATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(SCHEDULED_ANNOUNCEMENT_CODES_KEY, requestCodes.joinToString(","))
        .apply()
}

private fun loadScheduledAnnouncementRequestCodes(context: Context): List<Int> {
    val raw = context.getSharedPreferences(NOTIFICATION_PREFERENCES_NAME, Context.MODE_PRIVATE)
        .getString(SCHEDULED_ANNOUNCEMENT_CODES_KEY, "")
        .orEmpty()
    if (raw.isBlank()) return emptyList()
    return raw.split(",").mapNotNull { it.toIntOrNull() }
}

private const val COUNTDOWN_NOTIFICATION_REQUEST_CODE = 20_000
private const val LEGACY_PRAYER_NOTIFICATION_REQUEST_CODE = 10_000
private const val AZAN_NOTIFICATION_REQUEST_CODE = 25_000
private const val CLASS_NOTIFICATION_REQUEST_CODE = 30_000
private const val ANNOUNCEMENT_NOTIFICATION_REQUEST_CODE = 40_000
private const val NOTIFICATION_PREFERENCES_NAME = "darul_ummah_notification_preferences"
private const val SCHEDULED_ANNOUNCEMENT_CODES_KEY = "scheduled_announcement_codes"
private const val DARK_MODE_PREFERENCE_KEY = "dark_mode_enabled"
private val PRAYER_ALERT_OFFSETS = listOf(30, 10)
private const val CLASS_ALERT_OFFSET_MINUTES = 60
private const val ANNOUNCEMENT_ALERT_OFFSET_MINUTES = 60
