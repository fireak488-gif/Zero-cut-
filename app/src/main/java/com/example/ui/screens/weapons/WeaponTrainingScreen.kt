package com.example.ui.screens.weapons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Weapon
import com.example.model.WeaponCatalog
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
import kotlin.math.sqrt

@Composable
fun WeaponTrainingScreen(
    onLaunchDrill: (DrillType) -> Unit
) {
    val weapons = WeaponCatalog.weapons
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedRangeFilter by remember { mutableStateOf("All Ranges") }
    var selectedWeapon by remember { mutableStateOf(weapons.first()) }

    val categories = listOf("All", "Assault Rifle", "SMG", "Shotgun", "Sniper", "Pistol")
    val ranges = listOf("All Ranges", "Close Range", "Mid Range", "Long Range")

    val filteredWeapons = weapons.filter { w ->
        val catMatches = selectedCategory == "All" || w.category == selectedCategory
        val rangeMatches = when (selectedRangeFilter) {
            "Close Range" -> w.optimalRange.contains("Close", ignoreCase = true) || w.optimalRange.contains("Point", ignoreCase = true)
            "Mid Range" -> w.optimalRange.contains("Mid", ignoreCase = true)
            "Long Range" -> w.optimalRange.contains("Long", ignoreCase = true)
            else -> true
        }
        catMatches && rangeMatches
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
                text = "WEAPON MASTERY & RECOIL LAB",
                style = MaterialTheme.typography.titleLarge,
                color = CyberCyan,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Stats, damage mechanics & interactive drag headshot curve practice",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Category Filter
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan,
                            selectedLabelColor = StealthBlack
                        )
                    )
                }
            }
        }

        // Range Mode Filter
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ranges) { r ->
                    FilterChip(
                        selected = selectedRangeFilter == r,
                        onClick = { selectedRangeFilter = r },
                        label = { Text(r, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonOrange,
                            selectedLabelColor = StealthBlack
                        )
                    )
                }
            }
        }

        // Weapon Selector Horizontal Grid
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredWeapons) { weapon ->
                    val isSelected = weapon.id == selectedWeapon.id
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) CyberCyan else DarkCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedWeapon = weapon }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = weapon.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isSelected) CyberCyan else TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = weapon.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Head: ${weapon.headshotDamage} dmg",
                                style = MaterialTheme.typography.labelSmall,
                                color = HeadshotRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Weapon Detailed Overview Card
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
                        Column {
                            Text(
                                text = selectedWeapon.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${selectedWeapon.category} • ${selectedWeapon.optimalRange}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberCyan
                            )
                        }

                        // Drag headshot technique badge
                        Box(
                            modifier = Modifier
                                .background(NeonOrange.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, NeonOrange, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = selectedWeapon.dragSpeed,
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Damage Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("HEADSHOT", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("${selectedWeapon.headshotDamage}", style = MaterialTheme.typography.titleLarge, color = HeadshotRed, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("BODY DMG", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("${selectedWeapon.bodyDamage}", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Black)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("MAGAZINE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("${selectedWeapon.magazine}", style = MaterialTheme.typography.titleLarge, color = CyberCyan, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bars for Rate of fire, Range, Reload speed, Accuracy
                    WeaponStatBar("Rate of Fire", selectedWeapon.rateOfFire, NeonOrange)
                    WeaponStatBar("Effective Range", selectedWeapon.range, CyberCyan)
                    WeaponStatBar("Accuracy Base", selectedWeapon.accuracy, MatrixGreen)
                    WeaponStatBar("Reload Speed", selectedWeapon.reloadSpeed, GoldenRank)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "RECOIL & DRAG BEHAVIOR",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedWeapon.recoilDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val drill = when (selectedWeapon.recommendedDrill) {
                                "Reaction Drill" -> DrillType.REACTION
                                "Tracking Drill" -> DrillType.TRACKING
                                "Flick Drill" -> DrillType.FLICK
                                "Accuracy Test" -> DrillType.ACCURACY
                                else -> DrillType.HEADSHOT
                            }
                            onLaunchDrill(drill)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = StealthBlack),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("LAUNCH RECOMMENDED DRILL (${selectedWeapon.recommendedDrill.uppercase()})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Interactive Recoil & Drag Curve Simulator
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, NeonOrange, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INTERACTIVE RECOIL DRAG SIMULATOR",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Trace the green target path from bottom to top to master the drag stroke.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    InteractiveDragCurveCanvas(dragType = selectedWeapon.dragType)
                }
            }
        }
    }
}

@Composable
fun WeaponStatBar(label: String, value: Int, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text("$value / 100", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = DarkSurfaceVariant
        )
    }
}

@Composable
fun InteractiveDragCurveCanvas(dragType: String) {
    val userPoints = remember { mutableStateListOf<Offset>() }
    var dragAccuracyPct by remember { mutableFloatStateOf(0f) }
    var hasCompletedStroke by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0A0F17))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
            .pointerInput(dragType) {
                detectDragGestures(
                    onDragStart = { offset ->
                        userPoints.clear()
                        userPoints.add(offset)
                        hasCompletedStroke = false
                    },
                    onDrag = { change, _ ->
                        userPoints.add(change.position)
                    },
                    onDragEnd = {
                        hasCompletedStroke = true
                        // Calculate score based on proximity to ideal path
                        dragAccuracyPct = calculateStrokeAccuracy(userPoints, dragType, size.width.toFloat(), size.height.toFloat())
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Draw Ideal Drag Path
            val idealPath = Path()
            when (dragType) {
                "J_DRAG" -> {
                    // Starts lower center, dips slightly right then swings sharply up
                    idealPath.moveTo(w * 0.5f, h * 0.85f)
                    idealPath.quadraticTo(w * 0.65f, h * 0.82f, w * 0.52f, h * 0.20f)
                }
                "QUICK_FLICK" -> {
                    // Sharp linear snap straight up
                    idealPath.moveTo(w * 0.5f, h * 0.85f)
                    idealPath.lineTo(w * 0.5f, h * 0.18f)
                }
                else -> {
                    // Straight Up Drag
                    idealPath.moveTo(w * 0.5f, h * 0.85f)
                    idealPath.lineTo(w * 0.5f, h * 0.18f)
                }
            }

            // Draw Guide Path
            drawPath(
                path = idealPath,
                color = MatrixGreen.copy(alpha = 0.45f),
                style = Stroke(width = 8f, cap = StrokeCap.Round)
            )

            // Draw Start & Target Hit points
            drawCircle(color = CyberCyan, radius = 12f, center = Offset(w * 0.5f, h * 0.85f))
            drawCircle(color = HeadshotRed, radius = 14f, center = Offset(w * 0.5f, h * 0.18f))

            // Draw User's Dragged Path
            if (userPoints.size > 1) {
                val userPath = Path()
                userPath.moveTo(userPoints.first().x, userPoints.first().y)
                for (i in 1 until userPoints.size) {
                    userPath.lineTo(userPoints[i].x, userPoints[i].y)
                }
                drawPath(
                    path = userPath,
                    color = if (hasCompletedStroke && dragAccuracyPct >= 75f) MatrixGreen else NeonOrange,
                    style = Stroke(width = 6f, cap = StrokeCap.Round)
                )
            }
        }

        // Overlay instructions / score
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            if (hasCompletedStroke) {
                Text(
                    text = "${dragAccuracyPct.toInt()}% ACCURACY",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (dragAccuracyPct >= 75f) MatrixGreen else NeonOrange,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = if (dragAccuracyPct >= 80f) "PERFECT HEADSHOT DRAG!" else "DRAG CLOSER TO PATH",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            } else {
                Text(
                    text = "SWIPE UP TO PRACTICE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

fun calculateStrokeAccuracy(points: List<Offset>, dragType: String, w: Float, h: Float): Float {
    if (points.size < 4) return 20f
    val start = points.first()
    val end = points.last()

    // Must start near bottom and end near top
    val startAcc = (1f - (sqrt((start.x - w * 0.5f) * (start.x - w * 0.5f) + (start.y - h * 0.85f) * (start.y - h * 0.85f)) / 150f)).coerceIn(0f, 1f)
    val endAcc = (1f - (sqrt((end.x - w * 0.5f) * (end.x - w * 0.5f) + (end.y - h * 0.18f) * (end.y - h * 0.18f)) / 150f)).coerceIn(0f, 1f)

    return ((startAcc * 0.5f + endAcc * 0.5f) * 100f).coerceIn(35f, 98f)
}
