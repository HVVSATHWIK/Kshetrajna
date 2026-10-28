package com.kshetrajna.app.data

import android.content.Context
import android.util.Log
import com.kshetrajna.app.util.CapturedLocation
import com.kshetrajna.app.util.ImageStamper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class PhotoRepository(private val context: Context) {
    private val database = KshetrajnaDatabase.getInstance(context)
    private val dao = database.photoRecordDao()

    companion object {
        private const val TAG = "KshetrajnaWorkflow"
    }

    val allPhotos: Flow<List<PhotoRecord>> = dao.getAllPhotos()

    fun getPhotosDirectory(): File {
        val dir = File(context.filesDir, "photos")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun savePhotoRecord(
        tempFile: File,
        provenance: PhotoProvenance,
        primaryTimeText: String,
        secondaryTimeText: String? = null,
        rawTimestamp: Long = System.currentTimeMillis(),
        location: CapturedLocation? = null,
        locationProvenance: LocationProvenance = LocationProvenance.NONE,
        note: String = "",
        createStamp: Boolean = true
    ): PhotoRecord = withContext(Dispatchers.IO) {
        Log.d(TAG, "Save started: provenance=$provenance, isStamped=$createStamp, size=${tempFile.length()} bytes")

        if (!tempFile.exists() || tempFile.length() == 0L) {
            Log.e(TAG, "Save failed: temporary file is missing or empty")
            throw IllegalStateException("Cannot save nonexistent or empty image file")
        }

        val photosDir = getPhotosDirectory()
        val originalFile = File(photosDir, "ksh_${rawTimestamp}_orig.jpg")
        tempFile.copyTo(originalFile, overwrite = true)
        try { tempFile.delete() } catch (_: Exception) {}

        if (!originalFile.exists() || originalFile.length() == 0L) {
            Log.e(TAG, "Save failed: persistent copy could not be created")
            throw IllegalStateException("Failed to create durable image copy in internal storage")
        }

        var stampedPath: String? = null
        if (createStamp) {
            val stampedFile = File(photosDir, "ksh_${rawTimestamp}_stamped.jpg")
            try {
                val stampedBitmap = ImageStamper.createStampedBitmap(
                    originalFile = originalFile,
                    provenance = provenance,
                    primaryTimeText = primaryTimeText,
                    secondaryTimeText = secondaryTimeText,
                    locationText = location?.let { "${it.formattedCoordinates} (±${it.accuracyMeters.toInt()}m)" },
                    locationProvenance = locationProvenance,
                    noteText = note.ifBlank { null }
                )
                if (ImageStamper.saveBitmapToFile(stampedBitmap, stampedFile)) {
                    stampedPath = stampedFile.absolutePath
                }
            } catch (e: Exception) {
                Log.w(TAG, "Stamping failed (original preserved): ${e.message}")
            }
        }

        val record = PhotoRecord(
            originalFilePath = originalFile.absolutePath,
            stampedFilePath = stampedPath,
            timestamp = rawTimestamp,
            formattedDateTime = primaryTimeText,
            hasLocation = location != null,
            latitude = location?.latitude,
            longitude = location?.longitude,
            accuracyMeters = location?.accuracyMeters,
            locationSummary = location?.formattedCoordinates,
            addressText = location?.addressSummary,
            note = note,
            isStamped = createStamp && stampedPath != null,
            provenance = provenance.name,
            originalCaptureTime = secondaryTimeText,
            importTime = if (provenance == PhotoProvenance.IMPORTED) primaryTimeText else null,
            locationProvenance = locationProvenance.name
        )

        val id = dao.insertPhoto(record)
        val savedRecord = record.copy(id = id)
        Log.d(TAG, "Save succeeded: id=$id, provenance=$provenance, isStamped=${savedRecord.isStamped}")
        savedRecord
    }

    suspend fun updateNote(id: Long, note: String) = withContext(Dispatchers.IO) {
        dao.updateNote(id, note)
        Log.d(TAG, "Note updated: id=$id")
    }

    suspend fun deletePhoto(record: PhotoRecord) = withContext(Dispatchers.IO) {
        try {
            File(record.originalFilePath).delete()
        } catch (_: Exception) {}
        record.stampedFilePath?.let {
            try { File(it).delete() } catch (_: Exception) {}
        }
        dao.deletePhoto(record)
        Log.d(TAG, "Delete succeeded: id=${record.id}")
    }

    suspend fun deleteAllPhotos() = withContext(Dispatchers.IO) {
        dao.deleteAllPhotos()
        val dir = getPhotosDirectory()
        dir.listFiles()?.forEach {
            try { it.delete() } catch (_: Exception) {}
        }
        Log.d(TAG, "All application records cleared")
    }

    suspend fun getStorageUsage(): Pair<Int, Long> = withContext(Dispatchers.IO) {
        val count = dao.getPhotoCount()
        var totalBytes = 0L
        val dir = getPhotosDirectory()
        dir.listFiles()?.forEach {
            totalBytes += it.length()
        }
        Pair(count, totalBytes)
    }
}
