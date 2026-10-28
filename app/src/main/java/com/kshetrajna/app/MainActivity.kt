package com.kshetrajna.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kshetrajna.app.ui.AppScreen
import com.kshetrajna.app.ui.MainViewModel
import com.kshetrajna.app.ui.components.KshetrajnaAnimatedSplash
import com.kshetrajna.app.ui.screens.CameraScreen
import com.kshetrajna.app.ui.screens.CaptureReviewScreen
import com.kshetrajna.app.ui.screens.HistoryScreen
import com.kshetrajna.app.ui.screens.PhotoDetailScreen
import com.kshetrajna.app.ui.screens.SettingsScreen
import com.kshetrajna.app.ui.theme.KshetrajnaTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KshetrajnaTheme {
                var showSplash by rememberSaveable {
                    mutableStateOf(!viewModel.hasShownStartupSplash)
                }
                val currentScreen by viewModel.currentScreen.collectAsState()
                val userMessage by viewModel.userMessage.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(userMessage) {
                    userMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearUserMessage()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { _ ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        Crossfade(
                            targetState = showSplash,
                            animationSpec = tween(350),
                            label = "splash_transition"
                        ) { isSplash ->
                            if (isSplash) {
                                KshetrajnaAnimatedSplash(
                                    onSplashFinished = {
                                        viewModel.markStartupSplashCompleted()
                                        showSplash = false
                                    }
                                )
                            } else {
                                Crossfade(
                                    targetState = currentScreen,
                                    animationSpec = tween(220),
                                    label = "screen_transition"
                                ) { screen ->
                                    when (screen) {
                                        is AppScreen.Camera -> CameraScreen(viewModel = viewModel)
                                        is AppScreen.Review -> CaptureReviewScreen(reviewState = screen, viewModel = viewModel)
                                        is AppScreen.History -> HistoryScreen(viewModel = viewModel)
                                        is AppScreen.Detail -> PhotoDetailScreen(record = screen.record, viewModel = viewModel)
                                        is AppScreen.Settings -> SettingsScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
