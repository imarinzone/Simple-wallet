package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class CardArtDesign(
    val id: String,
    val name: String,
    val issuer: String,
    val cardType: String, // VISA, MASTERCARD, RUPAY, DINERS, etc.
    val imageUrl: String,
    val accentColorHex: String,
    val gradientEndHex: String,
    val description: String
)

/**
 * Service providing the curated catalog of official bank card artwork
 * (HDFC Bank & SBI Cards) from their official web portals.
 */
object CardArtOnlineService {

    /**
     * The verified catalog of official HDFC and SBI cards from the official website portals.
     */
    val OFFICIAL_CARD_DESIGNS: List<CardArtDesign> = listOf(
        // ================= HDFC BANK OFFICIAL CARDS =================
        CardArtDesign(
            id = "hdfc_pixel_play",
            name = "HDFC Pixel Play Credit Card",
            issuer = "HDFC Bank",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/card-facias/credit-card/pixel-play-b.png",
            accentColorHex = "#6366F1",
            gradientEndHex = "#4338CA",
            description = "Customizable digital-first card with retro pixel design and neon gradients"
        ),
        CardArtDesign(
            id = "hdfc_millennia",
            name = "HDFC Millennia Credit Card",
            issuer = "HDFC Bank",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/millennia-credit-card/images/millennia-credit-card.png",
            accentColorHex = "#1E293B",
            gradientEndHex = "#334155",
            description = "Cashback powerhouse with metallic dark slate finish and iridescent holographic badge"
        ),
        CardArtDesign(
            id = "hdfc_moneyback_plus",
            name = "HDFC MoneyBack+ Credit Card",
            issuer = "HDFC Bank",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/moneyback-plus-credit-card/images/moneyback-plus-credit-card.png",
            accentColorHex = "#0284C7",
            gradientEndHex = "#0369A1",
            description = "Rewards card with electric cobalt blue swirl and cashpoint multipliers"
        ),
        CardArtDesign(
            id = "hdfc_iocl",
            name = "IndianOil HDFC Bank Credit Card",
            issuer = "HDFC Bank / IndianOil",
            cardType = "RUPAY",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/card-facias/credit-card/iocl.png",
            accentColorHex = "#C2410C",
            gradientEndHex = "#9A3412",
            description = "Fuel credit card with IndianOil flame orange branding and free fuel reward points"
        ),
        CardArtDesign(
            id = "hdfc_freedom",
            name = "HDFC Freedom Credit Card",
            issuer = "HDFC Bank",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/freedom-credit-card/freedom-credit-card.png",
            accentColorHex = "#0D9488",
            gradientEndHex = "#0F766E",
            description = "Everyday rewards card with fresh teal finish and fuel surcharge waivers"
        ),
        CardArtDesign(
            id = "hdfc_swiggy",
            name = "Swiggy HDFC Bank Credit Card",
            issuer = "HDFC Bank / Swiggy",
            cardType = "MASTERCARD",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/swiggy-hdfc-bank-credit-card/images/card-facia-swiggy.png",
            accentColorHex = "#FC8019",
            gradientEndHex = "#EA580C",
            description = "Signature vibrant Swiggy orange dining cashback card with embossed lettering"
        ),
        CardArtDesign(
            id = "hdfc_infinia",
            name = "HDFC Infinia Metal Edition",
            issuer = "HDFC Bank",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/infinia-credit-card/images/infinia-credit-card.png",
            accentColorHex = "#0A0A0A",
            gradientEndHex = "#262626",
            description = "Super-premium invite-only pure metal card in midnight onyx with gold accents"
        ),
        CardArtDesign(
            id = "hdfc_diners_black",
            name = "HDFC Diners Club Black",
            issuer = "HDFC Bank",
            cardType = "DINERS",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/diners-club-black-credit-card/diners-club-black.png",
            accentColorHex = "#171717",
            gradientEndHex = "#404040",
            description = "Elite luxury travel card in matte black carbon finish with global Diners Club privileges"
        ),
        CardArtDesign(
            id = "hdfc_tata_neu_plus",
            name = "Tata Neu Plus HDFC Credit Card",
            issuer = "HDFC Bank / Tata Neu",
            cardType = "RUPAY",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/card-facias/credit-card/tata-neu-p.png",
            accentColorHex = "#7E22CE",
            gradientEndHex = "#6B21A8",
            description = "Royal violet card with 2% NeuCoins on partner Tata brands"
        ),
        CardArtDesign(
            id = "hdfc_marriott_bonvoy",
            name = "Marriott Bonvoy HDFC Credit Card",
            issuer = "HDFC Bank / Marriott",
            cardType = "DINERS",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/marriott-bonvoy-credit/images/Card-Facia-Marriott-Bonvoy.png",
            accentColorHex = "#881337",
            gradientEndHex = "#4C0519",
            description = "Burgundy wine hospitality card with free hotel night awards and Bonvoy points"
        ),
        CardArtDesign(
            id = "hdfc_irctc",
            name = "IRCTC HDFC Bank Credit Card",
            issuer = "HDFC Bank / IRCTC",
            cardType = "RUPAY",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/irctc-credit-card/images/Card-Facia-IRCTC.png",
            accentColorHex = "#1D4ED8",
            gradientEndHex = "#1E40AF",
            description = "Train travel companion card with railway executive lounge access and booking points"
        ),
        CardArtDesign(
            id = "hdfc_diners_privilege",
            name = "HDFC Diners Club Privilege",
            issuer = "HDFC Bank",
            cardType = "DINERS",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/diners-privilege-credit-card/images/diners-club-privilege.png",
            accentColorHex = "#334155",
            gradientEndHex = "#1E293B",
            description = "Premium lifestyle and dining privileges card with Diners Club privileges"
        ),
        CardArtDesign(
            id = "hdfc_tata_neu_infinity",
            name = "Tata Neu Infinity HDFC Credit Card",
            issuer = "HDFC Bank / Tata Neu",
            cardType = "RUPAY",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/card-facias/credit-card/tata-neu-i.png",
            accentColorHex = "#581C87",
            gradientEndHex = "#3B0764",
            description = "Deep amethyst purple card with 5% NeuCoins rewards and UPI Rupay integration"
        ),
        CardArtDesign(
            id = "hdfc_shoppers_stop_black",
            name = "Shoppers Stop Black HDFC Credit Card",
            issuer = "HDFC Bank / Shoppers Stop",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/shoppers-stop-black-hdfc-bank-credit-card/images/shoppers-stop-black-hdfc-bank-credit-card.png",
            accentColorHex = "#18181B",
            gradientEndHex = "#27272A",
            description = "Exclusive luxury shopping card with First Citizen points and VIP privileges"
        ),
        CardArtDesign(
            id = "hdfc_shoppers_stop_blue",
            name = "Shoppers Stop HDFC Credit Card",
            issuer = "HDFC Bank / Shoppers Stop",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/shoppers-stop-credit-card/Images/Card-Facia-Shoppers-Stop-Blue.png",
            accentColorHex = "#1E3A8A",
            gradientEndHex = "#1D4ED8",
            description = "Cobalt blue retail fashion card with First Citizen reward points"
        ),
        CardArtDesign(
            id = "hdfc_pixel_go",
            name = "HDFC Pixel Go Credit Card",
            issuer = "HDFC Bank",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/card-facias/credit-card/pixel-go.png",
            accentColorHex = "#4F46E5",
            gradientEndHex = "#3730A3",
            description = "Next-gen digital card with customizable billing cycles and instant digital issuance"
        ),
        CardArtDesign(
            id = "hdfc_upi_rupay",
            name = "HDFC UPI RuPay Credit Card",
            issuer = "HDFC Bank",
            cardType = "RUPAY",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/card-facias/credit-card/upi-rupay.png",
            accentColorHex = "#0284C7",
            gradientEndHex = "#0369A1",
            description = "Virtual-first RuPay card specifically crafted for UPI QR payments everywhere"
        ),
        CardArtDesign(
            id = "hdfc_easy_emi",
            name = "HDFC Easy EMI Credit Card",
            issuer = "HDFC Bank",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/easy-emi/images/easy-emi-credit-card.png",
            accentColorHex = "#0D9488",
            gradientEndHex = "#115E59",
            description = "Automatic EMI conversion on retail purchases with cashback incentives"
        ),
        CardArtDesign(
            id = "hdfc_platinum_times",
            name = "Platinum Times HDFC Credit Card",
            issuer = "HDFC Bank / Times",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/platinum-times-credit-card/images/platinum-times-credit-card.png",
            accentColorHex = "#475569",
            gradientEndHex = "#334155",
            description = "Movie and dining card with 25% discount on cinema tickets"
        ),
        CardArtDesign(
            id = "hdfc_titanium_times",
            name = "Titanium Times HDFC Credit Card",
            issuer = "HDFC Bank / Times",
            cardType = "MASTERCARD",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/titanium-times-card-credit-card/images/titanium-times-card-credit-card.png",
            accentColorHex = "#64748B",
            gradientEndHex = "#475569",
            description = "Titanium entertainment card with BookMyShow discounts and dining offers"
        ),
        CardArtDesign(
            id = "hdfc_regalia_gold",
            name = "HDFC Regalia Gold Credit Card",
            issuer = "HDFC Bank",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/regalia-gold-credit-card/images/regalia-gold-credit-card.png",
            accentColorHex = "#1E1B4B",
            gradientEndHex = "#D97706",
            description = "Imperial sapphire navy card with 24K gold filigree emblem and airport lounge access"
        ),
        CardArtDesign(
            id = "hdfc_harley_hog",
            name = "Harley-Davidson H.O.G. HDFC Credit Card",
            issuer = "HDFC Bank / Harley-Davidson",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/card-facias/credit-card/harley-hog.png",
            accentColorHex = "#EA580C",
            gradientEndHex = "#9A3412",
            description = "Motorcycle riders card with H.O.G. privileges and fuel surcharge waivers"
        ),
        CardArtDesign(
            id = "hdfc_harley_davidson",
            name = "Harley-Davidson HDFC Credit Card",
            issuer = "HDFC Bank / Harley-Davidson",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/card-facias/credit-card/harley.png",
            accentColorHex = "#18181B",
            gradientEndHex = "#C2410C",
            description = "Official Harley-Davidson merchandise rewards and dealership benefits"
        ),
        CardArtDesign(
            id = "hdfc_biz_black",
            name = "HDFC BizBlack Metal Edition",
            issuer = "HDFC Bank",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/card-facias/business-credit-cards/bizblack-metal-edition.jpg",
            accentColorHex = "#18181B",
            gradientEndHex = "#27272A",
            description = "Business executive card with titanium metal core and unlimited international lounges"
        ),
        CardArtDesign(
            id = "hdfc_biz_first",
            name = "HDFC BizFirst Credit Card",
            issuer = "HDFC Bank",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/card-facias/business-credit-cards/bizfirst.jpg",
            accentColorHex = "#0284C7",
            gradientEndHex = "#0369A1",
            description = "SME business card with up to 55 days interest-free period and business utility rewards"
        ),
        CardArtDesign(
            id = "hdfc_biz_grow",
            name = "HDFC BizGrow Credit Card",
            issuer = "HDFC Bank",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/card-facias/business-credit-cards/bizgrow.jpg",
            accentColorHex = "#059669",
            gradientEndHex = "#047857",
            description = "Commercial growth card with GST tax payment savings and vendor expense rewards"
        ),
        CardArtDesign(
            id = "hdfc_biz_power",
            name = "HDFC BizPower Credit Card",
            issuer = "HDFC Bank",
            cardType = "VISA",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/card-facias/business-credit-cards/bizpower.jpg",
            accentColorHex = "#D97706",
            gradientEndHex = "#B45309",
            description = "High-powered business spend card with 4X reward points on wholesale purchases"
        ),
        CardArtDesign(
            id = "hdfc_crc_ultimo",
            name = "HDFC Diners Club Ultimo",
            issuer = "HDFC Bank",
            cardType = "DINERS",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/card-facias/credit-card/crc-ULTIMO.png",
            accentColorHex = "#1E293B",
            gradientEndHex = "#0F172A",
            description = "Ultra-premium corporate charge card with customized spending limits"
        ),
        CardArtDesign(
            id = "hdfc_crc_uno",
            name = "HDFC Diners Club Uno",
            issuer = "HDFC Bank",
            cardType = "DINERS",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/card-facias/credit-card/crc-UNO.png",
            accentColorHex = "#334155",
            gradientEndHex = "#1E293B",
            description = "Premium corporate travel and entertainment card"
        ),
        CardArtDesign(
            id = "hdfc_swiggy_orange",
            name = "Swiggy HDFC Bank Card (Orange Facia)",
            issuer = "HDFC Bank / Swiggy",
            cardType = "MASTERCARD",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/swiggy-ornge-hdfc-bank-credit-card/facia.png",
            accentColorHex = "#FC8019",
            gradientEndHex = "#EA580C",
            description = "Bright orange Swiggy edition with 10% direct cashback on Swiggy orders"
        ),
        CardArtDesign(
            id = "hdfc_swiggy_black",
            name = "Swiggy HDFC Bank Card (Black Edition)",
            issuer = "HDFC Bank / Swiggy",
            cardType = "MASTERCARD",
            imageUrl = "https://hdfc.bank.in/content/dam/hdfcbankpws/in/en/personal-banking/discover-products/cards/credit-cards/swiggy-blck-hdfc-bank-credit-card/facia-swiggy-blck-cc.png",
            accentColorHex = "#18181B",
            gradientEndHex = "#27272A",
            description = "Stealth obsidian edition of the Swiggy cashback credit card"
        ),

        // ================= SBI CARD OFFICIAL ARTWORK =================
        CardArtDesign(
            id = "sbi_bank_of_maharashtra_prime",
            name = "Bank of Maharashtra SBI Card Prime",
            issuer = "SBI Card",
            cardType = "VISA",
            imageUrl = "https://sbicard.com/static-resources/img/card/card-face-assets/for-website/front/vertical/horizontal-converted-to-vertical/bank-of-maharastra-prime.png",
            accentColorHex = "#0369A1",
            gradientEndHex = "#075985",
            description = "Cobranded prime lifestyle card with club memberships and dining perks"
        ),
        CardArtDesign(
            id = "sbi_tata_select",
            name = "Tata SBI Card Select",
            issuer = "SBI Card / Tata",
            cardType = "VISA",
            imageUrl = "https://sbicard.com/static-resources/img/card/card-face-assets/for-website/front/vertical/horizontal-converted-to-vertical/tata-select-sbi-front-horizontal-to-vertical.png",
            accentColorHex = "#1E293B",
            gradientEndHex = "#0F172A",
            description = "Tata retail privileges card with Empower points across Croma, Westside, and Star"
        ),
        CardArtDesign(
            id = "sbi_tata_card",
            name = "Tata SBI Card",
            issuer = "SBI Card / Tata",
            cardType = "VISA",
            imageUrl = "https://sbicard.com/static-resources/img/card/card-face-assets/for-website/front/vertical/horizontal-converted-to-vertical/tata-sbi-front-horizontal-to-vertical.png",
            accentColorHex = "#334155",
            gradientEndHex = "#1E293B",
            description = "Value shopping card with universal reward points on all Tata group purchases"
        ),
        CardArtDesign(
            id = "sbi_uco_prime",
            name = "UCO Bank SBI Card Prime",
            issuer = "SBI Card",
            cardType = "VISA",
            imageUrl = "https://sbicard.com/static-resources/img/card/card-face-assets/for-website/front/vertical/horizontal-converted-to-vertical/uco-bank-prime-vertical.png",
            accentColorHex = "#0D9488",
            gradientEndHex = "#115E59",
            description = "Premium lifestyle and dining privileges card with UCO Bank cobranding"
        ),
        CardArtDesign(
            id = "sbi_psb_prime",
            name = "PSB SBI Card Prime",
            issuer = "SBI Card",
            cardType = "VISA",
            imageUrl = "https://sbicard.com/static-resources/img/card/card-face-assets/for-website/front/vertical/horizontal-converted-to-vertical/psb-card-prime-vertical.png",
            accentColorHex = "#1D4ED8",
            gradientEndHex = "#1E3A8A",
            description = "Punjab & Sind Bank cobranded card with milestone gift vouchers and travel perks"
        ),
        CardArtDesign(
            id = "sbi_shaurya",
            name = "Shaurya SBI Card",
            issuer = "SBI Card",
            cardType = "RUPAY",
            imageUrl = "https://sbicard.com/static-resources/img/card/card-face-assets/for-website/front/vertical/horizontal-converted-to-vertical/shaurya-sbi-card-vertical.png",
            accentColorHex = "#15803D",
            gradientEndHex = "#166534",
            description = "Dedicated defense personnel card with olive military crest and accelerated rewards"
        ),

        // ================= CHASE BANK OFFICIAL CARDS =================
        CardArtDesign(
            id = "chase_sapphire_preferred",
            name = "Chase Sapphire Preferred",
            issuer = "Chase",
            cardType = "VISA",
            imageUrl = "https://creditcards.chase.com/K-Marketplace/images/cardart/sapphire_preferred_card.png",
            accentColorHex = "#0C2340",
            gradientEndHex = "#1D4ED8",
            description = "Iconic deep sapphire navy travel card with premium point multipliers and trip protection"
        ),
        CardArtDesign(
            id = "chase_sapphire_reserve",
            name = "Chase Sapphire Reserve",
            issuer = "Chase",
            cardType = "VISA",
            imageUrl = "https://creditcards.chase.com/K-Marketplace/images/cardart/sapphire_reserve_card.png",
            accentColorHex = "#0A1118",
            gradientEndHex = "#1E293B",
            description = "Heavy metal premium travel card in midnight slate with Priority Pass lounge access"
        ),
        CardArtDesign(
            id = "chase_freedom_unlimited",
            name = "Chase Freedom Unlimited",
            issuer = "Chase",
            cardType = "VISA",
            imageUrl = "https://creditcards.chase.com/K-Marketplace/images/cardart/freedom_unlimited_card.png",
            accentColorHex = "#0284C7",
            gradientEndHex = "#0369A1",
            description = "Everyday cashback rewards card with 1.5% back on all purchases"
        ),
        CardArtDesign(
            id = "chase_freedom_flex",
            name = "Chase Freedom Flex",
            issuer = "Chase",
            cardType = "MASTERCARD",
            imageUrl = "https://creditcards.chase.com/K-Marketplace/images/cardart/freedom_flex_card.png",
            accentColorHex = "#2563EB",
            gradientEndHex = "#1D4ED8",
            description = "Rotating 5% quarterly bonus categories Mastercard"
        ),

        // ================= AMERICAN EXPRESS OFFICIAL CARDS =================
        CardArtDesign(
            id = "amex_platinum",
            name = "The Platinum Card from American Express",
            issuer = "American Express",
            cardType = "AMEX",
            imageUrl = "https://icm.aexp-static.com/Internet/Acquisition/US_en/AppConfig/OneSite/category/cardarts/platinum-card.png",
            accentColorHex = "#94A3B8",
            gradientEndHex = "#475569",
            description = "Ultra-premium brushed steel metal card with Centurion lounge and luxury travel credits"
        ),
        CardArtDesign(
            id = "amex_gold",
            name = "American Express Gold Card",
            issuer = "American Express",
            cardType = "AMEX",
            imageUrl = "https://icm.aexp-static.com/Internet/Acquisition/US_en/AppConfig/OneSite/category/cardarts/gold-card.png",
            accentColorHex = "#D97706",
            gradientEndHex = "#B45309",
            description = "Iconic solid gold card with 4X points at restaurants and US supermarkets"
        ),
        CardArtDesign(
            id = "amex_blue_cash_everyday",
            name = "Amex Blue Cash Everyday",
            issuer = "American Express",
            cardType = "AMEX",
            imageUrl = "https://icm.aexp-static.com/Internet/Acquisition/US_en/AppConfig/OneSite/category/cardarts/blue-cash-everyday.png",
            accentColorHex = "#0284C7",
            gradientEndHex = "#0369A1",
            description = "Clean crystal blue card with 3% cashback on grocery and online retail"
        ),

        // ================= APPLE CARD =================
        CardArtDesign(
            id = "apple_card",
            name = "Apple Card Titanium",
            issuer = "Apple / Goldman Sachs",
            cardType = "MASTERCARD",
            imageUrl = "https://www.apple.com/v/apple-card/m/images/overview/hero_apple_card__e7r3t6w71i26_large.png",
            accentColorHex = "#E2E8F0",
            gradientEndHex = "#CBD5E1",
            description = "Laser-etched pure white titanium card with Daily Cash rewards"
        ),

        // ================= CAPITAL ONE =================
        CardArtDesign(
            id = "capital_one_venture_x",
            name = "Capital One Venture X",
            issuer = "Capital One",
            cardType = "VISA",
            imageUrl = "https://www.capitalone.com/media/assets/credit-cards/venture-x/card-art-venture-x.png",
            accentColorHex = "#0F172A",
            gradientEndHex = "#1E293B",
            description = "Premium metal travel card with airport lounge access and $300 annual travel credit"
        ),
        CardArtDesign(
            id = "capital_one_quicksilver",
            name = "Capital One Quicksilver",
            issuer = "Capital One",
            cardType = "MASTERCARD",
            imageUrl = "https://www.capitalone.com/media/assets/credit-cards/quicksilver/card-art-quicksilver.png",
            accentColorHex = "#64748B",
            gradientEndHex = "#475569",
            description = "Silver metallic finish card with unlimited 1.5% cashback"
        ),

        // ================= CITI & DISCOVER =================
        CardArtDesign(
            id = "citi_double_cash",
            name = "Citi Double Cash Card",
            issuer = "Citi",
            cardType = "MASTERCARD",
            imageUrl = "https://www.citi.com/CRFD/ch/assets/card-art/double-cash.png",
            accentColorHex = "#0284C7",
            gradientEndHex = "#0369A1",
            description = "Dual cashback card with 1% when you buy and 1% as you pay"
        ),
        CardArtDesign(
            id = "citi_custom_cash",
            name = "Citi Custom Cash Card",
            issuer = "Citi",
            cardType = "MASTERCARD",
            imageUrl = "https://www.citi.com/CRFD/ch/assets/card-art/custom-cash.png",
            accentColorHex = "#0D9488",
            gradientEndHex = "#0F766E",
            description = "Automatic 5% cashback on your highest eligible spend category each billing cycle"
        ),
        CardArtDesign(
            id = "discover_it_cashback",
            name = "Discover it Cash Back",
            issuer = "Discover",
            cardType = "DISCOVER",
            imageUrl = "https://www.discover.com/content/dam/dfs/credit-cards/global/card-art/it-chrome-front.png",
            accentColorHex = "#EA580C",
            gradientEndHex = "#C2410C",
            description = "Chrome orange mirror finish card with 5% rotating quarterly cash back"
        ),
        CardArtDesign(
            id = "amazon_prime_rewards_visa",
            name = "Amazon Prime Rewards Visa Signature",
            issuer = "Amazon / Chase",
            cardType = "VISA",
            imageUrl = "https://m.media-amazon.com/images/G/01/credit/img21/chase/amazon-prime-rewards-visa-signature.png",
            accentColorHex = "#0F172A",
            gradientEndHex = "#232F3E",
            description = "Dark carbon card with 5% back at Amazon.com and Whole Foods Market for Prime members"
        )
    )

    /**
     * Filters and matches official card artwork from the curated catalog.
     * Token-based query search ensures typing partial or multi-word terms (e.g. "chase sapphire", "hdfc millennia", "amex gold")
     * correctly filters to the intended cards.
     */
    suspend fun searchCardArt(
        query: String = "",
        issuer: String = "",
        cardType: String = "",
        category: String = ""
    ): List<CardArtDesign> = withContext(Dispatchers.Default) {
        val cleanQuery = query.trim().lowercase(Locale.ROOT)
        val cleanIssuer = issuer.trim().lowercase(Locale.ROOT)
        val cleanType = cardType.trim().uppercase(Locale.ROOT)
        val cleanCategory = category.trim().lowercase(Locale.ROOT)

        if (cleanQuery.isBlank() && cleanIssuer.isBlank() && cleanType.isBlank() && cleanCategory.isBlank()) {
            return@withContext OFFICIAL_CARD_DESIGNS
        }

        // Tokenize query words so "chase sapphire" matches "Chase Sapphire Preferred"
        val queryTokens = cleanQuery.split(Regex("\\s+")).filter { it.isNotBlank() }

        val filtered = OFFICIAL_CARD_DESIGNS.filter { design ->
            val nameLower = design.name.lowercase(Locale.ROOT)
            val issuerLower = design.issuer.lowercase(Locale.ROOT)
            val descLower = design.description.lowercase(Locale.ROOT)
            val typeLower = design.cardType.lowercase(Locale.ROOT)
            val isAmex = issuerLower.contains("american express") || typeLower == "amex"
            val amexAliases = if (isAmex) " amex american express" else ""

            val searchableIdentity = "$nameLower $issuerLower $typeLower$amexAliases"

            val matchesQuery = if (queryTokens.isEmpty()) {
                true
            } else {
                queryTokens.all { token ->
                    searchableIdentity.contains(token) ||
                    descLower.split(Regex("[^a-z0-9]+")).any { word -> word == token || (word.length >= 4 && word.startsWith(token)) }
                }
            }

            val matchesCategory = if (cleanCategory.isBlank() || cleanCategory == "all") {
                true
            } else {
                when (cleanCategory) {
                    "amex" -> isAmex
                    "chase" -> issuerLower.contains("chase") || nameLower.contains("chase")
                    "hdfc" -> issuerLower.contains("hdfc") || nameLower.contains("hdfc")
                    "sbi" -> issuerLower.contains("sbi") || nameLower.contains("sbi")
                    "citi" -> issuerLower.contains("citi") || nameLower.contains("citi")
                    "capital one" -> issuerLower.contains("capital one") || nameLower.contains("capital one")
                    "discover" -> issuerLower.contains("discover") || typeLower == "discover"
                    "visa" -> typeLower == "visa" || nameLower.contains("visa")
                    "mastercard" -> typeLower == "mastercard" || nameLower.contains("mastercard")
                    "rupay" -> typeLower == "rupay" || nameLower.contains("rupay")
                    "metal" -> nameLower.contains("metal") || descLower.contains("metal")
                    else -> searchableIdentity.contains(cleanCategory)
                }
            }

            val matchesType = if (cleanType.isBlank()) {
                true
            } else {
                typeLower == cleanType.lowercase(Locale.ROOT)
            }

            matchesQuery && matchesCategory && matchesType
        }

        filtered
    }

    /**
     * Automatically suggests the best official card artwork matching a CardEntity
     * from the official HDFC and SBI designs.
     */
    fun suggestArtForCard(card: CardEntity): CardArtDesign? {
        val titleLower = card.title.lowercase(Locale.ROOT)
        val bankLower = card.bankOrIssuer.lowercase(Locale.ROOT)

        // 1. Direct card name & brand matches
        when {
            // HDFC matches
            titleLower.contains("pixel play") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_pixel_play" }

            titleLower.contains("pixel go") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_pixel_go" }

            titleLower.contains("pixel") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_pixel_play" }

            titleLower.contains("millennia") || (bankLower.contains("hdfc") && titleLower.contains("millennia")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_millennia" }

            titleLower.contains("moneyback") || (bankLower.contains("hdfc") && titleLower.contains("money")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_moneyback_plus" }

            titleLower.contains("infinia") || (bankLower.contains("hdfc") && titleLower.contains("infinia")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_infinia" }

            titleLower.contains("regalia") || (bankLower.contains("hdfc") && titleLower.contains("gold")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_regalia_gold" }

            titleLower.contains("diners black") || titleLower.contains("diners club black") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_diners_black" }

            titleLower.contains("diners privilege") || titleLower.contains("privilege") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_diners_privilege" }

            titleLower.contains("swiggy") -> {
                if (titleLower.contains("orange")) {
                    return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_swiggy_orange" }
                }
                if (titleLower.contains("black") || titleLower.contains("dark")) {
                    return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_swiggy_black" }
                }
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_swiggy" }
            }

            titleLower.contains("neu infinity") || (titleLower.contains("tata neu") && titleLower.contains("infinity")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_tata_neu_infinity" }

            titleLower.contains("tata neu") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_tata_neu_plus" }

            titleLower.contains("marriott") || titleLower.contains("bonvoy") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_marriott_bonvoy" }

            titleLower.contains("freedom") && (bankLower.contains("hdfc") || card.cardType == "VISA") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_freedom" }

            titleLower.contains("iocl") || titleLower.contains("indianoil") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_iocl" }

            titleLower.contains("irctc") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_irctc" }

            titleLower.contains("shoppers stop") -> {
                if (titleLower.contains("black")) {
                    return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_shoppers_stop_black" }
                }
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_shoppers_stop_blue" }
            }

            titleLower.contains("easy emi") || titleLower.contains("easyemi") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_easy_emi" }

            titleLower.contains("times") -> {
                if (titleLower.contains("titanium")) {
                    return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_titanium_times" }
                }
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_platinum_times" }
            }

            titleLower.contains("harley") -> {
                if (titleLower.contains("hog") || titleLower.contains("h.o.g")) {
                    return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_harley_hog" }
                }
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_harley_davidson" }
            }

            titleLower.contains("bizblack") || titleLower.contains("biz black") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_biz_black" }

            titleLower.contains("bizfirst") || titleLower.contains("biz first") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_biz_first" }

            titleLower.contains("bizgrow") || titleLower.contains("biz grow") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_biz_grow" }

            titleLower.contains("bizpower") || titleLower.contains("biz power") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_biz_power" }

            titleLower.contains("ultimo") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_crc_ultimo" }

            titleLower.contains("uno") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_crc_uno" }

            (bankLower.contains("hdfc") && (card.cardType == "RUPAY" || titleLower.contains("rupay") || titleLower.contains("upi"))) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_upi_rupay" }

            // SBI matches
            titleLower.contains("shaurya") || (bankLower.contains("sbi") && titleLower.contains("defense")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "sbi_shaurya" }

            titleLower.contains("tata sbi") && titleLower.contains("select") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "sbi_tata_select" }

            titleLower.contains("tata sbi") || (bankLower.contains("sbi") && titleLower.contains("tata")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "sbi_tata_card" }

            (bankLower.contains("sbi") || titleLower.contains("sbi")) && titleLower.contains("maharashtra") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "sbi_bank_of_maharashtra_prime" }

            (bankLower.contains("sbi") || titleLower.contains("sbi")) && titleLower.contains("uco") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "sbi_uco_prime" }

            (bankLower.contains("sbi") || titleLower.contains("sbi")) && (titleLower.contains("psb") || titleLower.contains("punjab & sind")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "sbi_psb_prime" }

            // Chase matches
            titleLower.contains("sapphire reserve") || (bankLower.contains("chase") && titleLower.contains("reserve")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "chase_sapphire_reserve" }

            titleLower.contains("sapphire") || (bankLower.contains("chase") && titleLower.contains("sapphire")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "chase_sapphire_preferred" }

            titleLower.contains("freedom flex") || (bankLower.contains("chase") && titleLower.contains("flex")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "chase_freedom_flex" }

            titleLower.contains("freedom") || (bankLower.contains("chase") && titleLower.contains("freedom")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "chase_freedom_unlimited" }

            // Amex matches
            titleLower.contains("platinum") && (bankLower.contains("amex") || bankLower.contains("american express") || titleLower.contains("amex")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "amex_platinum" }

            titleLower.contains("gold") && (bankLower.contains("amex") || bankLower.contains("american express") || titleLower.contains("amex")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "amex_gold" }

            titleLower.contains("blue cash") || (bankLower.contains("amex") && titleLower.contains("blue")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "amex_blue_cash_everyday" }

            // Apple Card
            titleLower.contains("apple") || bankLower.contains("apple") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "apple_card" }

            // Capital One
            titleLower.contains("venture x") || (bankLower.contains("capital one") && titleLower.contains("venture")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "capital_one_venture_x" }

            titleLower.contains("quicksilver") || (bankLower.contains("capital one") && titleLower.contains("quick")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "capital_one_quicksilver" }

            // Citi & Discover & Amazon
            titleLower.contains("double cash") || (bankLower.contains("citi") && titleLower.contains("double")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "citi_double_cash" }

            titleLower.contains("custom cash") || (bankLower.contains("citi") && titleLower.contains("custom")) ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "citi_custom_cash" }

            titleLower.contains("discover") || bankLower.contains("discover") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "discover_it_cashback" }

            titleLower.contains("amazon") || bankLower.contains("amazon") ->
                return OFFICIAL_CARD_DESIGNS.find { it.id == "amazon_prime_rewards_visa" }
        }

        // 2. Issuer bank match fallback
        if (bankLower.contains("hdfc")) {
            return OFFICIAL_CARD_DESIGNS.find { it.id == "hdfc_millennia" }
        }
        if (bankLower.contains("sbi")) {
            return OFFICIAL_CARD_DESIGNS.find { it.id == "sbi_tata_select" }
        }
        if (bankLower.contains("chase")) {
            return OFFICIAL_CARD_DESIGNS.find { it.id == "chase_sapphire_preferred" }
        }
        if (bankLower.contains("amex") || bankLower.contains("american express")) {
            return OFFICIAL_CARD_DESIGNS.find { it.id == "amex_gold" }
        }
        if (bankLower.contains("capital one")) {
            return OFFICIAL_CARD_DESIGNS.find { it.id == "capital_one_venture_x" }
        }
        if (bankLower.contains("citi")) {
            return OFFICIAL_CARD_DESIGNS.find { it.id == "citi_double_cash" }
        }

        return null
    }
}
