package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardEntity
import com.example.security.HapticsHelper
import com.example.ui.modifiers.rememberDeviceTiltState
import com.example.ui.modifiers.sensor3DTilt
import com.example.ui.theme.CardMaterialYouTheme
import com.example.ui.theme.MaterialYouThemeEngine

fun parseHexColor(hex: String, fallback: Color): Color {

    return try {
        val clean = hex.removePrefix("#")
        if (clean.length == 6) {
            Color(android.graphics.Color.parseColor("#$clean"))
        } else if (clean.length == 8) {
            Color(android.graphics.Color.parseColor("#$clean"))
        } else fallback
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun CreditCardItem(
    card: CardEntity,
    modifier: Modifier = Modifier,
    haptics: HapticsHelper? = null,
    isFlippedInitial: Boolean = false,
    enableTiltSensor: Boolean = true,
    onCardClick: (() -> Unit)? = null
) {
    var isFlipped by remember { mutableStateOf(isFlippedInitial) }
    var showFullCvv by remember { mutableStateOf(false) }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    // Real hardware device orientation sensor
    val tiltState by rememberDeviceTiltState(enabled = enableTiltSensor)

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "cardFlipAnimation"
    )

    // Generate complete Material You dynamic color theme from card palette
    val m3Theme = remember(card.themeColorHex, card.gradientEndHex) {
        MaterialYouThemeEngine.generateTheme(card)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .sensor3DTilt(
                tiltState = tiltState,
                maxTiltDegrees = 14f,
                baseElevation = 14.dp,
                shadowColor = m3Theme.ambientGlow,
                cornerRadius = 18.dp
            )
            .clip(RoundedCornerShape(18.dp))
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .background(m3Theme.cardGradient)
            .border(
                width = 1.dp,
                brush = m3Theme.borderBrush,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable {
                haptics?.cardFlip()
                if (onCardClick != null) {
                    onCardClick()
                } else {
                    isFlipped = !isFlipped
                }
            }
    ) {
        if (rotation <= 90f) {
            // FRONT OF CARD
            CardFrontContent(
                card = card,
                m3Theme = m3Theme,
                onFlipRequest = {
                    haptics?.cardFlip()
                    isFlipped = true
                }
            )
        } else {
            // BACK OF CARD (Mirror fix)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
            ) {
                CardBackContent(
                    card = card,
                    m3Theme = m3Theme,
                    showFullCvv = showFullCvv,
                    onToggleCvv = {
                        haptics?.cardSelect()
                        showFullCvv = !showFullCvv
                    },
                    onCopyNumber = {
                        clipboardManager.setText(AnnotatedString(card.cardNumber))
                        haptics?.success()
                    },
                    onFlipRequest = {
                        haptics?.cardFlip()
                        isFlipped = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CardFrontContent(
    card: CardEntity,
    m3Theme: CardMaterialYouTheme,
    onFlipRequest: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        // Metallic card surface sheen overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = size.width * 0.7f,
                center = Offset(size.width * 0.9f, size.height * 0.1f)
            )
        }

        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            // Top Row: Bank Name / Issuer + Contactless & Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)) {
                    Text(
                        text = card.bankOrIssuer.ifBlank { card.title }.uppercase(),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (card.bankOrIssuer.isNotBlank() && card.title != card.bankOrIssuer) {
                        Text(
                            text = card.title,
                            color = m3Theme.primary.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (card.nfcTagUid.isNotBlank() || card.scannedVia == "NFC") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(m3Theme.secondaryContainer.copy(alpha = 0.55f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "NFC ACTIVE",
                                color = m3Theme.onSecondaryContainer,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Icon(
                        imageVector = Icons.Default.Contactless,
                        contentDescription = "Contactless EMV",
                        tint = m3Theme.tertiary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Middle Row: EMV Chip + Card Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dynamic Metallic EMV Chip
                EmvChipView(
                    baseColor = m3Theme.chipBaseColor,
                    detailColor = m3Theme.chipDetailColor
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = card.maskedNumber,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            // Bottom Row: Cardholder Name, Expiry, and Network Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)) {
                    Text(
                        text = "CARDHOLDER",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = card.cardholderName.ifBlank { "CARDHOLDER" }.uppercase(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "EXPIRES",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = card.expiryDate.ifBlank { "••/••" },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                CardNetworkLogoBadge(cardType = card.cardType)
            }
        }
    }
}

@Composable
private fun CardBackContent(
    card: CardEntity,
    m3Theme: CardMaterialYouTheme,
    showFullCvv: Boolean,
    onToggleCvv: () -> Unit,
    onCopyNumber: () -> Unit,
    onFlipRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 14.dp)
    ) {
        // Magnetic Stripe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(Color(0xFF0F0F11))
                .border(width = 0.5.dp, color = m3Theme.outlineVariant)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Signature Bar & CVV Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // White signature strip
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Authorized Signature",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Dynamic Material You CVV Box with toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(m3Theme.tertiaryContainer)
                    .clickable { onToggleCvv() }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CVV: ",
                    color = m3Theme.onTertiaryContainer.copy(alpha = 0.75f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (showFullCvv) card.cvv.ifBlank { "•••" } else "•••",
                    color = m3Theme.onTertiaryContainer,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (showFullCvv) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Toggle CVV",
                    tint = m3Theme.onTertiaryContainer.copy(alpha = 0.85f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }


        Spacer(modifier = Modifier.weight(1f))

        // Bottom info and copy action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CUSTOMER SERVICE: 1-800-SECURE-VAULT",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 8.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Tap card to flip back",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )
            }

            IconButton(
                onClick = onCopyNumber,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Card Number",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmvChipView(
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFFFDE68A),
    detailColor: Color = Color(0xFFD97706)
) {
    Box(
        modifier = modifier
            .size(width = 38.dp, height = 28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        baseColor,
                        detailColor,
                        baseColor.copy(alpha = 0.85f)
                    )
                )
            )
            .border(0.5.dp, detailColor.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
            .padding(2.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1f
            // Chip internal division circuit lines
            drawLine(
                detailColor,
                Offset(size.width * 0.35f, 0f),
                Offset(size.width * 0.35f, size.height),
                strokeWidth
            )
            drawLine(
                detailColor,
                Offset(size.width * 0.65f, 0f),
                Offset(size.width * 0.65f, size.height),
                strokeWidth
            )
            drawLine(
                detailColor,
                Offset(0f, size.height * 0.5f),
                Offset(size.width, size.height * 0.5f),
                strokeWidth
            )
        }
    }
}


@Composable
fun CardNetworkLogoBadge(cardType: String, modifier: Modifier = Modifier) {
    when (cardType.uppercase()) {
        "VISA" -> {
            Text(
                text = "VISA",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                letterSpacing = 1.sp,
                modifier = modifier
            )
        }
        "MASTERCARD" -> {
            Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                )
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { translationX = -12f }
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B).copy(alpha = 0.9f))
                )
            }
        }
        "AMEX" -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF006FCF))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "AMEX",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        "DISCOVER" -> {
            Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DISC",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF97316))
                )
                Text(
                    text = "VER",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        "ID_CARD" -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "IDENTIFICATION",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        "TRANSIT" -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFA21CAF))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "TRANSIT PASS",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        else -> {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = cardType,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = modifier.size(24.dp)
            )
        }
    }
}
