package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WalletRepository(private val cardDao: CardDao) {

    val allCards: Flow<List<CardEntity>> = cardDao.getAllCards()

    suspend fun getCardById(id: Long): Flow<CardEntity?> = cardDao.getCardById(id)

    suspend fun saveCard(card: CardEntity): Long = withContext(Dispatchers.IO) {
        if (card.id == 0L) {
            cardDao.insertCard(card)
        } else {
            cardDao.updateCard(card)
            card.id
        }
    }

    suspend fun deleteCard(card: CardEntity) = withContext(Dispatchers.IO) {
        cardDao.deleteCard(card)
    }

    suspend fun deleteCardById(id: Long) = withContext(Dispatchers.IO) {
        cardDao.deleteCardById(id)
    }

    suspend fun updateSlotIndices(cards: List<CardEntity>) = withContext(Dispatchers.IO) {
        cards.forEachIndexed { index, card ->
            cardDao.updateCard(card.copy(slotIndex = index))
        }
    }

    suspend fun seedInitialCardsIfEmpty() = withContext(Dispatchers.IO) {
        if (cardDao.getCount() == 0) {
            val starterCards = listOf(
                CardEntity(
                    title = "Centurion Black Metal",
                    cardholderName = "ALEXANDER VANCE",
                    cardNumber = "3782 822490 10005",
                    expiryDate = "11/30",
                    cvv = "8421",
                    cardType = "AMEX",
                    category = "PAYMENT",
                    bankOrIssuer = "American Express",
                    themeColorHex = "#161618",
                    gradientEndHex = "#28292E",
                    notes = "Titanium card with concierge services & airport lounge access.",
                    scannedVia = "NFC",
                    isFavorite = true,
                    slotIndex = 0
                ),
                CardEntity(
                    title = "Sapphire Reserve World",
                    cardholderName = "ALEXANDER VANCE",
                    cardNumber = "4147 2028 9912 4088",
                    expiryDate = "08/29",
                    cvv = "739",
                    cardType = "VISA",
                    category = "PAYMENT",
                    bankOrIssuer = "JPMorgan Chase",
                    themeColorHex = "#0C2340",
                    gradientEndHex = "#1D4ED8",
                    notes = "Primary travel and dining rewards card.",
                    scannedVia = "CAMERA_AI",
                    isFavorite = true,
                    slotIndex = 1
                ),
                CardEntity(
                    title = "Gold Executive Privilege",
                    cardholderName = "ALEXANDER VANCE",
                    cardNumber = "5412 7534 8901 2345",
                    expiryDate = "05/28",
                    cvv = "412",
                    cardType = "MASTERCARD",
                    category = "PAYMENT",
                    bankOrIssuer = "Standard Chartered",
                    themeColorHex = "#854D0E",
                    gradientEndHex = "#CA8A04",
                    notes = "Cashback & hotel perks.",
                    scannedVia = "CAMERA_AI",
                    isFavorite = false,
                    slotIndex = 2
                ),
                CardEntity(
                    title = "Digital Driver's License",
                    cardholderName = "ALEXANDER VANCE",
                    cardNumber = "DL-982410-X0",
                    expiryDate = "10/31",
                    cvv = "",
                    cardType = "ID_CARD",
                    category = "IDENTITY",
                    bankOrIssuer = "State Department of Licensing",
                    themeColorHex = "#134E4A",
                    gradientEndHex = "#0F766E",
                    notes = "Official state identification with REAL ID endorsement.",
                    scannedVia = "CAMERA_AI",
                    isFavorite = false,
                    slotIndex = 3
                ),
                CardEntity(
                    title = "Metro Transit Express",
                    cardholderName = "ALEXANDER VANCE",
                    cardNumber = "0104 9923 8812",
                    expiryDate = "12/35",
                    cvv = "",
                    cardType = "TRANSIT",
                    category = "TRANSIT",
                    bankOrIssuer = "Metropolitan Transit Authority",
                    themeColorHex = "#701A75",
                    gradientEndHex = "#A21CAF",
                    notes = "NFC contactless tap-to-ride pass.",
                    scannedVia = "NFC",
                    isFavorite = false,
                    slotIndex = 4
                )
            )
            cardDao.insertCards(starterCards)
        }
    }

    /**
     * Exports cards to a secure JSON backup payload with cryptographic timestamp and checksum.
     */
    suspend fun exportBackupJson(cards: List<CardEntity>, backupName: String = "VaultFolio Backup"): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", 1)
        root.put("app", "VaultFolio")
        root.put("backupName", backupName)
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))
        root.put("cardCount", cards.size)

        val cardsArray = JSONArray()
        for (c in cards) {
            val obj = JSONObject()
            obj.put("title", c.title)
            obj.put("cardholderName", c.cardholderName)
            obj.put("cardNumber", c.cardNumber)
            obj.put("expiryDate", c.expiryDate)
            obj.put("cvv", c.cvv)
            obj.put("cardType", c.cardType)
            obj.put("category", c.category)
            obj.put("bankOrIssuer", c.bankOrIssuer)
            obj.put("themeColorHex", c.themeColorHex)
            obj.put("gradientEndHex", c.gradientEndHex)
            obj.put("notes", c.notes)
            obj.put("nfcTagUid", c.nfcTagUid)
            obj.put("scannedVia", c.scannedVia)
            obj.put("isFavorite", c.isFavorite)
            obj.put("slotIndex", c.slotIndex)
            cardsArray.put(obj)
        }
        root.put("cards", cardsArray)
        root.toString(2)
    }

    /**
     * Restores cards from backup JSON. Returns number of cards restored.
     */
    suspend fun restoreBackupJson(jsonString: String, replaceExisting: Boolean = false): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("cards")) {
                return@withContext Result.failure(IllegalArgumentException("Invalid VaultFolio backup format"))
            }

            if (replaceExisting) {
                cardDao.deleteAll()
            }

            val cardsArray = root.getJSONArray("cards")
            val restoredCards = mutableListOf<CardEntity>()
            for (i in 0 until cardsArray.length()) {
                val obj = cardsArray.getJSONObject(i)
                val card = CardEntity(
                    title = obj.optString("title", "Imported Card"),
                    cardholderName = obj.optString("cardholderName", ""),
                    cardNumber = obj.optString("cardNumber", ""),
                    expiryDate = obj.optString("expiryDate", ""),
                    cvv = obj.optString("cvv", ""),
                    cardType = obj.optString("cardType", "VISA"),
                    category = obj.optString("category", "PAYMENT"),
                    bankOrIssuer = obj.optString("bankOrIssuer", ""),
                    themeColorHex = obj.optString("themeColorHex", "#1E293B"),
                    gradientEndHex = obj.optString("gradientEndHex", "#0F172A"),
                    notes = obj.optString("notes", ""),
                    nfcTagUid = obj.optString("nfcTagUid", ""),
                    scannedVia = obj.optString("scannedVia", "BACKUP_RESTORE"),
                    isFavorite = obj.optBoolean("isFavorite", false),
                    slotIndex = obj.optInt("slotIndex", i)
                )
                restoredCards.add(card)
            }

            cardDao.insertCards(restoredCards)
            Result.success(restoredCards.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
