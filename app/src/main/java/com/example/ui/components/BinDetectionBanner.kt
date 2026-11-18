package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardBinLookupResult

/**
 * Compact Material 3 tonal pill displaying live offline IIN/BIN detection results
 * (card network, issuing bank, Luhn check status, and NFC preservation indicator).
 */
@Composable
fun BinDetectionBanner(
    lookupResult: CardBinLookupResult,
    isNfcOrigin: Boolean = false,
    showApplyDetectedAction: Boolean = false,
    onApplyDetected: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val hasNetwork = !lookupResult.networkDisplayName.isNullOrBlank()
    val hasBank = !lookupResult.issuerBank.isNullOrBlank()
    val showBanner = hasNetwork || hasBank || lookupResult.isCompleteLengthForLuhn

    AnimatedVisibility(
        visible = showBanner,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bin_detection_banner"),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when {
                            isNfcOrigin -> Icons.Default.Nfc
                            lookupResult.isLuhnValid -> Icons.Default.CheckCircle
                            hasBank -> Icons.Default.AccountBalance
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = if (lookupResult.isLuhnValid) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    val summaryText = buildString {
                        if (hasNetwork) {
                            append(lookupResult.networkDisplayName)
                        }
                        if (hasBank) {
                            if (isNotEmpty()) append(" • ")
                            append(lookupResult.issuerBank)
                        }
                        if (lookupResult.isCompleteLengthForLuhn) {
                            if (isNotEmpty()) append(" • ")
                            append(if (lookupResult.isLuhnValid) "Valid card number" else "Check digits")
                        } else if (!hasBank && lookupResult.digits.length in 1..5) {
                            if (isNotEmpty()) append(" • ")
                            append("Enter ${6 - lookupResult.digits.length} more digits for bank")
                        }
                    }

                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (showApplyDetectedAction && onApplyDetected != null) {
                    TextButton(
                        onClick = onApplyDetected,
                        modifier = Modifier.testTag("apply_detected_bin_button")
                    ) {
                        Text(
                            text = "Apply",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
