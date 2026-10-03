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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiCardScannerService
import com.example.ai.ScanResult
import com.example.data.CardEntity
import com.example.security.HapticsHelper
import com.example.ui.components.CameraXCardScannerView
import com.example.ui.components.CreditCardItem
import com.example.ui.theme.BitmapPaletteExtractor
import com.example.ui.theme.MaterialYouThemeEngine
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanCardScreen(
    onNavigateBack: () -> Unit,
    onCardSaved: (CardEntity) -> Unit,
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var scanResultCard by remember { mutableStateOf<CardEntity?>(null) }
    var statusText by remember { mutableStateOf("Take a photo or choose an image of your card to scan with Gemini 3.1 Pro OCR") }
    var aiModelNotice by remember { mutableStateOf<String?>(null) }

    // Editable form state for user verification
    var title by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var bankOrIssuer by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf("VISA") }
    var selectedHex by remember { mutableStateOf("#1E293B") }
    var secondaryHex by remember { mutableStateOf("#0F172A") }
    var extractedSwatches by remember { mutableStateOf<List<String>>(emptyList()) }

    // Process image with Gemini 3.1 Pro
    fun processImage(bitmap: Bitmap) {
        capturedBitmap = bitmap
        isAnalyzing = true
        statusText = "Analyzing card with Google ML Kit & Gemini AI OCR..."
        haptics?.cardSlide()

        // Extract Material You dynamic color palette from card photo
        val extractedPalette = BitmapPaletteExtractor.extractPalette(bitmap)
        selectedHex = extractedPalette.vibrantHex
        secondaryHex = extractedPalette.secondaryHex
        extractedSwatches = extractedPalette.suggestedSwatches

        coroutineScope.launch {
            val result = GeminiCardScannerService.analyzeCardImage(bitmap)
            isAnalyzing = false
            when (result) {
                is ScanResult.Success -> {
                    haptics?.success()
                    scanResultCard = result.card
                    aiModelNotice = "Scanned using ${result.modelUsed}"
                    statusText = when {
                        result.card.cardNumber.isNotBlank() && result.card.cardholderName.isNotBlank() ->
                            "Card number and cardholder name successfully extracted! Review below."
                        result.card.cardNumber.isNotBlank() ->
                            "Card number extracted! Please confirm cardholder name below."
                        result.card.cardholderName.isNotBlank() ->
                            "Cardholder name extracted! Please verify card number below."
                        else ->
                            "Card analyzed! Please enter or confirm any missing fields below."
                    }

                    // Fill form fields
                    title = result.card.title
                    cardholderName = result.card.cardholderName
                    cardNumber = result.card.cardNumber
                    expiryDate = result.card.expiryDate
                    cvv = result.card.cvv
                    bankOrIssuer = result.card.bankOrIssuer
                    cardType = result.card.cardType
                    if (result.card.themeColorHex.isNotBlank() && result.card.themeColorHex != "#1E293B") {
                        selectedHex = result.card.themeColorHex
                    }
                    if (result.card.gradientEndHex.isNotBlank() && result.card.gradientEndHex != "#0F172A") {
                        secondaryHex = result.card.gradientEndHex
                    }
                }

                is ScanResult.Error -> {
                    haptics?.error()
                    statusText = "Analysis note: ${result.message}"
                    if (result.fallbackCard != null) {
                        scanResultCard = result.fallbackCard
                        title = result.fallbackCard.title
                        cardNumber = result.fallbackCard.cardNumber
                    }
                }
            }
        }
    }

    // Photo Picker launcher
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
                statusText = "Failed to load selected image: ${e.message}"
            }
        }
    }

    // Camera snapshot launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            processImage(bitmap)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFF3C569),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Card Scanner",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF13100E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF13100E)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // CameraX Viewfinder / Captured Card Area
            if (capturedBitmap == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.Black)
                        .border(1.5.dp, Color(0xFFE5A93C).copy(alpha = 0.5f), RoundedCornerShape(22.dp))
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

                Spacer(modifier = Modifier.height(14.dp))

                // Gallery upload fallback button
                OutlinedButton(
                    onClick = {
                        haptics?.cardSelect()
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                ) {
                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Card Photo from Gallery", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            } else {
                // Captured Photo Review & OCR Processing State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1E1A17))
                        .border(1.5.dp, Color(0xFFE5A93C).copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "Card Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
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
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Gemini 3.1 Pro OCR",
                                    color = Color(0xFFE5A93C),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Extracting card details & security marks...",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Retake button to restart CameraX
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            haptics?.cardSelect()
                            capturedBitmap = null
                            scanResultCard = null
                            statusText = "Align your physical card in the CameraX frame to scan"
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retake", tint = Color(0xFFE5A93C), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retake Photo with CameraX", color = Color(0xFFE5A93C), fontWeight = FontWeight.Bold)
                    }
                }
            }


            Spacer(modifier = Modifier.height(12.dp))

            // AI Status Banner
            Text(
                text = statusText,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            if (aiModelNotice != null) {
                Text(
                    text = aiModelNotice ?: "",
                    color = Color(0xFF34D399),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // If we have scanned data, show Live Preview Card + Form to Edit
            AnimatedVisibility(visible = scanResultCard != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "LIVE CARD PREVIEW",
                        color = Color(0xFFE5A93C),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reconstructed card preview
                    CreditCardItem(
                        card = CardEntity(
                            title = title.ifBlank { "Card Title" },
                            cardholderName = cardholderName.ifBlank { "CARDHOLDER" },
                            cardNumber = cardNumber.ifBlank { "•••• •••• •••• ••••" },
                            expiryDate = expiryDate.ifBlank { "12/28" },
                            cvv = cvv,
                            cardType = cardType,
                            bankOrIssuer = bankOrIssuer,
                            themeColorHex = selectedHex,
                            gradientEndHex = selectedHex
                        ),
                        haptics = haptics
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "VERIFY DETAILS",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val fieldColors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE5A93C),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFE5A93C),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Card Label") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() }.take(19)
                            cardNumber = if (clean.length == 15) {
                                "${clean.substring(0, 4)} ${clean.substring(4, 10)} ${clean.substring(10)}"
                            } else {
                                clean.chunked(4).joinToString(" ")
                            }
                            cardType = when {
                                clean.startsWith("4") -> "VISA"
                                clean.startsWith("51") || clean.startsWith("52") || clean.startsWith("53") || clean.startsWith("54") || clean.startsWith("55") || (clean.length >= 4 && clean.substring(0, 4).toIntOrNull() in 2221..2720) -> "MASTERCARD"
                                clean.startsWith("34") || clean.startsWith("37") -> "AMEX"
                                clean.startsWith("6011") || clean.startsWith("65") -> "DISCOVER"
                                else -> cardType
                            }
                        },
                        label = { Text("Card Number") },
                        placeholder = { Text("16-digit card number") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = cardholderName,
                            onValueChange = { cardholderName = it.uppercase() },
                            label = { Text("Cardholder Name") },
                            placeholder = { Text("NAME ON CARD") },
                            modifier = Modifier.weight(1f),
                            colors = fieldColors,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }.take(4)
                                expiryDate = if (digits.length >= 3) {
                                    "${digits.substring(0, 2)}/${digits.substring(2)}"
                                } else digits
                            },
                            label = { Text("Expiry (MM/YY)") },
                            placeholder = { Text("12/28") },
                            modifier = Modifier.weight(0.7f),
                            colors = fieldColors,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = cvv,
                            onValueChange = { cvv = it.filter { c -> c.isDigit() }.take(4) },
                            label = { Text("CVV") },
                            placeholder = { Text("•••") },
                            modifier = Modifier.weight(0.5f),
                            colors = fieldColors,
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bankOrIssuer,
                        onValueChange = { bankOrIssuer = it },
                        label = { Text("Issuer / Bank Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Card color theme picker
                    Text(
                        text = "Card Finish Theme",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val palette = listOf(
                            "#1E293B", "#0C2340", "#854D0E", "#134E4A", "#701A75", "#161618"
                        )
                        for (hex in palette) {
                            val c = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (selectedHex == hex) 3.dp else 1.dp,
                                        color = if (selectedHex == hex) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedHex = hex }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save to Wallet button
                    ElevatedButton(
                        onClick = {
                            val newCard = CardEntity(
                                title = title.ifBlank { "New Card" },
                                cardholderName = cardholderName.uppercase(),
                                cardNumber = cardNumber,
                                expiryDate = expiryDate,
                                cvv = cvv,
                                cardType = cardType,
                                bankOrIssuer = bankOrIssuer,
                                themeColorHex = selectedHex,
                                gradientEndHex = selectedHex,
                                notes = "Scanned with Gemini 3.1 Pro OCR",
                                scannedVia = "CAMERA_AI",
                                createdAt = System.currentTimeMillis()
                            )
                            haptics?.success()
                            onCardSaved(newCard)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFFE5A93C),
                            contentColor = Color(0xFF231404)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save to Secured Wallet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}
