package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.CardDao
import com.example.data.CardEntity
import com.example.data.WalletRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WalletRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var cardDao: CardDao
    private lateinit var repository: WalletRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        cardDao = database.cardDao()
        repository = WalletRepository(cardDao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveCard() = runBlocking {
        val card = CardEntity(
            title = "Sapphire Reserve",
            cardholderName = "ALEXANDER VANCE",
            cardNumber = "4532 1192 8841 9920",
            expiryDate = "10/30",
            cvv = "321",
            cardType = "VISA",
            themeColorHex = "#161618"
        )

        val id = repository.saveCard(card)
        assertTrue(id > 0)

        val all = repository.allCards.first()
        assertEquals(1, all.size)
        assertEquals("Sapphire Reserve", all[0].title)
        assertEquals("4532 1192 8841 9920", all[0].cardNumber)
    }

    @Test
    fun testUpdateCardAndSlotReorder() = runBlocking {
        val card1 = CardEntity(title = "Card 1", cardholderName = "User", cardNumber = "1111", expiryDate = "01/26", slotIndex = 0)
        val card2 = CardEntity(title = "Card 2", cardholderName = "User", cardNumber = "2222", expiryDate = "02/26", slotIndex = 1)

        val id1 = repository.saveCard(card1)
        val id2 = repository.saveCard(card2)

        val initialCards = repository.allCards.first()
        assertEquals(2, initialCards.size)

        // Simulate dragging card2 to the top slot (reordering)
        val reordered = listOf(
            initialCards.first { it.id == id2 },
            initialCards.first { it.id == id1 }
        )
        repository.updateSlotIndices(reordered)

        val afterReorder = repository.allCards.first()
        assertEquals(id2, afterReorder[0].id)
        assertEquals(0, afterReorder[0].slotIndex)
        assertEquals(id1, afterReorder[1].id)
        assertEquals(1, afterReorder[1].slotIndex)
    }

    @Test
    fun testDeleteCard() = runBlocking {
        val card = CardEntity(title = "Card to Delete", cardholderName = "User", cardNumber = "9999", expiryDate = "01/26")
        val id = repository.saveCard(card)
        assertEquals(1, repository.allCards.first().size)

        repository.deleteCardById(id)
        assertEquals(0, repository.allCards.first().size)
    }

    @Test
    fun testEncryptedBackupExportAndRestore() = runBlocking {
        val card1 = CardEntity(title = "Card Alpha", cardholderName = "Alice", cardNumber = "4000111122223333", expiryDate = "03/28")
        val card2 = CardEntity(title = "Card Beta", cardholderName = "Bob", cardNumber = "5100111122223333", expiryDate = "04/28")
        repository.saveCard(card1)
        repository.saveCard(card2)

        val originalCards = repository.allCards.first()
        val password = "SuperSecretPassword123!"

        // Export encrypted
        val encryptedJson = repository.exportEncryptedBackupJson(originalCards, password)
        assertTrue(encryptedJson.contains("\"encrypted\": true"))

        // Attempt restore with wrong password -> should fail
        val failedResult = repository.restoreEncryptedBackupJson(encryptedJson, "WrongPassword", replaceExisting = true)
        assertTrue("Restoring with wrong password must fail", failedResult.isFailure)

        // Restore with correct password -> should succeed
        val successResult = repository.restoreEncryptedBackupJson(encryptedJson, password, replaceExisting = true)
        assertTrue(successResult.isSuccess)
        assertEquals(2, successResult.getOrNull())

        val restoredCards = repository.allCards.first()
        assertEquals(2, restoredCards.size)
        assertTrue(restoredCards.any { it.title == "Card Alpha" })
        assertTrue(restoredCards.any { it.title == "Card Beta" })
    }
}
