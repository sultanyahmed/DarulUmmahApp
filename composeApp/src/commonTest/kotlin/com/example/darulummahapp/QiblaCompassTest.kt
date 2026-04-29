package com.example.darulummahapp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QiblaCompassTest {
    @Test
    fun londonQiblaBearingMatchesExpectedRange() {
        val bearing = calculateQiblaBearingDegrees(
            latitude = 51.5074,
            longitude = -0.1278,
        )

        assertTrue(
            bearing in 118.9..120.3,
            "Expected London Qibla bearing near 119°, got $bearing",
        )
    }

    @Test
    fun newYorkQiblaBearingMatchesExpectedRange() {
        val bearing = calculateQiblaBearingDegrees(
            latitude = 40.7128,
            longitude = -74.0060,
        )

        assertTrue(
            bearing in 58.0..59.5,
            "Expected New York Qibla bearing near 58.5°, got $bearing",
        )
    }

    @Test
    fun turnDegreesUsesShortestDirection() {
        assertEquals(30.0, calculateTurnDegrees(90.0, 120.0), 0.0001)
        assertEquals(-20.0, calculateTurnDegrees(120.0, 100.0), 0.0001)
        assertEquals(-170.0, calculateTurnDegrees(350.0, 180.0), 0.0001)
    }
}
