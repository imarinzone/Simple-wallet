package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.BiometricAuthManager
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BiometricAuthTest {

    @Test
    fun testBiometricAvailabilityCheckRuns() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Verify checking biometric/keyguard availability runs cleanly without crashing
        val isAvailable = BiometricAuthManager.isBiometricAvailable(context)
        // Returns boolean without throwing exception
        assertNotNull(isAvailable)
    }
}
