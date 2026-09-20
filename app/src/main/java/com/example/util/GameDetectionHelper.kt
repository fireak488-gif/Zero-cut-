package com.example.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

sealed class DetectedGame(val displayName: String, val packageName: String?, val isMax: Boolean) {
    object None : DetectedGame("No supported game detected", null, false)
    object FreeFireStandard : DetectedGame("Free Fire", "com.dts.freefireth", false)
    object FreeFireMax : DetectedGame("Free Fire MAX", "com.dts.freefiremax", true)
}

data class GameDetectionState(
    val detectedGame: DetectedGame = DetectedGame.None,
    val isCompanionActive: Boolean = true,
    val isTrainingModeReady: Boolean = true,
    val isStandardInstalled: Boolean = false,
    val isMaxInstalled: Boolean = false,
    val hasUsageStatsPermission: Boolean = false,
    val simulatedStatusActive: Boolean = false,
    val lastCheckedTimestamp: Long = System.currentTimeMillis()
)

object GameDetectionHelper {
    const val PKG_FREE_FIRE = "com.dts.freefireth"
    const val PKG_FREE_FIRE_MAX = "com.dts.freefiremax"

    fun checkInstalledGames(context: Context): Pair<Boolean, Boolean> {
        val pm = context.packageManager
        val standardInstalled = isPackageInstalled(pm, PKG_FREE_FIRE)
        val maxInstalled = isPackageInstalled(pm, PKG_FREE_FIRE_MAX)
        return Pair(standardInstalled, maxInstalled)
    }

    private fun isPackageInstalled(pm: PackageManager, packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    fun detectActiveGame(context: Context, simulatedGame: DetectedGame? = null): GameDetectionState {
        if (simulatedGame != null && simulatedGame != DetectedGame.None) {
            return GameDetectionState(
                detectedGame = simulatedGame,
                isCompanionActive = true,
                isTrainingModeReady = true,
                isStandardInstalled = true,
                isMaxInstalled = true,
                simulatedStatusActive = true
            )
        }

        val (standardInstalled, maxInstalled) = checkInstalledGames(context)
        var detected: DetectedGame = DetectedGame.None
        var hasUsagePermission = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            val time = System.currentTimeMillis()
            val events = usm?.queryEvents(time - 1000 * 60 * 2, time) // last 2 minutes

            if (events != null) {
                val event = UsageEvents.Event()
                var latestPackage: String? = null
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        latestPackage = event.packageName
                    }
                }

                if (latestPackage == PKG_FREE_FIRE_MAX) {
                    detected = DetectedGame.FreeFireMax
                    hasUsagePermission = true
                } else if (latestPackage == PKG_FREE_FIRE) {
                    detected = DetectedGame.FreeFireStandard
                    hasUsagePermission = true
                } else if (latestPackage != null) {
                    hasUsagePermission = true
                }
            }
        }

        // If usage permission is not available but game is installed, we accurately identify installed version
        if (detected == DetectedGame.None) {
            if (maxInstalled && !standardInstalled) {
                detected = DetectedGame.FreeFireMax
            } else if (standardInstalled && !maxInstalled) {
                detected = DetectedGame.FreeFireStandard
            } else if (standardInstalled && maxInstalled) {
                detected = DetectedGame.FreeFireMax // Default to MAX if both installed
            }
        }

        return GameDetectionState(
            detectedGame = detected,
            isCompanionActive = true,
            isTrainingModeReady = true,
            isStandardInstalled = standardInstalled,
            isMaxInstalled = maxInstalled,
            hasUsageStatsPermission = hasUsagePermission
        )
    }

    fun launchGame(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }
}
