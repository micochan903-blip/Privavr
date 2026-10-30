package com.example.ui.vr

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VirtualAppType
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PrivacyGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardHighlight
import com.example.ui.theme.SurfaceOledDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Interactive virtual phone interface rendered on the huge VR screen.
 * When real MediaProjection is active, this displays the live mirrored device screen.
 * When idle/preview, this displays a rich interactive OS interface where users
 * can launch sample apps, test VR scaling, inspect privacy telemetry, or trigger
 * full live screen mirroring with one tap!
 */
@Composable
fun VirtualScreenContent(
    currentApp: VirtualAppType,
    isLiveMirrorActive: Boolean,
    onAppSelect: (VirtualAppType) -> Unit,
    onRequestLiveMirror: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf("") }
    var uptimeSeconds by remember { mutableLongStateOf(42L) }

    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        while (true) {
            currentTime = sdf.format(Date())
            delay(1000L)
            uptimeSeconds++
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceOledDark)
    ) {
        // Virtual Phone Status Bar
        VirtualStatusBar(
            currentTime = currentTime,
            isLive = isLiveMirrorActive,
            modifier = Modifier.fillMaxWidth()
        )

        // Main Virtual App Body
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                isLiveMirrorActive -> {
                    LiveMirrorActiveBanner(
                        onRequestStop = onRequestLiveMirror,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                currentApp == VirtualAppType.SYSTEM_HOME -> {
                    VirtualHomeScreen(
                        onRequestLiveMirror = onRequestLiveMirror,
                        onAppSelect = onAppSelect,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                currentApp == VirtualAppType.BROWSER -> {
                    VirtualBrowserApp(
                        onBack = { onAppSelect(VirtualAppType.SYSTEM_HOME) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                currentApp == VirtualAppType.MEDIA_PLAYER -> {
                    VirtualCinemaPlayer(
                        onBack = { onAppSelect(VirtualAppType.SYSTEM_HOME) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                currentApp == VirtualAppType.SYSTEM_STATS -> {
                    VirtualPrivacyMonitor(
                        uptimeSeconds = uptimeSeconds,
                        onBack = { onAppSelect(VirtualAppType.SYSTEM_HOME) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                currentApp == VirtualAppType.GALLERY -> {
                    VirtualGalleryApp(
                        onBack = { onAppSelect(VirtualAppType.SYSTEM_HOME) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                currentApp == VirtualAppType.NOTES -> {
                    VirtualNotesApp(
                        onBack = { onAppSelect(VirtualAppType.SYSTEM_HOME) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Virtual Phone Bottom Navigation Bar
        VirtualNavigationBar(
            onBack = { onAppSelect(VirtualAppType.SYSTEM_HOME) },
            onHome = { onAppSelect(VirtualAppType.SYSTEM_HOME) },
            onRecents = { onAppSelect(VirtualAppType.SYSTEM_STATS) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun VirtualStatusBar(
    currentTime: String,
    isLive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF020408))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (currentTime.isNotEmpty()) currentTime else "12:00",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (isLive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PrivacyGreen)
                )
                Text(
                    text = "LIVE VR CAST",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrivacyGreen,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Zero Network",
                tint = PrivacyGreen,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "0 KB/s OFFLINE",
                fontSize = 10.sp,
                color = PrivacyGreen,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "RAM Encrypted",
                tint = CyanNeon,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "100%",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun VirtualHomeScreen(
    onRequestLiveMirror: () -> Unit,
    onAppSelect: (VirtualAppType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Big Screen VR Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Brush.horizontalGradient(listOf(CyanNeon, ElectricAzure)), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Real Phone Screen Mirror",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Project your actual phone display in real-time onto this giant VR screen while your phone runs normally.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRequestLiveMirror,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = ObsidianBlack),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("start_mirror_button")
                ) {
                    Icon(Icons.Default.CastConnected, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("START PHONE MIRRORING", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Virtual Apps Grid
        Text(
            text = "VR PHONE APPS & EXPERIENCES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        val appList = listOf(
            AppItem(VirtualAppType.BROWSER, "Private Web", Icons.Default.Language, CyanNeon),
            AppItem(VirtualAppType.MEDIA_PLAYER, "VR Cinema", Icons.Default.PlayCircle, ElectricAzure),
            AppItem(VirtualAppType.GALLERY, "Photo Wall", Icons.Default.Photo, Color(0xFFE040FB)),
            AppItem(VirtualAppType.NOTES, "Encrypted Pad", Icons.Default.Description, WarningAmber),
            AppItem(VirtualAppType.SYSTEM_STATS, "Zero-Leak Audit", Icons.Default.Security, PrivacyGreen)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(appList) { app ->
                VirtualAppCard(
                    app = app,
                    onClick = { onAppSelect(app.type) }
                )
            }
        }
    }
}

private data class AppItem(
    val type: VirtualAppType,
    val title: String,
    val icon: ImageVector,
    val tint: Color
)

@Composable
private fun VirtualAppCard(
    app: AppItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(1.dp, SurfaceCardHighlight, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(app.tint.copy(alpha = 0.15f))
                    .border(1.dp, app.tint.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = app.icon,
                    contentDescription = app.title,
                    tint = app.tint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = app.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LiveMirrorActiveBanner(
    onRequestStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Brush.verticalGradient(listOf(Color(0xFF040A14), Color(0xFF081224))))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(PrivacyGreen.copy(alpha = 0.2f))
                    .border(2.dp, PrivacyGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CastConnected,
                    contentDescription = null,
                    tint = PrivacyGreen,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "Live Phone Mirroring Active",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Your phone's entire screen is streaming directly to this virtual VR cinema. Use other apps or swipe home — your screen stays in VR!",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(PrivacyGreen)
                )
                Text(
                    text = "60 FPS • 0 ms Network Lag • 100% Offline GPU Pipe",
                    fontSize = 11.sp,
                    color = PrivacyGreen,
                    fontFamily = FontFamily.Monospace
                )
            }

            Button(
                onClick = onRequestStop,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("DISCONNECT MIRRORING", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun VirtualBrowserApp(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(SurfaceOledDark)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = PrivacyGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "https://private.local/cinema",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            IconButton(onClick = { /* Refresh */ }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Giant Screen Web Experience",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Reading articles, watching online streams, and browsing documentation in virtual reality gives you a panoramic field of view that eases eye fatigue.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickWebBadge("Zero Cookies", PrivacyGreen)
                    QuickWebBadge("Strict Sandboxing", CyanNeon)
                    QuickWebBadge("Direct Hardware Rendering", ElectricAzure)
                }
            }
        }
    }
}

@Composable
private fun QuickWebBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun VirtualCinemaPlayer(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF020408))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "VR IMAX Player (120\" Projection)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black)
                .border(1.dp, CyanNeon.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = "Play",
                    tint = CyanNeon,
                    modifier = Modifier.size(56.dp)
                )
                Text(
                    text = "AURA VR: Interstellar Deep Voyage",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Spatial Audio Active • DCI-P3 Color Gamut",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { 0.42f },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = CyanNeon,
            trackColor = SurfaceCardHighlight
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("01:14:20", fontSize = 10.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
            Text("02:48:15", fontSize = 10.sp, color = TextSecondary, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun VirtualPrivacyMonitor(
    uptimeSeconds: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(SurfaceOledDark)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Hardware & Privacy Telemetry",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrivacyGreen
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val telemetryStats = listOf(
            "Network Egress" to "0 Bytes (No internet permission)",
            "Frame Pipeline" to "Local GPU SurfaceTexture (Direct Pipe)",
            "Storage Persistence" to "0 KB Disk (RAM-only volatile buffers)",
            "Sensor Frequency" to "100 Hz Low-Latency Gyro Vector",
            "Session Uptime" to "${uptimeSeconds}s (Auto-purged on exit)"
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            telemetryStats.forEach { (label, value) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, fontSize = 12.sp, color = TextSecondary)
                        Text(
                            value,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrivacyGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VirtualGalleryApp(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(SurfaceOledDark)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Private Media Wall",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.radialGradient(listOf(Color(0xFF311B92), Color(0xFF000000)))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Photo, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Panoramic 360 Vista", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Rendered locally inside VR virtual space", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun VirtualNotesApp(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(SurfaceOledDark)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Encrypted VR Scratchpad",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = WarningAmber
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "• Private VR Session Started\n• Shoulder-surfing protected via VR headset isolation\n• Physical screen blackout mode enabled",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 22.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun VirtualNavigationBar(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onRecents: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color(0xFF020408))
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onHome) {
            Icon(Icons.Default.Circle, contentDescription = "Home", tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
        IconButton(onClick = onRecents) {
            Icon(Icons.Default.Shield, contentDescription = "Security Recents", tint = PrivacyGreen, modifier = Modifier.size(16.dp))
        }
    }
}
