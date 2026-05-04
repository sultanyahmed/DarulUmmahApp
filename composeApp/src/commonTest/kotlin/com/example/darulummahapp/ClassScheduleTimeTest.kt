package com.example.darulummahapp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClassScheduleTimeTest {
    private val eveningClass = ClassSession(
        title = "Evening class",
        day = "Tuesday",
        bstTime = "19:00",
        audience = "Community",
        reminderMinuteOfDay = 19 * 60,
    )

    private val morningRangeClass = ClassSession(
        title = "Morning class",
        day = "Wednesday",
        bstTime = "11:00 - 12:30",
        audience = "Sisters",
        reminderMinuteOfDay = 11 * 60,
    )

    @Test
    fun bstKeepsCurrentClassTimes() {
        val date = DateTimeComponents(2026, 5, 4, 12, 0, 0)

        assertTrue(isBritishSummerTime(date))
        assertEquals("19:00", classSessionDisplayTime(eveningClass, date))
        assertEquals(19 * 60, classSessionReminderMinuteOfDay(eveningClass, date))
    }

    @Test
    fun gmtShowsClassesOneHourEarlierThanBstSourceTimes() {
        val date = DateTimeComponents(2026, 12, 1, 12, 0, 0)

        assertFalse(isBritishSummerTime(date))
        assertEquals("18:00", classSessionDisplayTime(eveningClass, date))
        assertEquals(18 * 60, classSessionReminderMinuteOfDay(eveningClass, date))
    }

    @Test
    fun gmtShiftsTimeRanges() {
        val date = DateTimeComponents(2026, 1, 7, 12, 0, 0)

        assertEquals("10:00 - 11:30", classSessionDisplayTime(morningRangeClass, date))
    }

    @Test
    fun ukClockChangeBoundaryDatesAreHandled() {
        assertTrue(isBritishSummerTime(DateTimeComponents(2026, 3, 29, 19, 0, 0)))
        assertFalse(isBritishSummerTime(DateTimeComponents(2026, 10, 25, 19, 0, 0)))
    }
}
