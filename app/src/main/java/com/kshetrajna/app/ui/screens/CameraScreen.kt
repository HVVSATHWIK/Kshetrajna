package com.kshetrajna.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import com.kshetrajna.app.util.LocationStatus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.kshetrajna.app.R
import com.kshetrajna.app.camera.CameraManager
import com.kshetrajna.app.ui.AppScreen
import com.kshetrajna.app.ui.MainViewModel
import com.kshetrajna.app.ui.components.KshetrajnaBrandHeader
import com.kshetrajna.app.ui.theme.Charcoal
import com.kshetrajna.app.ui.theme.CharcoalMuted
import com.kshetrajna.app.ui.theme.CharcoalSubtle
import com.kshetrajna.app.ui.theme.ForestGreen
import com.kshetrajna.app.ui.theme.ForestGreenContainer
import com.kshetrajna.app.ui.theme.ForestGreenDark
import com.kshetrajna.app.ui.theme.MutedSage
import com.kshetrajna.app.ui.theme.OutlineBorder
import com.kshetrajna.app.ui.theme.PrecisionBlue
import com.kshetrajna.app.ui.theme.PrecisionBlueBorder
import com.kshetrajna.app.ui.theme.PrecisionBlueContainer
import com.kshetrajna.app.ui.theme.PrecisionBlueDark
import com.kshetrajna.app.ui.theme.PrecisionBlueLight
import com.kshetrajna.app.ui.theme.PrecisionBlueSurface
import com.kshetrajna.app.ui.theme.PureWhite
import com.kshetrajna.app.ui.theme.TechnicalWhite
import com.kshetrajna.app.ui.theme.TechnicalWhiteCard
import com.kshetrajna.app.ui.theme.WarmGold
import com.kshetrajna.app.ui.theme.WarmIvory
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun CameraScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val cameraManager = remember { CameraManager(context) }
    var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }
    var cameraBound by remember { mutableStateOf(false) }
    var isStreamingFrames by remember { mutableStateOf(false) }
    var cameraErrorMessage by remember { mutableStateOf<String?>(null) }

    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val recordLocation by viewModel.recordLocationEnabled.collectAsState()
    val showGrid by viewModel.showGrid.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isAcquiringLocation by viewModel.isAcquiringLocation.collectAsState()
    val isCapturing by viewModel.isCapturing.collectAsState()
    val photoHistory by viewModel.photoHistory.collectAsState()

    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onPhotoSelected(uri)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cam = permissions[Manifest.permission.CAMERA] ?: false
        cameraPermissionGranted = cam
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.fetchCurrentLocation()
        }
        if (cam && previewViewInstance != null) {
            cameraManager.bindCamera(lifecycleOwner, previewViewInstance!!) { success, err ->
                cameraBound = success
                cameraErrorMessage = err
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraManager.unbind()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ForestGreenDark)
    ) {
        // Camera Viewfinder Area
        if (cameraPermissionGranted) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("camera_preview_view"),
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        // Use COMPATIBLE (TextureView) to ensure reliable frame rendering in Compose and emulators
                        this.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        this.scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewViewInstance = this

                        this.previewStreamState.observe(lifecycleOwner) { streamState ->
                            isStreamingFrames = (streamState == PreviewView.StreamState.STREAMING)
                        }

                        cameraManager.bindCamera(lifecycleOwner, this) { success, err ->
                            cameraBound = success
                            cameraErrorMessage = err
                        }
                    }
                },
                update = { view ->
                    previewViewInstance = view
                }
            )

            // Diagnostic feedback if camera initialization encounters an issue
            if (!cameraBound && cameraErrorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ForestGreenDark.copy(alpha = 0.9f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = WarmGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Camera Initialization Notice",
                            color = WarmIvory,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = cameraErrorMessage ?: "Unable to bind camera",
                            color = WarmIvory.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = {
                                previewViewInstance?.let { view ->
                                    cameraManager.bindCamera(lifecycleOwner, view) { success, err ->
                                        cameraBound = success
                                        cameraErrorMessage = err
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarmGold,
                                contentColor = Charcoal
                            )
                        ) {
                            Text("Retry Camera", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        } else {
            // Permission request placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ForestGreenDark)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = WarmGold,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "Camera Access Required",
                        color = WarmIvory,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Kshetrajna captures authentic field photographs with accurate local timestamps and optional coordinates.",
                        color = WarmIvory.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarmGold,
                            contentColor = Charcoal
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("grant_camera_permission_button")
                    ) {
                        Text("Grant Camera Permission", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Composition Grid (Rule of Thirds)
        if (showGrid) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val gridColor = Color(0x44FFFFFF)

                // Vertical lines
                drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1f)
                drawLine(gridColor, Offset(2f * w / 3f, 0f), Offset(2f * w / 3f, h), strokeWidth = 1f)

                // Horizontal lines
                drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1f)
                drawLine(gridColor, Offset(0f, 2f * h / 3f), Offset(w, 2f * h / 3f), strokeWidth = 1f)
            }
        }

        // Top App Bar (Deep Forest Green header with integrated compact location telemetry)
        val locationStatus by viewModel.locationStatus.collectAsState()
        var showLocationDetailDialog by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            color = ForestGreen.copy(alpha = 0.95f),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(bottom = 6.dp)
            ) {
                // Header row: Brand insignia + quick tools
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Brand Header with Insignia and Wordmark
                    KshetrajnaBrandHeader(
                        insigniaSize = 26.dp,
                        textColor = WarmIvory,
                        goldColor = WarmGold,
                        showTagline = true,
                        modifier = Modifier.testTag("brand_header")
                    )

                    // Action controls: Flash, Grid, Settings
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Flash Mode Toggle
                        IconButton(
                            onClick = { flashMode = cameraManager.toggleFlash() },
                            modifier = Modifier.testTag("flash_toggle_button")
                        ) {
                            Icon(
                                imageVector = when (flashMode) {
                                    ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                                    ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                                    else -> Icons.Default.FlashOff
                                },
                                contentDescription = stringResource(R.string.desc_flash),
                                tint = if (flashMode != ImageCapture.FLASH_MODE_OFF) WarmGold else WarmIvory
                            )
                        }

                        // Grid Toggle
                        IconButton(
                            onClick = { viewModel.toggleGrid() },
                            modifier = Modifier.testTag("grid_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = stringResource(R.string.desc_grid),
                                tint = if (showGrid) WarmGold else WarmIvory.copy(alpha = 0.7f)
                            )
                        }

                        // Settings Button
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.Settings) },
                            modifier = Modifier.testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.desc_settings),
                                tint = WarmIvory
                            )
                        }
                    }
                }

                // Compact, unobtrusive location status strip (respects viewfinder space, no diagnostic dots)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .clickable { showLocationDetailDialog = true }
                            .testTag("location_status_pill"),
                        color = Color(0x33000000),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (locationStatus) {
                                is LocationStatus.Ready -> PrecisionBlueBorder.copy(alpha = 0.5f)
                                is LocationStatus.PoorAccuracy -> WarmGold.copy(alpha = 0.5f)
                                is LocationStatus.Acquiring -> PrecisionBlueBorder.copy(alpha = 0.35f)
                                is LocationStatus.PermissionDenied -> Color(0xFFE57373).copy(alpha = 0.5f)
                                else -> OutlineBorder.copy(alpha = 0.25f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = when (locationStatus) {
                                    is LocationStatus.Ready -> Icons.Default.LocationOn
                                    is LocationStatus.PoorAccuracy -> Icons.Default.Warning
                                    is LocationStatus.Acquiring -> Icons.Default.LocationOn
                                    is LocationStatus.PermissionDenied -> Icons.Default.LocationOff
                                    is LocationStatus.Disabled -> Icons.Default.LocationOff
                                    is LocationStatus.Unavailable -> Icons.Default.LocationOff
                                },
                                contentDescription = "GPS Status",
                                tint = when (locationStatus) {
                                    is LocationStatus.Ready -> PrecisionBlueLight
                                    is LocationStatus.PoorAccuracy -> WarmGold
                                    is LocationStatus.Acquiring -> PrecisionBlueLight
                                    is LocationStatus.PermissionDenied -> Color(0xFFE57373)
                                    is LocationStatus.Disabled -> WarmIvory.copy(alpha = 0.5f)
                                    is LocationStatus.Unavailable -> WarmIvory.copy(alpha = 0.6f)
                                },
                                modifier = Modifier.size(13.dp)
                            )

                            Text(
                                text = when (val status = locationStatus) {
                                    is LocationStatus.Ready -> "GPS ready · ±${status.location.accuracyMeters.toInt()}m"
                                    is LocationStatus.PoorAccuracy -> "GPS low accuracy (±${status.location.accuracyMeters.toInt()}m)"
                                    is LocationStatus.Acquiring -> "Getting location…"
                                    is LocationStatus.PermissionDenied -> "Location permission needed"
                                    is LocationStatus.Unavailable -> "Location unavailable"
                                    is LocationStatus.Disabled -> "GPS disabled"
                                },
                                color = TechnicalWhite,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Location Detail Modal Dialog with Professional White & Blue Mixed Telemetry Cards
        if (showLocationDetailDialog) {
            AlertDialog(
                onDismissRequest = { showLocationDetailDialog = false },
                containerColor = ForestGreenDark,
                titleContentColor = WarmIvory,
                textContentColor = WarmIvory,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PrecisionBlueLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "GPS & Geodetic Telemetry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TechnicalWhite
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when (val status = locationStatus) {
                            is LocationStatus.Ready, is LocationStatus.PoorAccuracy -> {
                                val loc = if (status is LocationStatus.Ready) status.location else (status as LocationStatus.PoorAccuracy).location
                                val fixAgeSeconds = ((System.currentTimeMillis() - loc.fixTimestamp) / 1000).coerceAtLeast(0)

                                // Radar preview
                                com.kshetrajna.app.ui.components.CoordinateRadarMap(
                                    latitude = loc.latitude,
                                    longitude = loc.longitude,
                                    accuracyMeters = loc.accuracyMeters,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                )

                                // Professional White Card with Precision Blue mixed telemetry
                                Surface(
                                    color = TechnicalWhiteCard,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PrecisionBlueBorder.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "COORDINATES (DMS)",
                                            color = PrecisionBlueDark,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = loc.dmsCoordinates,
                                            color = Charcoal,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "DECIMAL DEGREES",
                                            color = PrecisionBlueDark,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = loc.decimalCoordinates,
                                            color = Charcoal,
                                            fontSize = 11.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Surface(
                                                color = PrecisionBlueContainer,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "ACCURACY: ±${loc.accuracyMeters.toInt()}m",
                                                    color = PrecisionBlueDark,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Surface(
                                                color = Color(0xFFF1F5F9),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "FIX AGE: ${fixAgeSeconds}s ago",
                                                    color = CharcoalMuted,
                                                    fontSize = 9.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        if (loc.altitudeMeters != null) {
                                            Text(
                                                text = "ALTITUDE: ${loc.altitudeMeters.toInt()}m above sea level",
                                                color = CharcoalSubtle,
                                                fontSize = 10.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "GEODETIC LOCALITY",
                                            color = PrecisionBlueDark,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = loc.addressSummary ?: "Address unavailable (offline satellite fix active)",
                                            color = Charcoal,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (status is LocationStatus.PoorAccuracy) {
                                    Text(
                                        text = "Notice: Estimated accuracy is currently ±${loc.accuracyMeters.toInt()}m. For higher precision, move outdoors with a clear view of the sky.",
                                        color = WarmGold,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            is LocationStatus.Acquiring -> {
                                Text(
                                    text = "Acquiring satellite lock. Hardware GPS receivers may take a few seconds to connect to orbital satellites when outdoors.",
                                    color = WarmIvory.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            is LocationStatus.PermissionDenied -> {
                                Text(
                                    text = "Location permission is not granted. Kshetrajna captures photographs without location by default, or you can grant permission to embed verified GPS coordinates.",
                                    color = WarmIvory.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            is LocationStatus.Unavailable -> {
                                Text(
                                    text = "Satellite GPS signal is currently unavailable. You may capture photos immediately without waiting; photos will be saved with authentic local timestamps and no fabricated coordinates.",
                                    color = WarmIvory.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            is LocationStatus.Disabled -> {
                                Text(
                                    text = "Location recording is currently turned off in settings. Enable it to record device coordinates at shutter release.",
                                    color = WarmIvory.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    when (locationStatus) {
                        is LocationStatus.PermissionDenied -> {
                            Button(
                                onClick = {
                                    showLocationDetailDialog = false
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WarmGold,
                                    contentColor = Charcoal
                                )
                            ) {
                                Text("Grant Permission", fontWeight = FontWeight.Bold)
                            }
                        }
                        is LocationStatus.Disabled -> {
                            Button(
                                onClick = {
                                    viewModel.setLocationRecording(true)
                                    showLocationDetailDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WarmGold,
                                    contentColor = Charcoal
                                )
                            ) {
                                Text("Enable Location", fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            Button(
                                onClick = {
                                    viewModel.fetchCurrentLocation()
                                    showLocationDetailDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrecisionBlue,
                                    contentColor = TechnicalWhite
                                )
                            ) {
                                Text("Refresh Fix", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { showLocationDetailDialog = false }
                    ) {
                        Text("Dismiss", color = WarmIvory.copy(alpha = 0.8f))
                    }
                }
            )
        }

        // Bottom Camera Controls Area (Mathematically Centered Shutter + Balanced Wings)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = ForestGreenDark.copy(alpha = 0.95f),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT SLOT: Field Journal / History Button and Photo Import (weight 1f, aligned Start)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Field Journal / History Button
                        BadgedBox(
                            badge = {
                                if (photoHistory.isNotEmpty()) {
                                    Badge(
                                        containerColor = WarmGold,
                                        contentColor = Charcoal
                                    ) {
                                        Text(
                                            text = "${photoHistory.size}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ForestGreenContainer)
                                    .border(1.5.dp, WarmGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .clickable { viewModel.navigateTo(AppScreen.History) }
                                    .testTag("gallery_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                val lastPhoto = photoHistory.firstOrNull()
                                if (lastPhoto != null) {
                                    AsyncImage(
                                        model = File(lastPhoto.originalFilePath),
                                        contentDescription = stringResource(R.string.desc_gallery),
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = stringResource(R.string.desc_gallery),
                                        tint = WarmIvory,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        // Photo Import Button (Secondary import option)
                        IconButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ForestGreenContainer)
                                .border(1.dp, TechnicalWhite.copy(alpha = 0.25f), CircleShape)
                                .testTag("import_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Import photo",
                                tint = TechnicalWhite.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // CENTER SLOT: Primary Shutter Button (Mathematically Centered horizontally)
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .border(3.5.dp, WarmGold, CircleShape)
                            .padding(5.dp)
                            .clip(CircleShape)
                            .background(TechnicalWhite)
                            .clickable(
                                enabled = !isCapturing,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = ForestGreen)
                            ) {
                                if (isCapturing) return@clickable
                                if (!cameraPermissionGranted) {
                                    viewModel.showMessage("Camera permission required.")
                                    return@clickable
                                }
                                if (!cameraBound) {
                                    viewModel.showMessage(
                                        cameraErrorMessage ?: "Camera sensor is not ready."
                                    )
                                    return@clickable
                                }

                                val photoDir = File(context.cacheDir, "captures")
                                photoDir.mkdirs()
                                val tempFile = File(photoDir, "cap_${System.currentTimeMillis()}.jpg")

                                scope.launch {
                                    try {
                                        val capturedFile = cameraManager.capturePhoto(tempFile)
                                        viewModel.onPhotoCaptured(capturedFile)
                                    } catch (e: Exception) {
                                        viewModel.showMessage("Camera capture failed: ${e.localizedMessage}")
                                    }
                                }
                            }
                            .testTag("shutter_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                color = ForestGreen,
                                strokeWidth = 3.dp
                            )
                        } else {
                            // Inner circular plate with subtle precision rim
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(WarmIvory)
                                    .border(1.dp, ForestGreen.copy(alpha = 0.25f), CircleShape)
                            )
                        }
                    }
                }

                // RIGHT SLOT: Quick Location Toggle & Camera Switch (weight 1f, aligned End)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quick Location Toggle
                        IconButton(
                            onClick = { viewModel.toggleLocationRecording() },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (recordLocation) PrecisionBlueSurface else ForestGreenContainer)
                                .border(
                                    1.dp,
                                    if (recordLocation) PrecisionBlueLight else WarmIvory.copy(alpha = 0.3f),
                                    CircleShape
                                )
                                .testTag("toggle_location_button")
                        ) {
                            Icon(
                                imageVector = if (recordLocation) Icons.Default.LocationOn else Icons.Default.LocationOff,
                                contentDescription = stringResource(R.string.desc_toggle_location),
                                tint = if (recordLocation) PrecisionBlueLight else WarmIvory.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Switch Camera (Front/Rear)
                        IconButton(
                            onClick = {
                                previewViewInstance?.let { view ->
                                    cameraManager.toggleCamera(lifecycleOwner, view) { success, err ->
                                        cameraBound = success
                                        cameraErrorMessage = err
                                    }
                                }
                            },
                            enabled = cameraBound,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ForestGreenContainer)
                                .border(1.dp, WarmIvory.copy(alpha = 0.3f), CircleShape)
                                .testTag("switch_camera_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlipCameraAndroid,
                                contentDescription = stringResource(R.string.desc_switch_camera),
                                tint = if (cameraBound) WarmIvory else WarmIvory.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
