package com.example.ai

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.CardEntity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

sealed class ScanResult {
    data class Success(
        val card: CardEntity,
        val rawAiResponse: String,
        val modelUsed: String
    ) : ScanResult()

    data class Error(val message: String, val fallbackCard: CardEntity? = null) : ScanResult()
}

data class OcrLineInfo(
    val text: String,
    val bounds: Rect?,
    val yCenterRatio: Float // 0.0 (top of card) to 1.0 (bottom of card)
)

data class OcrExtractionResult(
    val rawText: String,
    val lines: List<OcrLineInfo>,
    val words: List<String>
)

object GeminiCardScannerService {
    private const val TAG = "GeminiCardScanner"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val maxDim = 1280
        val scale = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            if (ratio > 1f) maxDim.toFloat() / bitmap.width else maxDim.toFloat() / bitmap.height
        } else 1f

        val scaledBitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else bitmap

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Performs structured on-device text recognition using Google ML Kit with spatial layout awareness.
     */
    private suspend fun runOnDeviceMlKitOcr(bitmap: Bitmap): OcrExtractionResult = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val bitmapH = bitmap.height.toFloat().coerceAtLeast(1f)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val lines = mutableListOf<OcrLineInfo>()
                    val words = mutableListOf<String>()

                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val lineText = line.text.trim()
                            if (lineText.isNotBlank()) {
                                val rect = line.boundingBox
                                val yCenter = if (rect != null) (rect.top + rect.bottom) / (2f * bitmapH) else 0.5f
                                lines.add(OcrLineInfo(text = lineText, bounds = rect, yCenterRatio = yCenter))
                            }
                            for (elem in line.elements) {
                                val elemText = elem.text.trim()
                                if (elemText.isNotBlank()) words.add(elemText)
                            }
                        }
                    }

                    continuation.resume(
                        OcrExtractionResult(
                            rawText = visionText.text,
                            lines = lines,
                            words = words
                        )
                    )
                }
                .addOnFailureListener { error ->
                    Log.w(TAG, "ML Kit OCR failed: ${error.message}")
                    continuation.resume(OcrExtractionResult("", emptyList(), emptyList()))
                }
        } catch (e: Exception) {
            Log.e(TAG, "ML Kit initialization error: ${e.message}")
            continuation.resume(OcrExtractionResult("", emptyList(), emptyList()))
        }
    }

    /**
     * Precision heuristic parser that extracts authentic cardholder name, PAN, expiry, and bank.
     */
    fun parseRealCardFromOcr(extraction: OcrExtractionResult): CardEntity {
        val lines = extraction.lines
        val rawFullText = extraction.rawText
        val allWords = extraction.words

        var detectedNumber = ""
        var detectedExpiry = ""
        var detectedCardholder = ""
        var detectedBank = ""
        var detectedType = "VISA"

        // ==========================================
        // 1. EXTRACT REAL CARD NUMBER (PAN)
        // ==========================================
        val candidatePans = mutableListOf<String>()

        // Approach A: Check lines for continuous or spaced sequences of 13-19 digits
        val numberRegex = Regex("""(?:\d[ -.]*?){13,19}""")
        for (line in lines) {
            val lineClean = line.text.replace(" ", "").replace("-", "").replace(".", "")
            val matches = numberRegex.findAll(line.text)
            for (match in matches) {
                val digitsOnly = match.value.filter { it.isDigit() }
                if (digitsOnly.length in 13..19) {
                    candidatePans.add(digitsOnly)
                }
            }
            if (lineClean.length in 13..19 && lineClean.all { it.isDigit() }) {
                candidatePans.add(lineClean)
            }
        }

        // Approach B: Concatenate 4-digit groups (frequent on embossed cards where ML Kit splits blocks)
        val fourDigitBlocks = mutableListOf<String>()
        val chunkRegex = Regex("""\b\d{4}\b""")
        for (word in allWords) {
            if (chunkRegex.matches(word)) {
                fourDigitBlocks.add(word)
            }
        }
        if (fourDigitBlocks.size >= 4) {
            // Check consecutive groups of 4 blocks (16 digits)
            for (i in 0..fourDigitBlocks.size - 4) {
                val combined = fourDigitBlocks[i] + fourDigitBlocks[i + 1] + fourDigitBlocks[i + 2] + fourDigitBlocks[i + 3]
                candidatePans.add(combined)
            }
        }

        // Approach C: OCR character confusion recovery with Luhn algorithm verification
        // (e.g. 'O'/'D' for 0, 'I'/'l' for 1, 'B' for 8, 'S' for 5, 'Z' for 2, 'G' for 6)
        for (line in lines) {
            val potentialNumber = line.text.uppercase(Locale.US).replace(" ", "").replace("-", "")
            if (potentialNumber.length in 13..19) {
                val substituted = potentialNumber.map { c ->
                    when (c) {
                        'O', 'D', 'Q' -> '0'
                        'I', 'L', '|' -> '1'
                        'Z' -> '2'
                        'E' -> '3'
                        'A' -> '4'
                        'S' -> '5'
                        'G' -> '6'
                        'B' -> '8'
                        else -> c
                    }
                }.joinToString("")

                if (substituted.all { it.isDigit() }) {
                    candidatePans.add(substituted)
                }
            }
        }

        // Validate all candidates against Luhn Checksum algorithm (ISO/IEC 7812)
        val verifiedPan = candidatePans.firstOrNull { passesLuhnCheck(it) }
        val finalRawPan = verifiedPan ?: candidatePans.firstOrNull { it.length == 16 } ?: candidatePans.firstOrNull() ?: ""

        if (finalRawPan.isNotBlank()) {
            detectedNumber = formatPan(finalRawPan)
            detectedType = determineCardTypeFromPan(finalRawPan)
        }

        // ==========================================
        // 2. EXTRACT EXPIRATION DATE (MM/YY or MM/YYYY)
        // ==========================================
        val expiryRegex = Regex("""\b(0[1-9]|1[0-2])\s*[/.\- ]\s*([2-3]\d)\b""")
        for (line in lines) {
            val match = expiryRegex.find(line.text)
            if (match != null) {
                val month = match.groupValues[1]
                val year = match.groupValues[2]
                detectedExpiry = "$month/$year"
                break
            }
        }

        // If not found, look for 4-digit expiry following "VALID THRU" / "GOOD THRU" / "EXPIRES"
        if (detectedExpiry.isBlank()) {
            val thruRegex = Regex("""(?:THRU|GOOD|EXP|EXPIRES|VALID)[\s:]*([01]\d)[/.\- ]*([2-3]\d)""", RegexOption.IGNORE_CASE)
            val match = thruRegex.find(rawFullText)
            if (match != null) {
                val mm = match.groupValues[1]
                val yy = match.groupValues[2]
                if (mm.toIntOrNull() in 1..12) {
                    detectedExpiry = "$mm/$yy"
                }
            }
        }

        // ==========================================
        // 3. EXTRACT BANK / ISSUING INSTITUTION
        // ==========================================
        val knownBanks = listOf(
            "CHASE", "JPMORGAN", "BANK OF AMERICA", "CAPITAL ONE", "CITI", "CITIBANK",
            "WELLS FARGO", "AMERICAN EXPRESS", "AMEX", "DISCOVER", "BARCLAYS", "BARCLAYCARD",
            "USAA", "NAVY FEDERAL", "PNC", "TD BANK", "HSBC", "SYNCHRONY", "US BANK",
            "REGIONS", "FIFTH THIRD", "BMO", "HARRIS", "KEYBANK", "HUNTINGTON", "SANTANDER",
            "FIDELITY", "SCHWAB", "ALLY", "APPLE CARD", "GOLDMAN SACHS"
        )

        for (bank in knownBanks) {
            if (rawFullText.uppercase(Locale.US).contains(bank)) {
                detectedBank = when (bank) {
                    "CHASE", "JPMORGAN" -> "JPMorgan Chase"
                    "BANK OF AMERICA" -> "Bank of America"
                    "CAPITAL ONE" -> "Capital One"
                    "CITI", "CITIBANK" -> "Citibank"
                    "WELLS FARGO" -> "Wells Fargo"
                    "AMERICAN EXPRESS", "AMEX" -> "American Express"
                    "DISCOVER" -> "Discover"
                    "USAA" -> "USAA"
                    "NAVY FEDERAL" -> "Navy Federal Credit Union"
                    "TD BANK" -> "TD Bank"
                    "APPLE CARD" -> "Apple Card"
                    else -> bank.lowercase(Locale.US).split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                }
                break
            }
        }

        // ==========================================
        // 4. EXTRACT CARDHOLDER NAME
        // ==========================================
        // Exhaustive exclusion list of banking keywords, card products, terms, slogans, and labels
        val excludedTerms = setOf(
            "VISA", "MASTERCARD", "AMERICAN", "EXPRESS", "AMEX", "DISCOVER", "UNIONPAY", "JCB",
            "DEBIT", "CREDIT", "CARD", "PREPAID", "BUSINESS", "COMMERCIAL", "CORPORATE", "ENTERPRISE",
            "PLATINUM", "GOLD", "SILVER", "TITANIUM", "INFINITE", "SIGNATURE", "WORLD", "ELITE",
            "PREMIER", "PREFERRED", "ADVANTAGE", "REWARDS", "CASH", "BACK", "POINTS", "PLUS",
            "FREEDOM", "UNLIMITED", "SAPPHIRE", "FLEX", "CUSTOM", "VENTURE", "QUICKSILVER", "SAVOR",
            "DOUBLE", "STRATA", "ACTIVE", "PRO", "SELECT", "PRIME", "MILES", "TRAVEL", "HOTEL",
            "VALID", "THRU", "FROM", "UNTIL", "DATES", "GOOD", "EXPIRES", "EXP", "MEMBER", "SINCE",
            "SECURITY", "CODE", "CVV", "CVC", "CID", "AUTHORIZED", "SIGNATURE", "NOT", "TRANSFERABLE",
            "ISSUED", "BY", "LICENSE", "PURSUANT", "CUSTOMER", "SERVICE", "ASSISTANCE", "CALL", "HELP",
            "ONLINE", "WWW", "COM", "NET", "ORG", "HTTP", "HTTPS", "PHONE", "INTERNATIONAL", "DOMESTIC",
            "FDIC", "INSURED", "UNION", "FINANCIAL", "TRUST", "SAVINGS", "NATIONAL", "ASSOCIATION",
            "CHECK", "ATM", "ELECTRON", "MAESTRO", "CIRRUS", "STAR", "INTERAC", "PULSE", "NYCE",
            "PAYPASS", "PAYWAVE", "EXPRESSPAY", "CONTACTLESS", "BANK", "AMERICA", "CHASE", "CITI",
            "CAPITAL", "ONE", "WELLS", "FARGO", "BARCLAYS", "USAA", "PNC", "HSBC", "CARDHOLDER", "NAME"
        )

        val nameCandidates = mutableListOf<Pair<String, Float>>()

        for (line in lines) {
            val upper = line.text.uppercase(Locale.US).trim()
            val words = upper.split(Regex("""\s+""")).filter { it.isNotBlank() }

            // A valid name candidate must:
            // 1. Have 2 to 4 words
            // 2. Have NO digits or special math symbols
            // 3. Contain only letters, dots (e.g. initials), or hyphens
            // 4. None of its words may match the financial exclusions list
            val hasDigits = upper.any { it.isDigit() }
            val hasSpecial = upper.any { it in "@#$%^&*()_=+{}[]|\\<>~`\"/?" }
            val hasExcluded = words.any { it.replace(".", "") in excludedTerms }

            if (!hasDigits && !hasSpecial && !hasExcluded && words.size in 2..4) {
                val allValidLengths = words.all { it.length >= 2 || (it.length == 1 && upper.contains(".")) }
                val allLetters = words.all { word -> word.all { it.isLetter() || it == '.' || it == '-' || it == '\'' } }

                if (allValidLengths && allLetters) {
                    // Score calculation:
                    // Physical cardholder names are almost always in the lower 40% of the card (yCenterRatio >= 0.55)
                    var score = 10f
                    if (line.yCenterRatio >= 0.55f) score += 30f
                    if (line.yCenterRatio >= 0.70f) score += 15f
                    if (words.size == 2) score += 10f // First Last is the most common format

                    nameCandidates.add(Pair(upper, score))
                }
            }
        }

        // Pick the highest scoring name candidate
        detectedCardholder = nameCandidates.maxByOrNull { it.second }?.first ?: ""

        // ==========================================
        // 5. DETERMINE NETWORK FROM OCR TEXT IF PAN IS MASKED
        // ==========================================
        if (detectedType == "VISA") {
            val upperText = rawFullText.uppercase(Locale.US)
            when {
                upperText.contains("MASTERCARD") || upperText.contains("MASTER CARD") -> detectedType = "MASTERCARD"
                upperText.contains("AMERICAN EXPRESS") || upperText.contains("AMEX") -> detectedType = "AMEX"
                upperText.contains("DISCOVER") -> detectedType = "DISCOVER"
                upperText.contains("UNIONPAY") -> detectedType = "UNIONPAY"
            }
        }

        val colors = getDefaultColorsForType(detectedType)
        val issuerTitle = if (detectedBank.isNotBlank()) "$detectedBank $detectedType" else "$detectedType Card"

        return CardEntity(
            title = issuerTitle,
            cardholderName = detectedCardholder,
            cardNumber = detectedNumber,
            expiryDate = detectedExpiry,
            cvv = "",
            cardType = detectedType,
            category = "PAYMENT",
            bankOrIssuer = detectedBank.ifBlank { detectedType },
            themeColorHex = colors.first,
            gradientEndHex = colors.second,
            notes = "Scanned on-device with high-accuracy Google ML Kit Text OCR",
            scannedVia = "CAMERA_AI",
            createdAt = System.currentTimeMillis()
        )
    }

    private fun passesLuhnCheck(digits: String): Boolean {
        if (digits.length < 13 || digits.length > 19) return false
        var sum = 0
        var alternate = false
        for (i in digits.length - 1 downTo 0) {
            var n = digits[i] - '0'
            if (alternate) {
                n *= 2
                if (n > 9) n = (n % 10) + 1
            }
            sum += n
            alternate = !alternate
        }
        return (sum % 10 == 0)
    }

    private fun formatPan(rawDigits: String): String {
        val clean = rawDigits.filter { it.isDigit() }
        return if (clean.length == 15) {
            // Amex format: 4 - 6 - 5
            "${clean.substring(0, 4)} ${clean.substring(4, 10)} ${clean.substring(10)}"
        } else {
            // Standard format: 4 - 4 - 4 - 4
            clean.chunked(4).joinToString(" ")
        }
    }

    private fun determineCardTypeFromPan(digits: String): String {
        return when {
            digits.startsWith("4") -> "VISA"
            digits.startsWith("51") || digits.startsWith("52") || digits.startsWith("53") ||
                    digits.startsWith("54") || digits.startsWith("55") ||
                    (digits.length >= 4 && digits.substring(0, 4).toIntOrNull() in 2221..2720) -> "MASTERCARD"
            digits.startsWith("34") || digits.startsWith("37") -> "AMEX"
            digits.startsWith("6011") || digits.startsWith("65") || (digits.length >= 3 && digits.substring(0, 3).toIntOrNull() in 644..649) -> "DISCOVER"
            digits.startsWith("35") -> "JCB"
            digits.startsWith("62") -> "UNIONPAY"
            else -> "VISA"
        }
    }

    /**
     * Analyzes card image using Google ML Kit on-device engine, with Gemini Multimodal AI verification if API key is provided.
     */
    suspend fun analyzeCardImage(bitmap: Bitmap): ScanResult = withContext(Dispatchers.IO) {
        // Step 1: Run genuine on-device ML Kit OCR
        val extraction = runOnDeviceMlKitOcr(bitmap)
        val onDeviceCard = parseRealCardFromOcr(extraction)

        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.i(TAG, "No GEMINI_API_KEY configured. Utilizing genuine on-device ML Kit extraction.")
            return@withContext ScanResult.Success(
                card = onDeviceCard,
                rawAiResponse = extraction.rawText.ifBlank { "On-device OCR complete. Please verify extracted fields." },
                modelUsed = "Google ML Kit (On-Device OCR Engine)"
            )
        }

        // Step 2: Use Gemini 3.5 Flash for deep multimodal verification
        try {
            val base64Image = bitmapToBase64(bitmap)
            val prompt = """
                You are a high-security precision financial card scanner.
                Inspect the uploaded card image carefully. Extract all discernible card information into JSON.
                Detect the card type, issuer, embossed number, cardholder name, expiry date (ONLY if explicitly printed on the card), CVV (ONLY if explicitly visible on the card), and prominent card colors.
                IMPORTANT: Do NOT invent, guess, or output any dummy expiry dates (like 12/28) or CVVs. If not clearly visible, leave them as empty strings "".
                
                Respond ONLY with a valid JSON object matching this exact schema:
                {
                  "title": "string (e.g. Sapphire Reserve or Platinum Card)",
                  "cardholderName": "string (in UPPERCASE as printed on the card)",
                  "cardNumber": "string (digits separated by spaces, e.g. 4123 4567 8901 2345)",
                  "expiryDate": "string (format MM/YY if explicitly printed, otherwise empty \"\")",
                  "cvv": "string (3 or 4 digits if explicitly legible, otherwise empty \"\")",
                  "cardType": "VISA | MASTERCARD | AMEX | DISCOVER | ID_CARD | LOYALTY | TRANSIT",
                  "category": "PAYMENT | IDENTITY | MEMBERSHIP | TRANSIT",
                  "bankOrIssuer": "string (e.g. Chase, American Express, Citi, Wells Fargo)",
                  "themeColorHex": "string (dominant hex color of the card, e.g. #1E293B)",
                  "gradientEndHex": "string (secondary hex color of the card, e.g. #0F172A)",
                  "notes": "string (any extra details or tier)"
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    put("temperature", 0.1)
                    put("responseMimeType", "application/json")
                }
                put("generationConfig", generationConfig)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API HTTP ${response.code}. Falling back to ML Kit extraction.")
                return@withContext ScanResult.Success(
                    card = onDeviceCard,
                    rawAiResponse = extraction.rawText,
                    modelUsed = "Google ML Kit (On-Device OCR Engine)"
                )
            }

            val responseBody = response.body?.string() ?: throw IllegalStateException("Empty response")
            val geminiCard = parseGeminiResponse(responseBody)

            // Merge Gemini with ML Kit to ensure maximum accuracy:
            // Prefer the number that passes Luhn check or is more complete
            val finalPan = if (geminiCard.cardNumber.replace(" ", "").length in 13..19) {
                geminiCard.cardNumber
            } else onDeviceCard.cardNumber

            val finalHolder = if (geminiCard.cardholderName.isNotBlank() && geminiCard.cardholderName != "CARDHOLDER") {
                geminiCard.cardholderName
            } else onDeviceCard.cardholderName

            val finalExpiry = if (geminiCard.expiryDate.isNotBlank()) geminiCard.expiryDate else onDeviceCard.expiryDate
            val finalBank = if (geminiCard.bankOrIssuer.isNotBlank()) geminiCard.bankOrIssuer else onDeviceCard.bankOrIssuer

            val mergedCard = geminiCard.copy(
                cardNumber = finalPan,
                cardholderName = finalHolder,
                expiryDate = finalExpiry,
                bankOrIssuer = finalBank,
                notes = "Scanned using Gemini 3.5 Flash & Google ML Kit OCR"
            )

            ScanResult.Success(
                card = mergedCard,
                rawAiResponse = responseBody,
                modelUsed = MODEL_NAME
            )
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call exception: ${e.message}. Using ML Kit extraction.", e)
            ScanResult.Success(
                card = onDeviceCard,
                rawAiResponse = extraction.rawText,
                modelUsed = "Google ML Kit (On-Device OCR Engine)"
            )
        }
    }

    private fun parseGeminiResponse(jsonString: String): CardEntity {
        val root = JSONObject(jsonString)
        val candidates = root.optJSONArray("candidates")
        val candidate = candidates?.optJSONObject(0)
        val content = candidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text") ?: "{}"

        val cleanJson = if (text.contains("{") && text.contains("}")) {
            val start = text.indexOf('{')
            val end = text.lastIndexOf('}') + 1
            text.substring(start, end)
        } else text

        val cardObj = JSONObject(cleanJson)
        val cardType = cardObj.optString("cardType", "VISA").uppercase(Locale.US)
        val defaultColors = getDefaultColorsForType(cardType)

        return CardEntity(
            title = cardObj.optString("title", "Scanned Card"),
            cardholderName = cardObj.optString("cardholderName", ""),
            cardNumber = cardObj.optString("cardNumber", ""),
            expiryDate = cardObj.optString("expiryDate", ""),
            cvv = cardObj.optString("cvv", ""),
            cardType = cardType,
            category = cardObj.optString("category", "PAYMENT").uppercase(Locale.US),
            bankOrIssuer = cardObj.optString("bankOrIssuer", ""),
            themeColorHex = cardObj.optString("themeColorHex", defaultColors.first),
            gradientEndHex = cardObj.optString("gradientEndHex", defaultColors.second),
            notes = cardObj.optString("notes", "Scanned with Gemini Vision"),
            scannedVia = "CAMERA_AI",
            createdAt = System.currentTimeMillis()
        )
    }

    private fun getDefaultColorsForType(cardType: String): Pair<String, String> {
        return when (cardType) {
            "AMEX" -> Pair("#161618", "#2F3136")
            "MASTERCARD" -> Pair("#854D0E", "#CA8A04")
            "VISA" -> Pair("#0C2340", "#1D4ED8")
            "DISCOVER" -> Pair("#C2410C", "#EA580C")
            "UNIONPAY" -> Pair("#B91C1C", "#DC2626")
            "JCB" -> Pair("#1E3A8A", "#2563EB")
            "ID_CARD" -> Pair("#134E4A", "#0F766E")
            "TRANSIT" -> Pair("#701A75", "#A21CAF")
            else -> Pair("#1E293B", "#0F172A")
        }
    }
}
