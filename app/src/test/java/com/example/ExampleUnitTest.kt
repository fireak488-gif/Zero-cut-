package com.example

import com.example.model.WeaponCatalog
import com.example.ui.screens.aimtrainer.DrillType
import com.example.ui.screens.sensitivity.calculateRecommendedSensitivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSensitivityAlgorithm_lowEndDeviceBoostsSensitivity() {
        val lowEndRec = calculateRecommendedSensitivity("Low-End (2-4GB RAM)", "60Hz", "Balanced Drag")
        val flagshipRec = calculateRecommendedSensitivity("Flagship / High-End", "120Hz", "Balanced Drag")

        // Low end should have higher general and red dot to compensate for lower touch sampling
        assertTrue(lowEndRec.general >= flagshipRec.general)
        assertTrue(lowEndRec.redDot >= flagshipRec.redDot)
        assertEquals(100, lowEndRec.general)
    }

    @Test
    fun testSensitivityAlgorithm_pcEmulatorUsesLowerSensitivity() {
        val pcRec = calculateRecommendedSensitivity("PC Emulator", "144Hz+", "Sniper Quick Switch")
        assertTrue(pcRec.general <= 80)
        assertTrue(pcRec.sniperScope <= 40)
    }

    @Test
    fun testWeaponCatalog_weaponsLoadedCorrectly() {
        val weapons = WeaponCatalog.weapons
        assertTrue(weapons.isNotEmpty())

        val desertEagle = weapons.firstOrNull { it.id == "desert_eagle" }
        assertTrue(desertEagle != null)
        assertEquals(90, desertEagle?.bodyDamage)
        assertEquals(495, desertEagle?.headshotDamage)
    }

    @Test
    fun testDrillTypes_haveTitlesAndDurations() {
        for (drill in DrillType.values()) {
            assertTrue(drill.title.isNotBlank())
            assertTrue(drill.defaultDurationSecs > 0)
        }
    }
}


