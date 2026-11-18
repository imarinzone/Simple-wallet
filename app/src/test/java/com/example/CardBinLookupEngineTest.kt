package com.example

import com.example.data.CardBinLookupEngine
import com.example.data.PaymentNetwork
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardBinLookupEngineTest {

    @Test
    fun detectNetwork_supportsAllEightNetworks() {
        assertEquals(PaymentNetwork.VISA.key, CardBinLookupEngine.detectNetwork("4"))
        assertEquals(PaymentNetwork.VISA.key, CardBinLookupEngine.detectNetwork("4147201234567890"))

        assertEquals(PaymentNetwork.MASTERCARD.key, CardBinLookupEngine.detectNetwork("51"))
        assertEquals(PaymentNetwork.MASTERCARD.key, CardBinLookupEngine.detectNetwork("5599123456789012"))
        assertEquals(PaymentNetwork.MASTERCARD.key, CardBinLookupEngine.detectNetwork("2221001234567890"))
        assertEquals(PaymentNetwork.MASTERCARD.key, CardBinLookupEngine.detectNetwork("2720991234567890"))

        assertEquals(PaymentNetwork.RUPAY.key, CardBinLookupEngine.detectNetwork("5086071234567890"))
        assertEquals(PaymentNetwork.RUPAY.key, CardBinLookupEngine.detectNetwork("6070941234567890"))
        assertEquals(PaymentNetwork.RUPAY.key, CardBinLookupEngine.detectNetwork("6521501234567890"))
        assertEquals(PaymentNetwork.RUPAY.key, CardBinLookupEngine.detectNetwork("8172871234567890"))
        assertEquals(PaymentNetwork.RUPAY.key, CardBinLookupEngine.detectNetwork("8200123456789012"))

        assertEquals(PaymentNetwork.AMEX.key, CardBinLookupEngine.detectNetwork("340020000000007"))
        assertEquals(PaymentNetwork.AMEX.key, CardBinLookupEngine.detectNetwork("378282246310005"))

        assertEquals(PaymentNetwork.DINERS.key, CardBinLookupEngine.detectNetwork("30001234567890"))
        assertEquals(PaymentNetwork.DINERS.key, CardBinLookupEngine.detectNetwork("30951234567890"))
        assertEquals(PaymentNetwork.DINERS.key, CardBinLookupEngine.detectNetwork("36001234567890"))
        assertEquals(PaymentNetwork.DINERS.key, CardBinLookupEngine.detectNetwork("38001234567890"))
        assertEquals(PaymentNetwork.DINERS.key, CardBinLookupEngine.detectNetwork("39001234567890"))

        assertEquals(PaymentNetwork.DISCOVER.key, CardBinLookupEngine.detectNetwork("6011000990139424"))
        assertEquals(PaymentNetwork.DISCOVER.key, CardBinLookupEngine.detectNetwork("6221260000000000"))
        assertEquals(PaymentNetwork.DISCOVER.key, CardBinLookupEngine.detectNetwork("6229250000000000"))
        assertEquals(PaymentNetwork.DISCOVER.key, CardBinLookupEngine.detectNetwork("6440000000000000"))
        assertEquals(PaymentNetwork.DISCOVER.key, CardBinLookupEngine.detectNetwork("6500000000000000"))

        assertEquals(PaymentNetwork.JCB.key, CardBinLookupEngine.detectNetwork("3528000000000000"))
        assertEquals(PaymentNetwork.JCB.key, CardBinLookupEngine.detectNetwork("3589999999999999"))

        assertEquals(PaymentNetwork.UNIONPAY.key, CardBinLookupEngine.detectNetwork("6200000000000000"))
        assertEquals(PaymentNetwork.UNIONPAY.key, CardBinLookupEngine.detectNetwork("6221250000000000"))
        assertEquals(PaymentNetwork.UNIONPAY.key, CardBinLookupEngine.detectNetwork("6229260000000000"))
    }

    @Test
    fun detectNetwork_handlesSpacesAndShortInputsSafely() {
        assertNull(CardBinLookupEngine.detectNetwork(""))
        assertNull(CardBinLookupEngine.detectNetwork("   "))
        assertNull(CardBinLookupEngine.detectNetwork("9999"))
        assertEquals(PaymentNetwork.VISA.key, CardBinLookupEngine.detectNetwork("4014 0312 3456 7890"))
    }

    @Test
    fun passesLuhnCheck_validatesStandardAndAmexNumbers() {
        assertTrue(CardBinLookupEngine.passesLuhnCheck("4030090000000007"))
        assertFalse(CardBinLookupEngine.passesLuhnCheck("4030090000000008"))
        assertTrue(CardBinLookupEngine.passesLuhnCheck("340020000000007"))
        assertTrue(CardBinLookupEngine.passesLuhnCheck("3782 822463 10005"))
        assertFalse(CardBinLookupEngine.passesLuhnCheck("4111"))
    }

    @Test
    fun offlineBankBinLookup_resolvesMajorIndianAndGlobalIssuers() {
        val hdfcVisa = CardBinLookupEngine.lookup("4014031234567890")
        assertEquals("VISA", hdfcVisa.network)
        assertEquals("HDFC Bank", hdfcVisa.issuerBank)

        val hdfcRupay = CardBinLookupEngine.lookup("5086071234567890")
        assertEquals("RUPAY", hdfcRupay.network)
        assertEquals("HDFC Bank", hdfcRupay.issuerBank)

        val sbiVisa = CardBinLookupEngine.lookup("4030090000000007")
        assertEquals("VISA", sbiVisa.network)
        assertEquals("SBI Card", sbiVisa.issuerBank)
        assertTrue(sbiVisa.isLuhnValid)

        val sbiDebitRupay = CardBinLookupEngine.lookup("6070941234567890")
        assertEquals("RUPAY", sbiDebitRupay.network)
        assertEquals("State Bank of India", sbiDebitRupay.issuerBank)

        val hsbcVisa = CardBinLookupEngine.lookup("4384111234567890")
        assertEquals("VISA", hsbcVisa.network)
        assertEquals("HSBC", hsbcVisa.issuerBank)

        val hsbcMastercard = CardBinLookupEngine.lookup("5431271234567890")
        assertEquals("MASTERCARD", hsbcMastercard.network)
        assertEquals("HSBC", hsbcMastercard.issuerBank)

        val icici = CardBinLookupEngine.lookup("4375511234567890")
        assertEquals("ICICI Bank", icici.issuerBank)

        val axis = CardBinLookupEngine.lookup("4052681234567890")
        assertEquals("Axis Bank", axis.issuerBank)

        val chase = CardBinLookupEngine.lookup("4147201234567890")
        assertEquals("JPMorgan Chase", chase.issuerBank)

        val citi = CardBinLookupEngine.lookup("5218761234567890")
        assertEquals("Citibank", citi.issuerBank)

        val capOne = CardBinLookupEngine.lookup("5155991234567890")
        assertEquals("Capital One", capOne.issuerBank)

        val bofa = CardBinLookupEngine.lookup("4313011234567890")
        assertEquals("Bank of America", bofa.issuerBank)

        val wells = CardBinLookupEngine.lookup("4167241234567890")
        assertEquals("Wells Fargo", wells.issuerBank)

        // Longest prefix match (6 digits over 4 digits)
        val firstDirect = CardBinLookupEngine.lookup("4543123456789012")
        assertEquals("First Direct (HSBC UK)", firstDirect.issuerBank)

        // Unmapped Visa BIN still resolves network cleanly with null issuerBank
        val unknownVisa = CardBinLookupEngine.lookup("4999991234567890")
        assertEquals("VISA", unknownVisa.network)
        assertNull(unknownVisa.issuerBank)
    }

    @Test
    fun nfcPrecedence_neverOverridesNonGenericNfcNetworkOrIssuer() {
        // 1. NFC gave specific network + specific Tag 50/9F12 issuer label -> preserve both even if PAN differs
        val preserved = CardBinLookupEngine.resolveWithNfcPrecedence(
            existingNetwork = "MASTERCARD",
            existingBank = "HDFC REGALIA GOLD",
            rawCardNumber = "4030090000000007", // SBI Visa BIN
            isFromNfc = true
        )
        assertEquals("MASTERCARD", preserved.network)
        assertEquals("HDFC REGALIA GOLD", preserved.bankOrIssuer)
        assertFalse(preserved.filledNetworkFromBin)
        assertFalse(preserved.filledBankFromBin)

        // 2. NFC gave VISA + generic "Visa Contactless" bank placeholder -> keep VISA, fill SBI Card bank
        val fillGenericBank = CardBinLookupEngine.resolveWithNfcPrecedence(
            existingNetwork = "VISA",
            existingBank = "Visa Contactless",
            rawCardNumber = "4030090000000007",
            isFromNfc = true
        )
        assertEquals("VISA", fillGenericBank.network)
        assertEquals("SBI Card", fillGenericBank.bankOrIssuer)
        assertFalse(fillGenericBank.filledNetworkFromBin)
        assertTrue(fillGenericBank.filledBankFromBin)

        // 3. NFC had generic "CONTACTLESS" network and blank bank -> fill both from BIN
        val fillBothMissing = CardBinLookupEngine.resolveWithNfcPrecedence(
            existingNetwork = "CONTACTLESS",
            existingBank = "Contactless Card",
            rawCardNumber = "4384111234567890",
            isFromNfc = true
        )
        assertEquals("VISA", fillBothMissing.network)
        assertEquals("HSBC", fillBothMissing.bankOrIssuer)
        assertTrue(fillBothMissing.filledNetworkFromBin)
        assertTrue(fillBothMissing.filledBankFromBin)
    }
}
