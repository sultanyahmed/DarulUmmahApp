package com.example.darulummahapp

interface Platform {
    val name: String
}

data class DateTimeComponents(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
)

expect fun getPlatform(): Platform

expect fun currentMinuteOfDay(): Int

expect fun currentSecondOfDay(): Int

expect fun currentIsoDayOfWeek(): Int

expect fun currentDateTimeComponents(): DateTimeComponents

expect fun updateNotificationSchedules(
    preferences: NotificationPreferences,
    timetable: PrayerTimetable,
    isoDayOfWeek: Int,
)

expect fun loadNotificationPreferences(): NotificationPreferences

expect fun saveNotificationPreferences(preferences: NotificationPreferences)

expect fun openPhoneNumber(phoneNumber: String)

expect fun openEmailAddress(emailAddress: String)

expect fun openMapDirections(address: String)

expect suspend fun fetchDarulUmmahHomeHtml(): String

expect suspend fun fetchDarulUmmahPrayerTimetableHtml(): String
