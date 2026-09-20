package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.TrainingRepository
import com.example.ui.screens.aimtrainer.AimTrainerScreen
import com.example.ui.screens.aimtrainer.DrillType
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.hud.HudGuideScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.sensitivity.SensitivityScreen
import com.example.ui.screens.stats.StatsScreen
import com.example.ui.screens.weapons.WeaponTrainingScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.StealthBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SoundHapticManager

enum class AppDestination(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
    AIM_TRAINER("Aim Trainer", Icons.Default.Adjust, "nav_aim_trainer"),
    SENSITIVITY("Sensitivity", Icons.Default.Tune, "nav_sensitivity"),
    HUD_GUIDE("HUD Guide", Icons.Default.TouchApp, "nav_hud_guide"),
    WEAPONS("Weapons", Icons.Default.Bolt, "nav_weapons"),
    STATS("Stats", Icons.Default.Timeline, "nav_stats"),
    SETTINGS("Settings", Icons.Default.Settings, "nav_settings")
}

@Composable
fun MainAppScreen(
    repository: TrainingRepository,
    soundHapticManager: SoundHapticManager
) {
    var currentDestination by remember { mutableStateOf(AppDestination.DASHBOARD) }
    var selectedDrillType by remember { mutableStateOf(DrillType.HEADSHOT) }

    var isOverlayActive by remember { mutableStateOf(false) }
    var isPcModeActive by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(StealthBlack)) {
        val isWideScreen = maxWidth >= 700.dp || isPcModeActive

        if (isWideScreen) {
            // PC / Tablet / Landscape Layout with Navigation Rail on Left
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = "ZERO",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "SHOT",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(NeonOrange.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .border(1.dp, NeonOrange, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("PC PRO", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = NeonOrange)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .border(width = 1.dp, color = DarkCardBorder)
                ) {
                    AppDestination.values().forEach { destination ->
                        NavigationRailItem(
                            selected = currentDestination == destination,
                            onClick = { currentDestination = destination },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = destination.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (currentDestination == destination) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = StealthBlack,
                                selectedTextColor = CyberCyan,
                                indicatorColor = CyberCyan,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag(destination.testTag)
                        )
                    }
                }

                // Main Content for Wide Screen
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    ScreenContent(
                        destination = currentDestination,
                        selectedDrill = selectedDrillType,
                        repository = repository,
                        soundHapticManager = soundHapticManager,
                        isOverlayActive = isOverlayActive,
                        onToggleOverlay = { isOverlayActive = it },
                        isPcModeActive = isPcModeActive,
                        onTogglePcMode = { isPcModeActive = it },
                        onNavigate = { dest -> currentDestination = dest },
                        onSelectDrillAndNavigate = { drill ->
                            selectedDrillType = drill
                            currentDestination = AppDestination.AIM_TRAINER
                        }
                    )
                }
            }
        } else {
            // Mobile Compact Layout with Bottom Navigation Bar
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = DarkSurface,
                        contentColor = TextPrimary,
                        tonalElevation = 8.dp,
                        modifier = Modifier.border(1.dp, DarkCardBorder)
                    ) {
                        // Display the 5 most frequent mobile destinations
                        val bottomNavItems = listOf(
                            AppDestination.DASHBOARD,
                            AppDestination.AIM_TRAINER,
                            AppDestination.SENSITIVITY,
                            AppDestination.WEAPONS,
                            AppDestination.STATS
                        )

                        bottomNavItems.forEach { destination ->
                            val isSelected = currentDestination == destination
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.title,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = StealthBlack,
                                    selectedTextColor = CyberCyan,
                                    indicatorColor = CyberCyan,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted
                                ),
                                modifier = Modifier.testTag(destination.testTag)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ScreenContent(
                        destination = currentDestination,
                        selectedDrill = selectedDrillType,
                        repository = repository,
                        soundHapticManager = soundHapticManager,
                        isOverlayActive = isOverlayActive,
                        onToggleOverlay = { isOverlayActive = it },
                        isPcModeActive = isPcModeActive,
                        onTogglePcMode = { isPcModeActive = it },
                        onNavigate = { dest -> currentDestination = dest },
                        onSelectDrillAndNavigate = { drill ->
                            selectedDrillType = drill
                            currentDestination = AppDestination.AIM_TRAINER
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenContent(
    destination: AppDestination,
    selectedDrill: DrillType,
    repository: TrainingRepository,
    soundHapticManager: SoundHapticManager,
    isOverlayActive: Boolean,
    onToggleOverlay: (Boolean) -> Unit,
    isPcModeActive: Boolean,
    onTogglePcMode: (Boolean) -> Unit,
    onNavigate: (AppDestination) -> Unit,
    onSelectDrillAndNavigate: (DrillType) -> Unit
) {
    when (destination) {
        AppDestination.DASHBOARD -> {
            DashboardScreen(
                repository = repository,
                onNavigateToAimTrainer = { drill -> onSelectDrillAndNavigate(drill) },
                onNavigateToSensitivity = { onNavigate(AppDestination.SENSITIVITY) },
                onNavigateToHudGuide = { onNavigate(AppDestination.HUD_GUIDE) },
                onNavigateToWeapons = { onNavigate(AppDestination.WEAPONS) },
                onNavigateToStats = { onNavigate(AppDestination.STATS) },
                onNavigateToSettings = { onNavigate(AppDestination.SETTINGS) }
            )
        }
        AppDestination.AIM_TRAINER -> {
            AimTrainerScreen(
                initialDrillType = selectedDrill,
                repository = repository,
                soundHapticManager = soundHapticManager,
                onNavigateBack = { onNavigate(AppDestination.DASHBOARD) }
            )
        }
        AppDestination.SENSITIVITY -> {
            SensitivityScreen(
                repository = repository
            )
        }
        AppDestination.HUD_GUIDE -> {
            HudGuideScreen(
                repository = repository
            )
        }
        AppDestination.WEAPONS -> {
            WeaponTrainingScreen(
                onLaunchDrill = { drill -> onSelectDrillAndNavigate(drill) }
            )
        }
        AppDestination.STATS -> {
            StatsScreen(
                repository = repository
            )
        }
        AppDestination.SETTINGS -> {
            SettingsScreen(
                repository = repository,
                soundHapticManager = soundHapticManager,
                isOverlayActive = isOverlayActive,
                onToggleOverlay = onToggleOverlay,
                isPcModeActive = isPcModeActive,
                onTogglePcMode = onTogglePcMode
            )
        }
    }
}
