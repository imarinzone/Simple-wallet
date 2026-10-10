package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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

/**
 * Dedicated Full-Page Screen for editing an existing card and managing its official artwork.
 * This is rendered as an independent full-screen page (NOT a bottom sheet) to avoid
 * conflicting with horizontal swiping when browsing artwork and to prevent accidental dismissals.
 * Per UX guidelines, copy actions are intentionally excluded while in the edit screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    card: CardEntity,
    isOnlineCardArtEnabled: Boolean,
    onEnableOnlineCardArt: () -> Unit,
    onSaveCard: (CardEntity) -> Unit,
    onDismiss: () -> Unit,
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    // Hardware & Gesture Back Handler to safely return to wallet
    BackHandler(onBack = onDismiss)

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

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
    var showArtPickerSection by remember { mutableStateOf(true) }
    var artSearchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchResults by remember { mutableStateOf<List<CardArtDesign>>(emptyList()) }
    var isSearchingArt by remember { mutableStateOf(false) }
    var showCustomUrlField by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf("") }
    var showSettingDisabledPrompt by remember { mutableStateOf(false) }

    fun triggerArtSearch(query: String = artSearchQuery, category: String = selectedCategory) {
        isSearchingArt = true
        coroutineScope.launch {
            val catFilter = if (category.equals("All", ignoreCase = true)) "" else category
            searchResults = CardArtOnlineService.searchCardArt(
                query = query,
                category = catFilter
            )
            isSearchingArt = false
        }
    }

    LaunchedEffect(Unit) {
        // Initial load of artwork catalog
        triggerArtSearch(query = "", category = "All")
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

    fun performSave() {
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
            cardType = cardType,
            cardArtUrl = cardArtUrl,
            themeColorHex = themeColorHex,
            gradientEndHex = gradientEndHex,
            notes = notes
        )
        haptics?.success()
        onSaveCard(updated)
        onDismiss()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Edit Card",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (card.title.isNotBlank()) {
                            Text(
                                text = card.title,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { performSave() }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // ================= 1. LIVE PREVIEW CARD =================
            CreditCardItem(
                card = previewCard,
                haptics = haptics
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ================= 2. OFFICIAL CARD ARTWORK SECTION =================
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (cardArtUrl.isNotBlank()) {
                        TextButton(
                            onClick = {
                                haptics?.cardSelect()
                                cardArtUrl = ""
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Art", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }

                    TextButton(
                        onClick = {
                            if (!isOnlineCardArtEnabled) {
                                showSettingDisabledPrompt = true
                            } else {
                                showArtPickerSection = !showArtPickerSection
                            }
                        }
                    ) {
                        Text(
                            text = if (showArtPickerSection) "Hide Gallery" else "Show Gallery",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showArtPickerSection) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    // Search Bar with instant real-time query filtering
                    OutlinedTextField(
                        value = artSearchQuery,
                        onValueChange = {
                            artSearchQuery = it
                            triggerArtSearch(query = it, category = selectedCategory)
                        },
                        placeholder = { Text("Search cards (e.g. Chase, Sapphire, Millennia, Amex, Swiggy)", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                focusManager.clearFocus()
                                triggerArtSearch(query = artSearchQuery, category = selectedCategory)
                            }
                        ),
                        trailingIcon = {
                            if (artSearchQuery.isNotBlank()) {
                                IconButton(onClick = {
                                    artSearchQuery = ""
                                    triggerArtSearch(query = "", category = selectedCategory)
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Chips
                    val categories = listOf("All", "Chase", "Amex", "HDFC", "SBI", "Capital One", "Citi", "Discover", "Metal", "Visa", "Mastercard", "RuPay")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isCatSelected = selectedCategory.equals(cat, ignoreCase = true)
                            FilterChip(
                                selected = isCatSelected,
                                onClick = {
                                    haptics?.cardSelect()
                                    selectedCategory = cat
                                    triggerArtSearch(query = artSearchQuery, category = cat)
                                },
                                label = {
                                    Text(text = cat, fontSize = 11.sp, fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal)
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Gallery of Card Designs
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No artwork matches '${artSearchQuery.ifBlank { selectedCategory }}'. Tap 'All' to browse catalog.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Swipe to browse (${searchResults.size} designs):",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            items(searchResults) { design ->
                                val isSelected = cardArtUrl == design.imageUrl
                                Box(
                                    modifier = Modifier
                                        .width(148.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .background(MaterialTheme.colorScheme.surface)
                                        .clickable {
                                            haptics?.cardSelect()
                                            cardArtUrl = design.imageUrl
                                            themeColorHex = design.accentColorHex
                                            gradientEndHex = design.gradientEndHex
                                            bankOrIssuer = design.issuer
                                            if (title.isBlank() || title.contains("Card", ignoreCase = true)) {
                                                title = design.name
                                            }
                                            if (design.cardType.isNotBlank()) {
                                                cardType = if (design.cardType.uppercase().contains("DINER")) "DINERS" else design.cardType.uppercase()
                                            }
                                        }
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(92.dp)
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(design.imageUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = design.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Option to paste a custom artwork image URL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showCustomUrlField = !showCustomUrlField }
                        ) {
                            Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showCustomUrlField) "Hide Custom URL" else "Use Custom Image URL",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (showCustomUrlField) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customUrlInput,
                                onValueChange = { customUrlInput = it },
                                placeholder = { Text("Paste online image URL (https://...)", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = fieldColors,
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            ElevatedButton(
                                onClick = {
                                    if (customUrlInput.isNotBlank()) {
                                        cardArtUrl = customUrlInput.trim()
                                        haptics?.success()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Apply", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ================= 3. EDITABLE FORM FIELDS (NO COPY BUTTONS) =================
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Card Label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = bankOrIssuer,
                onValueChange = { bankOrIssuer = it },
                label = { Text("Bank / Issuer (e.g. Chase, Amex, Apple)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Payment Network Selection
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
                        "RUPAY" to "RuPay",
                        "DISCOVER" to "Discover"
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
                placeholder = { Text("FULL NAME") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
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
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
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
                    shape = RoundedCornerShape(12.dp),
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
                    shape = RoundedCornerShape(12.dp),
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
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ================= 4. ACTION BUTTONS =================
            ElevatedButton(
                onClick = { performSave() },
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

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text("Cancel", fontSize = 14.sp)
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

/**
 * Backward compatibility alias for any existing callers.
 */
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
    EditCardScreen(
        card = card,
        isOnlineCardArtEnabled = isOnlineCardArtEnabled,
        onEnableOnlineCardArt = onEnableOnlineCardArt,
        onSaveCard = onSaveCard,
        onDismiss = onDismiss,
        haptics = haptics,
        modifier = modifier
    )
}
