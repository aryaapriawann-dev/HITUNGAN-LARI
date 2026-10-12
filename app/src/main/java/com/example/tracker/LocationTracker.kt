package com.example.tracker

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.example.data.model.RunPoint
import kotlin.math.cos
import kotlin.math.sin

class LocationTracker(
    private val context: Context,
    private val onLocationUpdate: (RunPoint) -> Unit
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private var isTracking = false
    private var lastLocation: Location? = null

    // For Demo / Simulation mode
    private var isSimulated = false
    private var simAngle = 0.0
    // Center around Gelora Bung Karno sports complex, Jakarta
    private val centerLat = -6.2185
    private val centerLng = 106.8020
    private val radiusLat = 0.0035 // ~400m
    private val radiusLng = 0.0040

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (!isTracking || isSimulated) return

            // Basic filtering for GPS jitter
            if (location.hasAccuracy() && location.accuracy > 35f) {
                return
            }

            val speedKmh = if (location.hasSpeed()) {
                (location.speed * 3.6).toDouble()
            } else {
                lastLocation?.let { prev ->
                    val dist = FloatArray(1)
                    Location.distanceBetween(prev.latitude, prev.longitude, location.latitude, location.longitude, dist)
                    val timeDiffSec = (location.time - prev.time) / 1000.0
                    if (timeDiffSec > 0) (dist[0] / timeDiffSec) * 3.6 else 0.0
                } ?: 0.0
            }

            lastLocation = location
            onLocationUpdate(
                RunPoint(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    timestamp = location.time,
                    speedKmh = speedKmh
                )
            )
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("MissingPermission")
    fun start(simulated: Boolean = false) {
        isTracking = true
        isSimulated = simulated
        lastLocation = null

        if (simulated) {
            simAngle = 0.0
            // Initial simulated point
            simulateTick()
            return
        }

        try {
            locationManager?.let { lm ->
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000L,
                        2f,
                        locationListener,
                        Looper.getMainLooper()
                    )
                } else if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000L,
                        2f,
                        locationListener,
                        Looper.getMainLooper()
                    )
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted, handled by caller
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun simulateTick() {
        if (!isTracking || !isSimulated) return
        
        // Realistic jogging speed: ~10.8 km/h = 3 m/s
        // Increase angle around stadium loop
        simAngle += 0.035
        val lat = centerLat + (radiusLat * sin(simAngle))
        val lng = centerLng + (radiusLng * cos(simAngle))
        
        // Slight natural speed fluctuation between 9.5 and 11.5 km/h
        val simulatedSpeed = 10.2 + (sin(simAngle * 3) * 1.0)
        
        onLocationUpdate(
            RunPoint(
                latitude = lat,
                longitude = lng,
                timestamp = System.currentTimeMillis(),
                speedKmh = simulatedSpeed
            )
        )
    }

    fun stop() {
        isTracking = false
        try {
            locationManager?.removeUpdates(locationListener)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        lastLocation = null
    }

    fun isGpsEnabled(): Boolean {
        return try {
            locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                    locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        } catch (e: Exception) {
            false
        }
    }
}
