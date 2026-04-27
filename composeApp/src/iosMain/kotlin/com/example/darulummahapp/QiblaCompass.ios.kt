package com.example.darulummahapp

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreLocation.CLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.CLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.CLAuthorizationStatusDenied
import platform.CoreLocation.CLAuthorizationStatusNotDetermined
import platform.CoreLocation.CLAuthorizationStatusRestricted
import platform.CoreLocation.CLHeading
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.Foundation.NSObject

private class IOSQiblaCompassController : QiblaCompassController {
    private val mutableState = MutableStateFlow(QiblaCompassState())
    override val state: StateFlow<QiblaCompassState> = mutableState.asStateFlow()

    private val locationManager = CLLocationManager()
    private var lastHeadingDegrees: Double? = null
    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null

    private val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) {
            val heading = if (didUpdateHeading.trueHeading >= 0.0) {
                didUpdateHeading.trueHeading
            } else {
                didUpdateHeading.magneticHeading
            }
            lastHeadingDegrees = normalizeDegrees(heading)
            publishState("Compass ready. Follow the gold Qibla marker.")
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
            publishState("Could not determine your location. Check Location Services and try again.")
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
            CLAuthorizationStatusNotDetermined -> {
                mutableState.value = QiblaCompassState(
                    status = "Allow location access to calculate the Qibla from your current position.",
                )
                locationManager.requestWhenInUseAuthorization()
            }
            CLAuthorizationStatusAuthorizedAlways,
            CLAuthorizationStatusAuthorizedWhenInUse -> {
                if (CLLocationManager.headingAvailable()) {
                    locationManager.startUpdatingHeading()
                }
                locationManager.startUpdatingLocation()
                publishState("Finding your location and compass heading...")
            }
            CLAuthorizationStatusDenied,
            CLAuthorizationStatusRestricted -> {
                mutableState.value = QiblaCompassState(
                    status = "Enable Location access in iPhone Settings to show the Qibla direction.",
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
        mutableState.value = QiblaCompassState(
            headingDegrees = headingDegrees,
            qiblaBearingDegrees = qiblaBearingDegrees,
            turnDegrees = if (headingDegrees != null && qiblaBearingDegrees != null) {
                calculateTurnDegrees(headingDegrees, qiblaBearingDegrees)
            } else {
                null
            },
            latitude = latitude,
            longitude = longitude,
            status = status,
            isLocationPermissionGranted = true,
            isHeadingAvailable = CLLocationManager.headingAvailable(),
        )
    }
}

actual fun createQiblaCompassController(): QiblaCompassController = IOSQiblaCompassController()
