package com.kshetrajna.app

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.kshetrajna.app.data.KshetrajnaDatabase
import com.kshetrajna.app.data.LocationProvenance
import com.kshetrajna.app.data.PhotoProvenance
import com.kshetrajna.app.data.PhotoRecord
import com.kshetrajna.app.data.PhotoRepository
import com.kshetrajna.app.util.CapturedLocation
import com.kshetrajna.app.util.ExifMetadataHelper
import com.kshetrajna.app.util.ImageStamper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KshetrajnaWorkflowRobolectricTest {

    private lateinit var context: Context
    private lateinit var repository: PhotoRepository
    private lateinit var testDir: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        repository = PhotoRepository(context)
        testDir = File(context.cacheDir, "test_photos").apply { mkdirs() }
    }

    @After
    fun tearDown() {
        runBlocking {
            repository.deleteAllPhotos()
            testDir.deleteRecursively()
        }
    }

    private fun createTestBitmapFile(name: String, width: Int = 200, height: Int = 150): File {
        val file = File(testDir, name)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
        }
        return file
    }

    @Test
    fun testExifExtractionOnPhotoWithoutExifDoesNotCrash() {
        val testFile = createTestBitmapFile("no_exif.jpg")
        val exifData = ExifMetadataHelper.extractExifData(testFile)

        assertNull(exifData.captureTimestamp)
        assertNull(exifData.formattedCaptureTime)
        assertNull(exifData.latitude)
        assertNull(exifData.longitude)
        assertNull(exifData.formattedCoordinates)
    }

    @Test
    fun testExifExtractionOnNonexistentFileHandlesGracefully() {
        val missingFile = File(testDir, "missing.jpg")
        val exifData = ExifMetadataHelper.extractExifData(missingFile)

        assertNull(exifData.captureTimestamp)
        assertNull(exifData.latitude)
    }

    @Test
    fun testSaveAndRetrieveCameraCaptureRecord() = runBlocking {
        val testFile = createTestBitmapFile("cam_test.jpg")
        val location = CapturedLocation(
            latitude = 12.9716,
            longitude = 77.5946,
            accuracyMeters = 5.0f,
            formattedCoordinates = "12.97160° N, 77.59460° E",
            addressSummary = "Bengaluru, Karnataka"
        )

        val saved = repository.savePhotoRecord(
            tempFile = testFile,
            provenance = PhotoProvenance.CAMERA_CAPTURE,
            primaryTimeText = "28 Oct 2026, 10:15:30",
            secondaryTimeText = null,
            rawTimestamp = 1793268930000L,
            location = location,
            locationProvenance = LocationProvenance.CAMERA_CAPTURE_DEVICE_GPS,
            note = "Site inspection observation",
            createStamp = true
        )

        assertTrue(saved.id > 0)
        assertEquals("CAMERA_CAPTURE", saved.provenance)
        assertEquals("CAMERA_CAPTURE_DEVICE_GPS", saved.locationProvenance)
        assertEquals("Site inspection observation", saved.note)
        assertTrue(saved.isStamped)
        assertNotNull(saved.stampedFilePath)
        assertTrue(File(saved.originalFilePath).exists())
        assertTrue(File(saved.stampedFilePath!!).exists())

        // Verify retrieval via flow
        val all = repository.allPhotos.first()
        assertEquals(1, all.size)
        assertEquals(saved.id, all[0].id)
        assertEquals("CAMERA_CAPTURE", all[0].provenance)
    }

    @Test
    fun testSaveImportedPhotoPreservesHonestMetadataDistinction() = runBlocking {
        val testFile = createTestBitmapFile("import_test.jpg")
        val originalExifTime = "14 Jun 2024, 18:22:15"
        val importTime = "Imported: 28 Oct 2026, 17:16:00"

        val saved = repository.savePhotoRecord(
            tempFile = testFile,
            provenance = PhotoProvenance.IMPORTED,
            primaryTimeText = importTime,
            secondaryTimeText = "Original EXIF: $originalExifTime",
            rawTimestamp = 1718389335000L,
            location = null,
            locationProvenance = LocationProvenance.NONE,
            note = "Field archive photo from previous survey",
            createStamp = true
        )

        assertEquals("IMPORTED", saved.provenance)
        assertEquals(importTime, saved.formattedDateTime)
        assertEquals("Original EXIF: $originalExifTime", saved.originalCaptureTime)
        assertEquals(LocationProvenance.NONE.name, saved.locationProvenance)
        assertFalse(saved.hasLocation)
        assertNull(saved.latitude)

        val all = repository.allPhotos.first()
        assertEquals(1, all.size)
        assertEquals("IMPORTED", all[0].provenance)
        assertEquals("Original EXIF: $originalExifTime", all[0].originalCaptureTime)
    }

    @Test
    fun testNoteEditingPersistsInDatabase() = runBlocking {
        val testFile = createTestBitmapFile("note_test.jpg")
        val saved = repository.savePhotoRecord(
            tempFile = testFile,
            provenance = PhotoProvenance.CAMERA_CAPTURE,
            primaryTimeText = "28 Oct 2026, 11:00:00",
            rawTimestamp = System.currentTimeMillis(),
            note = "Initial note",
            createStamp = false
        )

        assertEquals("Initial note", saved.note)

        repository.updateNote(saved.id, "Updated verified observation note")

        val all = repository.allPhotos.first()
        assertEquals("Updated verified observation note", all[0].note)
    }

    @Test
    fun testDeletePhotoRemovesFilesAndDatabaseRecord() = runBlocking {
        val testFile = createTestBitmapFile("delete_test.jpg")
        val saved = repository.savePhotoRecord(
            tempFile = testFile,
            provenance = PhotoProvenance.CAMERA_CAPTURE,
            primaryTimeText = "28 Oct 2026, 12:00:00",
            rawTimestamp = System.currentTimeMillis(),
            createStamp = true
        )

        val origFile = File(saved.originalFilePath)
        val stampedFile = File(saved.stampedFilePath!!)
        assertTrue(origFile.exists())
        assertTrue(stampedFile.exists())

        repository.deletePhoto(saved)

        assertFalse(origFile.exists())
        assertFalse(stampedFile.exists())
        val all = repository.allPhotos.first()
        assertTrue(all.isEmpty())
    }

    @Test
    fun testStampedImageGenerationDoesNotDistortDimensions() = runBlocking {
        val testFile = createTestBitmapFile("stamp_dim_test.jpg", width = 800, height = 600)

        val stampedBitmap = ImageStamper.createStampedBitmap(
            originalFile = testFile,
            provenance = PhotoProvenance.CAMERA_CAPTURE,
            primaryTimeText = "28 Oct 2026, 14:00:00",
            secondaryTimeText = null,
            locationText = "12.97160° N, 77.59460° E",
            locationProvenance = LocationProvenance.CAMERA_CAPTURE_DEVICE_GPS,
            noteText = "Inspection note"
        )

        assertNotNull(stampedBitmap)
        assertEquals(800, stampedBitmap.width)
        assertEquals(600, stampedBitmap.height)
    }

    @Test(expected = IllegalStateException::class)
    fun testEmptyFileSaveFailsAndDoesNotCreateRecord(): Unit = runBlocking {
        val emptyFile = File(testDir, "empty.jpg").apply { createNewFile() }
        repository.savePhotoRecord(
            tempFile = emptyFile,
            provenance = PhotoProvenance.CAMERA_CAPTURE,
            primaryTimeText = "28 Oct 2026, 15:00:00"
        )
    }

    @Test
    fun testStartupSplashShownStateTracking() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.kshetrajna.app.ui.MainViewModel(app)
        assertFalse("Initial state must not have shown splash", viewModel.hasShownStartupSplash)

        viewModel.markStartupSplashCompleted()
        assertTrue("State must reflect that splash was shown", viewModel.hasShownStartupSplash)
    }

    @Test
    fun testDmsConversionCalculatesCorrectCardinalAngles() {
        // Test Northern & Eastern hemisphere (e.g. Bengaluru, India)
        val dmsNorthEast = CapturedLocation.toDms(12.9716, 77.5946)
        assertTrue("Must contain N and E markers", dmsNorthEast.contains("N") && dmsNorthEast.contains("E"))
        assertTrue("Must contain degree markers", dmsNorthEast.contains("12°") && dmsNorthEast.contains("77°"))

        // Test Southern & Western hemisphere (e.g. Rio de Janeiro, Brazil)
        val dmsSouthWest = CapturedLocation.toDms(-22.9068, -43.1729)
        assertTrue("Must contain S and W markers", dmsSouthWest.contains("S") && dmsSouthWest.contains("W"))
        assertTrue("Must contain degree markers", dmsSouthWest.contains("22°") && dmsSouthWest.contains("43°"))
    }

    @Test
    fun testLocationStatusEvaluationThresholds() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val locationHelper = com.kshetrajna.app.util.LocationHelper(app)

        // Disabled
        val disabled = locationHelper.determineStatus(
            recordLocationEnabled = false,
            isAcquiring = false,
            location = null
        )
        assertTrue(disabled is com.kshetrajna.app.util.LocationStatus.Disabled)

        // High accuracy fix (e.g. 5m) -> Ready
        val highAccuracyLoc = CapturedLocation(
            latitude = 12.9716,
            longitude = 77.5946,
            accuracyMeters = 5.0f,
            formattedCoordinates = "12.97160° N, 77.59460° E"
        )
        val ready = locationHelper.determineStatus(
            recordLocationEnabled = true,
            isAcquiring = false,
            location = highAccuracyLoc
        )
        assertTrue(ready is com.kshetrajna.app.util.LocationStatus.Ready)

        // Poor accuracy fix (e.g. 120m) -> PoorAccuracy
        val poorAccuracyLoc = CapturedLocation(
            latitude = 12.9716,
            longitude = 77.5946,
            accuracyMeters = 120.0f,
            formattedCoordinates = "12.97160° N, 77.59460° E"
        )
        val poor = locationHelper.determineStatus(
            recordLocationEnabled = true,
            isAcquiring = false,
            location = poorAccuracyLoc
        )
        assertTrue(poor is com.kshetrajna.app.util.LocationStatus.PoorAccuracy)

        // Permission denied (no location, no permission granted in test env)
        val denied = locationHelper.determineStatus(
            recordLocationEnabled = true,
            isAcquiring = false,
            location = null
        )
        assertTrue(denied is com.kshetrajna.app.util.LocationStatus.PermissionDenied)
    }

    @Test
    fun testOriginalAndStampedFilesArePersistedSeparately() = runBlocking {
        val testFile = createTestBitmapFile("separate_test.jpg", width = 400, height = 300)
        val saved = repository.savePhotoRecord(
            tempFile = testFile,
            provenance = PhotoProvenance.CAMERA_CAPTURE,
            primaryTimeText = "28 Oct 2026, 16:30:00",
            createStamp = true
        )

        assertNotNull(saved.originalFilePath)
        assertNotNull(saved.stampedFilePath)
        assertTrue("Paths must be distinct files", saved.originalFilePath != saved.stampedFilePath)

        val origFile = File(saved.originalFilePath)
        val stampedFile = File(saved.stampedFilePath!!)
        assertTrue(origFile.exists() && origFile.length() > 0)
        assertTrue(stampedFile.exists() && stampedFile.length() > 0)
    }
}
