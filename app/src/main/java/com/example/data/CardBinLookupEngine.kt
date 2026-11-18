package com.example.data

import java.util.Locale

enum class PaymentNetwork(
    val key: String,
    val displayName: String,
    val validLengths: IntRange,
    val defaultColors: Pair<String, String>
) {
    VISA("VISA", "Visa", 13..19, "#0C2340" to "#1D4ED8"),
    MASTERCARD("MASTERCARD", "Mastercard", 16..16, "#854D0E" to "#CA8A04"),
    RUPAY("RUPAY", "RuPay", 16..16, "#065F46" to "#047857"),
    AMEX("AMEX", "AMEX", 15..15, "#161618" to "#2F3136"),
    DINERS("DINERS", "Diners Club", 14..16, "#0F172A" to "#1E3A8A"),
    DISCOVER("DISCOVER", "Discover", 16..19, "#C2410C" to "#EA580C"),
    JCB("JCB", "JCB", 16..19, "#1E3A8A" to "#2563EB"),
    UNIONPAY("UNIONPAY", "UnionPay", 16..19, "#B91C1C" to "#DC2626");

    companion object {
        fun fromKey(raw: String?): PaymentNetwork? {
            val normalized = raw?.trim()?.uppercase(Locale.US).orEmpty()
            return when {
                normalized == "VISA" -> VISA
                normalized == "MASTERCARD" -> MASTERCARD
                normalized == "RUPAY" -> RUPAY
                normalized == "AMEX" || normalized == "AMERICAN EXPRESS" -> AMEX
                normalized == "DINERS" || normalized.contains("DINER") -> DINERS
                normalized == "DISCOVER" -> DISCOVER
                normalized == "JCB" -> JCB
                normalized == "UNIONPAY" -> UNIONPAY
                else -> null
            }
        }
    }
}

data class BinIssuerRecord(
    val prefix: String,
    val issuerBank: String,
    val networkKey: String? = null,
    val cardProductType: String = "",
    val colors: Pair<String, String>? = null
)

data class CardBinLookupResult(
    val digits: String,
    val network: String?,
    val networkDisplayName: String?,
    val issuerBank: String?,
    val cardProductType: String?,
    val isLuhnValid: Boolean,
    val isCompleteLengthForLuhn: Boolean,
    val matchedPrefix: String?,
    val suggestedColors: Pair<String, String>?
)

data class NfcBinMergeResult(
    val network: String,
    val bankOrIssuer: String,
    val filledNetworkFromBin: Boolean,
    val filledBankFromBin: Boolean
)

/**
 * Strictly offline IIN/BIN calculation and issuing bank lookup engine.
 * - Detects 8 payment networks (Visa, Mastercard, RuPay, Amex, Diners Club, Discover, JCB, UnionPay)
 *   using longest/most-specific prefix rules so overlapping ranges never collide.
 * - Resolves issuing bank / merchant (e.g., SBI Card, HDFC Bank, HSBC, ICICI Bank, Axis Bank, Chase, etc.)
 *   using longest-prefix match (8 -> 6 -> 5 -> 4 digits) from a curated offline BIN table.
 * - Enforces NFC non-override rules: existing non-generic NFC network and issuer labels are preserved.
 */
object CardBinLookupEngine {

    private val GENERIC_NETWORK_VALUES = setOf(
        "", "CONTACTLESS", "UNKNOWN", "CARD", "PAYMENT", "MISC"
    )

    private val GENERIC_NFC_BANK_VALUES = setOf(
        "",
        "contactless",
        "contactless card",
        "visa contactless",
        "mastercard contactless",
        "rupay contactless",
        "amex contactless",
        "discover contactless",
        "diners contactless",
        "jcb contactless",
        "unionpay contactless",
        "visa",
        "mastercard",
        "rupay",
        "diners",
        "diners club",
        "jcb",
        "unionpay"
    )

    private val BANK_BRAND_COLORS: Map<String, Pair<String, String>> = mapOf(
        "HDFC Bank" to ("#0B2545" to "#1D4ED8"),
        "SBI Card" to ("#0F2942" to "#0284C7"),
        "State Bank of India" to ("#0E3A5D" to "#0284C7"),
        "HSBC" to ("#7F1D1D" to "#DC2626"),
        "First Direct (HSBC UK)" to ("#18181B" to "#3F3F46"),
        "ICICI Bank" to ("#7C2D12" to "#C2410C"),
        "Axis Bank" to ("#581C37" to "#9D174D"),
        "Kotak Mahindra Bank" to ("#881337" to "#DC2626"),
        "Saraswat Co-operative Bank" to ("#134E4A" to "#0F766E"),
        "JPMorgan Chase" to ("#0C2340" to "#1D4ED8"),
        "Citibank" to ("#075985" to "#0284C7"),
        "Capital One" to ("#0F172A" to "#334155"),
        "Bank of America" to ("#1E3A8A" to "#DC2626"),
        "Wells Fargo" to ("#7F1D1D" to "#B45309"),
        "Barclaycard" to ("#0369A1" to "#0284C7"),
        "MBNA" to ("#1E293B" to "#475569")
    )

    /**
     * Curated offline table of public IIN/BIN prefixes for major Indian and global issuing banks.
     * Indexed by prefix string; lookup checks longest match (8 down to 4 digits).
     */
    private val OFFLINE_BIN_LIST: List<BinIssuerRecord> = listOf(
        // ================= HDFC BANK =================
        BinIssuerRecord("401403", "HDFC Bank", "VISA", "Credit Platinum"),
        BinIssuerRecord("402219", "HDFC Bank", "VISA", "Credit Signature"),
        BinIssuerRecord("463917", "HDFC Bank", "VISA", "Credit Signature"),
        BinIssuerRecord("485498", "HDFC Bank", "VISA", "Credit Signature"),
        BinIssuerRecord("405533", "HDFC Bank", "VISA", "Credit/Debit"),
        BinIssuerRecord("405267", "HDFC Bank", "VISA", "Credit/Debit"),
        BinIssuerRecord("222703", "HDFC Bank", "MASTERCARD", "Credit Platinum"),
        BinIssuerRecord("518159", "HDFC Bank", "MASTERCARD", "Credit World"),
        BinIssuerRecord("508607", "HDFC Bank", "RUPAY", "Debit/Prepaid"),
        BinIssuerRecord("508608", "HDFC Bank", "RUPAY", "Debit/Prepaid"),
        BinIssuerRecord("508610", "HDFC Bank", "RUPAY", "Debit/Prepaid"),
        BinIssuerRecord("508614", "HDFC Bank", "RUPAY", "Debit/Prepaid"),
        BinIssuerRecord("607666", "HDFC Bank", "RUPAY", "Debit"),
        BinIssuerRecord("607673", "HDFC Bank", "RUPAY", "Debit"),
        BinIssuerRecord("817287", "HDFC Bank", "RUPAY", "Debit/Prepaid"),
        BinIssuerRecord("4042", "HDFC Bank", "VISA", "Credit/Debit"),
        BinIssuerRecord("4183", "HDFC Bank", "VISA", "Credit/Debit"),
        BinIssuerRecord("4214", "HDFC Bank", "VISA", "Debit"),

        // ================= SBI CARD & STATE BANK OF INDIA =================
        BinIssuerRecord("400667", "SBI Card", "VISA", "Credit Gold"),
        BinIssuerRecord("403009", "SBI Card", "VISA", "Credit Platinum"),
        BinIssuerRecord("403250", "SBI Card", "VISA", "Credit Signature"),
        BinIssuerRecord("409638", "SBI Card", "VISA", "Credit Rewards"),
        BinIssuerRecord("510223", "SBI Card", "MASTERCARD", "Credit World"),
        BinIssuerRecord("522967", "SBI Card", "MASTERCARD", "Credit"),
        BinIssuerRecord("340020", "SBI Card", "AMEX", "Credit"),
        BinIssuerRecord("340475", "SBI Card", "AMEX", "Credit"),
        BinIssuerRecord("370308", "SBI Card", "AMEX", "Credit"),
        BinIssuerRecord("607094", "State Bank of India", "RUPAY", "Debit Classic"),

        // ================= HSBC =================
        BinIssuerRecord("438411", "HSBC", "VISA", "Credit"),
        BinIssuerRecord("438430", "HSBC", "VISA", "Credit"),
        BinIssuerRecord("438431", "HSBC", "VISA", "Credit"),
        BinIssuerRecord("438446", "HSBC", "VISA", "Credit"),
        BinIssuerRecord("438468", "HSBC", "VISA", "Credit"),
        BinIssuerRecord("436499", "HSBC", "VISA", "Credit"),
        BinIssuerRecord("469874", "HSBC", "VISA", "Debit"),
        BinIssuerRecord("543127", "HSBC", "MASTERCARD", "Credit"),
        BinIssuerRecord("544211", "HSBC", "MASTERCARD", "Credit"),
        BinIssuerRecord("5431", "HSBC", "MASTERCARD", "Credit/Debit"),
        BinIssuerRecord("5434", "HSBC", "MASTERCARD", "Credit/Debit"),
        BinIssuerRecord("5442", "HSBC", "MASTERCARD", "Credit/Debit"),
        BinIssuerRecord("4543", "First Direct (HSBC UK)", "VISA", "Debit"),

        // ================= ICICI BANK =================
        BinIssuerRecord("437551", "ICICI Bank", "VISA", "Credit Platinum"),
        BinIssuerRecord("431581", "ICICI Bank", "VISA", "Credit Platinum"),
        BinIssuerRecord("407659", "ICICI Bank", "VISA", "Credit/Debit"),
        BinIssuerRecord("418157", "ICICI Bank", "VISA", "Credit/Debit"),
        BinIssuerRecord("222708", "ICICI Bank", "MASTERCARD", "Mastercard"),
        BinIssuerRecord("222709", "ICICI Bank", "MASTERCARD", "Mastercard"),
        BinIssuerRecord("222710", "ICICI Bank", "MASTERCARD", "Mastercard"),
        BinIssuerRecord("222940", "ICICI Bank", "MASTERCARD", "Mastercard"),
        BinIssuerRecord("508596", "ICICI Bank", "RUPAY", "Debit/Prepaid"),
        BinIssuerRecord("508600", "ICICI Bank", "RUPAY", "Debit/Prepaid"),
        BinIssuerRecord("508611", "ICICI Bank", "RUPAY", "Debit/Prepaid"),
        BinIssuerRecord("607667", "ICICI Bank", "RUPAY", "Debit"),
        BinIssuerRecord("817309", "ICICI Bank", "RUPAY", "Debit/Prepaid"),
        BinIssuerRecord("4089", "ICICI Bank", "VISA", "Credit/Debit"),

        // ================= AXIS BANK =================
        BinIssuerRecord("405268", "Axis Bank", "VISA", "Credit/Debit"),
        BinIssuerRecord("407438", "Axis Bank", "VISA", "Credit/Debit"),
        BinIssuerRecord("411146", "Axis Bank", "VISA", "Credit/Debit"),
        BinIssuerRecord("418174", "Axis Bank", "VISA", "Credit/Debit"),
        BinIssuerRecord("533467", "Axis Bank", "MASTERCARD", "Credit World"),
        BinIssuerRecord("552260", "Axis Bank", "MASTERCARD", "Credit"),
        BinIssuerRecord("508530", "Axis Bank", "RUPAY", "Debit/Prepaid"),
        BinIssuerRecord("508602", "Axis Bank", "RUPAY", "Debit/Prepaid"),
        BinIssuerRecord("817291", "Axis Bank", "RUPAY", "Debit/Prepaid"),
        BinIssuerRecord("817302", "Axis Bank", "RUPAY", "Debit/Prepaid"),

        // ================= KOTAK MAHINDRA & REGIONAL INDIA =================
        BinIssuerRecord("416644", "Kotak Mahindra Bank", "VISA", "Credit Platinum"),
        BinIssuerRecord("405393", "Kotak Mahindra Bank", "VISA", "Prepaid Corporate"),
        BinIssuerRecord("652150", "Saraswat Co-operative Bank", "RUPAY", "Debit Classic"),

        // ================= GLOBAL ISSUERS (CHASE, CITI, CAPITAL ONE, BOFA, WELLS FARGO, UK) =================
        BinIssuerRecord("414720", "JPMorgan Chase", "VISA", "Credit Rewards"),
        BinIssuerRecord("414740", "JPMorgan Chase", "VISA", "Credit Rewards"),
        BinIssuerRecord("414840", "JPMorgan Chase", "VISA", "Credit Rewards"),
        BinIssuerRecord("420767", "JPMorgan Chase", "VISA", "Debit/Credit"),
        BinIssuerRecord("467321", "JPMorgan Chase", "VISA", "Credit"),
        BinIssuerRecord("5466", "JPMorgan Chase", "MASTERCARD", "Credit"),
        BinIssuerRecord("5528", "JPMorgan Chase", "MASTERCARD", "Credit"),

        BinIssuerRecord("414711", "Citibank", "VISA", "Credit Signature"),
        BinIssuerRecord("405451", "Citibank", "VISA", "Credit Gold"),
        BinIssuerRecord("413085", "Citibank", "VISA", "Credit"),
        BinIssuerRecord("521876", "Citibank", "MASTERCARD", "Credit World"),
        BinIssuerRecord("517479", "Citibank", "MASTERCARD", "Credit"),
        BinIssuerRecord("518752", "Citibank", "MASTERCARD", "Credit"),

        BinIssuerRecord("425418", "Capital One", "VISA", "Credit Signature"),
        BinIssuerRecord("515599", "Capital One", "MASTERCARD", "Credit"),
        BinIssuerRecord("517669", "Capital One", "MASTERCARD", "Credit"),
        BinIssuerRecord("520094", "Capital One", "MASTERCARD", "Credit"),
        BinIssuerRecord("4143", "Capital One", "VISA", "Credit"),

        BinIssuerRecord("414716", "Bank of America", "VISA", "Credit Signature"),
        BinIssuerRecord("409975", "Bank of America", "VISA", "Credit/Debit"),
        BinIssuerRecord("431301", "Bank of America", "VISA", "Credit Preferred"),
        BinIssuerRecord("431302", "Bank of America", "VISA", "Credit Preferred"),
        BinIssuerRecord("431303", "Bank of America", "VISA", "Credit Preferred"),
        BinIssuerRecord("431304", "Bank of America", "VISA", "Credit Preferred"),
        BinIssuerRecord("431305", "Bank of America", "VISA", "Credit Preferred"),
        BinIssuerRecord("431306", "Bank of America", "VISA", "Credit Preferred"),
        BinIssuerRecord("431307", "Bank of America", "VISA", "Credit Preferred"),
        BinIssuerRecord("431308", "Bank of America", "VISA", "Credit Preferred"),
        BinIssuerRecord("417008", "Bank of America", "VISA", "Business"),
        BinIssuerRecord("417009", "Bank of America", "VISA", "Business"),
        BinIssuerRecord("417010", "Bank of America", "VISA", "Business"),
        BinIssuerRecord("417011", "Bank of America", "VISA", "Business"),

        BinIssuerRecord("416724", "Wells Fargo", "VISA", "Debit"),
        BinIssuerRecord("414718", "Wells Fargo", "VISA", "Credit/Debit"),
        BinIssuerRecord("446542", "Wells Fargo", "VISA", "Credit Classic"),

        BinIssuerRecord("4129", "MBNA", "VISA", "Credit"),
        BinIssuerRecord("5425", "Barclaycard", "MASTERCARD", "Credit")
    )

    private val BIN_MAP: Map<String, BinIssuerRecord> = OFFLINE_BIN_LIST.associateBy { it.prefix }

    /**
     * Detects the payment network strictly from the card number (PAN) digits using standard
     * ISO/IEC 7812 IIN/BIN ranges. Evaluates specific ranges before broad prefixes.
     */
    fun detectNetwork(rawCardNumber: String): String? {
        val digits = rawCardNumber.filter { it.isDigit() }
        if (digits.isEmpty()) return null

        val prefix2 = if (digits.length >= 2) digits.substring(0, 2).toIntOrNull() else null
        val prefix3 = if (digits.length >= 3) digits.substring(0, 3).toIntOrNull() else null
        val prefix4 = if (digits.length >= 4) digits.substring(0, 4).toIntOrNull() else null
        val prefix6 = if (digits.length >= 6) digits.substring(0, 6).toIntOrNull() else null

        // 1. Visa: always starts with 4
        if (digits.startsWith("4")) {
            return PaymentNetwork.VISA.key
        }

        // 2. American Express: 34, 37
        if (prefix2 == 34 || prefix2 == 37) {
            return PaymentNetwork.AMEX.key
        }

        // 3. JCB: 3528-3589
        if (prefix4 != null && prefix4 in 3528..3589) {
            return PaymentNetwork.JCB.key
        }

        // 4. Diners Club: 300-305, 3095, 36, 38-39
        if (
            (prefix3 != null && prefix3 in 300..305) ||
            prefix4 == 3095 ||
            prefix2 == 36 ||
            prefix2 == 38 ||
            prefix2 == 39
        ) {
            return PaymentNetwork.DINERS.key
        }

        // 5. Mastercard: 51-55 or 2221-2720
        if (
            (prefix2 != null && prefix2 in 51..55) ||
            (prefix4 != null && prefix4 in 2221..2720)
        ) {
            return PaymentNetwork.MASTERCARD.key
        }

        // 6. Discover specific 6-digit China UnionPay overlap range: 622126-622925
        if (prefix6 != null && prefix6 in 622126..622925) {
            return PaymentNetwork.DISCOVER.key
        }

        // 7. RuPay (India NPCI): check before Discover broad `65` and Maestro overlaps
        if (
            prefix3 == 508 ||
            (prefix6 != null && prefix6 in 508500..508999) ||
            (prefix3 != null && prefix3 in 606..608) ||
            (prefix6 != null && prefix6 in 606985..608500) ||
            (prefix4 != null && prefix4 in 6521..6531) ||
            (prefix6 != null && prefix6 in 652150..653149) ||
            prefix2 == 81 ||
            prefix2 == 82
        ) {
            return PaymentNetwork.RUPAY.key
        }

        // 8. Discover: 6011, 644-649, 65 (non-RuPay)
        if (
            prefix4 == 6011 ||
            (prefix3 != null && prefix3 in 644..649) ||
            prefix2 == 65
        ) {
            return PaymentNetwork.DISCOVER.key
        }

        // 9. UnionPay: 62 (outside Discover 622126-622925)
        if (prefix2 == 62) {
            return PaymentNetwork.UNIONPAY.key
        }

        return null
    }

    /**
     * Looks up the issuing bank/institution from the offline BIN table using longest-prefix matching
     * (8 -> 6 -> 5 -> 4 digits), falling back to scheme-owned issuers for Amex and Discover.
     */
    fun lookupIssuerRecord(rawCardNumber: String, detectedNetwork: String? = detectNetwork(rawCardNumber)): BinIssuerRecord? {
        val digits = rawCardNumber.filter { it.isDigit() }
        if (digits.length < 2) return null

        val candidateLengths = listOf(8, 6, 5, 4)
        for (len in candidateLengths) {
            if (digits.length >= len) {
                val prefix = digits.substring(0, len)
                BIN_MAP[prefix]?.let { return it }
            }
        }

        // Closed-loop / scheme-owned defaults when user enters at least 4 digits
        if (digits.length >= 4) {
            when (detectedNetwork) {
                PaymentNetwork.AMEX.key -> return BinIssuerRecord(
                    prefix = digits.take(4),
                    issuerBank = "American Express",
                    networkKey = PaymentNetwork.AMEX.key,
                    cardProductType = "Credit"
                )
                PaymentNetwork.DISCOVER.key -> return BinIssuerRecord(
                    prefix = digits.take(4),
                    issuerBank = "Discover",
                    networkKey = PaymentNetwork.DISCOVER.key,
                    cardProductType = "Credit"
                )
            }
        }

        return null
    }

    /**
     * Full offline BIN lookup combining network detection, issuer lookup, theme colors,
     * and ISO/IEC 7812 Luhn checksum verification.
     */
    fun lookup(rawCardNumber: String): CardBinLookupResult {
        val digits = rawCardNumber.filter { it.isDigit() }
        val networkFromRule = detectNetwork(digits)
        val issuerRecord = lookupIssuerRecord(digits, networkFromRule)
        val resolvedNetwork = networkFromRule ?: issuerRecord?.networkKey
        val networkEnum = PaymentNetwork.fromKey(resolvedNetwork)
        val luhnEligible = networkEnum?.let { digits.length in it.validLengths } ?: (digits.length in 13..19)
        val isLuhnValid = luhnEligible && passesLuhnCheck(digits)
        val colors = issuerRecord?.let { BANK_BRAND_COLORS[it.issuerBank] } ?: networkEnum?.defaultColors

        return CardBinLookupResult(
            digits = digits,
            network = resolvedNetwork,
            networkDisplayName = networkEnum?.displayName,
            issuerBank = issuerRecord?.issuerBank,
            cardProductType = issuerRecord?.cardProductType?.takeIf { it.isNotBlank() },
            isLuhnValid = isLuhnValid,
            isCompleteLengthForLuhn = luhnEligible,
            matchedPrefix = issuerRecord?.prefix,
            suggestedColors = colors
        )
    }

    /**
     * ISO/IEC 7812 Luhn Mod-10 checksum validator.
     */
    fun passesLuhnCheck(rawNumber: String): Boolean {
        val digits = rawNumber.filter { it.isDigit() }
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
        return sum % 10 == 0
    }

    fun isKnownPaymentNetwork(network: String?): Boolean {
        return PaymentNetwork.fromKey(network) != null
    }

    fun isGenericNfcNetwork(network: String?): Boolean {
        val norm = network?.trim()?.uppercase(Locale.US).orEmpty()
        return norm in GENERIC_NETWORK_VALUES || PaymentNetwork.fromKey(norm) == null
    }

    fun isGenericNfcBank(bankOrIssuer: String?): Boolean {
        val norm = bankOrIssuer?.trim()?.lowercase(Locale.ROOT).orEmpty()
        return norm in GENERIC_NFC_BANK_VALUES
    }

    /**
     * Merges card-number BIN findings with existing NFC values while enforcing strict NFC precedence:
     * - If NFC already provided a specific payment network, keep it intact.
     * - If NFC already provided a real (non-generic) bank/application label, keep it intact.
     * - Only populate missing or generic NFC fields from the card number BIN lookup.
     */
    fun resolveWithNfcPrecedence(
        existingNetwork: String,
        existingBank: String,
        rawCardNumber: String,
        isFromNfc: Boolean
    ): NfcBinMergeResult {
        val binResult = lookup(rawCardNumber)

        val canFillNetwork = if (isFromNfc) {
            isGenericNfcNetwork(existingNetwork)
        } else {
            true
        }

        val resolvedNetwork = if (canFillNetwork && !binResult.network.isNullOrBlank()) {
            binResult.network
        } else {
            existingNetwork
        }

        val canFillBank = if (isFromNfc) {
            isGenericNfcBank(existingBank)
        } else {
            existingBank.isBlank() || isGenericNfcBank(existingBank)
        }

        val resolvedBank = if (canFillBank && !binResult.issuerBank.isNullOrBlank()) {
            binResult.issuerBank
        } else {
            existingBank
        }

        return NfcBinMergeResult(
            network = resolvedNetwork,
            bankOrIssuer = resolvedBank,
            filledNetworkFromBin = canFillNetwork && !binResult.network.isNullOrBlank() && resolvedNetwork != existingNetwork,
            filledBankFromBin = canFillBank && !binResult.issuerBank.isNullOrBlank() && resolvedBank != existingBank
        )
    }

    fun getDefaultColorsForType(cardType: String, issuerBank: String? = null): Pair<String, String> {
        if (!issuerBank.isNullOrBlank()) {
            BANK_BRAND_COLORS[issuerBank]?.let { return it }
        }
        return when (cardType.uppercase(Locale.US)) {
            "AMEX" -> "#161618" to "#2F3136"
            "MASTERCARD" -> "#854D0E" to "#CA8A04"
            "VISA" -> "#0C2340" to "#1D4ED8"
            "RUPAY" -> "#065F46" to "#047857"
            "DINERS", "DINERS_CLUB", "DINNERCLUB" -> "#0F172A" to "#1E3A8A"
            "DISCOVER" -> "#C2410C" to "#EA580C"
            "UNIONPAY" -> "#B91C1C" to "#DC2626"
            "JCB" -> "#1E3A8A" to "#2563EB"
            "ID_CARD" -> "#134E4A" to "#0F766E"
            "TRANSIT" -> "#701A75" to "#A21CAF"
            else -> "#1E293B" to "#0F172A"
        }
    }
}
