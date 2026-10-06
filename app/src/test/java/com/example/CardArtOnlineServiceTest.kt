package com.example

import com.example.data.CardArtOnlineService
import com.example.data.CardEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardArtOnlineServiceTest {

    @Test
    fun testCatalogNotEmpty() {
        val designs = CardArtOnlineService.OFFICIAL_CARD_DESIGNS
        assertTrue("Designs list should have items", designs.isNotEmpty())
        assertTrue("Should contain both HDFC and SBI designs", designs.size >= 10)
    }

    @Test
    fun testSuggestArtForCard_pixelPlay() {
        val card = CardEntity(
            title = "My Pixel Play Card",
            cardholderName = "User",
            cardNumber = "4000123456789010",
            expiryDate = "10/30",
            bankOrIssuer = "HDFC Bank",
            cardType = "VISA"
        )
        val suggested = CardArtOnlineService.suggestArtForCard(card)
        assertNotNull(suggested)
        assertEquals("hdfc_pixel_play", suggested?.id)
    }

    @Test
    fun testSuggestArtForCard_millennia() {
        val card = CardEntity(
            title = "Millennia Cashback",
            cardholderName = "User",
            cardNumber = "4000123456789010",
            expiryDate = "10/30",
            bankOrIssuer = "HDFC Bank",
            cardType = "VISA"
        )
        val suggested = CardArtOnlineService.suggestArtForCard(card)
        assertNotNull(suggested)
        assertEquals("hdfc_millennia", suggested?.id)
    }

    @Test
    fun testSuggestArtForCard_sbiTata() {
        val card = CardEntity(
            title = "Tata SBI Card",
            cardholderName = "User",
            cardNumber = "4000123456789010",
            expiryDate = "10/30",
            bankOrIssuer = "SBI Card",
            cardType = "VISA"
        )
        val suggested = CardArtOnlineService.suggestArtForCard(card)
        assertNotNull(suggested)
        assertTrue(suggested?.id?.startsWith("sbi_tata") == true)
    }

    @Test
    fun testSuggestArtForCard_bankFallback() {
        val card = CardEntity(
            title = "Custom Platinum Card",
            cardholderName = "User",
            cardNumber = "4000123456789010",
            expiryDate = "10/30",
            bankOrIssuer = "HDFC Bank",
            cardType = "VISA"
        )
        val suggested = CardArtOnlineService.suggestArtForCard(card)
        assertNotNull("Should fallback to default HDFC card", suggested)
        assertEquals("HDFC Bank", suggested?.issuer)
    }

    @Test
    fun testSearchCardArt() = kotlinx.coroutines.runBlocking {
        val results = CardArtOnlineService.searchCardArt("pixel")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.name.contains("Pixel", ignoreCase = true) })
    }
}
