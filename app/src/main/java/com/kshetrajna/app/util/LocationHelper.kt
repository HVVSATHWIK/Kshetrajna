package com.kshetrajna.app.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.math.abs

data class CapturedLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val formattedCoordinates: String,
    val addressSummary: String? = null,
    val altitudeMeters: Double? = null,
    val bearingDegrees: Float? = null,
    val fixTimestamp: Long = System.currentTimeMillis(),
    val isStale: Boolean = false,
    val decimalCoordinates: String = String.format(Locale.US, "%.6f, %.6f", latitude, longitude),
    val dmsCoordinates: String = toDms(latitude, longitude)
) {
    companion object {
        fun toDms(lat: Double, lng: Double): String {
            val latDir = if (lat >= 0) "N" else "S"
            val lngDir = if (lng >= 0) "E" else "W"

            val latAbs = abs(lat)
            val latDeg = latAbs.toInt()
            val latMin = ((latAbs - latDeg) * 60).toInt()
            val latSec = ((latAbs - latDeg - latMin / 60.0) * 3600)

            val lngAbs = abs(lng)
            val lngDeg = lngAbs.toInt()
            val lngMin = ((lngAbs - lngDeg) * 60).toInt()
            val lngSec = ((lngAbs - lngDeg - lngMin / 60.0) * 3600)

            return String.format(
                Locale.US,
                "%d°%02d'%04.1f\"%s, %d°%02d'%04.1f\"%s",
                latDeg, latMin, latSec, latDir,
                lngDeg, lngMin, lngSec, lngDir
            )
        }
    }
}

sealed interface LocationStatus {
    object Disabled : LocationStatus
    object PermissionDenied : LocationStatus
    object Acquiring : LocationStatus
    data class Ready(val location: CapturedLocation) : LocationStatus
    data class PoorAccuracy(val location: CapturedLocation) : LocationStatus
    object Unavailable : LocationStatus
}

class LocationHelper(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    companion object {
        const val STALE_THRESHOLD_MS = 60_000L // 60 seconds
        const val ACCURACY_POOR_THRESHOLD_METERS = 50.0f
    }

    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(timeoutMs: Long = 4000L): CapturedLocation? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext null
        }

        try {
            val location: Location? = withTimeoutOrNull(timeoutMs) {
                val cts = CancellationTokenSource()
                try {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cts.token
                    ).await()
                } catch (_: Exception) {
                    null
                }
            } ?: try {
                fusedLocationClient.lastLocation.await()
            } catch (_: Exception) {
                null
            }

            if (location != null) {
                val now = System.currentTimeMillis()
                val fixAge = if (location.time > 0) now - location.time else 0L
                val isStale = fixAge > STALE_THRESHOLD_MS

                val formattedCoords = formatCoordinates(location.latitude, location.longitude)
                val address = resolveAddress(location.latitude, location.longitude)

                CapturedLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracyMeters = if (location.hasAccuracy()) location.accuracy else 0f,
                    formattedCoordinates = formattedCoords,
                    addressSummary = address,
                    altitudeMeters = if (location.hasAltitude()) location.altitude else null,
                    bearingDegrees = if (location.hasBearing()) location.bearing else null,
                    fixTimestamp = if (location.time > 0) location.time else now,
                    isStale = isStale
                )
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun determineStatus(
        recordLocationEnabled: Boolean,
        isAcquiring: Boolean,
        location: CapturedLocation?
    ): LocationStatus {
        if (!recordLocationEnabled) return LocationStatus.Disabled
        if (location != null) {
            return if (location.accuracyMeters > ACCURACY_POOR_THRESHOLD_METERS) {
                LocationStatus.PoorAccuracy(location)
            } else {
                LocationStatus.Ready(location)
            }
        }
        if (!hasLocationPermission()) return LocationStatus.PermissionDenied
        if (isAcquiring) return LocationStatus.Acquiring
        return LocationStatus.Unavailable
    }

    fun formatCoordinates(lat: Double, lng: Double): String {
        val latDir = if (lat >= 0) "N" else "S"
        val lngDir = if (lng >= 0) "E" else "W"
        return String.format(Locale.US, "%.5f° %s, %.5f° %s", abs(lat), latDir, abs(lng), lngDir)
    }

    suspend fun resolveAddress(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val parts = listOfNotNull(
                    addr.subLocality ?: addr.locality,
                    addr.adminArea,
                    addr.countryName
                )
                if (parts.isNotEmpty()) parts.joinToString(", ") else null
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}

