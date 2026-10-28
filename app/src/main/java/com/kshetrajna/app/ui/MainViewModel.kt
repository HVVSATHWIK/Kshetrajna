package com.kshetrajna.app.ui

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kshetrajna.app.data.LocationProvenance
import com.kshetrajna.app.data.PhotoProvenance
import com.kshetrajna.app.data.PhotoRecord
import com.kshetrajna.app.data.PhotoRepository
import com.kshetrajna.app.util.CapturedLocation
import com.kshetrajna.app.util.ExifMetadataHelper
import com.kshetrajna.app.util.LocationHelper
import com.kshetrajna.app.util.LocationStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface AppScreen {
    object Camera : AppScreen
    data class Review(
        val photoFile: File,
        val provenance: PhotoProvenance,
        val primaryTimeText: String,
        val secondaryTimeText: String?,
        val rawTimestamp: Long,
        val location: CapturedLocation?,
        val locationProvenance: LocationProvenance,
        val initialNote: String = "",
        val isStamped: Boolean = true
    ) : AppScreen
    object History : AppScreen
    data class Detail(val record: PhotoRecord) : AppScreen
    object Settings : AppScreen
}

data class StorageStats(
    val photoCount: Int = 0,
    val totalBytes: Long = 0L
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "KshetrajnaWorkflow"
    }

    private val repository = PhotoRepository(application)
    val locationHelper = LocationHelper(application)

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Camera)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _recordLocationEnabled = MutableStateFlow(true)
    val recordLocationEnabled: StateFlow<Boolean> = _recordLocationEnabled.asStateFlow()

    private val _defaultStampEnabled = MutableStateFlow(true)
    val defaultStampEnabled: StateFlow<Boolean> = _defaultStampEnabled.asStateFlow()

    private val _showGrid = MutableStateFlow(false)
    val showGrid: StateFlow<Boolean> = _showGrid.asStateFlow()

    private val _currentLocation = MutableStateFlow<CapturedLocation?>(null)
    val currentLocation: StateFlow<CapturedLocation?> = _currentLocation.asStateFlow()

    private val _isAcquiringLocation = MutableStateFlow(false)
    val isAcquiringLocation: StateFlow<Boolean> = _isAcquiringLocation.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _storageStats = MutableStateFlow(StorageStats())
    val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val locationStatus: StateFlow<LocationStatus> = combine(
        _recordLocationEnabled,
        _isAcquiringLocation,
        _currentLocation
    ) { enabled, acquiring, loc ->
        locationHelper.determineStatus(enabled, acquiring, loc)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocationStatus.Acquiring)

    private var _hasShownStartupSplash = false
    val hasShownStartupSplash: Boolean get() = _hasShownStartupSplash

    fun markStartupSplashCompleted() {
        _hasShownStartupSplash = true
    }

    val photoHistory: StateFlow<List<PhotoRecord>> = repository.allPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshStorageStats()
        fetchCurrentLocation()
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun toggleLocationRecording() {
        val next = !_recordLocationEnabled.value
        _recordLocationEnabled.value = next
        if (next) {
            fetchCurrentLocation()
        }
    }

    fun setLocationRecording(enabled: Boolean) {
        _recordLocationEnabled.value = enabled
        if (enabled) {
            fetchCurrentLocation()
        }
    }

    fun setDefaultStamp(enabled: Boolean) {
        _defaultStampEnabled.value = enabled
    }

    fun toggleGrid() {
        _showGrid.value = !_showGrid.value
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun fetchCurrentLocation() {
        if (!_recordLocationEnabled.value) return
        viewModelScope.launch {
            _isAcquiringLocation.value = true
            val loc = locationHelper.getCurrentLocation(timeoutMs = 3500L)
            if (loc != null) {
                _currentLocation.value = loc
            }
            _isAcquiringLocation.value = false
        }
    }

    fun onPhotoCaptured(file: File) {
        if (_isCapturing.value) return
        _isCapturing.value = true

        Log.d(TAG, "Camera capture succeeded: file=${file.name}, size=${file.length()} bytes")

        if (!file.exists() || file.length() == 0L) {
            Log.e(TAG, "Camera capture failed: file is missing or empty")
            _isCapturing.value = false
            _userMessage.value = "Camera capture produced an empty file"
            return
        }

        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
            val formattedTime = dateFormatter.format(Date(timestamp))

            // Obtain location without blocking shutter: use fresh cached fix or quick non-blocking poll
            val location = if (_recordLocationEnabled.value) {
                val cached = _currentLocation.value
                val isCachedFresh = cached != null && (timestamp - cached.fixTimestamp < LocationHelper.STALE_THRESHOLD_MS)
                if (isCachedFresh) {
                    cached
                } else {
                    // Try quick poll with 1.5s timeout, falling back immediately to cached fix if present
                    val fresh = locationHelper.getCurrentLocation(timeoutMs = 1500L)
                    fresh ?: cached
                }
            } else {
                null
            }

            val locProvenance = if (location != null) {
                LocationProvenance.CAMERA_CAPTURE_DEVICE_GPS
            } else {
                LocationProvenance.NONE
            }

            _isCapturing.value = false
            Log.d(TAG, "Review opened: provenance=CAMERA_CAPTURE, hasLocation=${location != null}")

            _currentScreen.value = AppScreen.Review(
                photoFile = file,
                provenance = PhotoProvenance.CAMERA_CAPTURE,
                primaryTimeText = formattedTime,
                secondaryTimeText = null,
                rawTimestamp = timestamp,
                location = location,
                locationProvenance = locProvenance,
                isStamped = _defaultStampEnabled.value
            )
        }
    }

    fun onPhotoSelected(uri: Uri) {
        if (_isCapturing.value) return
        _isCapturing.value = true

        Log.d(TAG, "Import selected: uri=$uri")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val stageDir = File(context.filesDir, "staged_imports")
                stageDir.mkdirs()
                val tempFile = File(stageDir, "imp_${System.currentTimeMillis()}.jpg")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                if (!tempFile.exists() || tempFile.length() == 0L) {
                    throw IllegalStateException("Failed to read image stream or file is empty")
                }

                // Verify decodability and dimensions without allocating full bitmap in memory
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(tempFile.absolutePath, bounds)

                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                    tempFile.delete()
                    throw IllegalStateException("Selected file is not a supported image format")
                }

                Log.d(
                    TAG,
                    "Image decoded: bounds=${bounds.outWidth}x${bounds.outHeight}, mimeType=${bounds.outMimeType}"
                )

                // Extract authentic EXIF metadata
                val exifData = ExifMetadataHelper.extractExifData(tempFile)

                val importTimestamp = System.currentTimeMillis()
                val formattedImportTime = "Imported: " + SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date(importTimestamp))

                val secondaryTimeText = exifData.formattedCaptureTime?.let {
                    "Original EXIF Capture: $it"
                } ?: "Original capture time: Unavailable in metadata"

                val location = if (exifData.latitude != null && exifData.longitude != null && exifData.formattedCoordinates != null) {
                    CapturedLocation(
                        latitude = exifData.latitude,
                        longitude = exifData.longitude,
                        accuracyMeters = 0f,
                        formattedCoordinates = exifData.formattedCoordinates,
                        addressSummary = null
                    )
                } else {
                    null
                }

                val locProvenance = if (location != null) {
                    LocationProvenance.EXIF_METADATA_GPS
                } else {
                    LocationProvenance.NONE
                }

                withContext(Dispatchers.Main) {
                    _isCapturing.value = false
                    Log.d(
                        TAG,
                        "Review opened: provenance=IMPORTED, hasExifDate=${exifData.formattedCaptureTime != null}, hasExifGps=${location != null}"
                    )
                    _currentScreen.value = AppScreen.Review(
                        photoFile = tempFile,
                        provenance = PhotoProvenance.IMPORTED,
                        primaryTimeText = formattedImportTime,
                        secondaryTimeText = secondaryTimeText,
                        rawTimestamp = exifData.captureTimestamp ?: importTimestamp,
                        location = location,
                        locationProvenance = locProvenance,
                        isStamped = _defaultStampEnabled.value
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Import failed: ${e.message}")
                withContext(Dispatchers.Main) {
                    _isCapturing.value = false
                    _userMessage.value = "Failed to load selected photo: ${e.localizedMessage ?: "unsupported format"}"
                }
            }
        }
    }

    suspend fun attachCurrentDeviceLocationToImport(): CapturedLocation? = withContext(Dispatchers.IO) {
        val loc = locationHelper.getCurrentLocation() ?: _currentLocation.value
        loc
    }

    fun saveReviewPhoto(
        photoFile: File,
        provenance: PhotoProvenance,
        primaryTimeText: String,
        secondaryTimeText: String?,
        rawTimestamp: Long,
        location: CapturedLocation?,
        locationProvenance: LocationProvenance,
        note: String,
        createStamp: Boolean
    ) {
        if (_isSaving.value) {
            Log.w(TAG, "Save ignored: duplicate save request in progress")
            return
        }
        _isSaving.value = true

        viewModelScope.launch {
            try {
                val record = repository.savePhotoRecord(
                    tempFile = photoFile,
                    provenance = provenance,
                    primaryTimeText = primaryTimeText,
                    secondaryTimeText = secondaryTimeText,
                    rawTimestamp = rawTimestamp,
                    location = location,
                    locationProvenance = locationProvenance,
                    note = note,
                    createStamp = createStamp
                )
                refreshStorageStats()
                _userMessage.value = "Record saved to Field Journal"
                _currentScreen.value = AppScreen.Detail(record)
            } catch (e: Exception) {
                Log.e(TAG, "Save failed: ${e.message}")
                _userMessage.value = "Save failed: ${e.localizedMessage ?: "Storage error"}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun updateRecordNote(id: Long, note: String) {
        viewModelScope.launch {
            try {
                repository.updateNote(id, note)
                val current = _currentScreen.value
                if (current is AppScreen.Detail && current.record.id == id) {
                    _currentScreen.value = AppScreen.Detail(current.record.copy(note = note))
                }
                _userMessage.value = "Note updated"
            } catch (e: Exception) {
                _userMessage.value = "Failed to update note: ${e.localizedMessage}"
            }
        }
    }

    fun deleteRecord(record: PhotoRecord) {
        viewModelScope.launch {
            try {
                repository.deletePhoto(record)
                refreshStorageStats()
                _userMessage.value = "Photo removed from Field Journal"
                _currentScreen.value = AppScreen.History
            } catch (e: Exception) {
                _userMessage.value = "Failed to delete record: ${e.localizedMessage}"
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                repository.deleteAllPhotos()
                refreshStorageStats()
                _userMessage.value = "All application records cleared"
                _currentScreen.value = AppScreen.Camera
            } catch (e: Exception) {
                _userMessage.value = "Failed to clear records: ${e.localizedMessage}"
            }
        }
    }

    fun refreshStorageStats() {
        viewModelScope.launch {
            val (count, bytes) = repository.getStorageUsage()
            _storageStats.value = StorageStats(photoCount = count, totalBytes = bytes)
        }
    }
}
