package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val cardholderName: String,
    val cardNumber: String,
    val expiryDate: String,
    val cvv: String = "",
    val cardType: String = "VISA", // VISA, MASTERCARD, AMEX, DISCOVER, ID_CARD, LOYALTY, TRANSIT
    val category: String = "PAYMENT", // PAYMENT, IDENTITY, MEMBERSHIP, TRANSIT
    val bankOrIssuer: String = "",
    val themeColorHex: String = "#1E293B",
    val gradientEndHex: String = "#0F172A",
    val notes: String = "",
    val nfcTagUid: String = "",
    val scannedVia: String = "CAMERA_AI", // CAMERA_AI, NFC, MANUAL
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val slotIndex: Int = 0
) {
    val maskedNumber: String
        get() {
            val clean = cardNumber.replace("\\s+".toRegex(), "")
            return if (clean.length >= 4) {
                val last4 = clean.takeLast(4)
                "•••• •••• •••• $last4"
            } else {
                cardNumber
            }
        }

    val formattedNumber: String
        get() {
            val digits = cardNumber.filter { it.isDigit() }
            return digits.chunked(4).joinToString(" ")
        }
}
