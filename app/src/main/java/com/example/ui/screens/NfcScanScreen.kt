package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardArtOnlineService
import com.example.data.CardEntity
import com.example.nfc.NfcCardReaderManager
import com.example.nfc.NfcReadResult
import com.example.security.HapticsHelper
import com.example.ui.components.CreditCardItem

class NfcExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0, 4) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 && trimmed.length > 2) out += "/"
        }

        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 4) return offset + 1
                return 5
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return 4
            }
        }
        return TransformedText(AnnotatedString(out), offsetTranslator)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScanScreen(
    onNavigateBack: () -> Unit,
    onCardSaved: (CardEntity) -> Unit,
    onNavigateToCameraScan: () -> Unit = {},
    isOnlineCardArtEnabled: Boolean = false,
    onEnableOnlineCardArt: () -> Unit = {},
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val nfcManager = remember { NfcCardReaderManager(context) }

    var readResult by remember { mutableStateOf<NfcReadResult?>(null) }
    var detectedCard by remember { mutableStateOf<CardEntity?>(null) }

    // Verification fields editable by user
    var cardTitle by remember { mutableStateOf("") }
    var rawCardNumber by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var rawExpiryDigits by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var bankOrIssuer by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf("VISA") }
    var cardArtUrl by remember { mutableStateOf("") }
    var showFullNumber by remember { mutableStateOf(false) }

    // Start real NFC hardware listener
    DisposableEffect(Unit) {
        if (activity != null) {
            nfcManager.startListening(activity) { result ->
                haptics?.success()
                readResult = result
                detectedCard = result.suggestedCard

                result.suggestedCard?.let { card ->
                    cardTitle = card.title
                    rawCardNumber = card.cardNumber.replace("\\s+".toRegex(), "")
                    cardholderName = card.cardholderName
                    rawExpiryDigits = card.expiryDate.filter { it.isDigit() }
                    bankOrIssuer = card.bankOrIssuer
                    cardType = card.cardType
                    if (isOnlineCardArtEnabled) {
                        val suggested = CardArtOnlineService.suggestArtForCard(card)
                        if (suggested != null) {
                            cardArtUrl = suggested.imageUrl
                            if (bankOrIssuer.isBlank()) {
                                bankOrIssuer = suggested.issuer
                            }
                            if (suggested.cardType.isNotBlank()) {
                                cardType = if (suggested.cardType.uppercase().contains("DINER")) "DINERS" else suggested.cardType.uppercase()
                            }
                        }
                    }
                }
            }
        }
        onDispose {
            if (activity != null) {
                nfcManager.stopListening(activity)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "nfcWaves")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )
    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NFC Reader",
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
                actions = {
                    IconButton(onClick = onNavigateToCameraScan) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Switch to Camera",
                            tint = Color(0xFF38BDF8)
                        )
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
            Spacer(modifier = Modifier.height(20.dp))

            // Animated NFC Pulse Radar
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer wave
                Box(
                    modifier = Modifier
                        .size((140 * waveScale).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8).copy(alpha = waveAlpha))
                )

                // Middle ring
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = 0.35f))
                        .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), CircleShape)
                )

                // Core NFC antenna sensor icon
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF38BDF8),
                                    Color(0xFF0369A1),
                                    Color(0xFF0C4A6E)
                                )
                            )
                        )
                        .border(3.dp, Color(0xFF7DD3FC), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "NFC Sensor",
                        tint = Color.White,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (detectedCard != null) "Card Detected" else "Hold card to back of phone",
                color = if (detectedCard != null) Color(0xFF34D399) else Color(0xFF38BDF8),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            if (!nfcManager.isNfcEnabled) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF7F1D1D).copy(alpha = 0.6f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "NFC is disabled in system settings",
                        color = Color(0xFFFCA5A5),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Detected Card with Verification & Editing
            AnimatedVisibility(visible = detectedCard != null) {
                if (detectedCard != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Preview",
                            color = Color(0xFF38BDF8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dynamic live preview card
                        val previewExpiry = if (rawExpiryDigits.length >= 4) "${rawExpiryDigits.take(2)}/${rawExpiryDigits.drop(2)}"
                        else if (rawExpiryDigits.length >= 2) "${rawExpiryDigits.take(2)}/"
                        else rawExpiryDigits

                        CreditCardItem(
                            card = CardEntity(
                                title = cardTitle.ifBlank { "${bankOrIssuer.ifBlank { cardType }} Card" },
                                cardholderName = cardholderName.ifBlank { "CARDHOLDER" },
                                cardNumber = rawCardNumber.ifBlank { "•••• •••• •••• ••••" },
                                expiryDate = previewExpiry,
                                cvv = cvv,
                                cardType = cardType,
                                bankOrIssuer = bankOrIssuer.ifBlank { "Contactless" },
                                themeColorHex = detectedCard!!.themeColorHex,
                                gradientEndHex = detectedCard!!.gradientEndHex,
                                nfcTagUid = detectedCard!!.nfcTagUid,
                                scannedVia = "NFC",
                                cardArtUrl = cardArtUrl
                            ),
                            haptics = haptics
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Prompt to use Camera if PAN is restricted
                        if (rawCardNumber.isBlank() || readResult?.hasFullCardNumber == false) {
                            OutlinedButton(
                                onClick = {
                                    haptics?.cardSelect()
                                    onNavigateToCameraScan()
                                },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                            ) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan with Camera for Full Number", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Editable verification form
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

                        OutlinedTextField(
                            value = cardTitle,
                            onValueChange = { cardTitle = it },
                            label = { Text("Card Label") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors,
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Card Number input with PaymentCardVisualTransformation to prevent cursor jump!
                        OutlinedTextField(
                            value = rawCardNumber,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(19)
                                rawCardNumber = clean
                                cardType = when {
                                    clean.startsWith("4") -> "VISA"
                                    clean.startsWith("51") || clean.startsWith("52") || clean.startsWith("53") || clean.startsWith("54") || clean.startsWith("55") || (clean.length >= 4 && clean.substring(0, 4).toIntOrNull() in 2221..2720) -> "MASTERCARD"
                                    clean.startsWith("34") || clean.startsWith("37") -> "AMEX"
                                    clean.startsWith("36") || clean.startsWith("38") || clean.startsWith("30") || clean.startsWith("39") -> "DINERS"
                                    clean.startsWith("60") || clean.startsWith("65") || clean.startsWith("81") || clean.startsWith("82") || clean.startsWith("508") || clean.startsWith("353") || clean.startsWith("356") -> "RUPAY"
                                    else -> cardType
                                }
                            },
                            label = { Text("Card Number") },
                            placeholder = { Text("16-digit card number") },
                            visualTransformation = PaymentCardVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors,
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Payment Network",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
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
                                    val isSelected = cardType.equals(netKey, ignoreCase = true) || (netKey == "DINERS" && cardType.uppercase().contains("DINER"))
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptics?.cardSelect()
                                            cardType = netKey
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

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = cardholderName,
                            onValueChange = { cardholderName = it.uppercase() },
                            label = { Text("Cardholder Name") },
                            placeholder = { Text("NAME ON CARD") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors,
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = rawExpiryDigits,
                                onValueChange = { input ->
                                    rawExpiryDigits = input.filter { it.isDigit() }.take(4)
                                },
                                label = { Text("Expiry (MM/YY)") },
                                placeholder = { Text("MM/YY") },
                                visualTransformation = NfcExpiryDateVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1.2f),
                                colors = fieldColors,
                                singleLine = true
                            )

                            // Added CVV field since NFC does not transmit CVV for security
                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { cvv = it.filter { c -> c.isDigit() }.take(4) },
                                label = { Text("CVV (Optional)") },
                                placeholder = { Text("•••") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(0.8f),
                                colors = fieldColors,
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = bankOrIssuer,
                            onValueChange = { bankOrIssuer = it },
                            label = { Text("Bank / Issuer") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = fieldColors,
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        ElevatedButton(
                            onClick = {
                                val finalExpiry = if (rawExpiryDigits.length >= 4) {
                                    "${rawExpiryDigits.take(2)}/${rawExpiryDigits.drop(2)}"
                                } else rawExpiryDigits

                                val finalCard = detectedCard!!.copy(
                                    title = cardTitle.ifBlank { "${bankOrIssuer.ifBlank { cardType }} Card" },
                                    cardNumber = rawCardNumber,
                                    cardholderName = cardholderName.uppercase(),
                                    expiryDate = finalExpiry,
                                    cvv = cvv,
                                    bankOrIssuer = bankOrIssuer.ifBlank { cardType },
                                    cardType = cardType,
                                    cardArtUrl = cardArtUrl
                                )
                                haptics?.success()
                                onCardSaved(finalCard)
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
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save Card",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}
