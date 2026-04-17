package com.example.darulummahapp

import kotlin.test.Test
import kotlin.test.assertEquals

class ComposeAppCommonTest {

    @Test
    fun parsesDarulUmmahHomepageTimetable() {
        val html = """
            <tbody class="timetable-font">
              <tr>
                <td>BEGINS</td>
                <td>4:35<span>AM</span></td>
                <td>1:05<span>PM</span></td>
                <td>5:49<span>PM</span></td>
                <td>8:03<span>PM</span></td>
                <td>9:19<span>PM</span></td>
                <td>1:30<span>PM</span></td>
              </tr>
              <tr>
                <td>JAMA'AH</td>
                <td>5:15<span>AM</span></td>
                <td>1:30<span>PM</span></td>
                <td>6:15<span>PM</span></td>
                <td>8:08<span>PM</span></td>
                <td>9:40<span>PM</span></td>
                <td>1:50<span>PM</span></td>
              </tr>
            </tbody>
        """.trimIndent()

        val timetable = parseDarulUmmahTimetable(html)

        assertEquals("04:35", timetable.dailyPrayerTimes[0].beginsTime)
        assertEquals("05:15", timetable.dailyPrayerTimes[0].jamaahTime)
        assertEquals("13:30", timetable.dailyPrayerTimes[1].jamaahTime)
        assertEquals("20:08", timetable.dailyPrayerTimes[3].jamaahTime)
        assertEquals("13:30", timetable.jumahTime.khutbahTime)
        assertEquals("13:50", timetable.jumahTime.salaatTime)
    }

    @Test
    fun fridayCountdownUsesJumahInsteadOfZuhr() {
        val timetable = PrayerTimetable(
            dailyPrayerTimes = listOf(
                PrayerTime("Fajr", "04:35", "05:15", 315),
                PrayerTime("Zuhr", "13:05", "13:30", 810),
                PrayerTime("Asr", "17:49", "18:15", 1095),
                PrayerTime("Maghrib", "20:03", "20:08", 1208),
                PrayerTime("Isha", "21:19", "21:40", 1300),
            ),
            jumahTime = JumahTime("13:30", "13:50"),
        )

        val upcoming = upcomingPrayer(
            timetable = timetable,
            minuteOfDay = 13 * 60,
            isoDayOfWeek = 5,
        )

        assertEquals("Jum'ah", upcoming.name)
        assertEquals("13:50", upcoming.jamaahTime)
        assertEquals(13 * 60 + 50, upcoming.minuteOfDay)
    }

    @Test
    fun formatsCountdownWithSeconds() {
        assertEquals("1h 02m 03s remaining", formatCountdown(3_723))
    }
}
