package com.example.darulummahapp

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreLocation.CLHeading
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.darwin.NSObject

private class IOSQiblaCompassController : QiblaCompassController {
    private val mutableState = MutableStateFlow(QiblaCompassState())
    override val state: StateFlow<QiblaCompassState> = mutableState.asStateFlow()

    private val locationManager = CLLocationManager()
    private var lastHeadingDegrees: Double? = null
    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null

    private val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) {
            if (didUpdateHeading.trueHeading >= 0.0) {
                lastHeadingDegrees = normalizeDegrees(didUpdateHeading.trueHeading)
                publishState("Compass ready. Follow the gold Qibla marker.")
            } else {
                lastHeadingDegrees = null
                publishState("Move phone in a figure 8 to calibrate")
            }
        }

        @OptIn(ExperimentalForeignApi::class)
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val location = didUpdateLocations.lastOrNull() as? CLLocation ?: return
            val coordinate = location.coordinate.useContents { this }
            lastLatitude = coordinate.latitude
            lastLongitude = coordinate.longitude
            publishState("Location found. Follow the gold Qibla marker.")
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: platform.Foundation.NSError) {
            publishState("Waiting for location")
        }

        override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
            updateAuthorizationState()
        }
    }

    init {
        locationManager.delegate = delegate
        locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
    }

    override fun start() {
        updateAuthorizationState()
    }

    override fun stop() {
        locationManager.stopUpdatingHeading()
        locationManager.stopUpdatingLocation()
    }

    private fun updateAuthorizationState() {
        when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusNotDetermined -> {
                mutableState.value = QiblaCompassState(
                    status = "Waiting for location",
                )
                locationManager.requestWhenInUseAuthorization()
            }
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> {
                if (CLLocationManager.headingAvailable()) {
                    locationManager.startUpdatingHeading()
                }
                locationManager.startUpdatingLocation()
                publishState(
                    if (CLLocationManager.headingAvailable()) {
                        "Waiting for location"
                    } else {
                        "Compass permission needed"
                    },
                )
            }
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> {
                mutableState.value = QiblaCompassState(
                    status = "Waiting for location",
                    isLocationPermissionGranted = false,
                    isHeadingAvailable = false,
                )
            }
            else -> {
                mutableState.value = QiblaCompassState(
                    status = "Qibla compass is unavailable on this device.",
                    isLocationPermissionGranted = false,
                )
            }
        }
    }

    private fun publishState(status: String) {
        val headingDegrees = lastHeadingDegrees
        val latitude = lastLatitude
        val longitude = lastLongitude
        val qiblaBearingDegrees = if (latitude != null && longitude != null) {
            calculateQiblaBearingDegrees(latitude, longitude)
        } else {
            null
        }
        val turnDegrees = if (headingDegrees != null && qiblaBearingDegrees != null) {
            calculateTurnDegrees(headingDegrees, qiblaBearingDegrees)
        } else {
            null
        }
        logQiblaState(
            latitude = latitude,
            longitude = longitude,
            headingDegrees = headingDegrees,
            qiblaBearingDegrees = qiblaBearingDegrees,
            turnDegrees = turnDegrees,
        )
        mutableState.value = QiblaCompassState(
            headingDegrees = headingDegrees,
            qiblaBearingDegrees = qiblaBearingDegrees,
            turnDegrees = turnDegrees,
            latitude = latitude,
            longitude = longitude,
            status = status,
            isLocationPermissionGranted = true,
            isHeadingAvailable = headingDegrees != null,
        )
    }

    private fun logQiblaState(
        latitude: Double?,
        longitude: Double?,
        headingDegrees: Double?,
        qiblaBearingDegrees: Double?,
        turnDegrees: Double?,
    ) {
        println("QiblaCompass user latitude: ${latitude ?: "waiting"}")
        println("QiblaCompass user longitude: ${longitude ?: "waiting"}")
        println("QiblaCompass device heading: ${headingDegrees ?: "waiting"}")
        println("QiblaCompass qibla bearing: ${qiblaBearingDegrees ?: "waiting"}")
        println("QiblaCompass turn angle: ${turnDegrees ?: "waiting"}")
    }
}

actual fun createQiblaCompassController(): QiblaCompassController = IOSQiblaCompassController()
