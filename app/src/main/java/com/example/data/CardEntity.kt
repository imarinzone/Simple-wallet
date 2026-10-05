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
    val cardType: String = "VISA", // VISA, MASTERCARD, AMEX, DISCOVER, ID_CARD, RC_CARD, VOUCHER, MISC, TRANSIT
    val category: String = "PAYMENT", // PAYMENT, IDENTITY, VEHICLE, VOUCHER, MEMBERSHIP, MISC
    val bankOrIssuer: String = "",
    val themeColorHex: String = "#1E293B",
    val gradientEndHex: String = "#0F172A",
    val notes: String = "",
    val nfcTagUid: String = "",
    val scannedVia: String = "CAMERA_AI", // CAMERA_AI, NFC, MANUAL
    val cardArtUrl: String = "", // Online artwork image URL for authentic card face
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val slotIndex: Int = 0
) {
    val isPaymentCard: Boolean
        get() = cardType in listOf("VISA", "MASTERCARD", "AMEX", "DISCOVER")

    val maskedNumber: String
        get() {
            val clean = cardNumber.trim()
            return when {
                clean.isBlank() -> "•••• •••• •••• ••••"
                isPaymentCard && clean.filter { it.isDigit() }.length >= 4 -> {
                    val digits = clean.filter { it.isDigit() }
                    val last4 = digits.takeLast(4)
                    "•••• •••• •••• $last4"
                }
                clean.length > 4 -> {
                    val prefix = clean.take(2)
                    val suffix = clean.takeLast(4)
                    val maskedMiddle = "•".repeat((clean.length - 6).coerceAtLeast(2))
                    "$prefix $maskedMiddle $suffix"
                }
                else -> clean
            }
        }

    val formattedNumber: String
        get() {
            return if (isPaymentCard) {
                val digits = cardNumber.filter { it.isDigit() }
                if (digits.length == 15) {
                    "${digits.take(4)} ${digits.drop(4).take(6)} ${digits.drop(10)}"
                } else {
                    digits.chunked(4).joinToString(" ")
                }
            } else {
                cardNumber
            }
        }
}
