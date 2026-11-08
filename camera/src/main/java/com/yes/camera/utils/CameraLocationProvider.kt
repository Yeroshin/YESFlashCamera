package com.yes.camera.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat

class CameraLocationProvider(private val context: Context) {
    private val locationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    private var latestLocation: Location? = null
    private val locationListener = LocationListener { location ->
        latestLocation = location
        Log.d("CameraLocationProvider", "Location updated in background: $location")
    }

    fun hasLocationPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocation == PackageManager.PERMISSION_GRANTED && coarseLocation == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (!hasLocationPermissions()) return
        val lm = locationManager ?: return
        try {
            latestLocation = getBestLastKnownLocation(lm)

            val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val netEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (gpsEnabled) {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    1f,
                    locationListener,
                    Looper.getMainLooper(),
                )
            }
            if (netEnabled) {
                lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000L,
                    1f,
                    locationListener,
                    Looper.getMainLooper(),
                )
            }
        } catch (e: Exception) {
            Log.e("CameraLocationProvider", "Error starting location tracking: ${e.message}")
        }
    }

    fun stopTracking() {
        val lm = locationManager ?: return
        try {
            lm.removeUpdates(locationListener)
        } catch (e: Exception) {
            Log.e("CameraLocationProvider", "Error stopping location tracking: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(): Location? {
        if (!hasLocationPermissions()) return null
        return latestLocation ?: locationManager?.let { getBestLastKnownLocation(it) }
    }

    private fun getBestLastKnownLocation(lm: LocationManager): Location? {
        return try {
            val gpsLoc = try {
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                } else null
            } catch (e: SecurityException) {
                null
            }

            val netLoc = try {
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                } else null
            } catch (e: SecurityException) {
                null
            }

            when {
                (gpsLoc != null) && (netLoc != null) -> {
                    if (gpsLoc.time > netLoc.time) gpsLoc else netLoc
                }
                gpsLoc != null -> gpsLoc
                else -> netLoc
            }
        } catch (e: Exception) {
            null
        }
    }
}
