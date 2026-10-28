package com.kshetrajna.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.kshetrajna.app.R
import com.kshetrajna.app.ui.AppScreen
import com.kshetrajna.app.ui.MainViewModel
import com.kshetrajna.app.ui.components.KshetrajnaInsignia
import com.kshetrajna.app.ui.theme.Charcoal
import com.kshetrajna.app.ui.theme.ForestGreen
import com.kshetrajna.app.ui.theme.MutedSage
import com.kshetrajna.app.ui.theme.OutlineBorder
import com.kshetrajna.app.ui.theme.PureWhite
import com.kshetrajna.app.ui.theme.WarmGold
import com.kshetrajna.app.ui.theme.WarmIvory
import com.kshetrajna.app.ui.theme.WarmIvorySurface

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recordLocation by viewModel.recordLocationEnabled.collectAsState()
    val defaultStamp by viewModel.defaultStampEnabled.collectAsState()
    val storageStats by viewModel.storageStats.collectAsState()

    var showClearDataDialog by remember { mutableStateOf(false) }

    var cameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        cameraGranted = permissions[Manifest.permission.CAMERA] ?: cameraGranted
        val loc = (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                  (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        locationGranted = loc
        if (loc) viewModel.fetchCurrentLocation()
    }

    BackHandler {
        viewModel.navigateTo(AppScreen.Camera)
    }

    val formattedStorageMb = remember(storageStats.totalBytes) {
        String.format(java.util.Locale.US, "%.2f MB", storageStats.totalBytes / (1024f * 1024f))
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
                .padding(bottom = 32.dp)
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
                        onClick = { viewModel.navigateTo(AppScreen.Camera) },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back),
                            tint = WarmIvory
                        )
                    }

                    Text(
                        text = "Settings & Privacy",
                        color = WarmIvory,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Permissions Status Section
            Text(
                text = "DEVICE PERMISSIONS",
                color = MutedSage,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Camera Permission Row
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
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Camera Access",
                                    color = Charcoal,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (cameraGranted) "Granted by device" else "Not granted",
                                    color = if (cameraGranted) ForestGreen else Color(0xFFB00020),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        if (!cameraGranted) {
                            OutlinedButton(
                                onClick = {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Grant", fontSize = 12.sp, color = ForestGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = OutlineBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Location Permission Row
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
                                tint = ForestGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Location Access (GPS)",
                                    color = Charcoal,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (locationGranted) "Granted by device" else "Not granted",
                                    color = if (locationGranted) ForestGreen else Color(0xFFB00020),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        if (!locationGranted) {
                            OutlinedButton(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Grant", fontSize = 12.sp, color = ForestGreen)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Capture Behavior Preferences
            Text(
                text = "CAPTURE PREFERENCES",
                color = MutedSage,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Record Location Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Acquire Coordinates with Captures",
                                color = Charcoal,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "When enabled, queries on-device GPS on demand when taking a shot.",
                                color = Charcoal.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Switch(
                            checked = recordLocation,
                            onCheckedChange = { viewModel.setLocationRecording(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = WarmGold,
                                checkedTrackColor = ForestGreen
                            ),
                            modifier = Modifier.testTag("settings_location_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = OutlineBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Default Stamp Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Generate Stamped Export by Default",
                                color = Charcoal,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Creates a visible information banner on captured photographs.",
                                color = Charcoal.copy(alpha = 0.65f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Switch(
                            checked = defaultStamp,
                            onCheckedChange = { viewModel.setDefaultStamp(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = WarmGold,
                                checkedTrackColor = ForestGreen
                            ),
                            modifier = Modifier.testTag("settings_stamp_switch")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Storage & Privacy Section
            Text(
                text = "STORAGE & PRIVACY",
                color = MutedSage,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WarmIvorySurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Storage info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Local Storage Usage",
                                color = Charcoal,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${storageStats.photoCount} photographs saved • $formattedStorageMb used",
                                color = Charcoal.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = OutlineBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Privacy disclosure
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "100% On-Device & Private",
                                color = Charcoal,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Kshetrajna does not transmit photographs, timestamps, or coordinates to any remote server or cloud service. All records remain exclusively on your device.",
                                color = Charcoal.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Clear Data Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedButton(
                    onClick = { showClearDataDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("clear_all_data_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFB00020)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB00020))
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Clear All Application Records", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Clear Confirmation Dialog
        if (showClearDataDialog) {
            AlertDialog(
                onDismissRequest = { showClearDataDialog = false },
                title = {
                    Text(
                        "Clear All Stored Records?",
                        color = Charcoal,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        "This will permanently delete all captured photographs, stamped images, and field journal records from this device. This operation cannot be reversed.",
                        color = Charcoal.copy(alpha = 0.85f)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showClearDataDialog = false
                            viewModel.clearAllData()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB00020))
                    ) {
                        Text("Delete Everything", color = PureWhite)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearDataDialog = false }) {
                        Text("Cancel", color = Charcoal)
                    }
                },
                containerColor = WarmIvorySurface
            )
        }
    }
}
