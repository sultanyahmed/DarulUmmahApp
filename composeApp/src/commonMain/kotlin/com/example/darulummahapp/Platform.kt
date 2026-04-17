package com.example.darulummahapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun currentMinuteOfDay(): Int

expect fun currentSecondOfDay(): Int

expect fun currentIsoDayOfWeek(): Int

expect fun updateNotificationSchedules(
    preferences: NotificationPreferences,
    timetable: PrayerTimetable,
    isoDayOfWeek: Int,
)

expect fun loadNotificationPreferences(): NotificationPreferences

expect fun saveNotificationPreferences(preferences: NotificationPreferences)

expect suspend fun fetchDarulUmmahHomeHtml(): String
