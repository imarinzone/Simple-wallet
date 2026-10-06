package com.example

import com.example.data.CardEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CardEntityTest {

    @Test
    fun testPaymentCardDetection() {
        val visa = CardEntity(title = "Visa Card", cardholderName = "Jane Doe", cardNumber = "4000123456789010", expiryDate = "12/28", cardType = "VISA")
        val mastercard = CardEntity(title = "MC", cardholderName = "Jane Doe", cardNumber = "5100123456789010", expiryDate = "12/28", cardType = "MASTERCARD")
        val amex = CardEntity(title = "Amex", cardholderName = "Jane Doe", cardNumber = "378282246310005", expiryDate = "12/28", cardType = "AMEX")
        val rupay = CardEntity(title = "RuPay", cardholderName = "Jane Doe", cardNumber = "6071123456789010", expiryDate = "12/28", cardType = "RUPAY")
        val idCard = CardEntity(title = "ID Card", cardholderName = "Jane Doe", cardNumber = "DL-98234-XYZ", expiryDate = "12/35", cardType = "ID_CARD")

        assertTrue(visa.isPaymentCard)
        assertTrue(mastercard.isPaymentCard)
        assertTrue(amex.isPaymentCard)
        assertTrue(rupay.isPaymentCard)
        assertFalse(idCard.isPaymentCard)
    }

    @Test
    fun testMaskedNumber_standard16Digits() {
        val card = CardEntity(
            title = "Sapphire",
            cardholderName = "Jane Doe",
            cardNumber = "4532 8912 3456 7890",
            expiryDate = "05/29",
            cardType = "VISA"
        )
        assertEquals("•••• •••• •••• 7890", card.maskedNumber)
    }

    @Test
    fun testMaskedNumber_nonPaymentCard() {
        val card = CardEntity(
            title = "Metro Pass",
            cardholderName = "Jane Doe",
            cardNumber = "123456789",
            expiryDate = "01/30",
            cardType = "TRANSIT"
        )
        // non-payment length > 4: prefix 2, masked middle, suffix 4
        assertEquals("12 ••• 6789", card.maskedNumber)
    }

    @Test
    fun testMaskedNumber_blankNumber() {
        val card = CardEntity(
            title = "Blank Card",
            cardholderName = "Jane Doe",
            cardNumber = "",
            expiryDate = "",
            cardType = "MISC"
        )
        assertEquals("•••• •••• •••• ••••", card.maskedNumber)
    }

    @Test
    fun testFormattedNumber_16Digits() {
        val card = CardEntity(
            title = "Debit",
            cardholderName = "Jane Doe",
            cardNumber = "4111222233334444",
            expiryDate = "08/27",
            cardType = "VISA"
        )
        assertEquals("4111 2222 3333 4444", card.formattedNumber)
    }

    @Test
    fun testFormattedNumber_15DigitsAmex() {
        val card = CardEntity(
            title = "Amex Gold",
            cardholderName = "Jane Doe",
            cardNumber = "378282246310005",
            expiryDate = "08/27",
            cardType = "AMEX"
        )
        assertEquals("3782 822463 10005", card.formattedNumber)
    }

    @Test
    fun testSlotIndexAndFavoriteState() {
        val card = CardEntity(
            id = 42L,
            title = "Primary Card",
            cardholderName = "Jane Doe",
            cardNumber = "4111222233334444",
            expiryDate = "08/27",
            cardType = "VISA",
            isFavorite = true,
            slotIndex = 2
        )
        assertTrue(card.isFavorite)
        assertEquals(2, card.slotIndex)
        assertEquals(42L, card.id)
    }
}
