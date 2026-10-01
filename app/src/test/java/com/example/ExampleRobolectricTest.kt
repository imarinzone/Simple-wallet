package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CardEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("VaultFolio", appName)
  }

  @Test
  fun `card entity formatting and masking test`() {
    val card = CardEntity(
      title = "Titanium Card",
      cardholderName = "ALEXANDER VANCE",
      cardNumber = "4532 1192 8841 9920",
      expiryDate = "10/30",
      cvv = "321",
      cardType = "VISA",
      themeColorHex = "#161618"
    )

    assertEquals("•••• •••• •••• 9920", card.maskedNumber)
    assertEquals("4532 1192 8841 9920", card.formattedNumber)
    assertNotNull(card.createdAt)
  }
}
