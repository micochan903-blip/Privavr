package com.example.ui.vr

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VrEnvironment
import com.example.model.VrSettings
import com.example.service.CaptureStatus
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PrivacyGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardHighlight
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun VrControlsHud(
    settings: VrSettings,
    captureStatus: CaptureStatus,
    onToggleFullscreenVr: () -> Unit,
    onToggleStereoMode: () -> Unit,
    onToggleHeadTracking: () -> Unit,
    onTogglePrivacyCurtain: () -> Unit,
    onRecenterGaze: () -> Unit,
    onUpdateCurvature: (Float) -> Unit,
    onUpdateScale: (Float) -> Unit,
    onUpdateDistance: (Float) -> Unit,
    onUpdateIpd: (Float) -> Unit,
    onUpdateBrightness: (Float) -> Unit,
    onSelectEnvironment: (VrEnvironment) -> Unit,
    onRequestLiveMirror: () -> Unit,
    onRequestPiP: () -> Unit,
    onOpenSecurityAudit: () -> Unit,
    onToggleHudVisibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Quick, 1: Projection & Display, 2: Environment

    Column(
        modifier = modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP HUD BAR
        TopHudBar(
            settings = settings,
            captureStatus = captureStatus,
            onToggleFullscreenVr = onToggleFullscreenVr,
            onRecenterGaze = onRecenterGaze,
            onOpenSecurityAudit = onOpenSecurityAudit,
            onToggleHudVisibility = onToggleHudVisibility
        )

        // BOTTOM CONTROLS PANEL
        AnimatedVisibility(
            visible = settings.hudVisible && !settings.isFullscreenVr,
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit = slideOutVertically { it / 2 } + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Brush.horizontalGradient(listOf(CyanNeon.copy(alpha = 0.5f), ElectricAzure.copy(alpha = 0.4f))), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceGlass)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    // Category Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TabButton(
                            title = "Controls",
                            icon = Icons.Default.Tune,
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        TabButton(
                            title = "Geometry",
                            icon = Icons.Default.CropLandscape,
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                        TabButton(
                            title = "Atmosphere",
                            icon = Icons.Default.Lightbulb,
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (selectedTab) {
                        0 -> QuickControlsTab(
                            settings = settings,
                            captureStatus = captureStatus,
                            onToggleFullscreenVr = onToggleFullscreenVr,
                            onToggleStereoMode = onToggleStereoMode,
                            onToggleHeadTracking = onToggleHeadTracking,
                            onTogglePrivacyCurtain = onTogglePrivacyCurtain,
                            onRequestLiveMirror = onRequestLiveMirror,
                            onRequestPiP = onRequestPiP
                        )
                        1 -> GeometrySlidersTab(
                            settings = settings,
                            onUpdateCurvature = onUpdateCurvature,
                            onUpdateScale = onUpdateScale,
                            onUpdateDistance = onUpdateDistance,
                            onUpdateIpd = onUpdateIpd,
                            onUpdateBrightness = onUpdateBrightness
                        )
                        2 -> AtmosphereTab(
                            selected = settings.environment,
                            onSelect = onSelectEnvironment
                        )
                    }
                }
            }
        }

        // Minimal Floating Badge when Fullscreen VR Mode is active
        if (settings.isFullscreenVr) {
            FullscreenFloatingPill(
                isStereo = settings.isStereoCardboardMode,
                onExitFullscreen = onToggleFullscreenVr,
                onRecenter = onRecenterGaze,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun TopHudBar(
    settings: VrSettings,
    captureStatus: CaptureStatus,
    onToggleFullscreenVr: () -> Unit,
    onRecenterGaze: () -> Unit,
    onOpenSecurityAudit: () -> Unit,
    onToggleHudVisibility: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceGlass)
            .border(1.dp, SurfaceCardHighlight, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Security Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(CyanNeon.copy(alpha = 0.2f))
                    .border(1.dp, CyanNeon, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = CyanNeon,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = "PrivaVR",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (captureStatus == CaptureStatus.ACTIVE) "LIVE MIRROR ACTIVE" else "0 B/s OFFLINE SAFE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (captureStatus == CaptureStatus.ACTIVE) CyanNeon else PrivacyGreen,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Action Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Security Audit button
            IconButton(
                onClick = onOpenSecurityAudit,
                modifier = Modifier.size(36.dp).testTag("security_audit_button")
            ) {
                Icon(Icons.Default.Security, contentDescription = "Security Audit", tint = PrivacyGreen, modifier = Modifier.size(18.dp))
            }

            // Recenter head tracking
            IconButton(
                onClick = onRecenterGaze,
                modifier = Modifier.size(36.dp).testTag("recenter_button")
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Recenter View", tint = CyanNeon, modifier = Modifier.size(20.dp))
            }

            // Fullscreen VR Mode toggle (As requested by user!)
            Button(
                onClick = onToggleFullscreenVr,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (settings.isFullscreenVr) CyanNeon else SurfaceCardHighlight,
                    contentColor = if (settings.isFullscreenVr) ObsidianBlack else TextPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("fullscreen_vr_toggle_button")
            ) {
                Icon(
                    imageVector = if (settings.isFullscreenVr) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = "Toggle Fullscreen VR",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (settings.isFullscreenVr) "EXIT VR" else "FULLSCREEN VR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuickControlsTab(
    settings: VrSettings,
    captureStatus: CaptureStatus,
    onToggleFullscreenVr: () -> Unit,
    onToggleStereoMode: () -> Unit,
    onToggleHeadTracking: () -> Unit,
    onTogglePrivacyCurtain: () -> Unit,
    onRequestLiveMirror: () -> Unit,
    onRequestPiP: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Main Screen Mirror Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRequestLiveMirror,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (captureStatus == CaptureStatus.ACTIVE) Color(0xFFC62828) else CyanNeon,
                    contentColor = if (captureStatus == CaptureStatus.ACTIVE) Color.White else ObsidianBlack
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).testTag("quick_mirror_action")
            ) {
                Icon(
                    imageVector = Icons.Default.CastConnected,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (captureStatus == CaptureStatus.ACTIVE) "STOP PHONE MIRROR" else "MIRROR PHONE SCREEN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onRequestPiP,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardHighlight, contentColor = TextPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("pip_button")
            ) {
                Icon(Icons.Default.PictureInPictureAlt, contentDescription = "PiP", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("PiP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Toggles Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stereo SBS toggle
            FilterChip(
                selected = settings.isStereoCardboardMode,
                onClick = onToggleStereoMode,
                label = {
                    Text(
                        text = if (settings.isStereoCardboardMode) "Stereo SBS (VR Headset)" else "Giant IMAX (Mono)",
                        fontSize = 11.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CropLandscape,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ElectricAzure.copy(alpha = 0.3f),
                    selectedLabelColor = CyanNeon
                ),
                modifier = Modifier.weight(1f)
            )

            // Head tracking toggle
            FilterChip(
                selected = settings.headTrackingEnabled,
                onClick = onToggleHeadTracking,
                label = {
                    Text(
                        text = if (settings.headTrackingEnabled) "Gyro 3DoF On" else "Locked Gaze",
                        fontSize = 11.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CyanNeon.copy(alpha = 0.25f),
                    selectedLabelColor = CyanNeon
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Privacy Curtain Toggle (Zero shoulder-surfing)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onTogglePrivacyCurtain),
            colors = CardDefaults.cardColors(containerColor = if (settings.privacyCurtainActive) Color(0xFF1B2B1B) else SurfaceCard)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (settings.privacyCurtainActive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = if (settings.privacyCurtainActive) PrivacyGreen else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Physical Screen Privacy Curtain",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (settings.privacyCurtainActive) PrivacyGreen else TextPrimary
                        )
                        Text(
                            text = "Blanks out physical display to prevent eavesdropping",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
                Switch(
                    checked = settings.privacyCurtainActive,
                    onCheckedChange = { onTogglePrivacyCurtain() },
                    colors = SwitchDefaults.colors(checkedThumbColor = PrivacyGreen, checkedTrackColor = PrivacyGreen.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
private fun GeometrySlidersTab(
    settings: VrSettings,
    onUpdateCurvature: (Float) -> Unit,
    onUpdateScale: (Float) -> Unit,
    onUpdateDistance: (Float) -> Unit,
    onUpdateIpd: (Float) -> Unit,
    onUpdateBrightness: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Screen Curvature Slider
        SliderItem(
            label = "Curved Cinema Arc",
            value = settings.screenCurvature,
            valueDisplay = "${(settings.screenCurvature * 100).toInt()}%",
            onValueChange = onUpdateCurvature,
            valueRange = 0f..1f
        )

        // Screen Scale Slider
        SliderItem(
            label = "Virtual Screen Size (IMAX)",
            value = settings.screenScale,
            valueDisplay = "${(settings.screenScale * 150).toInt()}\"",
            onValueChange = onUpdateScale,
            valueRange = 0.6f..1.8f
        )

        // Virtual Depth Distance
        SliderItem(
            label = "Screen Distance",
            value = settings.screenDistance,
            valueDisplay = "${String.format("%.1f", settings.screenDistance)}m",
            onValueChange = onUpdateDistance,
            valueRange = 0.6f..2.0f
        )

        // IPD Offset (Stereoscopic eye separation)
        if (settings.isStereoCardboardMode) {
            SliderItem(
                label = "Eye Separation (IPD)",
                value = settings.ipdOffset,
                valueDisplay = "${settings.ipdOffset.toInt()} px",
                onValueChange = onUpdateIpd,
                valueRange = -30f..30f
            )
        }
    }
}

@Composable
private fun SliderItem(
    label: String,
    value: Float,
    valueDisplay: String,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(valueDisplay, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanNeon, fontFamily = FontFamily.Monospace)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = CyanNeon,
                activeTrackColor = CyanNeon,
                inactiveTrackColor = SurfaceCardHighlight
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
private fun AtmosphereTab(
    selected: VrEnvironment,
    onSelect: (VrEnvironment) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("SELECT VIRTUAL THEATER ENVIRONMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.sp)

        VrEnvironment.entries.forEach { env ->
            val isSelected = env == selected
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(env) }
                    .border(
                        width = if (isSelected) 1.5.dp else 0.5.dp,
                        color = if (isSelected) CyanNeon else SurfaceCardHighlight,
                        shape = RoundedCornerShape(10.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) SurfaceCardHighlight else SurfaceCard
                )
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) CyanNeon else TextSecondary)
                    )
                    Column {
                        Text(env.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(env.description, fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) CyanNeon.copy(alpha = 0.2f) else SurfaceCard)
            .border(1.dp, if (selected) CyanNeon else SurfaceCardHighlight, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) CyanNeon else TextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) CyanNeon else TextSecondary
            )
        }
    }
}

@Composable
private fun FullscreenFloatingPill(
    isStereo: Boolean,
    onExitFullscreen: () -> Unit,
    onRecenter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(Color(0xDD000000))
            .border(1.dp, CyanNeon.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isStereo) "VR SBS ACTIVE" else "FULLSCREEN VR ACTIVE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyanNeon,
            fontFamily = FontFamily.Monospace
        )

        IconButton(
            onClick = onRecenter,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = "Recenter", tint = TextPrimary, modifier = Modifier.size(18.dp))
        }

        Button(
            onClick = onExitFullscreen,
            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = ObsidianBlack),
            shape = RoundedCornerShape(20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
        ) {
            Icon(Icons.Default.FullscreenExit, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("EXIT VR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
