package com.example.ui.screens.sensitivity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SensitivityPresetEntity
import com.example.data.repository.TrainingRepository
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
import kotlinx.coroutines.launch

@Composable
fun SensitivityScreen(
    repository: TrainingRepository
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val presets by repository.allSensitivityPresets.collectAsState(initial = emptyList())

    // Active Sensitivity Sliders
    var general by remember { mutableIntStateOf(95) }
    var redDot by remember { mutableIntStateOf(90) }
    var scope2x by remember { mutableIntStateOf(86) }
    var scope4x by remember { mutableIntStateOf(82) }
    var sniperScope by remember { mutableIntStateOf(58) }
    var freeLook by remember { mutableIntStateOf(68) }

    var selectedPresetName by remember { mutableStateOf("Custom Configuration") }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showCompareDialog by remember { mutableStateOf(false) }
    var comparePreset by remember { mutableStateOf<SensitivityPresetEntity?>(null) }
    var showRecommendationGenerator by remember { mutableStateOf(false) }

    // Recommendation Inputs
    var deviceCategory by remember { mutableStateOf("Mid-Range (4-6GB RAM)") }
    var refreshRate by remember { mutableStateOf("90Hz") }
    var playStyle by remember { mutableStateOf("One-Tap Drag (Rusher)") }
    var selectedDpi by remember { mutableIntStateOf(411) }

    // Dialog Input
    var newPresetName by remember { mutableStateOf("") }
    var newPresetNotes by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StealthBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header with Security Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SENSITIVITY ASSISTANT",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberCyan,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Fine-tune Free Fire sensitivity with smart device analytics",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // Compliance Notice Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.border(1.dp, Color(0x3300E5FF), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MatrixGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "FAIR PLAY VERIFIED: ZERO SHOT does not modify game files, memory, or APKs. Manually apply these values in Free Fire Settings > Sensitivity.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Preset Quick Selector
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAVED PRESETS",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                    TextButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SAVE CURRENT", color = CyberCyan, style = MaterialTheme.typography.labelMedium)
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets) { preset ->
                        val isCurrent = preset.name == selectedPresetName
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) DarkSurfaceVariant else DarkSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .border(
                                    width = if (isCurrent) 1.5.dp else 1.dp,
                                    color = if (isCurrent) CyberCyan else DarkCardBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    general = preset.general
                                    redDot = preset.redDot
                                    scope2x = preset.scope2x
                                    scope4x = preset.scope4x
                                    sniperScope = preset.sniperScope
                                    freeLook = preset.freeLook
                                    selectedPresetName = preset.name
                                }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isCurrent) CyberCyan else TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (preset.isDefault) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "PRO",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoldenRank,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                                Text(
                                    text = preset.deviceCategory,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Gen: ${preset.general} • Red: ${preset.redDot}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Smart Recommendation Generator Toggle
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonOrange.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "DEVICE RECOMMENDATION ASSISTANT",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Calculate optimal values based on your hardware specs",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                        TextButton(onClick = { showRecommendationGenerator = !showRecommendationGenerator }) {
                            Text(
                                text = if (showRecommendationGenerator) "HIDE" else "CALCULATE",
                                color = NeonOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    AnimatedVisibility(visible = showRecommendationGenerator) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Divider(color = DarkCardBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Device Category:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val categories = listOf("Low-End (2-4GB)", "Mid-Range (4-6GB)", "Flagship (8GB+)", "Tablet / iPad", "PC Emulator")
                                items(categories) { cat ->
                                    FilterChip(
                                        selected = deviceCategory == cat,
                                        onClick = { deviceCategory = cat },
                                        label = { Text(cat, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonOrange,
                                            selectedLabelColor = StealthBlack
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Refresh Rate:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val rates = listOf("60Hz", "90Hz", "120Hz", "144Hz+")
                                items(rates) { rate ->
                                    FilterChip(
                                        selected = refreshRate == rate,
                                        onClick = { refreshRate = rate },
                                        label = { Text(rate, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CyberCyan,
                                            selectedLabelColor = StealthBlack
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Play Style:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val styles = listOf("One-Tap Drag (Rusher)", "Balanced (CS/Ranked)", "Sniper Headhunter")
                                items(styles) { style ->
                                    FilterChip(
                                        selected = playStyle == style,
                                        onClick = { playStyle = style },
                                        label = { Text(style, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MatrixGreen,
                                            selectedLabelColor = StealthBlack
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    // Intelligent algorithm based on device physics
                                    val rec = calculateRecommendedSensitivity(deviceCategory, refreshRate, playStyle)
                                    general = rec.general
                                    redDot = rec.redDot
                                    scope2x = rec.scope2x
                                    scope4x = rec.scope4x
                                    sniperScope = rec.sniperScope
                                    freeLook = rec.freeLook
                                    selectedPresetName = "Auto-Tuned ($deviceCategory)"
                                    Toast.makeText(context, "Calculated optimized settings for $deviceCategory!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonOrange, contentColor = StealthBlack),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("APPLY OPTIMIZED VALUES", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }

        // Active Sensitivity Sliders Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, DarkCardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CURRENT SENSITIVITY PANEL",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            IconButton(onClick = {
                                if (presets.isNotEmpty()) {
                                    comparePreset = presets.firstOrNull { it.name != selectedPresetName } ?: presets.first()
                                    showCompareDialog = true
                                }
                            }) {
                                Icon(Icons.Default.CompareArrows, contentDescription = "Compare Presets", tint = CyberCyan)
                            }
                            IconButton(onClick = {
                                val text = """
                                    [ZERO SHOT SENSITIVITY CONFIG]
                                    Preset: $selectedPresetName
                                    General: $general
                                    Red Dot: $redDot
                                    2x Scope: $scope2x
                                    4x Scope: $scope4x
                                    Sniper Scope: $sniperScope
                                    Free Look: $freeLook
                                """.trimIndent()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("ZERO SHOT Sensitivity", text))
                                Toast.makeText(context, "Copied! Paste or enter into Free Fire settings.", Toast.LENGTH_LONG).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Values", tint = MatrixGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SensitivitySliderRow("General Sensitivity", general, "Affects overall screen camera rotation speed & drag headshot initiation") { general = it }
                    SensitivitySliderRow("Red Dot", redDot, "Directly governs crosshair drag flick when aiming down sights without scope") { redDot = it }
                    SensitivitySliderRow("2x Scope", scope2x, "For mid-range rifles (SCAR, XM8, MP5-X)") { scope2x = it }
                    SensitivitySliderRow("4x Scope", scope4x, "For long-range ARs and DMRs (M4A1, SVD, Woodpecker)") { scope4x = it }
                    SensitivitySliderRow("Sniper Scope", sniperScope, "AWM & Kar98k precision tracking speed") { sniperScope = it }
                    SensitivitySliderRow("Free Look", freeLook, "Camera swivel speed while sprinting or driving") { freeLook = it }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val text = """
                                [ZERO SHOT SENSITIVITY]
                                General: $general
                                Red Dot: $redDot
                                2x Scope: $scope2x
                                4x Scope: $scope4x
                                Sniper Scope: $sniperScope
                                Free Look: $freeLook
                            """.trimIndent()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("ZERO SHOT Sensitivity", text))
                            Toast.makeText(context, "Values copied! Enter Free Fire > Settings > Sensitivity to apply.", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = StealthBlack),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("copy_sensitivity_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("COPY CONFIGURATION FOR FREE FIRE", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Custom Preset", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Save your current sensitivity settings to your local profile.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        label = { Text("Preset Name") },
                        placeholder = { Text("e.g. My Ranked Setup") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPresetNotes,
                        onValueChange = { newPresetNotes = it },
                        label = { Text("Notes (optional)") },
                        placeholder = { Text("e.g. For MP40 & M1887 rusher") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            val entity = SensitivityPresetEntity(
                                name = newPresetName,
                                deviceCategory = "Custom",
                                general = general,
                                redDot = redDot,
                                scope2x = scope2x,
                                scope4x = scope4x,
                                sniperScope = sniperScope,
                                freeLook = freeLook,
                                notes = newPresetNotes
                            )
                            coroutineScope.launch {
                                repository.saveSensitivityPreset(entity)
                                selectedPresetName = newPresetName
                                showSaveDialog = false
                                newPresetName = ""
                                newPresetNotes = ""
                                Toast.makeText(context, "Preset saved!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = StealthBlack)
                ) {
                    Text("SAVE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Compare Dialog
    if (showCompareDialog && comparePreset != null) {
        AlertDialog(
            onDismissRequest = { showCompareDialog = false },
            title = { Text("Compare Presets", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                val target = comparePreset!!
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Setting", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                        Text("Current", style = MaterialTheme.typography.labelMedium, color = CyberCyan)
                        Text(target.name.take(10), style = MaterialTheme.typography.labelMedium, color = GoldenRank)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = DarkCardBorder)

                    CompareItemRow("General", general, target.general)
                    CompareItemRow("Red Dot", redDot, target.redDot)
                    CompareItemRow("2x Scope", scope2x, target.scope2x)
                    CompareItemRow("4x Scope", scope4x, target.scope4x)
                    CompareItemRow("Sniper", sniperScope, target.sniperScope)
                    CompareItemRow("Free Look", freeLook, target.freeLook)
                }
            },
            confirmButton = {
                TextButton(onClick = { showCompareDialog = false }) {
                    Text("CLOSE", color = CyberCyan)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun SensitivitySliderRow(
    title: String,
    value: Int,
    description: String,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleMedium,
                color = CyberCyan,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = CyberCyan,
                activeTrackColor = CyberCyan,
                inactiveTrackColor = DarkSurfaceVariant
            )
        )
    }
}

@Composable
fun CompareItemRow(label: String, currentVal: Int, compareVal: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Text("$currentVal", style = MaterialTheme.typography.bodyMedium, color = CyberCyan, fontWeight = FontWeight.Bold)
        val diff = currentVal - compareVal
        val diffText = if (diff > 0) "+$diff" else "$diff"
        Text(
            "$compareVal ($diffText)",
            style = MaterialTheme.typography.bodyMedium,
            color = if (diff == 0) TextMuted else GoldenRank
        )
    }
}

data class RecommendedSensitivity(
    val general: Int,
    val redDot: Int,
    val scope2x: Int,
    val scope4x: Int,
    val sniperScope: Int,
    val freeLook: Int
)

// Device Recommendation Algorithm
fun calculateRecommendedSensitivity(
    category: String,
    refreshRate: String,
    playStyle: String
): RecommendedSensitivity {
    var general = 95
    var redDot = 90
    var scope2x = 86
    var scope4x = 82
    var sniper = 58
    var freeLook = 68

    // Hardware adjustment: low end needs higher sensitivity to compensate for touch polling lag
    if (category.contains("Low-End")) {
        general = 100
        redDot = 100
        scope2x = 96
        scope4x = 92
        sniper = 72
        freeLook = 85
    } else if (category.contains("PC Emulator")) {
        general = 75
        redDot = 70
        scope2x = 65
        scope4x = 60
        sniper = 42
        freeLook = 50
    } else if (category.contains("Tablet") || category.contains("iPad")) {
        general = 88
        redDot = 84
        scope2x = 80
        scope4x = 76
        sniper = 52
        freeLook = 60
    }

    // Refresh rate adjustment: High Hz allows tighter micro-adjustments
    if (refreshRate == "120Hz" || refreshRate == "144Hz+") {
        general = (general - 3).coerceAtLeast(60)
        redDot = (redDot - 2).coerceAtLeast(60)
    }

    // Play Style adjustment
    if (playStyle.contains("One-Tap")) {
        general = (general + 4).coerceAtMost(100)
        redDot = (redDot + 5).coerceAtMost(100)
    } else if (playStyle.contains("Sniper")) {
        sniper = (sniper - 10).coerceAtLeast(35)
        scope4x = (scope4x - 5).coerceAtLeast(50)
    }

    return RecommendedSensitivity(general, redDot, scope2x, scope4x, sniper, freeLook)
}
