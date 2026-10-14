package com.wanderspectra.wear.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

data class WearLocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

object WearLocationManager {

    private const val TAG = "WearLocationManager"
    private const val STALE_LOCATION_THRESHOLD_MS = 2 * 60 * 1000L // 2 minutes

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
            Log.w(TAG, "Location permissions not granted on Wear OS device")
            onResult(null)
            return
        }

        val fusedClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        fusedClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null && isFreshLocation(location)) {
                    Log.d(TAG, "Using fresh last known location: ${location.latitude}, ${location.longitude}")
                    onResult(
                        WearLocationData(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy,
                            timestamp = location.time
                        )
                    )
                } else {
                    Log.d(TAG, "Last known location is null or stale, requesting current location")
                    requestFreshLocation(context, fusedClient, onResult)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to get last known location", exception)
                requestFreshLocation(context, fusedClient, onResult)
            }
    }

    @SuppressLint("MissingPermission")
    private fun requestFreshLocation(
        context: Context,
        fusedClient: FusedLocationProviderClient,
        onResult: (WearLocationData?) -> Unit
    ) {
        val cancellationTokenSource = CancellationTokenSource()

        fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location: Location? ->
            if (location != null) {
                Log.d(TAG, "Current location acquired: ${location.latitude}, ${location.longitude}")
                onResult(
                    WearLocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        accuracy = location.accuracy,
                        timestamp = location.time
                    )
                )
            } else {
                Log.w(TAG, "Current location task returned null, attempting System LocationManager fallback")
                fetchFromSystemLocationManager(context, onResult)
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Failed to get current location from FusedLocationClient", exception)
            fetchFromSystemLocationManager(context, onResult)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchFromSystemLocationManager(
        context: Context,
        onResult: (WearLocationData?) -> Unit
    ) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onResult(null)
            return
        }

        try {
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            val bestLocation = when {
                gpsLocation != null && networkLocation != null -> {
                    if (gpsLocation.time > networkLocation.time) gpsLocation else networkLocation
                }
                gpsLocation != null -> gpsLocation
                else -> networkLocation
            }

            if (bestLocation != null) {
                Log.d(TAG, "System LocationManager fallback location: ${bestLocation.latitude}, ${bestLocation.longitude}")
                onResult(
                    WearLocationData(
                        latitude = bestLocation.latitude,
                        longitude = bestLocation.longitude,
                        accuracy = bestLocation.accuracy,
                        timestamp = bestLocation.time
                    )
                )
            } else {
                Log.w(TAG, "System LocationManager returned no location")
                onResult(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing System LocationManager", e)
            onResult(null)
        }
    }

    private fun isFreshLocation(location: Location): Boolean {
        val locationAge = System.currentTimeMillis() - location.time
        return locationAge < STALE_LOCATION_THRESHOLD_MS
    }
}
