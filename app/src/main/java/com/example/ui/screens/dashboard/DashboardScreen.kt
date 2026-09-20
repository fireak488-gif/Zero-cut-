package com.example.ui.screens.dashboard

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TrainingRepository
import com.example.ui.screens.aimtrainer.DrillType
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GoldenRank
import com.example.ui.theme.HeadshotRed
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.StealthBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.DetectedGame
import com.example.util.GameDetectionHelper
import com.example.util.GameDetectionState

@Composable
fun DashboardScreen(
    repository: TrainingRepository,
    onNavigateToAimTrainer: (DrillType) -> Unit,
    onNavigateToSensitivity: () -> Unit,
    onNavigateToHudGuide: () -> Unit,
    onNavigateToWeapons: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val allSessions by repository.allSessions.collectAsState(initial = emptyList())
    val overallBest by repository.overallBestScore.collectAsState(initial = 0)
    val avgAccuracy by repository.averageAccuracy.collectAsState(initial = 0f)

    // Game Status Detection State
    var detectionState by remember {
        mutableStateOf(GameDetectionHelper.detectActiveGame(context))
    }
    val (isStandardInstalled, isMaxInstalled) = remember {
        GameDetectionHelper.checkInstalledGames(context)
    }
    val isGameInstalled = isStandardInstalled || isMaxInstalled
    val detectedGame = detectionState.detectedGame
    val isGameActive = detectedGame != DetectedGame.None

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StealthBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header with Gaming Branding & Game Detection Pill
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ZERO",
                                style = MaterialTheme.typography.headlineMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SHOT",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(NeonOrange.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .border(1.dp, NeonOrange, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("PANEL", fontSize = 10.sp, fontWeight = FontWeight.Black, color = NeonOrange)
                            }
                        }
                        Text(
                            text = "COMPANION & TRAINING SUITE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            letterSpacing = 2.sp
                        )
                    }

                    // Settings Shortcut
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Game Detection Status Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, if (detectedGame != null) MatrixGreen else DarkCardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isGameActive) MatrixGreen else NeonOrange)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isGameActive) "ACTIVE TARGET: ${detectedGame.displayName}" else "GAME STATUS: STANDBY",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isGameActive) MatrixGreen else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isGameActive) "Optimized companion telemetry active" else "Launch Free Fire or practice with drills",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = {
                                detectionState = GameDetectionHelper.detectActiveGame(context)
                                Toast.makeText(
                                    context,
                                    if (detectionState.detectedGame != DetectedGame.None) "Target Detected: ${detectionState.detectedGame.displayName}" else "No active game running. Ready for drills.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CyberCyan, modifier = Modifier.size(18.dp))
                        }

                        if (isGameInstalled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    val pkg = detectedGame.packageName ?: if (isMaxInstalled) GameDetectionHelper.PKG_FREE_FIRE_MAX else GameDetectionHelper.PKG_FREE_FIRE
                                    val launched = GameDetectionHelper.launchGame(context, pkg)
                                    if (!launched) {
                                        Toast.makeText(context, "Cannot open game automatically on this device", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "Launch Game", tint = GoldenRank, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Quick Training Action Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, CyberCyan, RoundedCornerShape(16.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(CyberCyan.copy(alpha = 0.12f), Color.Transparent)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Whatshot, contentDescription = null, tint = NeonOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "QUICK WARMUP ROUTINE",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "60-second high-intensity drag headshot drill before entering Ranked match",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onNavigateToAimTrainer(DrillType.HEADSHOT) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = StealthBlack),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("quick_warmup_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("START HEADSHOT WARMUP", fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // Telemetry Summary Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                        .clickable { onNavigateToStats() }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = GoldenRank, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BEST SCORE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${overallBest ?: 0} PTS",
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldenRank,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                        .clickable { onNavigateToStats() }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Adjust, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AVG ACCURACY", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${(avgAccuracy ?: 0f).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            color = CyberCyan,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Section Title: Training Modules
        item {
            Text(
                text = "CORE COMPANION MODULES",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // Module Card 1: AIM TRAINER
        item {
            ModuleActionCard(
                title = "1. AIM TRAINER DRILLS",
                subtitle = "Headshot drills, moving targets, reflex reaction test & drone tracking",
                icon = Icons.Default.Adjust,
                accentColor = CyberCyan,
                badgeText = "6 DRILL MODES",
                onClick = { onNavigateToAimTrainer(DrillType.HEADSHOT) }
            )
        }

        // Module Card 2: SENSITIVITY ASSISTANT
        item {
            ModuleActionCard(
                title = "2. SENSITIVITY ASSISTANT",
                subtitle = "Hardware auto-calculator (DPI, RAM, Hz), custom presets & comparisons",
                icon = Icons.Default.Tune,
                accentColor = NeonOrange,
                badgeText = "SMART TUNER",
                onClick = onNavigateToSensitivity
            )
        }

        // Module Card 3: HUD & LAYOUT GUIDE
        item {
            ModuleActionCard(
                title = "3. HUD & CUSTOM LAYOUT GUIDE",
                subtitle = "2, 3 & 4-finger claw setups, fire button size clearance & Gloo wall ergonomics",
                icon = Icons.Default.TouchApp,
                accentColor = MatrixGreen,
                badgeText = "CLAW GUIDE",
                onClick = onNavigateToHudGuide
            )
        }

        // Module Card 4: WEAPON TRAINING & RECOIL LAB
        item {
            ModuleActionCard(
                title = "4. WEAPON MASTERY & RECOIL LAB",
                subtitle = "Damage matrix, optimal ranges & interactive drag headshot curve simulator",
                icon = Icons.Default.Bolt,
                accentColor = HeadshotRed,
                badgeText = "DRAG CURVES",
                onClick = onNavigateToWeapons
            )
        }

        // Module Card 5: STAT TRACKER
        item {
            ModuleActionCard(
                title = "5. PLAYER STAT TRACKER",
                subtitle = "Session logs, progression charts, accuracy curves & reaction telemetry",
                icon = Icons.Default.Timeline,
                accentColor = GoldenRank,
                badgeText = "TELEMETRY",
                onClick = onNavigateToStats
            )
        }

        // Module Card 6: SETTINGS & COMPANION CONFIG
        item {
            ModuleActionCard(
                title = "6. SETTINGS & COMPANION CONFIG",
                subtitle = "Floating status pill overlay, audio feedback, language & fair play policy",
                icon = Icons.Default.Layers,
                accentColor = TextMuted,
                badgeText = "SETTINGS",
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
fun ModuleActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badgeText: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 9.sp,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
