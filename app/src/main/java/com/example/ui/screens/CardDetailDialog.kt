package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardEntity
import com.example.security.HapticsHelper
import com.example.ui.components.CreditCardItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailBottomSheet(
    card: CardEntity,
    onDismiss: () -> Unit,
    onDeleteCard: (CardEntity) -> Unit,
    onToggleFavorite: (CardEntity) -> Unit,
    onEditCard: (CardEntity) -> Unit = {},
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var showFullNumber by remember { mutableStateOf(false) }
    var showCvv by remember { mutableStateOf(false) }
    var copiedLabel by remember { mutableStateOf<String?>(null) }

    fun copyToClipboard(label: String, text: String) {
        if (text.isNotBlank()) {
            clipboardManager.setText(AnnotatedString(text))
            haptics?.success()
            copiedLabel = label
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.title.ifBlank { "Card Details" },
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row {
                    IconButton(
                        onClick = {
                            haptics?.cardSelect()
                            onToggleFavorite(card.copy(isFavorite = !card.isFavorite))
                        }
                    ) {
                        Icon(
                            imageVector = if (card.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (card.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Card Preview (Flat, shadowless)
            CreditCardItem(
                card = card,
                haptics = haptics
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Copied notification banner
            if (copiedLabel != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF34D399).copy(alpha = 0.2f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "✓ $copiedLabel Copied to Clipboard",
                        color = Color(0xFF34D399),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Quick Data Actions: Separate Copy Number & Copy Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        copyToClipboard("Card Number", card.cardNumber)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Number", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                if (card.cardholderName.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            copyToClipboard("Cardholder Name", card.cardholderName)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Name", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (card.expiryDate.isNotBlank() || card.cvv.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (card.expiryDate.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                copyToClipboard("Expiry Date", card.expiryDate)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Expiry", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (card.cvv.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                copyToClipboard("CVV Code", card.cvv)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy CVV", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card Details List with individual Copy buttons and Number Reveal Toggle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cardholder Name
                DetailRowWithActions(
                    label = if (card.cardType in listOf("ID_CARD", "RC_CARD")) "Holder / Owner" else "Cardholder",
                    displayValue = card.cardholderName.ifBlank { "N/A" },
                    onCopy = if (card.cardholderName.isNotBlank()) {
                        { copyToClipboard("Cardholder Name", card.cardholderName) }
                    } else null
                )

                // Card / Document Number (with Unmask / Reveal toggle button)
                val displayedNumber = if (showFullNumber) {
                    card.formattedNumber.ifBlank { card.cardNumber }
                } else {
                    card.maskedNumber
                }

                DetailRowWithActions(
                    label = when (card.cardType) {
                        "RC_CARD" -> "Vehicle Reg Number"
                        "ID_CARD" -> "ID / Document Number"
                        "VOUCHER" -> "Voucher Code"
                        "MISC" -> "Card Number / ID"
                        else -> "Card Number"
                    },
                    displayValue = displayedNumber,
                    isMonospace = true,
                    onToggleVisibility = {
                        haptics?.cardSelect()
                        showFullNumber = !showFullNumber
                    },
                    isVisibilityOn = showFullNumber,
                    onCopy = {
                        copyToClipboard("Card Number", card.cardNumber)
                    }
                )

                // Expiration
                if (card.expiryDate.isNotBlank()) {
                    DetailRowWithActions(
                        label = if (card.cardType in listOf("ID_CARD", "RC_CARD")) "Valid Till" else "Expiration",
                        displayValue = card.expiryDate,
                        isMonospace = true,
                        onCopy = { copyToClipboard("Expiry Date", card.expiryDate) }
                    )
                }

                // CVV
                if (card.cvv.isNotBlank()) {
                    DetailRowWithActions(
                        label = "CVV",
                        displayValue = if (showCvv) card.cvv else "•••",
                        isMonospace = true,
                        onToggleVisibility = {
                            haptics?.cardSelect()
                            showCvv = !showCvv
                        },
                        isVisibilityOn = showCvv,
                        onCopy = { copyToClipboard("CVV Code", card.cvv) }
                    )
                }

                // Issuer / Bank / Department
                if (card.bankOrIssuer.isNotBlank()) {
                    DetailRowWithActions(
                        label = if (card.cardType == "RC_CARD") "RTO / Authority" else "Issuer / Bank",
                        displayValue = card.bankOrIssuer,
                        onCopy = { copyToClipboard("Issuer", card.bankOrIssuer) }
                    )
                }

                // Card Type / Classification
                DetailRowWithActions(
                    label = "Card Type",
                    displayValue = when (card.cardType) {
                        "RC_CARD" -> "Vehicle Registration (RC)"
                        "ID_CARD" -> "Identity Document (ID)"
                        "VOUCHER" -> "Voucher / Gift Card"
                        "MISC" -> "Miscellaneous Card"
                        else -> card.cardType
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Edit Card & Artwork Action
            Button(
                onClick = {
                    haptics?.cardSelect()
                    onDismiss()
                    onEditCard(card)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Card & Artwork", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Delete Card Action
            OutlinedButton(
                onClick = {
                    haptics?.error()
                    onDeleteCard(card)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171).copy(alpha = 0.5f))
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Remove Card", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
private fun DetailRowWithActions(
    label: String,
    displayValue: String,
    isMonospace: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null,
    isVisibilityOn: Boolean = false,
    onCopy: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.weight(0.9f)
        )

        Row(
            modifier = Modifier.weight(1.3f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayValue,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                modifier = Modifier.weight(1f, fill = false)
            )

            if (onToggleVisibility != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isVisibilityOn) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Visibility",
                        tint = Color(0xFFE5A93C),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (onCopy != null) {
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy $label",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
