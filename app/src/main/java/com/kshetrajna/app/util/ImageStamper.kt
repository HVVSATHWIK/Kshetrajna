package com.kshetrajna.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.media.ExifInterface
import android.util.Log
import androidx.core.content.FileProvider
import com.kshetrajna.app.data.LocationProvenance
import com.kshetrajna.app.data.PhotoProvenance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

object ImageStamper {
    private const val TAG = "KshetrajnaWorkflow"

    suspend fun createStampedBitmap(
        originalFile: File,
        provenance: PhotoProvenance,
        primaryTimeText: String,
        secondaryTimeText: String? = null,
        locationText: String? = null,
        locationProvenance: LocationProvenance = LocationProvenance.NONE,
        noteText: String? = null
    ): Bitmap = withContext(Dispatchers.IO) {
        val originalBitmap = decodeFileWithOrientation(originalFile)
        val width = originalBitmap.width
        val height = originalBitmap.height

        val workingBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(workingBitmap)

        // Proportional scale factor
        val baseScale = max(width, height) / 1400f
        val bannerPadding = (22 * baseScale).coerceAtLeast(16f)
        val brandFontSize = (24 * baseScale).coerceAtLeast(16f)
        val taglineFontSize = (13 * baseScale).coerceAtLeast(10f)
        val detailFontSize = (17 * baseScale).coerceAtLeast(11f)

        // Assemble honest metadata lines
        val lines = mutableListOf<String>()
        val provenanceLabel = when (provenance) {
            PhotoProvenance.CAMERA_CAPTURE -> "SOURCE: Kshetrajna Camera Capture"
            PhotoProvenance.IMPORTED -> "SOURCE: Imported Photograph"
        }
        lines.add(provenanceLabel)
        lines.add(primaryTimeText)

        if (!secondaryTimeText.isNullOrBlank()) {
            lines.add(secondaryTimeText)
        }

        if (!locationText.isNullOrBlank()) {
            val locLabel = when (locationProvenance) {
                LocationProvenance.EXIF_METADATA_GPS -> "LOCATION (EXIF): $locationText"
                LocationProvenance.IMPORT_TIME_DEVICE_GPS -> "LOCATION (Import-time Device GPS): $locationText"
                LocationProvenance.CAMERA_CAPTURE_DEVICE_GPS -> "LOCATION: $locationText"
                LocationProvenance.NONE -> "LOCATION: $locationText"
            }
            lines.add(locLabel)
        } else {
            lines.add("LOCATION: Not recorded in photo metadata")
        }

        if (!noteText.isNullOrBlank()) {
            lines.add("NOTE: $noteText")
        }

        val lineHeight = detailFontSize * 1.42f
        val bannerHeight = (bannerPadding * 2) + brandFontSize + taglineFontSize + (lines.size * lineHeight) + (14 * baseScale)
        val bannerTop = height - bannerHeight

        // Draw translucent dark forest-green banner background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E80B2B20") // Rich deep forest green with ~91% opacity
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, bannerTop, width.toFloat(), height.toFloat(), bgPaint)

        // Draw gold accent top border line
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4A574") // Warm Gold
            strokeWidth = (2.5f * baseScale).coerceAtLeast(2f)
            style = Paint.Style.STROKE
        }
        canvas.drawLine(0f, bannerTop, width.toFloat(), bannerTop, borderPaint)

        // Draw Kshetrajna emblem mark in the banner
        val emblemRadius = (16 * baseScale).coerceAtLeast(12f)
        val emblemCenterX = bannerPadding + emblemRadius
        val emblemCenterY = bannerTop + bannerPadding + emblemRadius

        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4A574")
            style = Paint.Style.STROKE
            strokeWidth = (2f * baseScale).coerceAtLeast(1.5f)
        }
        // Arch outline
        val archRect = RectF(
            emblemCenterX - emblemRadius,
            emblemCenterY - emblemRadius,
            emblemCenterX + emblemRadius,
            emblemCenterY + emblemRadius
        )
        canvas.drawArc(archRect, 180f, 180f, false, goldPaint)

        // Sun / observer point
        val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4A574")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(emblemCenterX, emblemCenterY - (emblemRadius * 0.2f), emblemRadius * 0.28f, sunPaint)

        // Horizon curve
        val sagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8DAA91")
            style = Paint.Style.STROKE
            strokeWidth = (1.5f * baseScale).coerceAtLeast(1f)
        }
        canvas.drawLine(
            emblemCenterX - (emblemRadius * 0.8f),
            emblemCenterY + (emblemRadius * 0.35f),
            emblemCenterX + (emblemRadius * 0.8f),
            emblemCenterY + (emblemRadius * 0.35f),
            sagePaint
        )

        // Draw Wordmark "KSHETRAJNA" and Sanskrit "क्षेत्रज्ञ"
        val wordmarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8F5ED") // Warm Ivory
            textSize = brandFontSize
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            letterSpacing = 0.08f
        }
        val textStartX = emblemCenterX + emblemRadius + (16 * baseScale)
        var currentY = bannerTop + bannerPadding + brandFontSize * 0.85f
        canvas.drawText("KSHETRAJNA  |  क्षेत्रज्ञ", textStartX, currentY, wordmarkPaint)

        // Tagline "CAPTURE WHAT MATTERS."
        val taglinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4A574") // Warm gold
            textSize = taglineFontSize
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            letterSpacing = 0.12f
        }
        currentY += taglineFontSize * 1.5f
        canvas.drawText("CAPTURE WHAT MATTERS.", textStartX, currentY, taglinePaint)

        // Draw metadata details with high contrast against the dark background
        val detailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FAF8F3")
            textSize = detailFontSize
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        currentY += (12 * baseScale)
        for (line in lines) {
            currentY += lineHeight
            canvas.drawText(line, bannerPadding, currentY, detailPaint)
        }

        return@withContext workingBitmap
    }

    suspend fun saveBitmapToFile(bitmap: Bitmap, destinationFile: File, quality: Int = 92): Boolean =
        withContext(Dispatchers.IO) {
            try {
                destinationFile.parentFile?.mkdirs()
                FileOutputStream(destinationFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                    out.flush()
                }
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save bitmap: ${e.message}")
                false
            }
        }

    fun sharePhoto(context: Context, imageFile: File, title: String = "Share Kshetrajna Photograph") {
        try {
            Log.d(TAG, "Export started: file=${imageFile.name}, size=${imageFile.length()} bytes")
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, imageFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Log.d(TAG, "Export completed: share intent launched")
        } catch (e: Exception) {
            Log.e(TAG, "Export failed: ${e.message}")
        }
    }

    fun decodeFileWithOrientation(file: File, maxDimension: Int = 2560): Bitmap {
        if (!file.exists() || file.length() == 0L) {
            throw IllegalArgumentException("Cannot decode nonexistent or empty file: ${file.absolutePath}")
        }

        // Measure bounds first
        val boundsOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
        val origWidth = boundsOptions.outWidth
        val origHeight = boundsOptions.outHeight

        if (origWidth <= 0 || origHeight <= 0) {
            throw IllegalStateException("Image format not recognized or file corrupted at ${file.absolutePath}")
        }

        var sampleSize = 1
        while (origWidth / (sampleSize * 2) >= maxDimension || origHeight / (sampleSize * 2) >= maxDimension) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val bitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
            ?: throw IllegalStateException("Could not decode image at ${file.absolutePath}")

        return try {
            val exif = ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            val matrix = android.graphics.Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
                else -> return bitmap
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            bitmap
        }
    }
}
