package com.example.ui.screens.hud

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.HudLayoutEntity
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
fun HudGuideScreen(
    repository: TrainingRepository
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val savedLayouts by repository.allHudLayouts.collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: 2-Finger, 1: 3-Finger, 2: 4-Finger, 3: Custom Builder
    var showSaveDialog by remember { mutableStateOf(false) }
    var newLayoutName by remember { mutableStateOf("") }

    // Interactive Button Position State (normalized 0f..1f coordinates)
    var fireButtonX by remember { mutableFloatStateOf(0.82f) }
    var fireButtonY by remember { mutableFloatStateOf(0.74f) }
    var fireButtonSize by remember { mutableIntStateOf(45) }

    var glooWallX by remember { mutableFloatStateOf(0.22f) }
    var glooWallY by remember { mutableFloatStateOf(0.62f) }
    var glooWallSize by remember { mutableIntStateOf(85) }

    var jumpButtonX by remember { mutableFloatStateOf(0.90f) }
    var jumpButtonY by remember { mutableFloatStateOf(0.48f) }
    var jumpButtonSize by remember { mutableIntStateOf(65) }

    var crouchButtonX by remember { mutableFloatStateOf(0.78f) }
    var crouchButtonY by remember { mutableFloatStateOf(0.50f) }
    var crouchButtonSize by remember { mutableIntStateOf(65) }

    var scopeButtonX by remember { mutableFloatStateOf(0.88f) }
    var scopeButtonY by remember { mutableFloatStateOf(0.30f) }
    var scopeButtonSize by remember { mutableIntStateOf(70) }

    // When selecting a layout from preset tabs, apply preset values
    fun applyPreset(fingerType: Int) {
        val preset = savedLayouts.firstOrNull { it.fingerType == fingerType && it.isPreset }
        if (preset != null) {
            fireButtonX = preset.fireButtonX
            fireButtonY = preset.fireButtonY
            fireButtonSize = preset.fireButtonSize
            glooWallX = preset.glooWallX
            glooWallY = preset.glooWallY
            glooWallSize = preset.glooWallSize
            jumpButtonX = preset.jumpButtonX
            jumpButtonY = preset.jumpButtonY
            jumpButtonSize = preset.jumpButtonSize
            crouchButtonX = preset.crouchButtonX
            crouchButtonY = preset.crouchButtonY
            crouchButtonSize = preset.crouchButtonSize
            scopeButtonX = preset.scopeButtonX
            scopeButtonY = preset.scopeButtonY
            scopeButtonSize = preset.scopeButtonSize
        } else {
            when (fingerType) {
                2 -> {
                    fireButtonX = 0.82f; fireButtonY = 0.74f; fireButtonSize = 46
                    glooWallX = 0.22f; glooWallY = 0.62f; glooWallSize = 85
                    jumpButtonX = 0.90f; jumpButtonY = 0.48f; jumpButtonSize = 65
                    crouchButtonX = 0.78f; crouchButtonY = 0.50f; crouchButtonSize = 65
                    scopeButtonX = 0.88f; scopeButtonY = 0.30f; scopeButtonSize = 70
                }
                3 -> {
                    fireButtonX = 0.84f; fireButtonY = 0.74f; fireButtonSize = 42
                    glooWallX = 0.25f; glooWallY = 0.60f; glooWallSize = 90
                    jumpButtonX = 0.92f; jumpButtonY = 0.50f; jumpButtonSize = 70
                    crouchButtonX = 0.80f; crouchButtonY = 0.52f; crouchButtonSize = 70
                    scopeButtonX = 0.90f; scopeButtonY = 0.32f; scopeButtonSize = 75
                }
                4 -> {
                    fireButtonX = 0.85f; fireButtonY = 0.75f; fireButtonSize = 40
                    glooWallX = 0.22f; glooWallY = 0.58f; glooWallSize = 95
                    jumpButtonX = 0.86f; jumpButtonY = 0.18f; jumpButtonSize = 75
                    crouchButtonX = 0.78f; crouchButtonY = 0.52f; crouchButtonSize = 75
                    scopeButtonX = 0.92f; scopeButtonY = 0.36f; scopeButtonSize = 80
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StealthBlack)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "HUD & CUSTOM LAYOUT GUIDE",
                style = MaterialTheme.typography.titleLarge,
                color = CyberCyan,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Master claw grip ergonomics and fire button drag clearance",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Layout Selector Tabs
        item {
            val tabs = listOf("2-FINGER", "3-FINGER", "4-FINGER", "INTERACTIVE EDITOR")
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = DarkSurface,
                contentColor = CyberCyan,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = CyberCyan
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            if (index in 0..2) {
                                applyPreset(index + 2)
                            }
                        },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) CyberCyan else TextMuted
                            )
                        }
                    )
                }
            }
        }

        // HUD Interactive Canvas Preview
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, CyberCyan, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedTabIndex) {
                                0 -> "2-FINGER THUMB PREVIEW"
                                1 -> "3-FINGER CLAW PREVIEW"
                                2 -> "4-FINGER PRO CLAW PREVIEW"
                                else -> "CUSTOM HUD SIMULATOR (DRAG TO REPOSITION)"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        if (selectedTabIndex == 3) {
                            TextButton(onClick = { showSaveDialog = true }) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = MatrixGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SAVE LAYOUT", color = MatrixGreen, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Phone Screen Mockup Aspect Ratio 16:9
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF070A0F))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    ) {
                        val canvasW = constraints.maxWidth.toFloat()
                        val canvasH = constraints.maxHeight.toFloat()

                        // Background guides & safe zones
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Upward Drag Safe Zone highlight behind Fire button
                            val fireCx = fireButtonX * canvasW
                            val fireCy = fireButtonY * canvasH
                            drawLine(
                                color = CyberCyan.copy(alpha = 0.3f),
                                start = Offset(fireCx, fireCy),
                                end = Offset(fireCx, fireCy - 120f),
                                strokeWidth = 3f
                            )
                            drawCircle(
                                color = CyberCyan.copy(alpha = 0.15f),
                                radius = 70f,
                                center = Offset(fireCx, fireCy - 60f)
                            )

                            // Center crosshair reference
                            drawCircle(
                                color = Color.White.copy(alpha = 0.2f),
                                radius = 10f,
                                center = Offset(canvasW * 0.5f, canvasH * 0.5f)
                            )
                        }

                        // Fire Button (Draggable if tab 3)
                        HudDraggableElement(
                            name = "FIRE",
                            color = HeadshotRed,
                            sizeDp = (fireButtonSize * 0.9f).dp,
                            posX = fireButtonX,
                            posY = fireButtonY,
                            canvasW = canvasW,
                            canvasH = canvasH,
                            isDraggable = selectedTabIndex == 3,
                            onPositionChange = { nx, ny -> fireButtonX = nx; fireButtonY = ny }
                        )

                        // Gloo Wall Button
                        HudDraggableElement(
                            name = "GLOO",
                            color = CyberCyan,
                            sizeDp = (glooWallSize * 0.8f).dp,
                            posX = glooWallX,
                            posY = glooWallY,
                            canvasW = canvasW,
                            canvasH = canvasH,
                            isDraggable = selectedTabIndex == 3,
                            onPositionChange = { nx, ny -> glooWallX = nx; glooWallY = ny }
                        )

                        // Jump Button
                        HudDraggableElement(
                            name = "JUMP",
                            color = NeonOrange,
                            sizeDp = (jumpButtonSize * 0.8f).dp,
                            posX = jumpButtonX,
                            posY = jumpButtonY,
                            canvasW = canvasW,
                            canvasH = canvasH,
                            isDraggable = selectedTabIndex == 3,
                            onPositionChange = { nx, ny -> jumpButtonX = nx; jumpButtonY = ny }
                        )

                        // Crouch Button
                        HudDraggableElement(
                            name = "CROUCH",
                            color = GoldenRank,
                            sizeDp = (crouchButtonSize * 0.8f).dp,
                            posX = crouchButtonX,
                            posY = crouchButtonY,
                            canvasW = canvasW,
                            canvasH = canvasH,
                            isDraggable = selectedTabIndex == 3,
                            onPositionChange = { nx, ny -> crouchButtonX = nx; crouchButtonY = ny }
                        )

                        // Scope Button
                        HudDraggableElement(
                            name = "SCOPE",
                            color = MatrixGreen,
                            sizeDp = (scopeButtonSize * 0.8f).dp,
                            posX = scopeButtonX,
                            posY = scopeButtonY,
                            canvasW = canvasW,
                            canvasH = canvasH,
                            isDraggable = selectedTabIndex == 3,
                            onPositionChange = { nx, ny -> scopeButtonX = nx; scopeButtonY = ny }
                        )

                        // Analog Movement Joystick (Static display on left)
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = (canvasW * 0.12f).toInt(),
                                        y = (canvasH * 0.62f).toInt()
                                    )
                                }
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0x33475569))
                                .border(1.dp, Color(0x6694A3B8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("JOYSTICK", fontSize = 7.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Custom Size Slider Controls if Tab == 3 (Interactive Editor)
        if (selectedTabIndex == 3) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("FINE TUNE BUTTON SIZES", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))

                        HudSizeSliderRow("Fire Button Size: $fireButtonSize%", fireButtonSize) { fireButtonSize = it }
                        HudSizeSliderRow("Gloo Wall Button Size: $glooWallSize%", glooWallSize) { glooWallSize = it }
                        HudSizeSliderRow("Jump Button Size: $jumpButtonSize%", jumpButtonSize) { jumpButtonSize = it }
                        HudSizeSliderRow("Crouch Button Size: $crouchButtonSize%", crouchButtonSize) { crouchButtonSize = it }
                        HudSizeSliderRow("Scope Button Size: $scopeButtonSize%", scopeButtonSize) { scopeButtonSize = it }
                    }
                }
            }
        }

        // Fire-Button Size Guide
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GoldenRank, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FIRE-BUTTON SIZE & PLACEMENT GUIDE",
                            style = MaterialTheme.typography.titleSmall,
                            color = GoldenRank,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• Optimal Size (40% - 48%): Smaller fire buttons provide superior precision for One-Tap Headshots with Desert Eagle, M1887, and Woodpecker. Smaller surface area forces consistent touch centering, enabling smooth, uninterrupted upward drag swipes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Placement Sweet Spot: Position the Right Fire Button in the lower-right quadrant, leaving at least 35% of free vertical space above it. If placed too high, your thumb will run out of screen space before locking the headshot.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Button Position & Ergonomics Guide
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PanTool, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CLAW ERGONOMICS & GLOO WALL PLACEMENT",
                            style = MaterialTheme.typography.titleSmall,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• Fast Gloo Wall (360° Defense): Position the Gloo Wall button near your left thumb (or left index on claw). Sequence: Fire -> Tap Gloo -> Drag Downward -> Tap Left Fire Button. This creates instant protection in under 0.2s.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Jump & Crouch Separation: Keep Jump and Crouch at least 25dp apart to eliminate mis-clicks during intense jump-shot and slide-cancel routines.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    // Save Custom Layout Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Custom Layout", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Save this button layout to your custom presets.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newLayoutName,
                        onValueChange = { newLayoutName = it },
                        label = { Text("Layout Name") },
                        placeholder = { Text("e.g. My Fast Gloo Claw") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newLayoutName.isNotBlank()) {
                            val entity = HudLayoutEntity(
                                name = newLayoutName,
                                fingerType = if (selectedTabIndex in 0..2) selectedTabIndex + 2 else 3,
                                fireButtonSize = fireButtonSize,
                                fireButtonX = fireButtonX,
                                fireButtonY = fireButtonY,
                                leftFireButtonSize = 55,
                                leftFireButtonX = 0.18f,
                                leftFireButtonY = 0.25f,
                                glooWallSize = glooWallSize,
                                glooWallX = glooWallX,
                                glooWallY = glooWallY,
                                jumpButtonSize = jumpButtonSize,
                                jumpButtonX = jumpButtonX,
                                jumpButtonY = jumpButtonY,
                                crouchButtonSize = crouchButtonSize,
                                crouchButtonX = crouchButtonX,
                                crouchButtonY = crouchButtonY,
                                scopeButtonSize = scopeButtonSize,
                                scopeButtonX = scopeButtonX,
                                scopeButtonY = scopeButtonY,
                                analogJoystickSize = 80,
                                isPreset = false,
                                description = "Custom created layout"
                            )
                            coroutineScope.launch {
                                repository.saveHudLayout(entity)
                                showSaveDialog = false
                                newLayoutName = ""
                                Toast.makeText(context, "Custom HUD layout saved!", Toast.LENGTH_SHORT).show()
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
}

@Composable
fun HudDraggableElement(
    name: String,
    color: Color,
    sizeDp: androidx.compose.ui.unit.Dp,
    posX: Float,
    posY: Float,
    canvasW: Float,
    canvasH: Float,
    isDraggable: Boolean,
    onPositionChange: (Float, Float) -> Unit
) {
    val px = posX * canvasW
    val py = posY * canvasH

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (px - 25).toInt().coerceIn(0, (canvasW - 50).toInt()),
                    y = (py - 25).toInt().coerceIn(0, (canvasH - 50).toInt())
                )
            }
            .size(sizeDp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.35f))
            .border(2.dp, color, CircleShape)
            .pointerInput(isDraggable) {
                if (isDraggable) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newX = ((posX * canvasW + dragAmount.x) / canvasW).coerceIn(0.05f, 0.95f)
                        val newY = ((posY * canvasH + dragAmount.y) / canvasH).coerceIn(0.05f, 0.95f)
                        onPositionChange(newX, newY)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontSize = 9.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HudSizeSliderRow(title: String, value: Int, onValueChange: (Int) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 30f..100f,
            colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
        )
    }
}
