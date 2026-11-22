package com.example

import com.example.data.AppUpdateService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun releaseUpgradeVersioning_isStrictlyHigherThanLegacyVersions() {
    assertEquals("com.aistudio.vaultfolio.kxpzql", BuildConfig.APPLICATION_ID)
    assertTrue(
      "BuildConfig.VERSION_CODE (${BuildConfig.VERSION_CODE}) must exceed legacy 1.0.x/1.1.x codes so in-place upgrades succeed without uninstalling",
      BuildConfig.VERSION_CODE >= 1_020_000
    )
    assertFalse(AppUpdateService.isVersionNewer("1.0.18", BuildConfig.VERSION_NAME))
    assertFalse(AppUpdateService.isVersionNewer("1.1.99", BuildConfig.VERSION_NAME))
    assertFalse(AppUpdateService.isVersionNewer(BuildConfig.VERSION_NAME, BuildConfig.VERSION_NAME))
    assertTrue(AppUpdateService.isVersionNewer("1.2.5", "1.2.0"))
  }
}
