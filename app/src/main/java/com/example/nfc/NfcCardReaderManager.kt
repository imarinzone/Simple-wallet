package com.example.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.example.data.CardEntity

data class NfcReadResult(
    val tagUidHex: String,
    val cardType: String,
    val estimatedBank: String,
    val emvAid: String = "",
    val suggestedCard: CardEntity? = null
)

class NfcCardReaderManager(private val context: Context) {
    val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)

    val isNfcSupported: Boolean
        get() = nfcAdapter != null

    val isNfcEnabled: Boolean
        get() = nfcAdapter?.isEnabled == true

    fun startListening(activity: Activity, onTagRead: (NfcReadResult) -> Unit) {
        if (nfcAdapter == null || !nfcAdapter.isEnabled) return

        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        val readerCallback = NfcAdapter.ReaderCallback { tag ->
            val result = parseTag(tag)
            activity.runOnUiThread {
                onTagRead(result)
            }
        }

        try {
            nfcAdapter.enableReaderMode(activity, readerCallback, flags, Bundle())
        } catch (e: Exception) {
            Log.e("NfcManager", "Failed to enable reader mode: ${e.message}")
        }
    }

    fun stopListening(activity: Activity) {
        try {
            nfcAdapter?.disableReaderMode(activity)
        } catch (e: Exception) {
            Log.e("NfcManager", "Failed to disable reader mode: ${e.message}")
        }
    }

    fun enableForegroundDispatch(activity: Activity) {
        if (nfcAdapter == null || !nfcAdapter.isEnabled) return
        try {
            val intent = Intent(activity, activity.javaClass).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getActivity(activity, 0, intent, flags)
            val filters = arrayOf(
                IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
                IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
                IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
            )
            val techList = arrayOf(
                arrayOf("android.nfc.tech.IsoDep"),
                arrayOf("android.nfc.tech.NfcA"),
                arrayOf("android.nfc.tech.NfcB"),
                arrayOf("android.nfc.tech.Ndef")
            )
            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techList)
        } catch (e: Exception) {
            Log.e("NfcManager", "Failed to enable foreground dispatch: ${e.message}")
        }
    }

    fun disableForegroundDispatch(activity: Activity) {
        try {
            nfcAdapter?.disableForegroundDispatch(activity)
        } catch (e: Exception) {
            Log.e("NfcManager", "Failed to disable foreground dispatch: ${e.message}")
        }
    }

    /**
     * Parses NFC tag from incoming system Intent
     */
    fun parseIntent(intent: Intent): NfcReadResult? {
        val action = intent.action ?: return null
        if (action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            action == NfcAdapter.ACTION_TECH_DISCOVERED ||
            action == NfcAdapter.ACTION_NDEF_DISCOVERED
        ) {
            val tag: Tag? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            }
            if (tag != null) {
                return parseTag(tag)
            }
        }
        return null
    }

    fun parseTag(tag: Tag): NfcReadResult {
        val uidBytes = tag.id ?: byteArrayOf()
        val uidHex = uidBytes.joinToString(":") { "%02X".format(it) }

        var detectedType = "CONTACTLESS_CARD"
        var bankName = "Contactless Card"
        var aidHex = ""

        // Try reading IsoDep for EMV contactless cards
        val isoDep = IsoDep.get(tag)
        if (isoDep != null) {
            try {
                isoDep.connect()
                // Send SELECT PPSE (2PAY.SYS.DDF01) command APDU
                // 00 A4 04 00 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 00
                val selectPpse = byteArrayOf(
                    0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), 0x0E.toByte(),
                    '2'.code.toByte(), 'P'.code.toByte(), 'A'.code.toByte(), 'Y'.code.toByte(),
                    '.'.code.toByte(), 'S'.code.toByte(), 'Y'.code.toByte(), 'S'.code.toByte(),
                    '.'.code.toByte(), 'D'.code.toByte(), 'D'.code.toByte(), 'F'.code.toByte(),
                    '0'.code.toByte(), '1'.code.toByte(), 0x00.toByte()
                )
                val response = isoDep.transceive(selectPpse)
                val respHex = response.joinToString("") { "%02X".format(it) }

                if (respHex.contains("A000000003")) {
                    detectedType = "VISA"
                    bankName = "Visa Contactless"
                    aidHex = "A000000003"
                } else if (respHex.contains("A000000004")) {
                    detectedType = "MASTERCARD"
                    bankName = "Mastercard PayPass"
                    aidHex = "A000000004"
                } else if (respHex.contains("A000000025")) {
                    detectedType = "AMEX"
                    bankName = "American Express ExpressPay"
                    aidHex = "A000000025"
                }
                isoDep.close()
            } catch (e: Exception) {
                Log.d("NfcManager", "IsoDep parse: ${e.message}")
            }
        }

        val last4 = if (uidHex.length >= 5) uidHex.takeLast(5).replace(":", "") else "NFC1"
        val suggestedCard = CardEntity(
            title = "$bankName Tag",
            cardholderName = "NFC CARDHOLDER",
            cardNumber = "•••• •••• •••• $last4",
            expiryDate = "12/29",
            cvv = "",
            cardType = detectedType,
            category = "PAYMENT",
            bankOrIssuer = bankName,
            themeColorHex = "#0284C7",
            gradientEndHex = "#0369A1",
            notes = "Scanned directly via hardware NFC antenna. UID: $uidHex",
            nfcTagUid = uidHex,
            scannedVia = "NFC",
            createdAt = System.currentTimeMillis()
        )

        return NfcReadResult(
            tagUidHex = uidHex,
            cardType = detectedType,
            estimatedBank = bankName,
            emvAid = aidHex,
            suggestedCard = suggestedCard
        )
    }
}
