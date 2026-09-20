package com.example.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TrainingRepository
import com.example.service.OverlayStatusService
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
import com.example.util.SoundHapticManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: TrainingRepository,
    soundHapticManager: SoundHapticManager,
    isOverlayActive: Boolean,
    onToggleOverlay: (Boolean) -> Unit,
    isPcModeActive: Boolean,
    onTogglePcMode: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var soundEnabled by remember { mutableStateOf(soundHapticManager.soundEnabled) }
    var vibrationEnabled by remember { mutableStateOf(soundHapticManager.vibrationEnabled) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StealthBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "SETTINGS & COMPANION CONFIG",
                style = MaterialTheme.typography.titleLarge,
                color = CyberCyan,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Audio, haptics, fair-play verification & floating indicators",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Floating Overlay Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GAME STATUS & OVERLAY WIDGET",
                        style = MaterialTheme.typography.titleSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lightweight non-intrusive status pill displaying active companion state",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = MatrixGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Movable Floating Status Pill", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text("Displays ZERO SHOT: ACTIVE over apps", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = isOverlayActive,
                            onCheckedChange = { enable ->
                                if (enable) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        Toast.makeText(context, "Grant 'Display over other apps' permission to show overlay", Toast.LENGTH_LONG).show()
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    } else {
                                        onToggleOverlay(true)
                                        val serviceIntent = Intent(context, OverlayStatusService::class.java).apply {
                                            putExtra(OverlayStatusService.EXTRA_GAME_NAME, "Free Fire")
                                        }
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            context.startForegroundService(serviceIntent)
                                        } else {
                                            context.startService(serviceIntent)
                                        }
                                        Toast.makeText(context, "ZERO SHOT floating status active", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    onToggleOverlay(false)
                                    val serviceIntent = Intent(context, OverlayStatusService::class.java).apply {
                                        action = OverlayStatusService.ACTION_STOP_OVERLAY
                                    }
                                    context.startService(serviceIntent)
                                    Toast.makeText(context, "Floating status disabled", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StealthBlack,
                                checkedTrackColor = MatrixGreen
                            )
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = DarkCardBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = NeonOrange)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("PC / Wide Screen Layout Mode", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text("Adapts layout with side panel for keyboard & mouse players", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = isPcModeActive,
                            onCheckedChange = { onTogglePcMode(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StealthBlack,
                                checkedTrackColor = NeonOrange
                            )
                        )
                    }
                }
            }
        }

        // Sound & Haptics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FEEDBACK & PREFERENCES",
                        style = MaterialTheme.typography.titleSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = CyberCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Hit & Headshot Sound FX", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text("Audio feedback when target is struck", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                soundEnabled = it
                                soundHapticManager.soundEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = StealthBlack, checkedTrackColor = CyberCyan)
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = DarkCardBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = NeonOrange)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Tactical Haptic Vibration", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text("Vibrate device on headshot hits", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = {
                                vibrationEnabled = it
                                soundHapticManager.vibrationEnabled = it
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = StealthBlack, checkedTrackColor = NeonOrange)
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = DarkCardBorder)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLanguageDialog = true }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = GoldenRank)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Display Language", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text("Current: $selectedLanguage", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }
                        TextButton(onClick = { showLanguageDialog = true }) {
                            Text("CHANGE", color = CyberCyan)
                        }
                    }
                }
            }
        }

        // Privacy & Fair Play Information
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MatrixGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ZERO SHOT FAIR PLAY GUARANTEE", style = MaterialTheme.typography.titleSmall, color = MatrixGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ZERO SHOT is strictly a standalone training companion and aim simulator. It operates completely isolated from Free Fire and does NOT inject code, read or write game memory, intercept network packets, or modify APK assets. It complies 100% with esports fair play terms.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showPrivacyDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("VIEW FULL PRIVACY & COMPLIANCE DETAILS", color = TextPrimary)
                    }
                }
            }
        }

        // Data Reset Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, HeadshotRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = HeadshotRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RESET TRAINING DATA", style = MaterialTheme.typography.titleSmall, color = HeadshotRed, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Permanently wipe all session scores, personal bests, and custom preset logs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = HeadshotRed.copy(alpha = 0.2f), contentColor = HeadshotRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("WIPE ALL STATS & PRESETS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // About ZERO SHOT Branding Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ZERO SHOT",
                        style = MaterialTheme.typography.headlineSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "TRAIN • IMPROVE • DOMINATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonOrange,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Professional Gaming Companion Panel v1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        val languages = listOf("English", "Español (Spanish)", "Português (Portuguese)", "Bahasa Indonesia", "हिन्दी (Hindi)")
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language", color = TextPrimary) },
            text = {
                Column {
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = lang.split(" ").first()
                                    showLanguageDialog = false
                                    Toast.makeText(context, "Language set to $selectedLanguage", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lang, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("CLOSE", color = CyberCyan)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Safety, Privacy & Fair Play Policy", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = """
                            1. ZERO SHOT does NOT provide cheats, scripts, auto-headshot hooks, M-lock, wallhack, or aimbots.
                            2. ZERO SHOT runs as an external analytical companion. It never accesses game files or processes in memory.
                            3. Sensitivity and layout recommendations are applied manually by the player inside the game's official settings menu.
                            4. All training session statistics and custom presets are stored 100% locally on your device via an encrypted SQLite database. No personal data is transmitted to remote servers.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = StealthBlack)
                ) {
                    Text("I UNDERSTAND")
                }
            },
            containerColor = DarkSurface
        )
    }

    // Wipe Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Confirm Full Wipe", color = HeadshotRed, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to reset all training session history and custom presets? This cannot be undone.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            repository.resetAllTrainingData()
                            showResetDialog = false
                            Toast.makeText(context, "Training data cleared successfully", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HeadshotRed, contentColor = Color.White)
                ) {
                    Text("CONFIRM WIPE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
