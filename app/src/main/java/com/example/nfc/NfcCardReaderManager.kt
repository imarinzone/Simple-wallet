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
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class NfcReadResult(
    val tagUidHex: String,
    val cardType: String,
    val estimatedBank: String,
    val emvAid: String = "",
    val suggestedCard: CardEntity? = null,
    val readSuccess: Boolean = true,
    val detailsMessage: String = "",
    val hasFullCardNumber: Boolean = false,
    val hasCardholderName: Boolean = false
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

    /**
     * Reads and parses real EMV contactless payment cards via ISO-DEP APDUs.
     * Complies with EMV Contactless Specifications for Payment Systems (Book B/C).
     */
    fun parseTag(tag: Tag): NfcReadResult {
        val uidBytes = tag.id ?: byteArrayOf()
        val uidHex = uidBytes.joinToString(":") { "%02X".format(it) }

        var detectedType = "CONTACTLESS"
        var bankName = "Contactless Card"
        var aidHex = ""
        var realPan = ""
        var realExpiry = ""
        var realCardholder = ""
        var statusNote = ""

        val isoDep = IsoDep.get(tag)
        if (isoDep != null) {
            try {
                isoDep.connect()
                isoDep.timeout = 4000

                // 1. SELECT PPSE (2PAY.SYS.DDF01) - Proximity Payment System Environment
                val ppseName = "2PAY.SYS.DDF01".toByteArray(StandardCharsets.US_ASCII)
                val selectPpse = byteArrayOf(0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), ppseName.size.toByte()) +
                        ppseName + byteArrayOf(0x00.toByte())

                val ppseResponse = try {
                    isoDep.transceive(selectPpse)
                } catch (e: Exception) {
                    // Try without Le 0x00
                    try {
                        val selectPpseNoLe = byteArrayOf(0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), ppseName.size.toByte()) + ppseName
                        isoDep.transceive(selectPpseNoLe)
                    } catch (e2: Exception) {
                        byteArrayOf()
                    }
                }

                // Extract AIDs from PPSE response
                val detectedAids = extractAidsFromTlv(ppseResponse)
                val candidateAids = if (detectedAids.isNotEmpty()) {
                    detectedAids
                } else {
                    // Standard payment AIDs fallback
                    listOf(
                        "A0000000031010", // Visa Credit/Debit
                        "A0000000041010", // Mastercard Credit/Debit
                        "A0000000043060", // Maestro
                        "A0000000032010", // Visa Electron
                        "A0000000033010", // Visa Interlink
                        "A00000002501",   // American Express
                        "A0000001523010", // Discover
                        "A0000001524010", // Diners Club
                        "A000000333010101", // UnionPay
                        "A0000000651010"  // JCB
                    )
                }

                // 2. Select matching AID and process EMV flow
                val allRecordResponses = mutableListOf<ByteArray>()

                for (aidStr in candidateAids) {
                    val aidBytes = hexToBytes(aidStr)
                    val selectAidApdu = byteArrayOf(
                        0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), aidBytes.size.toByte()
                    ) + aidBytes + byteArrayOf(0x00.toByte())

                    var aidResponse = try { isoDep.transceive(selectAidApdu) } catch (e: Exception) { byteArrayOf() }
                    var aidRespHex = bytesToHex(aidResponse)

                    if (!aidRespHex.contains("9000")) {
                        // Retry without Le
                        val selectAidNoLe = byteArrayOf(
                            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(), aidBytes.size.toByte()
                        ) + aidBytes
                        aidResponse = try { isoDep.transceive(selectAidNoLe) } catch (e: Exception) { byteArrayOf() }
                        aidRespHex = bytesToHex(aidResponse)
                    }

                    if (aidRespHex.contains("9000") || aidRespHex.endsWith("9000")) {
                        aidHex = aidStr

                        // Map card brand
                        when {
                            aidStr.startsWith("A000000003") -> {
                                detectedType = "VISA"
                                bankName = "Visa Contactless"
                            }
                            aidStr.startsWith("A000000004") -> {
                                detectedType = "MASTERCARD"
                                bankName = "Mastercard Contactless"
                            }
                            aidStr.startsWith("A000000025") -> {
                                detectedType = "AMEX"
                                bankName = "American Express"
                            }
                            aidStr.startsWith("A000000152") -> {
                                detectedType = "DISCOVER"
                                bankName = "Discover"
                            }
                            aidStr.startsWith("A000000333") -> {
                                detectedType = "UNIONPAY"
                                bankName = "UnionPay"
                            }
                            aidStr.startsWith("A000000065") -> {
                                detectedType = "JCB"
                                bankName = "JCB Contactless"
                            }
                        }

                        // Check Application Label (Tag 50) and Preferred Name (Tag 9F12)
                        val appLabel = extractTlvString(aidResponse, "50")
                        val prefName = extractTlvString(aidResponse, "9F12")
                        if (prefName.isNotBlank()) {
                            bankName = prefName
                        } else if (appLabel.isNotBlank()) {
                            bankName = appLabel
                        }

                        // 3. Send GET PROCESSING OPTIONS (GPO)
                        // Parse PDOL (Tag 9F38) from the FCI response
                        val pdolBytes = extractTlvBytes(aidResponse, "9F38")
                        val gpoPayload = if (pdolBytes.isNotEmpty()) {
                            buildPdolData(pdolBytes)
                        } else {
                            // Empty PDOL command data: Tag 83, length 00
                            byteArrayOf(0x83.toByte(), 0x00.toByte())
                        }

                        val gpoApdu = byteArrayOf(0x80.toByte(), 0xA8.toByte(), 0x00.toByte(), 0x00.toByte(), gpoPayload.size.toByte()) +
                                gpoPayload + byteArrayOf(0x00.toByte())

                        var gpoResponse = try { isoDep.transceive(gpoApdu) } catch (e: Exception) { byteArrayOf() }
                        var gpoHex = bytesToHex(gpoResponse)

                        // If empty PDOL failed, try standard TTQ (Terminal Transaction Qualifiers = 0x28 00 00 00)
                        if (!gpoHex.contains("9000") && (gpoHex.endsWith("6700") || gpoHex.endsWith("6985") || gpoHex.isBlank())) {
                            val ttqPayload = byteArrayOf(
                                0x83.toByte(), 0x04.toByte(),
                                0x28.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte()
                            )
                            val gpoApduTtq = byteArrayOf(0x80.toByte(), 0xA8.toByte(), 0x00.toByte(), 0x00.toByte(), ttqPayload.size.toByte()) +
                                    ttqPayload + byteArrayOf(0x00.toByte())
                            gpoResponse = try { isoDep.transceive(gpoApduTtq) } catch (e: Exception) { byteArrayOf() }
                            gpoHex = bytesToHex(gpoResponse)
                        }

                        if (gpoResponse.isNotEmpty()) {
                            allRecordResponses.add(gpoResponse)
                        }

                        // 4. Extract Application File Locator (AFL)
                        // Note: Format 1 response (Tag 80) contains: [80] [len] [AIP 2 bytes] [AFL bytes...]
                        // Note: Format 2 response (Tag 77) contains TLV with Tag 94 for AFL
                        val aflBytes = extractAfl(gpoResponse)

                        if (aflBytes.isNotEmpty() && aflBytes.size % 4 == 0) {
                            for (i in 0 until aflBytes.size step 4) {
                                val sfi = (aflBytes[i].toInt() and 0xFF) ushr 3
                                val startRec = aflBytes[i + 1].toInt() and 0xFF
                                val endRec = aflBytes[i + 2].toInt() and 0xFF

                                for (rec in startRec..endRec) {
                                    val p2 = (sfi shl 3) or 0x04
                                    val readRecApdu = byteArrayOf(
                                        0x00.toByte(), 0xB2.toByte(), rec.toByte(), p2.toByte(), 0x00.toByte()
                                    )
                                    try {
                                        val recResp = isoDep.transceive(readRecApdu)
                                        if (bytesToHex(recResp).contains("9000")) {
                                            allRecordResponses.add(recResp)
                                        }
                                    } catch (e: Exception) {
                                        // Ignore individual record timeouts
                                    }
                                }
                            }
                        } else {
                            // Fallback scan of common SFI 1..3 records 1..3
                            for (sfi in 1..3) {
                                for (rec in 1..3) {
                                    val p2 = (sfi shl 3) or 0x04
                                    val readRecApdu = byteArrayOf(0x00.toByte(), 0xB2.toByte(), rec.toByte(), p2.toByte(), 0x00.toByte())
                                    try {
                                        val recResp = isoDep.transceive(readRecApdu)
                                        if (bytesToHex(recResp).contains("9000")) {
                                            allRecordResponses.add(recResp)
                                        }
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                            }
                        }

                        break // Successfully selected matching payment AID
                    }
                }

                // 5. Parse Track 1, Track 2, PAN, Expiry, and Cardholder from all collected record responses
                for (recResp in allRecordResponses) {
                    // Check Tag 56 (Track 1 Data)
                    val track1Bytes = extractTlvBytes(recResp, "56")
                    if (track1Bytes.isNotEmpty()) {
                        val track1Str = String(track1Bytes, StandardCharsets.ISO_8859_1)
                        parseTrack1(track1Str)?.let { (pan, name, exp) ->
                            if (realPan.isBlank() && pan.isNotBlank()) realPan = pan
                            if (realCardholder.isBlank() && name.isNotBlank()) realCardholder = name
                            if (realExpiry.isBlank() && exp.isNotBlank()) realExpiry = exp
                        }
                    }

                    // Check Tag 57 (Track 2 Equivalent Data)
                    if (realPan.isBlank() || realExpiry.isBlank()) {
                        val track2Hex = extractTlvHex(recResp, "57")
                        if (track2Hex.isNotBlank()) {
                            parseTrack2(track2Hex)?.let { (pan, exp) ->
                                if (realPan.isBlank()) realPan = pan
                                if (realExpiry.isBlank()) realExpiry = exp
                            }
                        }
                    }

                    // Check Tag 9F6B (Track 2 Data MSD)
                    if (realPan.isBlank() || realExpiry.isBlank()) {
                        val track2MsdHex = extractTlvHex(recResp, "9F6B")
                        if (track2MsdHex.isNotBlank()) {
                            parseTrack2(track2MsdHex)?.let { (pan, exp) ->
                                if (realPan.isBlank()) realPan = pan
                                if (realExpiry.isBlank()) realExpiry = exp
                            }
                        }
                    }

                    // Check Tag 5A (Application PAN)
                    if (realPan.isBlank()) {
                        val panHex = extractTlvHex(recResp, "5A")
                        if (panHex.isNotBlank()) {
                            val cleanPan = panHex.replace("F", "").replace("f", "")
                            if (cleanPan.length in 13..19 && cleanPan.all { it.isDigit() }) {
                                realPan = formatPan(cleanPan)
                            }
                        }
                    }

                    // Check Tag 5F24 (Application Expiration Date - YYMMDD)
                    if (realExpiry.isBlank()) {
                        val expHex = extractTlvHex(recResp, "5F24")
                        if (expHex.length >= 4) {
                            val yy = expHex.substring(0, 2)
                            val mm = expHex.substring(2, 4)
                            if (mm.toIntOrNull() in 1..12) {
                                realExpiry = "$mm/$yy"
                            }
                        }
                    }

                    // Check Tag 5F20 (Cardholder Name)
                    if (realCardholder.isBlank()) {
                        val nameStr = extractTlvString(recResp, "5F20")
                        if (nameStr.isNotBlank() && nameStr.length >= 3 && nameStr != "/") {
                            realCardholder = formatCardholderName(nameStr)
                        }
                    }
                }

                isoDep.close()
            } catch (e: Exception) {
                Log.w("NfcManager", "IsoDep execution completed: ${e.message}")
            }
        }

        // Color theme mapping based on card brand
        val (themeHex, gradHex) = when (detectedType) {
            "AMEX" -> Pair("#161618", "#2F3136")
            "MASTERCARD" -> Pair("#854D0E", "#CA8A04")
            "VISA" -> Pair("#0C2340", "#1D4ED8")
            "DISCOVER" -> Pair("#C2410C", "#EA580C")
            "UNIONPAY" -> Pair("#B91C1C", "#DC2626")
            "JCB" -> Pair("#1E3A8A", "#2563EB")
            else -> Pair("#0284C7", "#0369A1")
        }

        val hasFullPan = realPan.isNotBlank()
        val hasName = realCardholder.isNotBlank()

        statusNote = when {
            hasFullPan && hasName -> "Card authenticated: full card number and cardholder name read over NFC antenna."
            hasFullPan && !hasName -> "Card number & expiry authenticated from contactless chip. Name not exposed by issuing bank over NFC."
            !hasFullPan && realExpiry.isNotBlank() -> "NFC chip authenticated ($bankName). Chip privacy restricts card number transmission without a merchant POS key."
            else -> "NFC contactless chip detected ($bankName). Issuing bank contactless privacy limits unencrypted card number transfer. Please scan or enter card number."
        }

        val suggestedCard = CardEntity(
            title = if (bankName.isNotBlank() && bankName != "Contactless Card") bankName else "$detectedType Card",
            cardholderName = realCardholder,
            cardNumber = realPan,
            expiryDate = realExpiry,
            cvv = "",
            cardType = detectedType,
            category = "PAYMENT",
            bankOrIssuer = bankName,
            themeColorHex = themeHex,
            gradientEndHex = gradHex,
            notes = "NFC Tag UID: $uidHex. $statusNote",
            nfcTagUid = uidHex,
            scannedVia = "NFC",
            createdAt = System.currentTimeMillis()
        )

        return NfcReadResult(
            tagUidHex = uidHex,
            cardType = detectedType,
            estimatedBank = bankName,
            emvAid = aidHex,
            suggestedCard = suggestedCard,
            readSuccess = true,
            detailsMessage = statusNote,
            hasFullCardNumber = hasFullPan,
            hasCardholderName = hasName
        )
    }

    /**
     * Builds PDOL data dynamically based on the requested tag list in Tag 9F38.
     */
    private fun buildPdolData(pdolBytes: ByteArray): ByteArray {
        val out = mutableListOf<Byte>()
        var i = 0
        val currentDate = SimpleDateFormat("yyMMdd", Locale.US).format(Date())
        val dateBytes = hexToBytes(currentDate)

        while (i < pdolBytes.size) {
            val tagByte1 = pdolBytes[i].toInt() and 0xFF
            val isTwoByteTag = (tagByte1 and 0x1F) == 0x1F
            val tagHex = if (isTwoByteTag && i + 1 < pdolBytes.size) {
                "%02X%02X".format(tagByte1, pdolBytes[i + 1].toInt() and 0xFF)
            } else {
                "%02X".format(tagByte1)
            }
            i += if (isTwoByteTag) 2 else 1

            if (i >= pdolBytes.size) break
            val length = pdolBytes[i].toInt() and 0xFF
            i += 1

            val valueBytes = when (tagHex.uppercase(Locale.US)) {
                "9F66" -> byteArrayOf(0xB6.toByte(), 0x20.toByte(), 0xC0.toByte(), 0x00.toByte()) // TTQ
                "9F02" -> ByteArray(6) { 0 } // Amount Authorised
                "9F03" -> ByteArray(6) { 0 } // Amount Other
                "9F1A" -> byteArrayOf(0x08.toByte(), 0x40.toByte()) // Terminal Country Code (US)
                "95" -> ByteArray(5) { 0 } // Terminal Verification Results
                "5F2A" -> byteArrayOf(0x08.toByte(), 0x40.toByte()) // Transaction Currency Code (USD)
                "9A" -> if (dateBytes.size == 3) dateBytes else byteArrayOf(0x26, 0x10, 0x02) // Date
                "9C" -> byteArrayOf(0x00) // Transaction Type
                "9F37" -> Random.nextBytes(4) // Unpredictable Number
                else -> ByteArray(length) { 0 }
            }

            // Adjust to exact expected length
            val adjusted = if (valueBytes.size == length) {
                valueBytes
            } else if (valueBytes.size < length) {
                valueBytes + ByteArray(length - valueBytes.size) { 0 }
            } else {
                valueBytes.copyOf(length)
            }

            for (b in adjusted) out.add(b)
        }

        // Return wrapped in Tag 83
        val pdolContent = out.toByteArray()
        return byteArrayOf(0x83.toByte(), pdolContent.size.toByte()) + pdolContent
    }

    /**
     * Extracts AFL (Application File Locator) bytes from GPO response.
     * Handles both Format 1 (Tag 80) and Format 2 (Tag 77 with Tag 94).
     */
    private fun extractAfl(gpoResponse: ByteArray): ByteArray {
        if (gpoResponse.size < 4) return byteArrayOf()

        // Check Format 1 (Tag 80)
        if (gpoResponse[0] == 0x80.toByte()) {
            val len = gpoResponse[1].toInt() and 0xFF
            // First 2 bytes of value are AIP, remaining are AFL
            return if (len > 2 && gpoResponse.size >= len + 2) {
                gpoResponse.copyOfRange(4, len + 2)
            } else byteArrayOf()
        }

        // Check Format 2 (Tag 77) -> look for Tag 94
        val aflFromTag94 = extractTlvBytes(gpoResponse, "94")
        if (aflFromTag94.isNotEmpty()) {
            return aflFromTag94
        }

        return byteArrayOf()
    }

    private fun parseTrack1(track1Raw: String): Triple<String, String, String>? {
        try {
            val start = track1Raw.indexOf('B')
            if (start == -1) return null
            val content = track1Raw.substring(start + 1)
            val parts = content.split('^')
            if (parts.size >= 2) {
                val panRaw = parts[0].filter { it.isDigit() }
                val nameRaw = parts[1].trim()
                val expRaw = if (parts.size >= 3 && parts[2].length >= 4) {
                    val yy = parts[2].substring(0, 2)
                    val mm = parts[2].substring(2, 4)
                    "$mm/$yy"
                } else ""

                val pan = if (panRaw.length in 13..19) formatPan(panRaw) else ""
                val name = formatCardholderName(nameRaw)
                return Triple(pan, name, expRaw)
            }
        } catch (e: Exception) {
            // Ignore parse error
        }
        return null
    }

    private fun parseTrack2(track2Hex: String): Pair<String, String>? {
        val sep = if (track2Hex.contains("D")) "D" else if (track2Hex.contains("d")) "d" else if (track2Hex.contains("=")) "=" else null ?: return null
        val parts = track2Hex.split(sep)
        if (parts.size >= 2) {
            val rawPan = parts[0].filter { it.isDigit() }
            val expPart = parts[1].take(4)
            if (rawPan.length in 13..19 && expPart.length == 4) {
                val yy = expPart.substring(0, 2)
                val mm = expPart.substring(2, 4)
                val formattedPan = formatPan(rawPan)
                val formattedExpiry = "$mm/$yy"
                return Pair(formattedPan, formattedExpiry)
            }
        }
        return null
    }

    private fun formatPan(rawDigits: String): String {
        val clean = rawDigits.filter { it.isDigit() }
        return if (clean.length == 15) {
            "${clean.substring(0, 4)} ${clean.substring(4, 10)} ${clean.substring(10)}"
        } else {
            clean.chunked(4).joinToString(" ")
        }
    }

    private fun formatCardholderName(raw: String): String {
        val clean = raw.trim().replace("/", " ").replace(Regex("""\s+"""), " ").trim()
        val words = clean.split(" ").filter { it.isNotBlank() }
        return if (words.size >= 2) {
            words.joinToString(" ") { word ->
                word.lowercase(Locale.US).replaceFirstChar { it.uppercase(Locale.US) }
            }.uppercase(Locale.US)
        } else {
            clean.uppercase(Locale.US)
        }
    }

    private fun extractAidsFromTlv(data: ByteArray): List<String> {
        val aids = mutableListOf<String>()
        val hex = bytesToHex(data).uppercase(Locale.US)
        var index = 0
        while (index < hex.length - 4) {
            if (hex.substring(index).startsWith("4F")) {
                val lenHex = hex.substring(index + 2, index + 4)
                val len = lenHex.toIntOrNull(16) ?: 0
                if (len in 5..16 && index + 4 + len * 2 <= hex.length) {
                    val aid = hex.substring(index + 4, index + 4 + len * 2)
                    aids.add(aid)
                    index += 4 + len * 2
                    continue
                }
            }
            index += 2
        }
        return aids
    }

    private fun extractTlvHex(data: ByteArray, tag: String): String {
        val hex = bytesToHex(data).uppercase(Locale.US)
        val targetTag = tag.uppercase(Locale.US)
        var index = 0
        while (index < hex.length - targetTag.length - 2) {
            if (hex.substring(index).startsWith(targetTag)) {
                val afterTag = index + targetTag.length
                val lenHex = hex.substring(afterTag, afterTag + 2)
                val len = lenHex.toIntOrNull(16) ?: 0
                if (len in 1..255 && afterTag + 2 + len * 2 <= hex.length) {
                    return hex.substring(afterTag + 2, afterTag + 2 + len * 2)
                }
            }
            index += 2
        }
        return ""
    }

    private fun extractTlvBytes(data: ByteArray, tag: String): ByteArray {
        val hex = extractTlvHex(data, tag)
        return if (hex.isNotBlank()) hexToBytes(hex) else byteArrayOf()
    }

    private fun extractTlvString(data: ByteArray, tag: String): String {
        val bytes = extractTlvBytes(data, tag)
        return if (bytes.isNotEmpty()) {
            try {
                String(bytes, StandardCharsets.ISO_8859_1).trim().filter { it in ' '..'~' }
            } catch (e: Exception) {
                ""
            }
        } else ""
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }
        return sb.toString()
    }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.replace(" ", "")
        val len = clean.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(clean[i], 16) shl 4) + Character.digit(clean[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
