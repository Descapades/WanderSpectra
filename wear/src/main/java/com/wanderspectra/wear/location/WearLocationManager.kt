package com.wanderspectra.wear.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import android.location.LocationListener
import android.os.Looper
import android.os.Handler


data class WearLocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

object WearLocationManager {

    private const val TAG = "WearLocationManager"

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation(
        context: Context,
        onResult: (WearLocationData?) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Location permissions not granted")
            onResult(null)
            return
        }

        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.w(TAG, "GPS provider is disabled")
            onResult(null)
            return
        }

        val handler = Handler(Looper.getMainLooper())
        var completed = false

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (completed) return
                completed = true
                locationManager.removeUpdates(this)
                handler.removeCallbacksAndMessages(null)
                onResult(
                    WearLocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = location.time
                    )
                )
            }
        }

        val timeoutRunnable = Runnable {
            if (!completed) {
                completed = true
                locationManager.removeUpdates(listener)
                Log.w(TAG, "GPS request timed out after 15 seconds")
                onResult(null)
            }
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                0f,
                listener,
                Looper.getMainLooper()
            )

            handler.postDelayed(timeoutRunnable, 15_000L)

        } catch (e: Exception) {
            if (!completed) {
                completed = true
                handler.removeCallbacks(timeoutRunnable)
                locationManager.removeUpdates(listener)
                Log.e(TAG, "Failed to request GPS location", e)
                onResult(null)
            }
        }
    }
}
