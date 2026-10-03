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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardEntity
import com.example.nfc.NfcCardReaderManager
import com.example.nfc.NfcReadResult
import com.example.security.HapticsHelper
import com.example.ui.components.CreditCardItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScanScreen(
    onNavigateBack: () -> Unit,
    onCardSaved: (CardEntity) -> Unit,
    onNavigateToCameraScan: () -> Unit = {},
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
    var cardNumber by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var bankOrIssuer by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf("VISA") }

    // Start real NFC hardware listener
    DisposableEffect(Unit) {
        if (activity != null) {
            nfcManager.startListening(activity) { result ->
                haptics?.success()
                readResult = result
                detectedCard = result.suggestedCard

                result.suggestedCard?.let { card ->
                    cardTitle = card.title
                    cardNumber = card.cardNumber
                    cardholderName = card.cardholderName
                    expiryDate = card.expiryDate
                    bankOrIssuer = card.bankOrIssuer
                    cardType = card.cardType
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
                            text = "NFC Contactless Reader",
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
                            contentDescription = "Switch to Camera OCR",
                            tint = Color(0xFF38BDF8)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F172A)
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
                        .shadow(16.dp, CircleShape)
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
                text = if (detectedCard != null) "CARD DETECTED VIA NFC" else "READY TO SCAN",
                color = if (detectedCard != null) Color(0xFF34D399) else Color(0xFF38BDF8),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (detectedCard != null)
                    (readResult?.detailsMessage?.ifBlank { "Contactless ISO-14443 EMV Card Read" } ?: "Contactless Card Read")
                else
                    "Hold any physical credit card, debit card, or transit card firmly against the back of your device",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Real NFC Hardware Status & Guidance Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (nfcManager.isNfcEnabled) Color(0xFF34D399) else Color(0xFFF87171))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (nfcManager.isNfcEnabled) "HARDWARE NFC ANTENNA ACTIVE" else "NFC DISABLED IN SYSTEM SETTINGS",
                            color = if (nfcManager.isNfcEnabled) Color(0xFF38BDF8) else Color(0xFFF87171),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (nfcManager.isNfcEnabled)
                                "ISO/IEC 14443 Type A & B Contactless EMV Reader active. Hold card near top back of device."
                            else
                                "Please enable NFC in your device settings to tap and read cards.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Display Detected Card with Verification & Editing
            AnimatedVisibility(visible = detectedCard != null) {
                if (detectedCard != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "EXTRACTED CARD DETAILS",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic live preview card
                        CreditCardItem(
                            card = CardEntity(
                                title = cardTitle.ifBlank { "NFC Card" },
                                cardholderName = cardholderName.ifBlank { "CARDHOLDER" },
                                cardNumber = cardNumber.ifBlank { "•••• •••• •••• ••••" },
                                expiryDate = expiryDate.ifBlank { "••/••" },
                                cvv = "",
                                cardType = cardType,
                                bankOrIssuer = bankOrIssuer.ifBlank { "Contactless" },
                                themeColorHex = detectedCard!!.themeColorHex,
                                gradientEndHex = detectedCard!!.gradientEndHex,
                                nfcTagUid = detectedCard!!.nfcTagUid,
                                scannedVia = "NFC"
                            ),
                            haptics = haptics
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Prompt to use Camera OCR if PAN is restricted by the issuing bank's chip
                        if (cardNumber.isBlank() || readResult?.hasFullCardNumber == false) {
                            OutlinedButton(
                                onClick = {
                                    haptics?.cardSelect()
                                    onNavigateToCameraScan()
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8))
                            ) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scan with Camera OCR to Fill Details", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Editable verification form
                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFF38BDF8),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = cardholderName,
                                onValueChange = { cardholderName = it.uppercase() },
                                label = { Text("Cardholder Name") },
                                placeholder = { Text("NAME ON CARD") },
                                modifier = Modifier.weight(1.3f),
                                colors = fieldColors,
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { expiryDate = it },
                                label = { Text("Expiry (MM/YY)") },
                                placeholder = { Text("12/28") },
                                modifier = Modifier.weight(0.9f),
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

                        Spacer(modifier = Modifier.height(14.dp))

                        // NFC Diagnostic Specs Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1E293B))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "NFC TELEMETRY",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "UID: ${readResult?.tagUidHex ?: "UNKNOWN"}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (readResult?.emvAid?.isNotBlank() == true) {
                                    Text(
                                        text = "EMV AID: ${readResult?.emvAid}",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        ElevatedButton(
                            onClick = {
                                val finalCard = detectedCard!!.copy(
                                    title = cardTitle.ifBlank { "${bankOrIssuer.ifBlank { cardType }} Card" },
                                    cardNumber = cardNumber,
                                    cardholderName = cardholderName.uppercase(),
                                    expiryDate = expiryDate,
                                    bankOrIssuer = bankOrIssuer.ifBlank { cardType },
                                    cardType = cardType
                                )
                                haptics?.success()
                                onCardSaved(finalCard)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = Color(0xFF0284C7),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save to Secured Wallet",
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
