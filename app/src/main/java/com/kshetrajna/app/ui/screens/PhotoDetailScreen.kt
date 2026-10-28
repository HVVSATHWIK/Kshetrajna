package com.kshetrajna.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kshetrajna.app.R
import com.kshetrajna.app.data.LocationProvenance
import com.kshetrajna.app.data.PhotoProvenance
import com.kshetrajna.app.data.PhotoRecord
import com.kshetrajna.app.ui.AppScreen
import com.kshetrajna.app.ui.MainViewModel
import com.kshetrajna.app.ui.theme.Charcoal
import com.kshetrajna.app.ui.theme.ForestGreen
import com.kshetrajna.app.ui.theme.ForestGreenDark
import com.kshetrajna.app.ui.theme.MutedSage
import com.kshetrajna.app.ui.theme.OutlineBorder
import com.kshetrajna.app.ui.theme.PureWhite
import com.kshetrajna.app.ui.theme.WarmGold
import com.kshetrajna.app.ui.theme.WarmIvory
import com.kshetrajna.app.ui.theme.WarmIvorySurface
import com.kshetrajna.app.util.ImageStamper
import java.io.File

@Composable
fun PhotoDetailScreen(
    record: PhotoRecord,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditingNote by remember { mutableStateOf(false) }
    var editedNote by remember { mutableStateOf(record.note) }
    var selectedTab by remember { mutableIntStateOf(if (record.stampedFilePath != null) 0 else 1) }

    val hasStamped = record.stampedFilePath != null
    val currentImageFile = if (selectedTab == 0 && hasStamped) {
        File(record.stampedFilePath!!)
    } else {
        File(record.originalFilePath)
    }

    BackHandler {
        viewModel.navigateTo(AppScreen.History)
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
                .padding(bottom = 80.dp)
        ) {
            // Header
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
                        onClick = { viewModel.navigateTo(AppScreen.History) },
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back),
                            tint = WarmIvory
                        )
                    }

                    Text(
                        text = "Photo Details",
                        color = WarmIvory,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row {
                        IconButton(
                            onClick = {
                                ImageStamper.sharePhoto(
                                    context = context,
                                    imageFile = currentImageFile,
                                    title = "Export Kshetrajna Photo"
                                )
                            },
                            modifier = Modifier.testTag("detail_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.desc_share),
                                tint = WarmGold
                            )
                        }

                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("detail_delete_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = stringResource(R.string.desc_delete),
                                tint = WarmIvory.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Tab bar (Stamped vs Original)
            if (hasStamped) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = ForestGreenDark,
                    contentColor = WarmIvory,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = WarmGold
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Stamped Version",
                                color = if (selectedTab == 0) WarmGold else WarmIvory.copy(alpha = 0.7f),
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Original Photo",
                                color = if (selectedTab == 1) WarmGold else WarmIvory.copy(alpha = 0.7f),
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Main Photo Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .border(1.dp, OutlineBorder, RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = currentImageFile,
                    contentDescription = "Photograph detail",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f),
                    contentScale = ContentScale.Fit
                )
            }

            // Metadata Inspection Card
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
                            imageVector = if (record.provenance == "IMPORTED") Icons.Default.PhotoLibrary else Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (record.provenance == "IMPORTED") "IMPORTED PHOTOGRAPH" else "KSHETRAJNA CAMERA CAPTURE",
                            color = MutedSage,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date & Time
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
                                text = record.formattedDateTime,
                                color = Charcoal,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (record.provenance == "IMPORTED") "Imported date" else "Capture timestamp (Device Clock)",
                                color = Charcoal.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Original EXIF time if available
                    if (record.originalCaptureTime != null) {
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
                                    text = record.originalCaptureTime,
                                    color = Charcoal,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Embedded EXIF metadata",
                                    color = Charcoal.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = OutlineBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Location & Geodetic Telemetry
                    var isDetailCoordinatesExpanded by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (record.hasLocation) ForestGreen else Charcoal.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "GEODETIC POSITION",
                                color = MutedSage,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        if (record.hasLocation && record.accuracyMeters != null) {
                            Text(
                                text = "±${record.accuracyMeters.toInt()}m",
                                color = ForestGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (record.hasLocation && record.latitude != null && record.longitude != null) {
                        // Canvas Coordinate Radar Map
                        com.kshetrajna.app.ui.components.CoordinateRadarMap(
                            latitude = record.latitude,
                            longitude = record.longitude,
                            accuracyMeters = record.accuracyMeters ?: 10f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Resolved Address
                        Text(
                            text = record.addressText ?: "Address unavailable (offline satellite fix active)",
                            color = Charcoal,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "${record.locationSummary} · Reported accuracy ±${record.accuracyMeters?.toInt() ?: 0}m",
                            color = Charcoal.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )

                        val locDesc = when (record.locationProvenance) {
                            LocationProvenance.EXIF_METADATA_GPS.name -> "Genuine GPS extracted from original EXIF headers"
                            LocationProvenance.IMPORT_TIME_DEVICE_GPS.name -> "Device GPS position recorded at import time"
                            LocationProvenance.CAMERA_CAPTURE_DEVICE_GPS.name -> "Captured via hardware GPS at shutter release"
                            else -> "Recorded location coordinates"
                        }
                        Text(
                            text = locDesc,
                            color = MutedSage,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Expandable coordinates button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { isDetailCoordinatesExpanded = !isDetailCoordinatesExpanded }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isDetailCoordinatesExpanded) "Hide Geodetic Details" else "Show Full Coordinates (DMS & Decimal)",
                                color = ForestGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = if (isDetailCoordinatesExpanded) androidx.compose.material.icons.Icons.Default.ExpandLess else androidx.compose.material.icons.Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (isDetailCoordinatesExpanded) {
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
                                        text = com.kshetrajna.app.util.CapturedLocation.toDms(record.latitude, record.longitude),
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
                                        text = String.format(java.util.Locale.US, "%.6f, %.6f", record.latitude, record.longitude),
                                        color = Charcoal,
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )

                                    Text(
                                        text = "RECORDED TIMESTAMP: ${record.formattedDateTime}",
                                        color = Charcoal.copy(alpha = 0.65f),
                                        fontSize = 9.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    } else {
                        Column {
                            Text(
                                text = "Location not recorded",
                                color = Charcoal,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "No location coordinates associated with this record",
                                color = Charcoal.copy(alpha = 0.5f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes Card
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
                        Text(
                            text = "FIELD NOTE",
                            color = MutedSage,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        if (!isEditingNote) {
                            IconButton(
                                onClick = { isEditingNote = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.desc_edit_note),
                                    tint = ForestGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isEditingNote) {
                        OutlinedTextField(
                            value = editedNote,
                            onValueChange = { editedNote = it },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ForestGreen,
                                unfocusedBorderColor = OutlineBorder
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            TextButton(onClick = {
                                editedNote = record.note
                                isEditingNote = false
                            }) {
                                Text("Cancel", color = Charcoal)
                            }
                            Button(
                                onClick = {
                                    viewModel.updateRecordNote(record.id, editedNote)
                                    isEditingNote = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                            ) {
                                Text("Save Note", color = WarmIvory)
                            }
                        }
                    } else {
                        Text(
                            text = if (record.note.isNotBlank()) record.note else "No field notes attached to this photograph.",
                            color = if (record.note.isNotBlank()) Charcoal else Charcoal.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Bottom Action Bar
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        ImageStamper.sharePhoto(
                            context = context,
                            imageFile = currentImageFile,
                            title = "Share Kshetrajna Photograph"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("export_share_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = PureWhite
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = WarmGold
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (selectedTab == 0 && hasStamped) "Export Stamped Photograph" else "Export Original Photograph",
                        fontWeight = FontWeight.SemiBold,
                        color = WarmIvory
                    )
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        "Delete Photograph?",
                        color = Charcoal,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "This will permanently delete this photograph and its recorded metadata from your local device storage. This action cannot be undone.",
                        color = Charcoal.copy(alpha = 0.8f)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteRecord(record)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))
                    ) {
                        Text("Delete", color = PureWhite)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = Charcoal)
                    }
                },
                containerColor = WarmIvorySurface
            )
        }
    }
}
