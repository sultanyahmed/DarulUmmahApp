package com.example.darulummahapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface Platform {
    val name: String
}

internal const val MOSQUE_TIME_ZONE_ID = "Europe/London"

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
    announcements: List<Announcement>,
)

expect fun loadNotificationPreferences(): NotificationPreferences

expect fun saveNotificationPreferences(preferences: NotificationPreferences)

expect fun loadDarkModePreference(): Boolean?

expect fun saveDarkModePreference(enabled: Boolean)

expect fun openPhoneNumber(phoneNumber: String)

expect fun openEmailAddress(emailAddress: String)

expect fun openMapDirections(address: String)

expect fun openExternalUrl(url: String)

@Composable
expect fun YouTubeLivePlayer(
    channelId: String,
    modifier: Modifier = Modifier,
)

@Composable
expect fun YouTubeVideoPlayer(
    videoId: String,
    modifier: Modifier = Modifier,
)

expect suspend fun fetchDarulUmmahHomeHtml(): String

expect suspend fun fetchDarulUmmahYouTubeFeedXml(): String
