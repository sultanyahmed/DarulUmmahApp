package com.example.darulummahapp

import kotlinx.coroutines.flow.StateFlow
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class QiblaCompassState(
    val headingDegrees: Double? = null,
    val qiblaBearingDegrees: Double? = null,
    val turnDegrees: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String = "Open Settings while allowing location access to show the Qibla compass.",
    val isLocationPermissionGranted: Boolean = false,
    val isHeadingAvailable: Boolean = false,
)

interface QiblaCompassController {
    val state: StateFlow<QiblaCompassState>

    fun start()

    fun stop()
}

expect fun createQiblaCompassController(): QiblaCompassController

internal fun calculateQiblaBearingDegrees(
    latitude: Double,
    longitude: Double,
): Double {
    val kaabaLatitude = 21.4225
    val kaabaLongitude = 39.8262
    val latitudeRadians = latitude.toRadians()
    val kaabaLatitudeRadians = kaabaLatitude.toRadians()
    val longitudeDeltaRadians = (kaabaLongitude - longitude).toRadians()
    val y = sin(longitudeDeltaRadians)
    val x = cos(latitudeRadians) * tan(kaabaLatitudeRadians) -
        sin(latitudeRadians) * cos(longitudeDeltaRadians)
    return normalizeDegrees(atan2(y, x).toDegrees())
}

internal fun calculateTurnDegrees(
    headingDegrees: Double,
    qiblaBearingDegrees: Double,
): Double {
    return normalizeSignedDegrees(qiblaBearingDegrees - headingDegrees)
}

internal fun normalizeDegrees(value: Double): Double {
    var normalized = value % 360.0
    if (normalized < 0) normalized += 360.0
    return normalized
}

internal fun normalizeSignedDegrees(value: Double): Double {
    var normalized = normalizeDegrees(value)
    if (normalized > 180.0) normalized -= 360.0
    return normalized
}

private fun Double.toRadians(): Double = this * PI / 180.0

private fun Double.toDegrees(): Double = this * 180.0 / PI

private fun tan(value: Double): Double = sin(value) / cos(value)
