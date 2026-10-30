package com.example.ui.vr

import android.Manifest
import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.Rational
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.VrSettings
import com.example.service.CaptureStatus
import com.example.ui.components.PrivacySecurityDialog
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PrivacyGreen
import com.example.viewmodel.VrViewModel
import kotlinx.coroutines.launch

@Composable
fun VrScreen(
    viewModel: VrViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val headOrientation by viewModel.headOrientation.collectAsStateWithLifecycle()
    val captureStatus by viewModel.captureStatus.collectAsStateWithLifecycle()
    val isSecurityAuditOpen by viewModel.isSecurityAuditOpen.collectAsStateWithLifecycle()

    // Activity result launcher for MediaProjection permission
    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.onProjectionPermissionGranted(result.resultCode, result.data!!)
            scope.launch {
                snackbarHostState.showSnackbar("Phone screen mirror stream connected to VR theater")
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Screen capture permission was declined")
            }
        }
    }

    // Permission launcher for POST_NOTIFICATIONS (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val mpm = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjectionLauncher.launch(mpm.createScreenCaptureIntent())
    }

    val requestMediaCapture: () -> Unit = {
        if (captureStatus == CaptureStatus.ACTIVE) {
            viewModel.stopMirroring()
            scope.launch {
                snackbarHostState.showSnackbar("Screen mirroring stopped")
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                val mpm = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                mediaProjectionLauncher.launch(mpm.createScreenCaptureIntent())
            }
        }
    }

    // Fullscreen system bars management
    DisposableEffect(settings.isFullscreenVr) {
        activity?.let { act ->
            val window = act.window
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            if (settings.isFullscreenVr) {
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsetsCompat.Type.systemBars())
                // Keep screen on while in fullscreen VR
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                controller.show(WindowInsetsCompat.Type.systemBars())
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        onDispose {
            activity?.let { act ->
                val controller = WindowCompat.getInsetsController(act.window, act.window.decorView)
                controller.show(WindowInsetsCompat.Type.systemBars())
                act.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    // Back handler behavior
    BackHandler {
        if (settings.isFullscreenVr) {
            viewModel.toggleFullscreenVr()
        } else if (isSecurityAuditOpen) {
            viewModel.closeSecurityAudit()
        } else if (settings.selectedApp != com.example.model.VirtualAppType.SYSTEM_HOME) {
            viewModel.selectApp(com.example.model.VirtualAppType.SYSTEM_HOME)
        } else {
            activity?.finish()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ObsidianBlack
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Main VR Theater Canvas (Curved Display + Environment + Parallax)
            VrTheaterCanvas(
                settings = settings,
                headOrientation = headOrientation,
                captureStatus = captureStatus,
                onAppSelect = { app -> viewModel.selectApp(app) },
                onRequestLiveMirror = requestMediaCapture,
                onToggleHud = { viewModel.toggleHudVisibility() },
                modifier = Modifier.fillMaxSize()
            )

            // VR Controls Overlay & HUD
            VrControlsHud(
                settings = settings,
                captureStatus = captureStatus,
                onToggleFullscreenVr = { viewModel.toggleFullscreenVr() },
                onToggleStereoMode = { viewModel.toggleStereoMode() },
                onToggleHeadTracking = { viewModel.toggleHeadTracking() },
                onTogglePrivacyCurtain = { viewModel.togglePrivacyCurtain() },
                onRecenterGaze = { viewModel.recenterGaze() },
                onUpdateCurvature = { viewModel.updateCurvature(it) },
                onUpdateScale = { viewModel.updateScale(it) },
                onUpdateDistance = { viewModel.updateDistance(it) },
                onUpdateIpd = { viewModel.updateIpd(it) },
                onUpdateBrightness = { viewModel.updateBrightness(it) },
                onSelectEnvironment = { viewModel.selectEnvironment(it) },
                onRequestLiveMirror = requestMediaCapture,
                onRequestPiP = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            val params = PictureInPictureParams.Builder()
                                .setAspectRatio(Rational(16, 9))
                                .build()
                            activity?.enterPictureInPictureMode(params)
                        } catch (e: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar("PiP mode unavailable: ${e.message}")
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("PiP requires Android 8.0+")
                        }
                    }
                },
                onOpenSecurityAudit = { viewModel.openSecurityAudit() },
                onToggleHudVisibility = { viewModel.toggleHudVisibility() },
                modifier = Modifier.fillMaxSize()
            )

            // Physical Screen Privacy Curtain (Anti-shoulder surfing shield)
            AnimatedVisibility(
                visible = settings.privacyCurtainActive && !settings.isFullscreenVr,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.94f))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, PrivacyGreen, RoundedCornerShape(16.dp))
                            .background(Color(0xFF040A06))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.layout.Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "PRIVACY BLACKOUT ACTIVE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrivacyGreen
                            )
                            Text(
                                text = "Physical screen is dimmed to prevent shoulder surfing. Your VR display continues running privately.",
                                fontSize = 12.sp,
                                color = Color.White,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.togglePrivacyCurtain() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrivacyGreen, contentColor = ObsidianBlack),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("DEACTIVATE CURTAIN", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Security & Privacy Audit Dialog
            if (isSecurityAuditOpen) {
                PrivacySecurityDialog(
                    onDismiss = { viewModel.closeSecurityAudit() }
                )
            }
        }
    }
}
