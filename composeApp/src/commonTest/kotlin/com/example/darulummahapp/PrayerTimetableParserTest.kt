package com.example.darulummahapp

import kotlin.test.Test
import kotlin.test.assertEquals

class PrayerTimetableParserTest {
    @Test
    fun currentGridParserUsesPrayerPeriodWhenSourceMeridiemIsWrong() {
        val html = """
            <div class="col-md-4">
                <div class="Demo">FAJR BEGINS</div><div class="Demo">3:54AM</div>
                <div class="Demo">FAJR JAMA'AH</div><div class="Demo">4:30AM</div>
                <div class="Demo">ZUHR BEGINS</div><div class="Demo">1:02AM</div>
                <div class="Demo">ZUHR JAMA'AH</div><div class="Demo">1:30PM</div>
                <div class="Demo">ASR BEGINS</div><div class="Demo">6:06PM</div>
                <div class="Demo">ASR JAMA'AH</div><div class="Demo">6:30PM</div>
                <div class="Demo">MAGHRIB BEGINS</div><div class="Demo">8:29PM</div>
                <div class="Demo">MAGHRIB JAMA'AH</div><div class="Demo">8:34PM</div>
                <div class="Demo">ISHA BEGINS</div><div class="Demo">9:45PM</div>
                <div class="Demo">ISHA JAMA'AH</div><div class="Demo">10:10PM</div>
            </div>
            <div class="col-md-8"></div>
        """.trimIndent()

        val zuhr = parseDarulUmmahTimetable(html).dailyPrayerTimes.single { it.name == "Zuhr" }

        assertEquals("13:02", zuhr.beginsTime)
        assertEquals("13:30", zuhr.jamaahTime)
        assertEquals(13 * 60 + 30, zuhr.minuteOfDay)
    }

    @Test
    fun fullCalendarParserUsesPrayerPeriodWhenSourceOmitsMeridiem() {
        val html = """
            <h2>FULL TIMETABLE 2026</h2>
            <table>
                <tr>
                    <td>3 May</td>
                    <td>04:20</td>
                    <td>05:35</td>
                    <td>05:00</td>
                    <td>1:02</td>
                    <td>13:30</td>
                    <td>17:45</td>
                    <td>18:15</td>
                    <td>20:25</td>
                    <td>20:30</td>
                    <td>21:38</td>
                    <td>22:00</td>
                </tr>
            </table>
        """.trimIndent()

        val prayerTime = parseFullCalendarTimetable(html).single()

        assertEquals("13:02", prayerTime.dhuhrBegins)
        assertEquals("13:30", prayerTime.dhuhrJamaah)
        assertEquals("17:45", prayerTime.asrBegins)
        assertEquals("18:15", prayerTime.asrJamaah)
        assertEquals("20:25", prayerTime.maghribBegins)
        assertEquals("20:30", prayerTime.maghribJamaah)
        assertEquals("21:38", prayerTime.ishaBegins)
        assertEquals("22:00", prayerTime.ishaJamaah)
    }

    @Test
    fun timetableRowFallbackUsesPrayerPeriodWhenSourceMeridiemIsWrong() {
        val html = """
            <tbody class="timetable-font">
                <tr>
                    <td>BEGINS</td>
                    <td>3:54<span>AM</span></td>
                    <td>1:02<span>AM</span></td>
                    <td>6:06<span>PM</span></td>
                    <td>8:29<span>PM</span></td>
                    <td>9:45<span>PM</span></td>
                    <td>1:15<span>PM</span></td>
                </tr>
                <tr>
                    <td>JAMA'AH</td>
                    <td>4:30<span>AM</span></td>
                    <td>1:30<span>PM</span></td>
                    <td>6:30<span>PM</span></td>
                    <td>8:34<span>PM</span></td>
                    <td>10:10<span>PM</span></td>
                    <td>1:30<span>PM</span></td>
                </tr>
            </tbody>
        """.trimIndent()

        val zuhr = parseDarulUmmahTimetable(html).dailyPrayerTimes.single { it.name == "Zuhr" }

        assertEquals("13:02", zuhr.beginsTime)
        assertEquals("13:30", zuhr.jamaahTime)
    }
}
