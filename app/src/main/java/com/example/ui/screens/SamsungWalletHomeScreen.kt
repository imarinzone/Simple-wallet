package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardEntity
import com.example.security.HapticsHelper
import com.example.ui.components.CreditCardItem
import com.example.ui.components.LeatherFinish
import com.example.ui.components.parseHexColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * Minimal, hyper-clean Samsung Wallet & Google Wallet inspired home interface.
 * - Horizontal sliding card carousel
 * - Active card details & Expressive big actions right below on the home page
 * - Minimal top bar with 3 dots (Settings, Sync, Lock)
 * - Single prominent "+ Add to Wallet" button at the bottom
 */
@Composable
fun SamsungWalletHomeScreen(
    cards: List<CardEntity>,
    onAddNewCard: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSync: () -> Unit,
    onLockWallet: () -> Unit,
    onDeleteCard: (CardEntity) -> Unit,
    onToggleFavorite: (CardEntity) -> Unit,
    leatherFinish: LeatherFinish,
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var showMenu by remember { mutableStateOf(false) }
    var cardToDelete by remember { mutableStateOf<CardEntity?>(null) }
    var isCardRevealed by remember { mutableStateOf(false) }
    var isNfcPayingAnimation by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { cards.size.coerceAtLeast(1) }
    )

    // Reset CVV reveal when changing cards
    LaunchedEffect(pagerState.currentPage) {
        isCardRevealed = false
        haptics?.cardSlide()
    }

    val activeCard = if (cards.isNotEmpty() && pagerState.currentPage < cards.size) {
        cards[pagerState.currentPage]
    } else {
        null
    }

    val primaryAccent = activeCard?.let { parseHexColor(it.themeColorHex, Color(0xFFE5A93C)) }
        ?: Color(0xFFE5A93C)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F0E0D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ================= 1. CLEAN TOP APP BAR =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Wallet",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    if (cards.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(horizontal = 9.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1}/${cards.size}",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Top right 3-dots overflow menu
                Box {
                    IconButton(
                        onClick = {
                            haptics?.cardSelect()
                            showMenu = true
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(Color(0xFF1E1A17))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings", color = Color.White, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFFE5A93C))
                            },
                            onClick = {
                                showMenu = false
                                onNavigateToSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sync & Cloud Backup", color = Color.White, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = Color(0xFF38BDF8))
                            },
                            onClick = {
                                showMenu = false
                                onNavigateToSync()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lock Wallet Now", color = Color.White, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFF87171))
                            },
                            onClick = {
                                showMenu = false
                                onLockWallet()
                            }
                        )
                    }
                }
            }

            // ================= 2. MAIN SCROLLABLE CONTENT =================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                if (cards.isEmpty()) {
                    // Empty Wallet State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF231C16)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = null,
                                    tint = Color(0xFFE5A93C),
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "No Cards in Wallet",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the button below to add a card via AI camera scan or NFC tap",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 30.dp),
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(6.dp))

                    // ================= 3. HORIZONTAL CARD SLIDER =================
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 32.dp),
                        pageSpacing = 16.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        val card = cards[page]
                        val pageOffset = (
                            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        ).absoluteValue

                        val cardScale by animateFloatAsState(
                            targetValue = if (pageOffset < 0.5f) 1f else 0.92f,
                            animationSpec = spring(stiffness = 300f),
                            label = "scale"
                        )
                        val cardAlpha by animateFloatAsState(
                            targetValue = if (pageOffset < 0.5f) 1f else 0.7f,
                            animationSpec = spring(stiffness = 300f),
                            label = "alpha"
                        )

                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = cardScale
                                    scaleY = cardScale
                                    alpha = cardAlpha
                                }
                        ) {
                            CreditCardItem(
                                card = card,
                                haptics = haptics
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Animated Dot Indicators
                    if (cards.size > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(cards.size) { index ->
                                val isSelected = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .height(6.dp)
                                        .width(if (isSelected) 22.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color(0xFFE5A93C) else Color.White.copy(alpha = 0.2f)
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // ================= 4. MATERIAL EXPRESSIVE BIG ACTION BUTTONS =================
                    if (activeCard != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Primary Contactless Pay Button
                            ElevatedButton(
                                onClick = {
                                    haptics?.success()
                                    isNfcPayingAnimation = true
                                    coroutineScope.launch {
                                        delay(2400)
                                        isNfcPayingAnimation = false
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = if (isNfcPayingAnimation) Color(0xFF10B981) else Color(0xFFE5A93C),
                                    contentColor = Color(0xFF1F1202)
                                ),
                                elevation = ButtonDefaults.elevatedButtonElevation(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isNfcPayingAnimation) Icons.Default.Check else Icons.Default.Contactless,
                                        contentDescription = "Pay",
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isNfcPayingAnimation) "Ready to Tap" else "Pay / Tap",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }

                            // Reveal CVV / Details Button
                            FilledTonalButton(
                                onClick = {
                                    haptics?.cardSelect()
                                    isCardRevealed = !isCardRevealed
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF261F1A),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = if (isCardRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Show Details",
                                    tint = Color(0xFFE5A93C),
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isCardRevealed) "Hide" else "CVV",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }

                            // Favorite Toggle Button
                            FilledTonalButton(
                                onClick = {
                                    haptics?.cardSelect()
                                    onToggleFavorite(activeCard.copy(isFavorite = !activeCard.isFavorite))
                                },
                                modifier = Modifier
                                    .size(54.dp),
                                shape = RoundedCornerShape(18.dp),
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Color(0xFF261F1A),
                                    contentColor = if (activeCard.isFavorite) Color(0xFFE5A93C) else Color.White.copy(alpha = 0.6f)
                                )
                            ) {
                                Icon(
                                    imageVector = if (activeCard.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // ================= 5. INLINE CARD DETAILS (Samsung Wallet Style) =================
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFF1B1613))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                                .padding(20.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = activeCard.title.ifBlank { "Payment Card" },
                                            color = Color.White,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${activeCard.bankOrIssuer} • ${activeCard.cardType}",
                                            color = Color(0xFFE5A93C),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    // Copy number button
                                    IconButton(
                                        onClick = {
                                            haptics?.cardSelect()
                                            clipboardManager.setText(AnnotatedString(activeCard.cardNumber))
                                            Toast.makeText(context, "Card number copied", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.08f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy Card Number",
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(16.dp))

                                // Information Grid
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("CARDHOLDER", color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = activeCard.cardholderName.ifBlank { "CARDHOLDER" },
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("EXPIRES", color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = activeCard.expiryDate.ifBlank { "••/••" },
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Column(modifier = Modifier.weight(0.8f)) {
                                        Text("SECURITY CODE", color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isCardRevealed) activeCard.cvv.ifBlank { "•••" } else "•••",
                                            color = if (isCardRevealed) Color(0xFFE5A93C) else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // NFC Telemetry if present
                                if (activeCard.nfcTagUid.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF0369A1).copy(alpha = 0.2f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sensors,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "NFC Tag UID: ${activeCard.nfcTagUid}",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Quick Delete button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            haptics?.cardSelect()
                                            cardToDelete = activeCard
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Remove Card",
                                            color = Color(0xFFEF4444).copy(alpha = 0.8f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }

            // ================= 6. BOTTOM BAR: SHOW ONLY THE ADD BUTTON =================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color(0xFF0F0E0D),
                tonalElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    ElevatedButton(
                        onClick = {
                            haptics?.cardSelect()
                            onAddNewCard()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFFE5A93C),
                            contentColor = Color(0xFF1E1002)
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E1002)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Card",
                                    tint = Color(0xFFE5A93C),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Add to Wallet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }
            }
        }

        // Delete Card Confirmation Dialog
        if (cardToDelete != null) {
            AlertDialog(
                onDismissRequest = { cardToDelete = null },
                title = { Text("Remove Card", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to remove ${cardToDelete!!.title} from your wallet?",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            haptics?.cardSelect()
                            onDeleteCard(cardToDelete!!)
                            cardToDelete = null
                        }

                    ) {
                        Text("Remove", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { cardToDelete = null }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                    }
                },
                containerColor = Color(0xFF1E1A17)
            )
        }
    }
}
