package com.example.data

import android.content.Context
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

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

    /**
     * Builds raw cards JSON.
     */
    fun buildRawCardsJson(cards: List<CardEntity>, backupName: String = "VaultFolio Backup"): String {
        val root = JSONObject()
        root.put("version", 2)
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
            obj.put("cardArtUrl", c.cardArtUrl)
            obj.put("isFavorite", c.isFavorite)
            obj.put("slotIndex", c.slotIndex)
            cardsArray.put(obj)
        }
        root.put("cards", cardsArray)
        return root.toString(2)
    }

    /**
     * Exports cards encrypted with a user-provided passphrase using AES-256-GCM + PBKDF2.
     */
    suspend fun exportEncryptedBackupJson(
        cards: List<CardEntity>,
        passphrase: String,
        backupName: String = "VaultFolio Backup"
    ): String = withContext(Dispatchers.IO) {
        val rawJson = buildRawCardsJson(cards, backupName)
        if (passphrase.isBlank()) {
            return@withContext rawJson
        }

        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)

        val iv = ByteArray(12) // Standard GCM 96-bit IV
        random.nextBytes(iv)

        val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, 10000, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKeyBytes = factory.generateSecret(keySpec).encoded
        val secretKey = SecretKeySpec(secretKeyBytes, "AES")

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val ciphertext = cipher.doFinal(rawJson.toByteArray(StandardCharsets.UTF_8))

        val encryptedRoot = JSONObject()
        encryptedRoot.put("version", 2)
        encryptedRoot.put("app", "VaultFolio")
        encryptedRoot.put("encrypted", true)
        encryptedRoot.put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
        encryptedRoot.put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
        encryptedRoot.put("ciphertext", Base64.encodeToString(ciphertext, Base64.NO_WRAP))
        encryptedRoot.put("cardCount", cards.size)
        encryptedRoot.put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))

        encryptedRoot.toString(2)
    }

    /**
     * Restores cards from an encrypted or plain backup payload.
     * Decrypts using the passphrase if encrypted.
     */
    suspend fun restoreEncryptedBackupJson(
        payload: String,
        passphrase: String,
        replaceExisting: Boolean = false
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val trimmed = payload.trim()
            val jsonRoot = JSONObject(trimmed)

            val decryptedJsonString: String = if (jsonRoot.optBoolean("encrypted", false)) {
                if (passphrase.isBlank()) {
                    return@withContext Result.failure(IllegalArgumentException("This backup is encrypted. Please enter the passphrase."))
                }

                val saltBase64 = jsonRoot.getString("salt")
                val ivBase64 = jsonRoot.getString("iv")
                val ciphertextBase64 = jsonRoot.getString("ciphertext")

                val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
                val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
                val ciphertext = Base64.decode(ciphertextBase64, Base64.NO_WRAP)

                val keySpec = PBEKeySpec(passphrase.toCharArray(), salt, 10000, 256)
                val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                val secretKeyBytes = factory.generateSecret(keySpec).encoded
                val secretKey = SecretKeySpec(secretKeyBytes, "AES")

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
                val decryptedBytes = cipher.doFinal(ciphertext)
                String(decryptedBytes, StandardCharsets.UTF_8)
            } else {
                trimmed
            }

            restoreRawCardsJson(decryptedJsonString, replaceExisting)
        } catch (e: javax.crypto.AEADBadTagException) {
            Result.failure(IllegalArgumentException("Incorrect passphrase or corrupted backup data."))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException(e.message ?: "Failed to restore backup"))
        }
    }

    private suspend fun restoreRawCardsJson(jsonString: String, replaceExisting: Boolean): Result<Int> {
        val root = JSONObject(jsonString)
        if (!root.has("cards")) {
            return Result.failure(IllegalArgumentException("Invalid VaultFolio backup format"))
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
                cardArtUrl = obj.optString("cardArtUrl", ""),
                isFavorite = obj.optBoolean("isFavorite", false),
                slotIndex = obj.optInt("slotIndex", i)
            )
            restoredCards.add(card)
        }

        cardDao.insertCards(restoredCards)
        return Result.success(restoredCards.size)
    }

    suspend fun exportBackupJson(cards: List<CardEntity>, backupName: String = "VaultFolio Backup"): String = withContext(Dispatchers.IO) {
        buildRawCardsJson(cards, backupName)
    }

    suspend fun restoreBackupJson(jsonString: String, replaceExisting: Boolean = false): Result<Int> = withContext(Dispatchers.IO) {
        restoreEncryptedBackupJson(jsonString, "", replaceExisting)
    }
}
