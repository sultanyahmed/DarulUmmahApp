package com.example.darulummahapp

import kotlin.test.Test
import kotlin.test.assertEquals

class PrayerTimetableParserTest {
    @Test
    fun fullCalendarParserTreatsOneOClockDhuhrAsAfternoon() {
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
    }
}
