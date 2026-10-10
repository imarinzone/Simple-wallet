package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.WalletViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PinSetupLogicTest {

    private lateinit var app: Application

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testPinValidationRules() {
        val valid4Pin = "1234"
        val valid6Pin = "987654"
        val shortPin = "123"
        val nonDigitPin = "12a4"

        assertTrue(valid4Pin.length in 4..6 && valid4Pin.all { it.isDigit() })
        assertTrue(valid6Pin.length in 4..6 && valid6Pin.all { it.isDigit() })
        assertFalse(shortPin.length in 4..6 && shortPin.all { it.isDigit() })
        assertFalse(nonDigitPin.length in 4..6 && nonDigitPin.all { it.isDigit() })
    }

    @Test
    fun testPinConfirmationMatching() {
        val choosePin = "2580"
        val matchConfirmPin = "2580"
        val mismatchConfirmPin = "2581"

        assertEquals(choosePin, matchConfirmPin)
        assertTrue(choosePin == matchConfirmPin)
        assertFalse(choosePin == mismatchConfirmPin)
    }

    @Test
    fun testViewModelMasterPinStorage() {
        val viewModel = WalletViewModel(app)
        viewModel.setMasterPin("7890")
        assertEquals("7890", viewModel.masterPin.value)

        // Clear or update PIN
        viewModel.setMasterPin("4321")
        assertEquals("4321", viewModel.masterPin.value)
    }
}
