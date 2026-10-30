package com.example.ui.vr

import android.graphics.SurfaceTexture
import android.view.TextureView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.model.VirtualAppType
import com.example.model.VrEnvironment
import com.example.model.VrSettings
import com.example.sensor.HeadOrientation
import com.example.service.CaptureStatus
import com.example.service.ScreenCaptureManager
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SurfaceOledDark
import kotlin.math.roundToInt

@Composable
fun VrTheaterCanvas(
    settings: VrSettings,
    headOrientation: HeadOrientation,
    captureStatus: CaptureStatus,
    onAppSelect: (VirtualAppType) -> Unit,
    onRequestLiveMirror: () -> Unit,
    onToggleHud: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 3DoF head tracking parallax offsets
    val headPanX = if (settings.headTrackingEnabled) (-headOrientation.yaw * 6.5f) else 0f
    val headTiltY = if (settings.headTrackingEnabled) (headOrientation.pitch * 6.5f) else 0f
    val headRoll = if (settings.headTrackingEnabled) headOrientation.roll else 0f

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onToggleHud() },
                    onTap = { onToggleHud() }
                )
            }
            .testTag("vr_theater_canvas")
    ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight

        // 1. Render Environment Background with subtle 3D parallax
        VrEnvironmentBackground(
            environment = settings.environment,
            panX = headPanX * 0.35f,
            tiltY = headTiltY * 0.35f,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Render Virtual Display (Stereoscopic SBS or Monocular Giant Screen)
        if (settings.isStereoCardboardMode) {
            // Stereoscopic Dual-Eye Mode (Left Eye + Right Eye)
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = headRoll
                    }
            ) {
                // Left Eye Viewport
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    VirtualScreenViewport(
                        settings = settings,
                        eyeOffset = -settings.ipdOffset,
                        panX = headPanX,
                        tiltY = headTiltY,
                        captureStatus = captureStatus,
                        onAppSelect = onAppSelect,
                        onRequestLiveMirror = onRequestLiveMirror,
                        isLeftEye = true
                    )
                }

                // Center VR Lens Calibration Divider
                VrCenterDivider(modifier = Modifier.fillMaxHeight())

                // Right Eye Viewport
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    VirtualScreenViewport(
                        settings = settings,
                        eyeOffset = settings.ipdOffset,
                        panX = headPanX,
                        tiltY = headTiltY,
                        captureStatus = captureStatus,
                        onAppSelect = onAppSelect,
                        onRequestLiveMirror = onRequestLiveMirror,
                        isLeftEye = false
                    )
                }
            }
        } else {
            // Monocular Giant Cinema Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = headRoll
                    },
                contentAlignment = Alignment.Center
            ) {
                VirtualScreenViewport(
                    settings = settings,
                    eyeOffset = 0f,
                    panX = headPanX,
                    tiltY = headTiltY,
                    captureStatus = captureStatus,
                    onAppSelect = onAppSelect,
                    onRequestLiveMirror = onRequestLiveMirror,
                    isLeftEye = true
                )
            }
        }
    }
}

@Composable
private fun VirtualScreenViewport(
    settings: VrSettings,
    eyeOffset: Float,
    panX: Float,
    tiltY: Float,
    captureStatus: CaptureStatus,
    onAppSelect: (VirtualAppType) -> Unit,
    onRequestLiveMirror: () -> Unit,
    isLeftEye: Boolean
) {
    // Dynamic screen sizing relative to viewport
    val baseScale = settings.screenScale * (1f / settings.screenDistance.coerceAtLeast(0.5f))
    val curvature = settings.screenCurvature

    Box(
        modifier = Modifier
            .offset { IntOffset((panX + eyeOffset).roundToInt(), tiltY.roundToInt()) }
            .graphicsLayer {
                scaleX = baseScale
                scaleY = baseScale
                rotationY = ((panX + eyeOffset) / 25f)
                rotationX = -(tiltY / 25f)
                cameraDistance = 8000f
                alpha = settings.screenBrightness
            },
        contentAlignment = Alignment.Center
    ) {
        // Ambient screen glow bloom behind the giant virtual screen
        if (settings.ambientGlowEnabled) {
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .height(580.dp)
                    .drawBehind {
                        val glowBrush = Brush.radialGradient(
                            colors = listOf(
                                CyanNeon.copy(alpha = 0.28f * settings.screenBrightness),
                                ElectricAzure.copy(alpha = 0.15f * settings.screenBrightness),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.maxDimension * 0.7f
                        )
                        drawRect(brush = glowBrush)
                    }
            )
        }

        // Curved Screen Frame & Bezel
        val cornerRadius = (16 + curvature * 24).dp
        val screenBorderColor = if (settings.ambientGlowEnabled) {
            CyanNeon.copy(alpha = 0.6f)
        } else {
            Color(0xFF202A3C)
        }

        CardboardCurvedSurface(
            curvature = curvature,
            modifier = Modifier
                .width(280.dp)
                .height(490.dp)
                .clip(RoundedCornerShape(cornerRadius))
                .border(2.dp, screenBorderColor, RoundedCornerShape(cornerRadius))
                .background(SurfaceOledDark)
        ) {
            if (captureStatus == CaptureStatus.ACTIVE) {
                // Live Hardware Phone Screen Mirrored via TextureView
                LiveHardwareScreenTexture(modifier = Modifier.fillMaxSize())
            } else {
                // Interactive Companion UI when projection is not active
                VirtualScreenContent(
                    currentApp = settings.selectedApp,
                    isLiveMirrorActive = false,
                    onAppSelect = onAppSelect,
                    onRequestLiveMirror = onRequestLiveMirror,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun CardboardCurvedSurface(
    curvature: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Simulates physical screen curvature with edge gradient shadows
    Box(
        modifier = modifier
    ) {
        content()

        if (curvature > 0.05f) {
            // Left curved edge shadow
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(28.dp)
                    .align(Alignment.CenterStart)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.55f * curvature),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Right curved edge shadow
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(28.dp)
                    .align(Alignment.CenterEnd)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.55f * curvature)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun LiveHardwareScreenTexture(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            TextureView(ctx).apply {
                surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                        ScreenCaptureManager.attachSurfaceTexture(surface)
                    }

                    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                        ScreenCaptureManager.attachSurfaceTexture(surface)
                    }

                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                        ScreenCaptureManager.detachSurfaceTexture()
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                        // Frame updated
                    }
                }
            }
        },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            ScreenCaptureManager.detachSurfaceTexture()
        }
    }
}

@Composable
private fun VrEnvironmentBackground(
    environment: VrEnvironment,
    panX: Float,
    tiltY: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset { IntOffset(panX.roundToInt(), tiltY.roundToInt()) }
    ) {
        when (environment) {
            VrEnvironment.DEEP_SPACE -> {
                // Cosmic Nebula Background Image
                Image(
                    painter = painterResource(id = R.drawable.vr_nebula_bg_1793391061898),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Ambient vignette over space
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                            )
                        )
                )
            }
            VrEnvironment.PURE_VOID -> {
                // 100% OLED black void with subtle distant orientation crosshair
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    drawLine(
                        color = Color(0x22FFFFFF),
                        start = Offset(cx - 30f, cy),
                        end = Offset(cx + 30f, cy),
                        strokeWidth = 1.5f
                    )
                    drawLine(
                        color = Color(0x22FFFFFF),
                        start = Offset(cx, cy - 30f),
                        end = Offset(cx, cy + 30f),
                        strokeWidth = 1.5f
                    )
                }
            }
            VrEnvironment.CYBER_GRID -> {
                // Cyberpunk Neon Grid
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = Color(0xFF030712))
                    val horizonY = size.height * 0.7f

                    // Perspective grid lines
                    val step = size.width / 14
                    for (i in 0..14) {
                        val x = i * step
                        drawLine(
                            color = Color(0x3300E5FF),
                            start = Offset(size.width / 2, horizonY),
                            end = Offset(x, size.height),
                            strokeWidth = 1.5f
                        )
                    }
                    // Horizontal lines
                    var currY = horizonY
                    var gap = 12f
                    while (currY < size.height) {
                        drawLine(
                            color = Color(0x2200E5FF),
                            start = Offset(0f, currY),
                            end = Offset(size.width, currY),
                            strokeWidth = 1.2f
                        )
                        currY += gap
                        gap *= 1.35f
                    }
                }
            }
            VrEnvironment.WARM_CINEMA -> {
                // Ambient cinema hall
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF08060F),
                                    Color(0xFF151025),
                                    Color(0xFF0A0713)
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun VrCenterDivider(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(2.dp)
            .background(Color(0xFF101622)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(4.dp)
                .background(CyanNeon)
        )
        Text(
            text = "VR",
            fontSize = 9.sp,
            color = Color(0x44FFFFFF),
            fontFamily = FontFamily.Monospace
        )
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(4.dp)
                .background(CyanNeon)
        )
    }
}
