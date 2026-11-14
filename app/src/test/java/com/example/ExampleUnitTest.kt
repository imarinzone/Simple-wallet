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
      "BuildConfig.VERSION_CODE (${BuildConfig.VERSION_CODE}) must exceed legacy codes so in-place upgrades succeed without uninstalling",
      BuildConfig.VERSION_CODE >= 1_010_000
    )
    assertFalse(AppUpdateService.isVersionNewer("1.0.18", BuildConfig.VERSION_NAME))
    assertFalse(AppUpdateService.isVersionNewer(BuildConfig.VERSION_NAME, BuildConfig.VERSION_NAME))
    assertTrue(AppUpdateService.isVersionNewer("1.1.25", "1.1.0"))
  }
}
