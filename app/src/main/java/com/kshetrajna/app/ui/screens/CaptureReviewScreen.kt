package com.kshetrajna.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kshetrajna.app.R
import com.kshetrajna.app.data.LocationProvenance
import com.kshetrajna.app.data.PhotoProvenance
import com.kshetrajna.app.ui.AppScreen
import com.kshetrajna.app.ui.MainViewModel
import com.kshetrajna.app.ui.components.KshetrajnaInsignia
import com.kshetrajna.app.ui.theme.Charcoal
import com.kshetrajna.app.ui.theme.ForestGreen
import com.kshetrajna.app.ui.theme.ForestGreenDark
import com.kshetrajna.app.ui.theme.MutedSage
import com.kshetrajna.app.ui.theme.OutlineBorder
import com.kshetrajna.app.ui.theme.PureWhite
import com.kshetrajna.app.ui.theme.WarmGold
import com.kshetrajna.app.ui.theme.WarmIvory
import com.kshetrajna.app.ui.theme.WarmIvorySurface
import com.kshetrajna.app.util.CapturedLocation
import kotlinx.coroutines.launch

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import com.kshetrajna.app.ui.components.CoordinateRadarMap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CaptureReviewScreen(
    reviewState: AppScreen.Review,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var noteText by remember { mutableStateOf(reviewState.initialNote) }
    var includeStamp by remember { mutableStateOf(reviewState.isStamped) }

    var currentLocationOverride by remember { mutableStateOf<CapturedLocation?>(reviewState.location) }
    var currentLocationProvenance by remember { mutableStateOf(reviewState.locationProvenance) }
    var attachDeviceGpsToImport by remember { mutableStateOf(false) }
    var isCoordinatesExpanded by remember { mutableStateOf(false) }

    val isSaving by viewModel.isSaving.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    BackHandler(enabled = !isSaving) {
        viewModel.navigateTo(AppScreen.Camera)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(bottom = 90.dp)
        ) {
            // Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ForestGreen,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { if (!isSaving) viewModel.navigateTo(AppScreen.Camera) },
                        enabled = !isSaving,
                        modifier = Modifier.testTag("review_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.desc_close),
                            tint = WarmIvory
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KshetrajnaInsignia(size = 22.dp, horizonColor = WarmIvory)
                        Text(
                            text = if (reviewState.provenance == PhotoProvenance.CAMERA_CAPTURE) "Review Field Capture" else "Review Imported Photo",
                            color = WarmIvory,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            // Image Preview Card (Preserving genuine aspect ratio)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(1.dp, OutlineBorder, RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = reviewState.photoFile,
                    contentDescription = "Captured Photograph",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Live Stamp Preview Overlay
                if (includeStamp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color(0xE80B2B20))
                            .border(width = 1.dp, color = WarmGold.copy(alpha = 0.5f), shape = RoundedCornerShape(0.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                KshetrajnaInsignia(size = 14.dp, horizonColor = WarmIvory)
                                Text(
                                    text = "KSHETRAJNA  |  क्षेत्रज्ञ",
                                    color = WarmIvory,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "• CAPTURE WHAT MATTERS.",
                                    color = WarmGold,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "SOURCE: " + if (reviewState.provenance == PhotoProvenance.CAMERA_CAPTURE) "Kshetrajna Camera Capture" else "Imported Photograph",
                                color = WarmGold,
                                fontSize = 8.5.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            Text(
                                text = "TIME: ${reviewState.primaryTimeText}",
                                color = WarmIvory.copy(alpha = 0.9f),
                                fontSize = 8.5.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            if (reviewState.secondaryTimeText != null) {
                                Text(
                                    text = reviewState.secondaryTimeText,
                                    color = WarmIvory.copy(alpha = 0.85f),
                                    fontSize = 8.5.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                            if (currentLocationOverride != null) {
                                val locTag = when (currentLocationProvenance) {
                                    LocationProvenance.EXIF_METADATA_GPS -> "LOC (EXIF): "
                                    LocationProvenance.IMPORT_TIME_DEVICE_GPS -> "LOC (Import-time GPS): "
                                    else -> "LOC: "
                                }
                                Text(
                                    text = "$locTag${currentLocationOverride!!.formattedCoordinates} (±${currentLocationOverride!!.accuracyMeters.toInt()}m)",
                                    color = WarmIvory.copy(alpha = 0.9f),
                                    fontSize = 8.5.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            } else {
                                Text(
                                    text = "LOC: Not recorded in photo metadata",
                                    color = WarmIvory.copy(alpha = 0.7f),
                                    fontSize = 8.5.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                            if (noteText.isNotBlank()) {
                                Text(
                                    text = "NOTE: $noteText",
                                    color = WarmGold,
                                    fontSize = 8.5.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Location & Topological Radar Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "GEODETIC POSITION & MAP",
                                color = MutedSage,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        if (currentLocationOverride != null) {
                            Text(
                                text = "±${currentLocationOverride!!.accuracyMeters.toInt()}m",
                                color = ForestGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (currentLocationOverride != null) {
                        val loc = currentLocationOverride!!

                        // Canvas Coordinate Radar Map
                        CoordinateRadarMap(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            accuracyMeters = loc.accuracyMeters,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Resolved Address
                        Text(
                            text = loc.addressSummary ?: "Address unavailable (offline satellite fix active)",
                            color = Charcoal,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "${loc.formattedCoordinates} · Reported accuracy ±${loc.accuracyMeters.toInt()}m",
                            color = Charcoal.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )

                        val provenanceDescription = when (currentLocationProvenance) {
                            LocationProvenance.EXIF_METADATA_GPS -> "Genuine GPS extracted from original EXIF headers"
                            LocationProvenance.IMPORT_TIME_DEVICE_GPS -> "Current device position recorded at import time"
                            LocationProvenance.CAMERA_CAPTURE_DEVICE_GPS -> "Captured via hardware GPS at shutter release"
                            LocationProvenance.NONE -> "No verified coordinates"
                        }
                        Text(
                            text = provenanceDescription,
                            color = MutedSage,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Expandable coordinates toggle button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { isCoordinatesExpanded = !isCoordinatesExpanded }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isCoordinatesExpanded) "Hide Geodetic Details" else "Show Full Coordinates (DMS & Decimal)",
                                color = ForestGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = if (isCoordinatesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (isCoordinatesExpanded) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                color = WarmIvory,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder.copy(alpha = 0.6f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "DEGREES, MINUTES, SECONDS (DMS)",
                                        color = MutedSage,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = loc.dmsCoordinates,
                                        color = Charcoal,
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "DECIMAL DEGREES (DD)",
                                        color = MutedSage,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = loc.decimalCoordinates,
                                        color = Charcoal,
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )

                                    if (loc.altitudeMeters != null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "ALTITUDE: ${loc.altitudeMeters.toInt()} meters above sea level",
                                            color = Charcoal.copy(alpha = 0.8f),
                                            fontSize = 10.sp
                                        )
                                    }

                                    val fixDate = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(Date(loc.fixTimestamp))
                                    Text(
                                        text = "FIX TIMESTAMP: $fixDate",
                                        color = Charcoal.copy(alpha = 0.65f),
                                        fontSize = 9.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    } else {
                        // Location unavailable
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Charcoal.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Location not attached",
                                    color = Charcoal,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (reviewState.provenance == PhotoProvenance.IMPORTED) "Photo contains no embedded GPS metadata" else "GPS was disabled or unavailable during capture",
                                    color = Charcoal.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // For imported photo without EXIF GPS: option to attach device location
                        if (reviewState.provenance == PhotoProvenance.IMPORTED && reviewState.location == null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = attachDeviceGpsToImport,
                                    onCheckedChange = { checked ->
                                        attachDeviceGpsToImport = checked
                                        if (checked) {
                                            scope.launch {
                                                val loc = viewModel.attachCurrentDeviceLocationToImport()
                                                currentLocationOverride = loc
                                                currentLocationProvenance = LocationProvenance.IMPORT_TIME_DEVICE_GPS
                                            }
                                        } else {
                                            currentLocationOverride = null
                                            currentLocationProvenance = LocationProvenance.NONE
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = ForestGreen)
                                )
                                Text(
                                    text = "Attach current device location (marked as import-time fix)",
                                    color = Charcoal,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Provenance & Time Inspection Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (reviewState.provenance == PhotoProvenance.CAMERA_CAPTURE) Icons.Default.PhotoCamera else Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (reviewState.provenance == PhotoProvenance.CAMERA_CAPTURE) "CAMERA CAPTURE PROVENANCE" else "IMPORTED PHOTO PROVENANCE",
                            color = MutedSage,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary Timestamp
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = reviewState.primaryTimeText,
                                color = Charcoal,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (reviewState.provenance == PhotoProvenance.CAMERA_CAPTURE) "Device system clock at capture" else "Time added to Kshetrajna",
                                color = Charcoal.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (reviewState.secondaryTimeText != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = WarmGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = reviewState.secondaryTimeText,
                                    color = Charcoal,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Read from embedded EXIF headers",
                                    color = Charcoal.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stamping Toggle Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Visible Information Stamp",
                            color = Charcoal,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Generates a separate stamped export with honest source, time, and coordinates banner. The unstamped original is always preserved.",
                            color = Charcoal.copy(alpha = 0.65f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Switch(
                        checked = includeStamp,
                        onCheckedChange = { includeStamp = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WarmGold,
                            checkedTrackColor = ForestGreen,
                            uncheckedThumbColor = WarmIvory,
                            uncheckedTrackColor = OutlineBorder
                        ),
                        modifier = Modifier.testTag("stamp_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Field Note Editor Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Field Observation Note",
                            color = Charcoal,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = {
                            Text(
                                "Add observation, field site details, or inspection note...",
                                color = Charcoal.copy(alpha = 0.4f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("field_note_input"),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ForestGreen,
                            unfocusedBorderColor = OutlineBorder,
                            focusedContainerColor = WarmIvory,
                            unfocusedContainerColor = WarmIvory
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // Bottom Action Bar with Duplicate Save Protection
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = WarmIvorySurface,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { if (!isSaving) viewModel.navigateTo(AppScreen.Camera) },
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("retake_button"),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreen)
                ) {
                    Text(
                        if (reviewState.provenance == PhotoProvenance.CAMERA_CAPTURE) "Retake" else "Cancel",
                        color = ForestGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        if (!isSaving) {
                            viewModel.saveReviewPhoto(
                                photoFile = reviewState.photoFile,
                                provenance = reviewState.provenance,
                                primaryTimeText = reviewState.primaryTimeText,
                                secondaryTimeText = reviewState.secondaryTimeText,
                                rawTimestamp = reviewState.rawTimestamp,
                                location = currentLocationOverride,
                                locationProvenance = currentLocationProvenance,
                                note = noteText,
                                createStamp = includeStamp
                            )
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp)
                        .testTag("save_photo_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = PureWhite,
                        disabledContainerColor = ForestGreen.copy(alpha = 0.6f),
                        disabledContentColor = PureWhite.copy(alpha = 0.7f)
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = WarmGold,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            "Saving Record...",
                            fontWeight = FontWeight.SemiBold,
                            color = WarmIvory
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = WarmGold
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            "Save to Journal",
                            fontWeight = FontWeight.SemiBold,
                            color = WarmIvory
                        )
                    }
                }
            }
        }
    }
}

