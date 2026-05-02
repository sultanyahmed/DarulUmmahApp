package com.example.darulummahapp

import kotlin.test.Test
import kotlin.test.assertEquals

class PrayerCountdownTest {
    private val timetable = PrayerTimetable(
        dailyPrayerTimes = listOf(
            PrayerTime("Fajr", "04:35", "05:15", 5 * 60 + 15),
            PrayerTime("Zuhr", "13:05", "13:30", 13 * 60 + 30),
            PrayerTime("Asr", "17:49", "18:15", 18 * 60 + 15),
            PrayerTime("Maghrib", "20:03", "20:08", 20 * 60 + 8),
            PrayerTime("Isha", "21:19", "21:40", 21 * 60 + 40),
        ),
        jumahTime = JumahTime(khutbahTime = "13:00", salaatTime = "13:30"),
    )

    @Test
    fun upcomingPrayerUsesSecondsBeforeExactPrayerTime() {
        val upcoming = upcomingPrayer(
            timetable = timetable,
            secondOfDay = (13 * 60 + 29) * 60 + 59,
            isoDayOfWeek = 1,
        )

        assertEquals("Zuhr", upcoming.name)
        assertEquals("13:30", upcoming.jamaahTime)
    }

    @Test
    fun upcomingPrayerMovesOnAtExactPrayerSecond() {
        val upcoming = upcomingPrayer(
            timetable = timetable,
            secondOfDay = (13 * 60 + 30) * 60,
            isoDayOfWeek = 1,
        )

        assertEquals("Asr", upcoming.name)
        assertEquals("18:15", upcoming.jamaahTime)
    }

    @Test
    fun upcomingPrayerWrapsToFajrAfterIsha() {
        val upcoming = upcomingPrayer(
            timetable = timetable,
            secondOfDay = (21 * 60 + 40) * 60,
            isoDayOfWeek = 1,
        )

        assertEquals("Fajr", upcoming.name)
        assertEquals("05:15", upcoming.jamaahTime)
    }

    @Test
    fun fridayUpcomingPrayerUsesJumahUntilSalaatTime() {
        val upcoming = upcomingPrayer(
            timetable = timetable,
            secondOfDay = (13 * 60 + 29) * 60 + 59,
            isoDayOfWeek = 5,
        )

        assertEquals("Jum'ah", upcoming.name)
        assertEquals("13:30", upcoming.jamaahTime)
    }
}
