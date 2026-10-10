package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiCardScannerService
import com.example.ai.ScanResult
import com.example.data.CardArtDesign
import com.example.data.CardArtOnlineService
import com.example.data.CardEntity
import com.example.security.HapticsHelper
import com.example.ui.components.CameraXCardScannerView
import com.example.ui.components.CreditCardItem
import com.example.ui.theme.BitmapPaletteExtractor
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

/**
 * Visual transformation that displays payment card numbers in 4-digit groups (e.g. 1234 5678 9012 3456)
 * without mutating the underlying String state, preventing Compose cursor jump bugs.
 */
class PaymentCardVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 19) text.text.substring(0, 19) else text.text
        val out = StringBuilder()
        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i % 4 == 3 && i != trimmed.lastIndex) {
                out.append(' ')
            }
        }
        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val spaces = (offset - 1) / 4
                return (offset + spaces).coerceAtMost(out.length)
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val spaces = offset / 5
                return (offset - spaces).coerceAtMost(text.length)
            }
        }
        return TransformedText(AnnotatedString(out.toString()), offsetTranslator)
    }
}

/**
 * Visual transformation that displays 4-digit expiry as MM/YY without mutating the String state,
 * preventing cursor jumps and digit skips.
 */
class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0, 4) else text.text
        val out = StringBuilder()
        for (i in trimmed.indices) {
            out.append(trimmed[i])
            if (i == 1 && trimmed.length > 2) {
                out.append('/')
            }
        }
        val transformed = out.toString()
        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 4) {
                    return if (trimmed.length > 2) offset + 1 else offset
                }
                return transformed.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= transformed.length) return offset - 1
                return trimmed.length
            }
        }
        return TransformedText(AnnotatedString(transformed), offsetTranslator)
    }
}

enum class CardCategoryType(val id: String, val label: String, val category: String) {
    PAYMENT("VISA", "Payment", "PAYMENT"),
    ID_CARD("ID_CARD", "ID Card", "IDENTITY"),
    RC_CARD("RC_CARD", "RC Vehicle", "VEHICLE"),
    VOUCHER("VOUCHER", "Voucher", "VOUCHER"),
    MISC("MISC", "Misc Card", "MISC")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanCardScreen(
    onNavigateBack: () -> Unit,
    onCardSaved: (CardEntity) -> Unit,
    initialManualMode: Boolean = false,
    isOnlineCardArtEnabled: Boolean = false,
    onEnableOnlineCardArt: () -> Unit = {},
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableStateOf(if (initialManualMode) 1 else 0) } // 0 = Camera Scan, 1 = Manual Entry
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }

    // Card Details State
    var selectedCategoryType by remember { mutableStateOf(CardCategoryType.PAYMENT) }
    var title by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var rawCardNumber by remember { mutableStateOf("") }
    var rawExpiryDigits by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var bankOrIssuer by remember { mutableStateOf("") }
    var selectedHex by remember { mutableStateOf("#1E293B") }
    var secondaryHex by remember { mutableStateOf("#0F172A") }
    var cardArtUrl by remember { mutableStateOf("") }

    // Online Card Art Gallery & Search State
    var showArtPickerSection by remember { mutableStateOf(false) }
    var artSearchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<CardArtDesign>>(emptyList()) }
    var isSearchingArt by remember { mutableStateOf(false) }
    var showSettingDisabledPrompt by remember { mutableStateOf(false) }

    var paymentNetwork by remember { mutableStateOf("VISA") }

    fun updateNetworkFromPan(pan: String) {
        val clean = pan.filter { it.isDigit() }
        when {
            clean.startsWith("4") -> paymentNetwork = "VISA"
            clean.startsWith("51") || clean.startsWith("52") || clean.startsWith("53") || clean.startsWith("54") || clean.startsWith("55") ||
                    (clean.length >= 4 && clean.substring(0, 4).toIntOrNull() in 2221..2720) -> paymentNetwork = "MASTERCARD"
            clean.startsWith("34") || clean.startsWith("37") -> paymentNetwork = "AMEX"
            clean.startsWith("36") || clean.startsWith("38") || clean.startsWith("30") || clean.startsWith("39") -> paymentNetwork = "DINERS"
            clean.startsWith("60") || clean.startsWith("65") || clean.startsWith("81") || clean.startsWith("82") || clean.startsWith("508") || clean.startsWith("353") || clean.startsWith("356") -> paymentNetwork = "RUPAY"
        }
    }

    // Determine card type string based on selected network or category
    val currentCardType = if (selectedCategoryType == CardCategoryType.PAYMENT) paymentNetwork else selectedCategoryType.name

    fun searchArt(customQuery: String? = null) {
        val q = customQuery ?: artSearchQuery
        isSearchingArt = true
        coroutineScope.launch {
            searchResults = CardArtOnlineService.searchCardArt(
                query = q,
                issuer = "",
                cardType = currentCardType
            )
            isSearchingArt = false
        }
    }

    // Process image with Gemini / OCR
    fun processImage(bitmap: Bitmap) {
        capturedBitmap = bitmap
        isAnalyzing = true
        statusText = ""
        haptics?.cardSlide()

        val extractedPalette = BitmapPaletteExtractor.extractPalette(bitmap)
        selectedHex = extractedPalette.vibrantHex
        secondaryHex = extractedPalette.secondaryHex

        coroutineScope.launch {
            val result = GeminiCardScannerService.analyzeCardImage(bitmap)
            isAnalyzing = false
            when (result) {
                is ScanResult.Success -> {
                    haptics?.success()
                    statusText = ""
                    title = result.card.title
                    cardholderName = result.card.cardholderName
                    rawCardNumber = result.card.cardNumber.replace("\\s+".toRegex(), "")
                    rawExpiryDigits = if (result.card.cardType in listOf("ID_CARD", "RC_CARD", "VOUCHER", "MISC")) {
                        result.card.expiryDate
                    } else {
                        result.card.expiryDate.filter { it.isDigit() }
                    }
                    cvv = result.card.cvv
                    bankOrIssuer = result.card.bankOrIssuer
                    if (result.card.cardType.uppercase() in listOf("VISA", "MASTERCARD", "DINERS", "DINERS_CLUB", "DINNERCLUB", "AMEX", "RUPAY")) {
                        paymentNetwork = if (result.card.cardType.uppercase().contains("DINER")) "DINERS" else result.card.cardType.uppercase()
                    } else {
                        updateNetworkFromPan(result.card.cardNumber)
                    }
                    selectedCategoryType = when (result.card.cardType) {
                        "ID_CARD" -> CardCategoryType.ID_CARD
                        "RC_CARD" -> CardCategoryType.RC_CARD
                        "VOUCHER" -> CardCategoryType.VOUCHER
                        "MISC" -> CardCategoryType.MISC
                        else -> CardCategoryType.PAYMENT
                    }
                    if (result.card.themeColorHex.isNotBlank() && result.card.themeColorHex != "#1E293B") {
                        selectedHex = result.card.themeColorHex
                    }
                    if (result.card.gradientEndHex.isNotBlank() && result.card.gradientEndHex != "#0F172A") {
                        secondaryHex = result.card.gradientEndHex
                    }

                    // If authentic online card artwork flag is enabled, auto-suggest authentic card design
                    if (isOnlineCardArtEnabled) {
                        val suggested = CardArtOnlineService.suggestArtForCard(result.card)
                        if (suggested != null) {
                            cardArtUrl = suggested.imageUrl
                            selectedHex = suggested.accentColorHex
                            secondaryHex = suggested.gradientEndHex
                            if (bankOrIssuer.isBlank()) {
                                bankOrIssuer = suggested.issuer
                            }
                            if (suggested.cardType.isNotBlank()) {
                                paymentNetwork = if (suggested.cardType.uppercase().contains("DINER")) "DINERS" else suggested.cardType.uppercase()
                            }
                        }
                    }
                }
                is ScanResult.Error -> {
                    haptics?.error()
                    statusText = result.message
                    if (result.fallbackCard != null) {
                        title = result.fallbackCard.title
                        rawCardNumber = result.fallbackCard.cardNumber.replace("\\s+".toRegex(), "")
                    }
                }
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                processImage(bitmap)
            } catch (e: Exception) {
                statusText = "Error reading photo"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == 0) "Scan Card" else "Add Card",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Mode Selector Tabs: Camera Scan vs Manual Entry
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        haptics?.cardSelect()
                        selectedTab = 0
                    },
                    text = { Text("Scan Card", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        haptics?.cardSelect()
                        selectedTab = 1
                    },
                    text = { Text("Manual Entry", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // TAB 0: Camera Scan Area
            if (selectedTab == 0) {
                if (capturedBitmap == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black)
                            .border(1.dp, Color(0xFFE5A93C).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    ) {
                        CameraXCardScannerView(
                            onCardImageCaptured = { bitmap ->
                                processImage(bitmap)
                            },
                            onError = { err ->
                                statusText = err
                            },
                            haptics = haptics,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            haptics?.cardSelect()
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose from Photos", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    // Captured image preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF1E1A17))
                            .border(1.dp, Color(0xFFE5A93C).copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Card Photo",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(18.dp))
                        )

                        if (isAnalyzing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.75f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Color(0xFFE5A93C))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Scanning card...",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            haptics?.cardSelect()
                            capturedBitmap = null
                            statusText = ""
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retake", tint = Color(0xFFE5A93C), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retake Photo", color = Color(0xFFE5A93C), fontWeight = FontWeight.Bold)
                    }
                }

                if (statusText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = statusText,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Preview & Form Section (Visible in Manual Mode or once photo is captured)
            val showForm = selectedTab == 1 || capturedBitmap != null

            if (showForm) {
                // Card Category Chips (Payment, ID Card, RC Vehicle, Voucher, Misc)
                Text(
                    text = "Card Type",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Horizontally scrollable chips so buttons NEVER squash vertically or wrap letters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardCategoryType.values().forEach { cat ->
                        val isSelected = selectedCategoryType == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptics?.cardSelect()
                                selectedCategoryType = cat
                            },
                            label = {
                                Text(
                                    text = cat.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            )
                        )
                    }
                }

                if (selectedCategoryType == CardCategoryType.PAYMENT) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Payment Network",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "VISA" to "Visa",
                            "MASTERCARD" to "Mastercard",
                            "DINERS" to "Diners Club",
                            "AMEX" to "AMEX",
                            "RUPAY" to "RuPay"
                        ).forEach { (netKey, netLabel) ->
                            val isSelected = paymentNetwork.equals(netKey, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    haptics?.cardSelect()
                                    paymentNetwork = netKey
                                },
                                label = {
                                    Text(
                                        text = netLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reconstructed live preview card (Flat, zero shadow)
                val previewExpiry = if (selectedCategoryType == CardCategoryType.PAYMENT) {
                    if (rawExpiryDigits.length >= 4) "${rawExpiryDigits.take(2)}/${rawExpiryDigits.drop(2)}"
                    else if (rawExpiryDigits.length >= 2) "${rawExpiryDigits.take(2)}/"
                    else rawExpiryDigits
                } else rawExpiryDigits

                val previewCard = CardEntity(
                    title = title.ifBlank {
                        when (selectedCategoryType) {
                            CardCategoryType.RC_CARD -> "Vehicle RC"
                            CardCategoryType.ID_CARD -> "ID Document"
                            CardCategoryType.VOUCHER -> "Gift Voucher"
                            CardCategoryType.MISC -> "Misc Card"
                            else -> "Payment Card"
                        }
                    },
                    cardholderName = cardholderName.ifBlank { "HOLDER NAME" },
                    cardNumber = rawCardNumber.ifBlank { "•••• •••• •••• ••••" },
                    expiryDate = previewExpiry,
                    cvv = cvv,
                    cardType = currentCardType,
                    category = selectedCategoryType.category,
                    bankOrIssuer = bankOrIssuer,
                    themeColorHex = selectedHex,
                    gradientEndHex = secondaryHex,
                    cardArtUrl = cardArtUrl
                )

                CreditCardItem(
                    card = previewCard,
                    haptics = haptics
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ================= AUTHENTIC WEB CARD ARTWORK SECTION =================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Official Card Artwork",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (cardArtUrl.isNotBlank()) {
                        TextButton(
                            onClick = {
                                haptics?.cardSelect()
                                cardArtUrl = ""
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Art", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = {
                        haptics?.cardSelect()
                        if (!isOnlineCardArtEnabled) {
                            showSettingDisabledPrompt = true
                        } else {
                            showArtPickerSection = !showArtPickerSection
                            if (showArtPickerSection && searchResults.isEmpty()) {
                                searchArt()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = if (showArtPickerSection) Icons.Default.Close else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showArtPickerSection) "Hide Artwork Gallery" else "Select Official Card Artwork",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                AnimatedVisibility(visible = showArtPickerSection) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = artSearchQuery,
                                onValueChange = {
                                    artSearchQuery = it
                                    searchArt(it)
                                },
                                placeholder = { Text("Search cards (e.g. Chase, Sapphire, Millennia, Swiggy, Amex)", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    if (artSearchQuery.isNotBlank()) {
                                        IconButton(onClick = {
                                            artSearchQuery = ""
                                            searchArt("")
                                        }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    } else {
                                        IconButton(onClick = { searchArt() }) {
                                            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isSearchingArt) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            }
                        } else if (searchResults.isEmpty()) {
                            Text(
                                text = "No cards matching filter. Try 'HDFC', 'SBI', 'Millennia', 'Swiggy', or 'Tata'.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Text(
                                text = "Tap a design to apply official card artwork:",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                items(searchResults) { design ->
                                    val isSelected = cardArtUrl == design.imageUrl
                                    Box(
                                        modifier = Modifier
                                            .width(135.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .background(MaterialTheme.colorScheme.surface)
                                            .clickable {
                                                haptics?.cardSelect()
                                                cardArtUrl = design.imageUrl
                                                selectedHex = design.accentColorHex
                                                secondaryHex = design.gradientEndHex
                                                bankOrIssuer = design.issuer
                                                if (title.isBlank() || title.contains("Card", ignoreCase = true)) {
                                                    title = design.name
                                                }
                                                if (design.cardType.isNotBlank()) {
                                                    paymentNetwork = if (design.cardType.uppercase().contains("DINER")) "DINERS" else design.cardType.uppercase()
                                                }
                                            }
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(85.dp)
                                            ) {
                                                AsyncImage(
                                                    model = ImageRequest.Builder(LocalContext.current)
                                                        .data(design.imageUrl)
                                                        .crossfade(true)
                                                        .build(),
                                                    contentDescription = design.name,
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                if (isSelected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(6.dp)
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(MaterialTheme.colorScheme.primary)
                                                            .align(Alignment.TopEnd),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                            }
                                            Column(modifier = Modifier.padding(6.dp)) {
                                                Text(
                                                    text = design.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = design.issuer,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Text Fields Form
                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )

                // 1. Title / Label
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            when (selectedCategoryType) {
                                CardCategoryType.RC_CARD -> "Vehicle / RC Name"
                                CardCategoryType.ID_CARD -> "ID Title (e.g. Passport, License)"
                                CardCategoryType.VOUCHER -> "Voucher / Brand Name"
                                else -> "Card Label"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Card / Document Number (Fixes cursor jumping + supports alphanumeric!)
                val isPayment = selectedCategoryType == CardCategoryType.PAYMENT
                OutlinedTextField(
                    value = rawCardNumber,
                    onValueChange = { input ->
                        if (isPayment) {
                            // Payment cards: store digits directly without mutating with spaces
                            rawCardNumber = input.filter { it.isDigit() }.take(19)
                        } else {
                            // ID cards, RC cards, Vouchers, Misc: completely alphanumeric without restrictive filters!
                            rawCardNumber = input.uppercase().take(30)
                        }
                    },
                    label = {
                        Text(
                            when (selectedCategoryType) {
                                CardCategoryType.RC_CARD -> "Registration Number (e.g. MH02AB1234)"
                                CardCategoryType.ID_CARD -> "ID / Document Number"
                                CardCategoryType.VOUCHER -> "Voucher / Gift Code"
                                CardCategoryType.MISC -> "Card / Member Number"
                                else -> "Card Number"
                            }
                        )
                    },
                    placeholder = {
                        Text(
                            if (isPayment) "16-digit card number" else "Alphanumeric card ID"
                        )
                    },
                    visualTransformation = if (isPayment) PaymentCardVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isPayment) KeyboardType.Number else KeyboardType.Ascii,
                        capitalization = KeyboardCapitalization.Characters
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Cardholder / Owner Name
                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = { cardholderName = it.uppercase() },
                    label = {
                        Text(
                            if (selectedCategoryType in listOf(CardCategoryType.ID_CARD, CardCategoryType.RC_CARD))
                                "Owner / Holder Name"
                            else
                                "Cardholder Name"
                        )
                    },
                    placeholder = { Text("FULL NAME") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Expiry Date & CVV (Clean digits input with ExpiryDateVisualTransformation to prevent cursor jumping or skipping)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = rawExpiryDigits,
                        onValueChange = { input ->
                            if (isPayment) {
                                rawExpiryDigits = input.filter { it.isDigit() }.take(4)
                            } else {
                                rawExpiryDigits = input.take(10)
                            }
                        },
                        label = {
                            Text(
                                if (isPayment) "Expiry (MM/YY)" else "Valid Till (Optional)"
                            )
                        },
                        placeholder = { Text(if (isPayment) "MM/YY" else "MM/YY or YYYY") },
                        visualTransformation = if (isPayment) ExpiryDateVisualTransformation() else VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (isPayment) KeyboardType.Number else KeyboardType.Ascii
                        ),
                        modifier = Modifier.weight(if (isPayment) 1.2f else 1f),
                        colors = fieldColors,
                        singleLine = true
                    )

                    if (isPayment || selectedCategoryType == CardCategoryType.MISC) {
                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { cvv = it.filter { c -> c.isDigit() }.take(4) },
                            label = { Text("CVV") },
                            placeholder = { Text("•••") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.8f),
                            colors = fieldColors,
                            singleLine = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 5. Issuer / Authority
                OutlinedTextField(
                    value = bankOrIssuer,
                    onValueChange = { bankOrIssuer = it },
                    label = {
                        Text(
                            when (selectedCategoryType) {
                                CardCategoryType.RC_CARD -> "RTO / Authority"
                                CardCategoryType.ID_CARD -> "Issuing Government / Organization"
                                CardCategoryType.VOUCHER -> "Store / Retailer"
                                else -> "Issuer / Bank"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Card Color Theme Picker (Scrollable)
                Text(
                    text = "Card Color",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val palette = listOf(
                        "#1E293B", "#0C2340", "#854D0E", "#134E4A", "#701A75", "#161618", "#1E3A8A", "#065F46"
                    )
                    for (hex in palette) {
                        val c = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(
                                    width = if (selectedHex == hex) 3.dp else 1.dp,
                                    color = if (selectedHex == hex) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Card Button
                ElevatedButton(
                    onClick = {
                        val finalExpiry = if (selectedCategoryType == CardCategoryType.PAYMENT && rawExpiryDigits.length >= 4) {
                            "${rawExpiryDigits.take(2)}/${rawExpiryDigits.drop(2)}"
                        } else rawExpiryDigits

                        val newCard = CardEntity(
                            title = title.ifBlank {
                                when (selectedCategoryType) {
                                    CardCategoryType.RC_CARD -> "Vehicle Registration"
                                    CardCategoryType.ID_CARD -> "ID Card"
                                    CardCategoryType.VOUCHER -> "Voucher"
                                    CardCategoryType.MISC -> "Card"
                                    else -> "${bankOrIssuer.ifBlank { currentCardType }} Card"
                                }
                            },
                            cardholderName = cardholderName.uppercase(),
                            cardNumber = rawCardNumber,
                            expiryDate = finalExpiry,
                            cvv = cvv,
                            cardType = currentCardType,
                            category = selectedCategoryType.category,
                            bankOrIssuer = bankOrIssuer,
                            themeColorHex = selectedHex,
                            gradientEndHex = secondaryHex,
                            cardArtUrl = cardArtUrl,
                            notes = if (capturedBitmap != null) "Scanned with Gemini 3.1 Pro OCR" else "Added manually",
                            scannedVia = if (capturedBitmap != null) "CAMERA_AI" else "MANUAL",
                            createdAt = System.currentTimeMillis()
                        )
                        haptics?.success()
                        onCardSaved(newCard)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Card",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Setting disabled dialog prompt
    if (showSettingDisabledPrompt) {
        AlertDialog(
            onDismissRequest = { showSettingDisabledPrompt = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("Enable Official Card Artwork?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Displaying official bank card designs is currently turned off in Settings. Would you like to enable it now?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                ElevatedButton(
                    onClick = {
                        onEnableOnlineCardArt()
                        showSettingDisabledPrompt = false
                        showArtPickerSection = true
                        searchArt()
                        haptics?.success()
                    },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Turn On", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingDisabledPrompt = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
