package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.CardArtDesign
import com.example.data.CardArtOnlineService
import com.example.data.CardEntity
import com.example.security.HapticsHelper
import com.example.ui.components.CreditCardItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardDialog(
    card: CardEntity,
    isOnlineCardArtEnabled: Boolean,
    onEnableOnlineCardArt: () -> Unit,
    onSaveCard: (CardEntity) -> Unit,
    onDismiss: () -> Unit,
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf(card.title) }
    var cardholderName by remember { mutableStateOf(card.cardholderName) }
    var rawCardNumber by remember { mutableStateOf(card.cardNumber.replace("\\s+".toRegex(), "")) }
    var rawExpiryDigits by remember { mutableStateOf(card.expiryDate.filter { it.isDigit() }) }
    var cvv by remember { mutableStateOf(card.cvv) }
    var bankOrIssuer by remember { mutableStateOf(card.bankOrIssuer) }
    var cardType by remember { mutableStateOf(card.cardType) }
    var cardArtUrl by remember { mutableStateOf(card.cardArtUrl) }
    var themeColorHex by remember { mutableStateOf(card.themeColorHex) }
    var gradientEndHex by remember { mutableStateOf(card.gradientEndHex) }
    var notes by remember { mutableStateOf(card.notes) }

    // Online Art Search State
    var showArtPickerSection by remember { mutableStateOf(false) }
    var artSearchQuery by remember { mutableStateOf(card.bankOrIssuer.ifBlank { card.title }) }
    var searchResults by remember { mutableStateOf<List<CardArtDesign>>(emptyList()) }
    var isSearchingArt by remember { mutableStateOf(false) }
    var showSettingDisabledPrompt by remember { mutableStateOf(false) }

    fun searchArt() {
        isSearchingArt = true
        coroutineScope.launch {
            searchResults = CardArtOnlineService.searchCardArt(
                query = artSearchQuery,
                issuer = bankOrIssuer,
                cardType = cardType
            )
            isSearchingArt = false
        }
    }

    LaunchedEffect(showArtPickerSection) {
        if (showArtPickerSection && searchResults.isEmpty()) {
            searchArt()
        }
    }

    val previewExpiry = if (rawExpiryDigits.length >= 4) "${rawExpiryDigits.take(2)}/${rawExpiryDigits.drop(2)}"
    else if (rawExpiryDigits.length >= 2) "${rawExpiryDigits.take(2)}/"
    else rawExpiryDigits.ifBlank { card.expiryDate }

    val previewCard = card.copy(
        title = title.ifBlank { "Card" },
        cardholderName = cardholderName.ifBlank { "CARDHOLDER" },
        cardNumber = rawCardNumber.ifBlank { "•••• •••• •••• ••••" },
        expiryDate = previewExpiry,
        cvv = cvv,
        bankOrIssuer = bankOrIssuer,
        cardType = cardType,
        cardArtUrl = cardArtUrl,
        themeColorHex = themeColorHex,
        gradientEndHex = gradientEndHex,
        notes = notes
    )

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
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Card & Artwork",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Preview Card with Authentic Face
            CreditCardItem(
                card = previewCard,
                haptics = haptics
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ================= ONLINE CARD ARTWORK SECTION =================
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
                        fontSize = 14.sp,
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

            Spacer(modifier = Modifier.height(8.dp))

            // Choose Official Artwork Button
            OutlinedButton(
                onClick = {
                    haptics?.cardSelect()
                    if (!isOnlineCardArtEnabled) {
                        showSettingDisabledPrompt = true
                    } else {
                        showArtPickerSection = !showArtPickerSection
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
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
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    // Search bar for official cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = artSearchQuery,
                            onValueChange = { artSearchQuery = it },
                            placeholder = { Text("Filter cards (e.g. Millennia, Swiggy, Tata, Shaurya)", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = fieldColors,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = { searchArt() }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isSearchingArt) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
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
                            text = "Select official bank card artwork:",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Horizontal Gallery of Card Artwork Cards
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            items(searchResults) { design ->
                                val isSelected = cardArtUrl == design.imageUrl
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
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
                                            themeColorHex = design.accentColorHex
                                            gradientEndHex = design.gradientEndHex
                                        }
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(88.dp)
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(design.imageUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = design.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                            )

                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .align(Alignment.TopEnd)
                                                        .padding(4.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }

                                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                            Text(
                                                text = design.name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = design.issuer,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ================= EDITABLE TEXT FIELDS =================
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Card Label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = bankOrIssuer,
                onValueChange = { bankOrIssuer = it },
                label = { Text("Bank / Issuer (e.g. Chase, Amex, Apple)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = cardholderName,
                onValueChange = { cardholderName = it.uppercase() },
                label = { Text("Cardholder Name") },
                placeholder = { Text("FULL NAME") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = rawCardNumber,
                onValueChange = { input ->
                    rawCardNumber = input.filter { it.isLetterOrDigit() }.take(24)
                },
                label = { Text("Card Number / Identifier") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1.2f),
                    colors = fieldColors,
                    singleLine = true
                )

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

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                singleLine = false,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Changes Button
            ElevatedButton(
                onClick = {
                    val finalExpiry = if (rawExpiryDigits.length >= 4) {
                        "${rawExpiryDigits.take(2)}/${rawExpiryDigits.drop(2)}"
                    } else rawExpiryDigits

                    val updated = card.copy(
                        title = title.ifBlank { card.title },
                        cardholderName = cardholderName.uppercase(),
                        cardNumber = rawCardNumber,
                        expiryDate = finalExpiry,
                        cvv = cvv,
                        bankOrIssuer = bankOrIssuer,
                        cardArtUrl = cardArtUrl,
                        themeColorHex = themeColorHex,
                        gradientEndHex = gradientEndHex,
                        notes = notes
                    )
                    haptics?.success()
                    onSaveCard(updated)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Changes", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                    "Displaying official card designs is currently turned off in Settings. Would you like to enable it now?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                ElevatedButton(
                    onClick = {
                        onEnableOnlineCardArt()
                        showSettingDisabledPrompt = false
                        showArtPickerSection = true
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
