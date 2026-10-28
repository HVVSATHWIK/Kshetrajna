package com.kshetrajna.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.kshetrajna.app.data.PhotoRecord
import com.kshetrajna.app.ui.AppScreen
import com.kshetrajna.app.ui.MainViewModel
import com.kshetrajna.app.ui.components.KshetrajnaInsignia
import com.kshetrajna.app.ui.theme.Charcoal
import com.kshetrajna.app.ui.theme.ForestGreen
import com.kshetrajna.app.ui.theme.MutedSage
import com.kshetrajna.app.ui.theme.OutlineBorder
import com.kshetrajna.app.ui.theme.WarmGold
import com.kshetrajna.app.ui.theme.WarmIvory
import com.kshetrajna.app.ui.theme.WarmIvorySurface
import java.io.File

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val photoHistory by viewModel.photoHistory.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, CAMERA, IMPORTED, LOCATION, NOTES

    val filteredPhotos = remember(photoHistory, selectedFilter) {
        when (selectedFilter) {
            "CAMERA" -> photoHistory.filter { it.provenance == "CAMERA_CAPTURE" }
            "IMPORTED" -> photoHistory.filter { it.provenance == "IMPORTED" }
            "LOCATION" -> photoHistory.filter { it.hasLocation }
            "NOTES" -> photoHistory.filter { it.note.isNotBlank() }
            else -> photoHistory
        }
    }

    BackHandler {
        viewModel.navigateTo(AppScreen.Camera)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ForestGreen,
                shadowElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.Camera) },
                        modifier = Modifier.testTag("history_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back),
                            tint = WarmIvory
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KshetrajnaInsignia(size = 22.dp, horizonColor = WarmIvory)
                        Text(
                            text = "Field Journal",
                            color = WarmIvory,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "(${photoHistory.size})",
                            color = WarmGold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.Camera) },
                        modifier = Modifier.testTag("history_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = stringResource(R.string.desc_shutter),
                            tint = WarmGold
                        )
                    }
                }
            }

            // Filter Chips (Scrollable row)
            if (photoHistory.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All Records") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = WarmIvory,
                            containerColor = WarmIvorySurface,
                            labelColor = Charcoal
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "CAMERA",
                        onClick = { selectedFilter = if (selectedFilter == "CAMERA") "ALL" else "CAMERA" },
                        label = { Text("Captures") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = WarmIvory,
                            containerColor = WarmIvorySurface,
                            labelColor = Charcoal
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "IMPORTED",
                        onClick = { selectedFilter = if (selectedFilter == "IMPORTED") "ALL" else "IMPORTED" },
                        label = { Text("Imported") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = WarmIvory,
                            containerColor = WarmIvorySurface,
                            labelColor = Charcoal
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "LOCATION",
                        onClick = { selectedFilter = if (selectedFilter == "LOCATION") "ALL" else "LOCATION" },
                        label = { Text("With Location") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = WarmIvory,
                            containerColor = WarmIvorySurface,
                            labelColor = Charcoal
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "NOTES",
                        onClick = { selectedFilter = if (selectedFilter == "NOTES") "ALL" else "NOTES" },
                        label = { Text("With Notes") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreen,
                            selectedLabelColor = WarmIvory,
                            containerColor = WarmIvorySurface,
                            labelColor = Charcoal
                        )
                    )
                }
            }

            // Grid or Empty State
            if (filteredPhotos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        KshetrajnaInsignia(
                            size = 72.dp,
                            archColor = WarmGold,
                            sunColor = WarmGold,
                            horizonColor = ForestGreen
                        )
                        Text(
                            text = if (photoHistory.isEmpty()) "No Photographs Yet" else "No Matching Records",
                            color = Charcoal,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (photoHistory.isEmpty()) {
                                "Kshetrajna preserves your observations with authenticated timestamps and coordinates.\n\nTake a photograph or import one from your library."
                            } else {
                                "Try adjusting your filters above to see other captured records."
                            },
                            color = Charcoal.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (photoHistory.isEmpty()) {
                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.Camera) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ForestGreen,
                                    contentColor = WarmIvory
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("empty_state_camera_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = WarmGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Open Camera", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("photo_grid"),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = 80.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPhotos, key = { it.id }) { photo ->
                        PhotoGridCard(
                            photo = photo,
                            onClick = { viewModel.navigateTo(AppScreen.Detail(photo)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoGridCard(
    photo: PhotoRecord,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("photo_card_${photo.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = File(photo.stampedFilePath ?: photo.originalFilePath),
                    contentDescription = "Saved photo from ${photo.formattedDateTime}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Provenance badge (top-start)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    color = if (photo.provenance == "IMPORTED") Color(0xDD3A3A3A) else ForestGreen.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (photo.provenance == "IMPORTED") "IMPORT" else "CAPTURE",
                        color = WarmIvory,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                // Stamped badge indicator (top-end)
                if (photo.isStamped) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        color = ForestGreen.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "STAMPED",
                            color = WarmGold,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Location badge indicator (bottom-start)
                if (photo.hasLocation) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp),
                        color = Color(0xCC000000),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = WarmGold,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "GPS",
                                color = WarmIvory,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = photo.formattedDateTime,
                    color = Charcoal,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )

                if (photo.note.isNotBlank()) {
                    Text(
                        text = photo.note,
                        color = Charcoal.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
