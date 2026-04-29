package com.example.darulummahapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.GeomagneticField
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private class AndroidQiblaCompassController : QiblaCompassController {
    private val mutableState = MutableStateFlow(QiblaCompassState())
    override val state: StateFlow<QiblaCompassState> = mutableState.asStateFlow()

    private var sensorManager: SensorManager? = null
    private var locationManager: LocationManager? = null
    private var lastHeadingDegrees: Double? = null
    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null
    private var lastAltitudeMeters: Double? = null
    private var isStarted = false

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR) return
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val magneticHeadingDegrees = normalizeDegrees(Math.toDegrees(orientation[0].toDouble()))
            val headingDegrees = magneticHeadingDegrees.toTrueNorthHeadingDegrees()
            lastHeadingDegrees = headingDegrees
            publishState("Compass ready. Follow the gold Qibla marker.")
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lastLatitude = location.latitude
            lastLongitude = location.longitude
            lastAltitudeMeters = if (location.hasAltitude()) location.altitude else 0.0
            publishState("Location found. Follow the gold Qibla marker.")
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

        override fun onProviderEnabled(provider: String) = Unit

        override fun onProviderDisabled(provider: String) = Unit
    }

    override fun start() {
        if (isStarted) return
        isStarted = true
        val context = AndroidAppContext.applicationContext
        if (context == null) {
            mutableState.value = QiblaCompassState(status = "Android app context is unavailable.")
            return
        }
        if (!hasLocationPermission(context)) {
            mutableState.value = QiblaCompassState(
                status = "Allow location access to show the Qibla direction from your current position.",
                isLocationPermissionGranted = false,
            )
            return
        }

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationVectorSensor == null) {
            mutableState.value = QiblaCompassState(
                status = "This device does not expose a compass heading sensor.",
                isLocationPermissionGranted = true,
                isHeadingAvailable = false,
            )
            return
        }

        sensorManager?.registerListener(
            sensorListener,
            rotationVectorSensor,
            SensorManager.SENSOR_DELAY_UI,
        )

        requestLocationUpdates(context)
        publishState("Finding your location and compass heading...")
    }

    override fun stop() {
        isStarted = false
        sensorManager?.unregisterListener(sensorListener)
        locationManager?.removeUpdates(locationListener)
    }

    private fun requestLocationUpdates(context: Context) {
        val manager = locationManager ?: return
        val enabledProviders = manager.getProviders(true)
        enabledProviders.forEach { provider ->
            val lastKnownLocation = runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
            if (lastKnownLocation != null) {
                lastLatitude = lastKnownLocation.latitude
                lastLongitude = lastKnownLocation.longitude
                lastAltitudeMeters = if (lastKnownLocation.hasAltitude()) lastKnownLocation.altitude else 0.0
            }
            runCatching {
                manager.requestLocationUpdates(
                    provider,
                    2_000L,
                    5f,
                    locationListener,
                    Looper.getMainLooper(),
                )
            }
        }
        if (enabledProviders.isEmpty()) {
            mutableState.value = QiblaCompassState(
                status = "Turn on Location services to calculate the Qibla from your position.",
                isLocationPermissionGranted = hasLocationPermission(context),
                isHeadingAvailable = lastHeadingDegrees != null,
            )
        } else {
            publishState("Finding your location and compass heading...")
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
            isHeadingAvailable = headingDegrees != null,
        )
    }

    private fun hasLocationPermission(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun Double.toTrueNorthHeadingDegrees(): Double {
        val latitude = lastLatitude ?: return this
        val longitude = lastLongitude ?: return this
        val altitudeMeters = lastAltitudeMeters ?: 0.0
        val declinationDegrees = GeomagneticField(
            latitude.toFloat(),
            longitude.toFloat(),
            altitudeMeters.toFloat(),
            System.currentTimeMillis(),
        ).declination.toDouble()
        return normalizeDegrees(this + declinationDegrees)
    }
}

actual fun createQiblaCompassController(): QiblaCompassController = AndroidQiblaCompassController()
