package com.kshetrajna.app.util

import android.media.ExifInterface
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ParsedExifData(
    val captureTimestamp: Long?,
    val formattedCaptureTime: String?,
    val latitude: Double?,
    val longitude: Double?,
    val formattedCoordinates: String?,
    val orientation: Int,
    val cameraModel: String?
)

object ExifMetadataHelper {
    private const val TAG = "KshetrajnaWorkflow"

    fun extractExifData(file: File): ParsedExifData {
        if (!file.exists() || file.length() == 0L) {
            return ParsedExifData(null, null, null, null, null, ExifInterface.ORIENTATION_NORMAL, null)
        }

        try {
            val exif = ExifInterface(file.absolutePath)

            // Extract Date/Time
            val rawDate = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
                ?: exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED)

            var parsedTimestamp: Long? = null
            var formattedTime: String? = null

            if (!rawDate.isNullOrBlank()) {
                val patterns = listOf(
                    "yyyy:MM:dd HH:mm:ss",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy:MM:dd HH:mm",
                    "yyyy-MM-dd'T'HH:mm:ss"
                )
                for (pattern in patterns) {
                    try {
                        val sdf = SimpleDateFormat(pattern, Locale.US)
                        val date = sdf.parse(rawDate.trim())
                        if (date != null) {
                            parsedTimestamp = date.time
                            val displayFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
                            formattedTime = displayFormat.format(date)
                            break
                        }
                    } catch (_: Exception) {}
                }
            }

            // Extract GPS
            val latLong = FloatArray(2)
            val hasGps = exif.getLatLong(latLong)
            val latitude = if (hasGps) latLong[0].toDouble() else null
            val longitude = if (hasGps) latLong[1].toDouble() else null

            val formattedCoords = if (latitude != null && longitude != null) {
                val latDir = if (latitude >= 0) "N" else "S"
                val lngDir = if (longitude >= 0) "E" else "W"
                String.format(Locale.US, "%.5f° %s, %.5f° %s", Math.abs(latitude), latDir, Math.abs(longitude), lngDir)
            } else {
                null
            }

            // Orientation
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            // Camera Model
            val make = exif.getAttribute(ExifInterface.TAG_MAKE)
            val model = exif.getAttribute(ExifInterface.TAG_MODEL)
            val cameraModel = when {
                !make.isNullOrBlank() && !model.isNullOrBlank() -> "$make $model"
                !model.isNullOrBlank() -> model
                else -> null
            }

            Log.d(
                TAG,
                "EXIF extracted for file ${file.name}: hasDate=${parsedTimestamp != null}, hasGps=$hasGps, orientation=$orientation"
            )

            return ParsedExifData(
                captureTimestamp = parsedTimestamp,
                formattedCaptureTime = formattedTime,
                latitude = latitude,
                longitude = longitude,
                formattedCoordinates = formattedCoords,
                orientation = orientation,
                cameraModel = cameraModel
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read EXIF for ${file.name}: ${e.message}")
            return ParsedExifData(null, null, null, null, null, ExifInterface.ORIENTATION_NORMAL, null)
        }
    }
}
