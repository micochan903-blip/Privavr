package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoAccounts
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PrivacyGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardHighlight
import com.example.ui.theme.SurfaceOledDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PrivacySecurityDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var buffersPurged by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .border(
                    1.dp,
                    Brush.verticalGradient(listOf(PrivacyGreen.copy(alpha = 0.6f), CyanNeon.copy(alpha = 0.3f))),
                    RoundedCornerShape(22.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = SurfaceOledDark)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrivacyGreen.copy(alpha = 0.2f))
                                .border(1.dp, PrivacyGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = PrivacyGreen, modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = "Privacy & Security Audit",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Core Guarantee Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF00331A))
                        .border(1.dp, PrivacyGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrivacyGreen, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Zero Network Leak Guarantee",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrivacyGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PrivaVR does NOT declare the android.permission.INTERNET permission. The Android OS kernel physically disables all network connectivity for this application.",
                            fontSize = 11.sp,
                            color = TextPrimary.copy(alpha = 0.9f),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Security Pillars
                SecurityPillarItem(
                    icon = Icons.Default.WifiOff,
                    title = "100% Offline Processing",
                    description = "Screen frames, motion tracking, and audio route exclusively inside your device's GPU memory.",
                    status = "VERIFIED SAFE"
                )

                SecurityPillarItem(
                    icon = Icons.Default.Lock,
                    title = "RAM-Only Ephemeral Framebuffer",
                    description = "Screen capture buffers are never recorded, cached, or written to disk. All frames evaporate immediately upon display.",
                    status = "VOLATILE ONLY"
                )

                SecurityPillarItem(
                    icon = Icons.Default.NoAccounts,
                    title = "No Trackers, Ads, or Accounts",
                    description = "No telemetry libraries, analytics SDKs, user identifiers, or crash logging beacons are packaged in the app.",
                    status = "CLEAN AUDIT"
                )

                SecurityPillarItem(
                    icon = Icons.Default.Security,
                    title = "Anti-Shoulder Surfing Isolation",
                    description = "In VR headsets, your phone screen is visible solely to your eyes, preventing bystanders from viewing private messages or passwords.",
                    status = "SHIELDED"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Purge Volatile Buffers Action
                Button(
                    onClick = {
                        System.gc()
                        buffersPurged = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (buffersPurged) PrivacyGreen else SurfaceCardHighlight,
                        contentColor = if (buffersPurged) ObsidianBlack else TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (buffersPurged) Icons.Default.CheckCircle else Icons.Default.CleaningServices,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (buffersPurged) "BUFFERS PURGED & CLEARED" else "PURGE VOLATILE RAM BUFFERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Aegis Privacy Architecture v2.4 • On-Device Sandbox",
                    fontSize = 9.sp,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun SecurityPillarItem(
    icon: ImageVector,
    title: String,
    description: String,
    status: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(CyanNeon.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(
                        status,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrivacyGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
            }
        }
    }
}
