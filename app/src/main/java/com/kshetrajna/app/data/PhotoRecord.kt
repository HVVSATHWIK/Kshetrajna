package com.kshetrajna.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PhotoProvenance {
    CAMERA_CAPTURE,
    IMPORTED
}

enum class LocationProvenance {
    CAMERA_CAPTURE_DEVICE_GPS,
    EXIF_METADATA_GPS,
    IMPORT_TIME_DEVICE_GPS,
    NONE
}

@Entity(tableName = "photo_records")
data class PhotoRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalFilePath: String,
    val stampedFilePath: String? = null,
    val timestamp: Long,
    val formattedDateTime: String,
    val hasLocation: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val locationSummary: String? = null,
    val addressText: String? = null,
    val note: String = "",
    val isStamped: Boolean = true,
    val provenance: String = "CAMERA_CAPTURE",
    val originalCaptureTime: String? = null,
    val importTime: String? = null,
    val locationProvenance: String = "CAMERA_CAPTURE_DEVICE_GPS"
)
