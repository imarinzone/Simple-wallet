package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.HapticSensitivity
import com.example.security.HapticsHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HapticsHelperTest {

    @Test
    fun testHapticsHelperInitialization() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val haptics = HapticsHelper(context)
        assertNotNull(haptics)
        assertEquals(HapticSensitivity.MEDIUM, haptics.sensitivity)
    }

    @Test
    fun testSensitivityTransitions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val haptics = HapticsHelper(context)

        haptics.sensitivity = HapticSensitivity.LIGHT
        assertEquals(HapticSensitivity.LIGHT, haptics.sensitivity)
        assertEquals("Light", haptics.sensitivity.label)

        haptics.sensitivity = HapticSensitivity.STRONG
        assertEquals(HapticSensitivity.STRONG, haptics.sensitivity)
        assertEquals("Strong", haptics.sensitivity.label)
    }

    @Test
    fun testHapticsTriggersDoNotCrash() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val haptics = HapticsHelper(context)

        // Ensure invocation of tactile feedback methods does not throw exceptions
        haptics.cardSlide()
        haptics.cardDragTick()
        haptics.cardDraw()
        haptics.cardSelect()
        haptics.stackExpand()
        haptics.stackCollapse()
        haptics.cardFlip()
        haptics.walletOpen()
        haptics.walletClose()
        haptics.success()
        haptics.error()
    }
}
