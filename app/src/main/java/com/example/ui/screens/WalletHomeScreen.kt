package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import kotlin.math.roundToInt
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

import com.example.data.CardEntity
import com.example.security.HapticsHelper
import com.example.ui.components.CreditCardItem
import com.example.ui.components.LeatherFinish
import com.example.ui.components.parseHexColor
import com.example.ui.theme.CardMaterialYouTheme
import com.example.ui.theme.MaterialYouThemeEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Precision geometric shape mimicking a modern smartphone's camera teardrop display cutout,
 * seamlessly emerging from the bottom display edge with organic concave fillets.
 */
class TeardropCameraNotchShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val radius = with(density) { 19.dp.toPx() }
        val cy = with(density) { 21.dp.toPx() }

        val path = Path().apply {
            moveTo(0f, h)
            cubicTo(
                w * 0.16f, h,
                cx - radius * 1.15f, h * 0.62f,
                cx - radius, cy
            )
            arcTo(
                rect = Rect(
                    left = cx - radius,
                    top = cy - radius,
                    right = cx + radius,
                    bottom = cy + radius
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            cubicTo(
                cx + radius * 1.15f, h * 0.62f,
                w * 0.84f, h,
                w, h
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Minimal, hyper-clean Samsung Wallet & Google Wallet inspired home interface.
 * - Horizontal sliding card carousel
 * - Active card details & Expressive big actions right below on the home page
 * - Minimal top bar with 3 dots (Settings, Sync, Lock)
 * - Single camera teardrop notch "+ Add" button attached directly to the bottom
 */
@Composable
fun WalletHomeScreen(
    cards: List<CardEntity>,
    onAddNewCard: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSync: () -> Unit,
    onLockWallet: () -> Unit,
    onDeleteCard: (CardEntity) -> Unit,
    onToggleFavorite: (CardEntity) -> Unit,
    onEditCard: (CardEntity) -> Unit = {},
    onReorderCards: (List<CardEntity>) -> Unit = {},
    leatherFinish: LeatherFinish,
    isOverlayOpen: Boolean = false,
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
    var isCardSelected by remember { mutableStateOf(false) }
    var isVerticalStackMode by remember { mutableStateOf(false) }

    val addCardDragOffset = remember { Animatable(0f) }
    var lastAddCardHapticMilestone by remember { mutableFloatStateOf(0f) }

    // Hardware back navigation handler: collapses stack or deselects card
    BackHandler(enabled = isVerticalStackMode || isCardSelected) {
        if (isVerticalStackMode) {
            haptics?.stackCollapse()
            isVerticalStackMode = false
        } else {
            haptics?.cardSelect()
            isCardSelected = false
        }
    }

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

    val activeM3Theme = remember(activeCard?.themeColorHex, activeCard?.gradientEndHex) {
        activeCard?.let { MaterialYouThemeEngine.generateTheme(it) }
    }

    val primaryAccent = activeM3Theme?.primary ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                Text(
                    text = "Wallet",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

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
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Settings", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            onClick = {
                                showMenu = false
                                onNavigateToSettings()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Backup & Sync", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            },
                            onClick = {
                                showMenu = false
                                onNavigateToSync()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lock Wallet", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error)
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
                        }
                    }
                } else {
                    AnimatedContent(
                        targetState = isVerticalStackMode,
                        transitionSpec = {
                            (fadeIn(tween(300)) + expandVertically(spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)))
                                .togetherWith(fadeOut(tween(200)) + shrinkVertically(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)))
                        },
                        label = "WalletStackModeTransition"
                    ) { inStackMode ->
                        if (inStackMode) {
                            // ================= ALL CARDS VERTICAL STACK (SAMSUNG WALLET STYLE) =================
                            VerticalStackedCardsView(
                                cards = cards,
                                onCollapseToCarousel = {
                                    haptics?.stackCollapse()
                                    isVerticalStackMode = false
                                },
                                onEditCard = onEditCard,
                                onDeleteCard = { card -> cardToDelete = card },
                                onToggleFavorite = onToggleFavorite,
                                onReorderCards = onReorderCards,
                                haptics = haptics
                            )
                        } else {
                            // ================= HORIZONTAL CAROUSEL WITH PHYSICAL CARD DRAG-DOWN =================
                            val cardDragOffset = remember { Animatable(0f) }
                            var lastHapticMilestone by remember { mutableFloatStateOf(0f) }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(Unit) {
                                        detectVerticalDragGestures(
                                            onDragStart = {
                                                lastHapticMilestone = 0f
                                            },
                                            onDragEnd = {
                                                val currentOffset = cardDragOffset.value
                                                if (currentOffset >= 48f) {
                                                    // Dragged down past threshold! Open Samsung Wallet stack!
                                                    haptics?.stackExpand()
                                                    isVerticalStackMode = true
                                                    coroutineScope.launch {
                                                        cardDragOffset.snapTo(0f)
                                                    }
                                                } else {
                                                    // Release before threshold: spring back to carousel
                                                    coroutineScope.launch {
                                                        cardDragOffset.animateTo(
                                                            0f,
                                                            spring(
                                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                                stiffness = Spring.StiffnessLow
                                                            )
                                                        )
                                                    }
                                                }
                                                lastHapticMilestone = 0f
                                            },
                                            onDragCancel = {
                                                coroutineScope.launch {
                                                    cardDragOffset.animateTo(0f)
                                                }
                                                lastHapticMilestone = 0f
                                            },
                                            onVerticalDrag = { change, dragAmount ->
                                                if (dragAmount > 0f || cardDragOffset.value > 0f) {
                                                    change.consume()
                                                    val nextOffset = (cardDragOffset.value + dragAmount * 0.75f).coerceIn(0f, 160f)
                                                    coroutineScope.launch {
                                                        cardDragOffset.snapTo(nextOffset)
                                                    }
                                                    // Tactile physical resistance tick every 16px of pull
                                                    if (nextOffset - lastHapticMilestone >= 16f) {
                                                        haptics?.cardDragTick()
                                                        lastHapticMilestone = nextOffset
                                                    }
                                                }
                                            }
                                        )
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Minimal subtle drag handle
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .size(width = 36.dp, height = 4.dp)
                                        .clickable {
                                            haptics?.stackExpand()
                                            isVerticalStackMode = true
                                        }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // ================= 3. HORIZONTAL CARD SLIDER WITH INTERACTIVE PHYSICAL DRAG =================
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Stack preview peeking cards behind active card while pulling down
                                    if (cardDragOffset.value > 6f && cards.size > 1) {
                                        val nextIndex = (pagerState.currentPage + 1) % cards.size
                                        val nextCard = cards[nextIndex]
                                        val peek1 = (cardDragOffset.value * 0.35f).roundToInt()

                                        if (cards.size > 2) {
                                            val thirdIndex = (pagerState.currentPage + 2) % cards.size
                                            val thirdCard = cards[thirdIndex]
                                            val peek2 = (cardDragOffset.value * 0.65f).roundToInt()
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 42.dp)
                                                    .offset { IntOffset(0, peek2) }
                                                    .graphicsLayer {
                                                        scaleX = 0.90f
                                                        alpha = (cardDragOffset.value / 120f).coerceIn(0f, 0.6f)
                                                    }
                                            ) {
                                                CreditCardItem(card = thirdCard, enableTiltSensor = false)
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 36.dp)
                                                .offset { IntOffset(0, peek1) }
                                                .graphicsLayer {
                                                    scaleX = 0.95f
                                                    alpha = (cardDragOffset.value / 90f).coerceIn(0f, 0.85f)
                                                }
                                        ) {
                                            CreditCardItem(card = nextCard, enableTiltSensor = false)
                                        }
                                    }

                                    // Active front card carousel with physical offset
                                    HorizontalPager(
                                        state = pagerState,
                                        contentPadding = PaddingValues(horizontal = 32.dp),
                                        pageSpacing = 16.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset { IntOffset(0, cardDragOffset.value.roundToInt()) }
                                    ) { page ->
                                        val card = cards[page]
                                        val rawOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                                        val absOffset = rawOffset.absoluteValue.coerceIn(0f, 1f)

                                        // Smooth continuous card transformation during swipe
                                        val cardScale = 1f - (absOffset * 0.08f)
                                        val cardAlpha = 1f - (absOffset * 0.28f)
                                        val rotationY = (-rawOffset * 10f).coerceIn(-18f, 18f)

                                        Box(
                                            modifier = Modifier
                                                .graphicsLayer {
                                                    scaleX = cardScale
                                                    scaleY = cardScale
                                                    alpha = cardAlpha
                                                    cameraDistance = 16f * density
                                                    this.rotationY = rotationY
                                                }
                                        ) {
                                            CreditCardItem(
                                                card = card,
                                                haptics = haptics,
                                                onCardClick = {
                                                    haptics?.cardSelect()
                                                    isCardSelected = !isCardSelected
                                                }
                                            )
                                        }
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
                                            if (isSelected) (activeM3Theme?.primary ?: Color(0xFFE5A93C)) else Color.White.copy(alpha = 0.2f)
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // ================= 4. MATERIAL EXPRESSIVE DETAILS (SHOWN ONLY WHEN A CARD IS SELECTED) =================
                    AnimatedVisibility(
                        visible = isCardSelected && activeCard != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        if (activeCard != null) {
                            Column {
                                // Selected Card Status Pill & Collapse button
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp)
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(activeM3Theme?.primary ?: Color(0xFF10B981))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Card Selected",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    TextButton(
                                        onClick = {
                                            haptics?.cardSelect()
                                            isCardSelected = false
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = activeM3Theme?.primary ?: Color(0xFFE5A93C))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Collapse",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Collapse", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
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
                                            .weight(1.2f)
                                            .height(50.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = if (isNfcPayingAnimation) Color(0xFF10B981) else (activeM3Theme?.primary ?: Color(0xFFE5A93C)),
                                            contentColor = activeM3Theme?.onPrimary ?: Color(0xFF1F1202)
                                        ),
                                        elevation = ButtonDefaults.elevatedButtonElevation(4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isNfcPayingAnimation) Icons.Default.Check else Icons.Default.Contactless,
                                                contentDescription = "Pay",
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isNfcPayingAnimation) "Ready" else "Pay / Tap",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
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
                                            .height(50.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = activeM3Theme?.surfaceContainerHigh ?: Color(0xFF261F1A),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isCardRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Show Details",
                                                tint = activeM3Theme?.primary ?: Color(0xFFE5A93C),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isCardRevealed) "Hide" else "CVV",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Favorite Toggle Button
                                    FilledTonalButton(
                                        onClick = {
                                            haptics?.cardSelect()
                                            onToggleFavorite(activeCard.copy(isFavorite = !activeCard.isFavorite))
                                        },
                                        modifier = Modifier
                                            .size(50.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = activeM3Theme?.surfaceContainerHigh ?: Color(0xFF261F1A),
                                            contentColor = if (activeCard.isFavorite) (activeM3Theme?.tertiary ?: Color(0xFFE5A93C)) else Color.White.copy(alpha = 0.6f)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = if (activeCard.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = "Favorite",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                        Spacer(modifier = Modifier.height(18.dp))

                        // ================= 5. INLINE CARD DETAILS (Samsung Wallet Style) =================
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(
                                            text = activeCard.title.ifBlank { "Payment Card" },
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${activeCard.bankOrIssuer} • ${activeCard.cardType}",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Copy number button
                                        IconButton(
                                            onClick = {
                                                haptics?.cardSelect()
                                                clipboardManager.setText(AnnotatedString(activeCard.cardNumber))
                                                Toast.makeText(context, "Card number copied", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CreditCard,
                                                contentDescription = "Copy Card Number",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Edit card & artwork button
                                        IconButton(
                                            onClick = {
                                                haptics?.cardSelect()
                                                onEditCard(activeCard)
                                            },
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Card & Artwork",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                Spacer(modifier = Modifier.height(14.dp))

                                // Information Grid with clickable copy functionality
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .padding(end = 4.dp)
                                            .clickable {
                                                if (activeCard.cardholderName.isNotBlank()) {
                                                    clipboardManager.setText(AnnotatedString(activeCard.cardholderName))
                                                    Toast.makeText(context, "Cardholder name copied", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    ) {
                                        Text("CARDHOLDER", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = activeCard.cardholderName.ifBlank { "CARDHOLDER" },
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(0.9f)
                                            .padding(end = 4.dp)
                                            .clickable {
                                                if (activeCard.expiryDate.isNotBlank()) {
                                                    clipboardManager.setText(AnnotatedString(activeCard.expiryDate))
                                                    Toast.makeText(context, "Expiry date copied", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    ) {
                                        Text("EXPIRES", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = activeCard.expiryDate.ifBlank { "Not set" },
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(0.8f)
                                            .clickable {
                                                if (activeCard.cvv.isNotBlank()) {
                                                    clipboardManager.setText(AnnotatedString(activeCard.cvv))
                                                    Toast.makeText(context, "CVV code copied", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    ) {
                                        Text("SECURITY CODE", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (activeCard.cvv.isBlank()) "Not set" else if (isCardRevealed) activeCard.cvv else "•••",
                                            color = if (isCardRevealed && activeCard.cvv.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
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

                                // Quick Edit & Delete actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            haptics?.cardSelect()
                                            onEditCard(activeCard)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Card & Artwork",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Edit Card & Artwork",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

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
                }
            }
        }
    }
}



            // ================= 6. SLEEK CAMERA TEARDROP NOTCH PLUS BUTTON & DRAG-UP ADD CARD ANIMATION =================
            if (!isOverlayOpen && !isVerticalStackMode && !isCardSelected && cardToDelete == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Emerging animated credit card pulling up from bottom slot
                    if (addCardDragOffset.value > 2f) {
                        val pullFraction = (addCardDragOffset.value / 180f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset { IntOffset(0, -(addCardDragOffset.value * 1.35f).roundToInt()) }
                                .padding(horizontal = 24.dp)
                                .graphicsLayer {
                                    scaleX = 0.88f + (pullFraction * 0.12f)
                                    scaleY = 0.88f + (pullFraction * 0.12f)
                                    alpha = (pullFraction * 1.4f).coerceIn(0f, 1f)
                                    rotationX = (1f - pullFraction) * 16f
                                    shadowElevation = 24f * pullFraction
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .height(180.dp)
                                    .shadow(20.dp, RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF262032),
                                                Color(0xFF181424),
                                                Color(0xFF100D18)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        brush = Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                Color.White.copy(alpha = 0.4f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "NEW CARD",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Contactless,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "•••• •••• •••• ••••",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 14.sp,
                                            letterSpacing = 2.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = if (addCardDragOffset.value >= 60f) "RELEASE TO ADD" else "SWIPE UP",
                                            color = if (addCardDragOffset.value >= 60f) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val teardropShape = remember { TeardropCameraNotchShape() }
                    Box(
                        modifier = Modifier
                            .width(62.dp)
                            .height(48.dp)
                            .offset { IntOffset(0, -(addCardDragOffset.value * 0.25f).roundToInt()) }
                            .shadow(
                                elevation = 8.dp + (addCardDragOffset.value * 0.05f).dp,
                                shape = teardropShape,
                                clip = false
                            )
                            .clip(teardropShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF282830),
                                        Color(0xFF101014)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.32f),
                                        Color.White.copy(alpha = 0.08f)
                                    )
                                ),
                                shape = teardropShape
                            )
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragStart = {
                                        lastAddCardHapticMilestone = 0f
                                    },
                                    onDragEnd = {
                                        val currentPull = addCardDragOffset.value
                                        if (currentPull >= 60f) {
                                            haptics?.cardDraw()
                                            coroutineScope.launch {
                                                addCardDragOffset.animateTo(
                                                    220f,
                                                    spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                                                )
                                                onAddNewCard()
                                                addCardDragOffset.snapTo(0f)
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                addCardDragOffset.animateTo(
                                                    0f,
                                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                                )
                                            }
                                        }
                                        lastAddCardHapticMilestone = 0f
                                    },
                                    onDragCancel = {
                                        coroutineScope.launch {
                                            addCardDragOffset.animateTo(0f)
                                        }
                                        lastAddCardHapticMilestone = 0f
                                    },
                                    onVerticalDrag = { change, dragAmount ->
                                        if (dragAmount < 0f || addCardDragOffset.value > 0f) {
                                            change.consume()
                                            val nextPull = (addCardDragOffset.value - dragAmount * 0.9f).coerceIn(0f, 240f)
                                            coroutineScope.launch {
                                                addCardDragOffset.snapTo(nextPull)
                                            }
                                            if (nextPull - lastAddCardHapticMilestone >= 18f) {
                                                haptics?.cardDragTick()
                                                lastAddCardHapticMilestone = nextPull
                                            }
                                        }
                                    }
                                )
                            }
                            .clickable {
                                haptics?.cardDraw()
                                coroutineScope.launch {
                                    addCardDragOffset.animateTo(
                                        160f,
                                        spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
                                    )
                                    onAddNewCard()
                                    addCardDragOffset.snapTo(0f)
                                }
                            },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        // Precision camera lens aperture hole
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF08080C))
                                .border(
                                    width = 1.dp,
                                    brush = Brush.radialGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            Color(0x33FFFFFF)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Card",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
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
                title = { Text("Remove Card", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to remove ${cardToDelete!!.title} from your wallet?",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

/**
 * Backward compatibility wrapper for callers referencing the old name.
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
    onEditCard: (CardEntity) -> Unit = {},
    onReorderCards: (List<CardEntity>) -> Unit = {},
    leatherFinish: LeatherFinish,
    isOverlayOpen: Boolean = false,
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    WalletHomeScreen(
        cards = cards,
        onAddNewCard = onAddNewCard,
        onNavigateToSettings = onNavigateToSettings,
        onNavigateToSync = onNavigateToSync,
        onLockWallet = onLockWallet,
        onDeleteCard = onDeleteCard,
        onToggleFavorite = onToggleFavorite,
        onEditCard = onEditCard,
        onReorderCards = onReorderCards,
        leatherFinish = leatherFinish,
        isOverlayOpen = isOverlayOpen,
        haptics = haptics,
        modifier = modifier
    )
}

/**
 * Samsung Wallet style vertical stacked cards view where cards overlap
 * and users can see all cards stacked on top of each other.
 * Supports long-press dragging to reorder cards, and tapping to inspect details.
 */
@Composable
private fun VerticalStackedCardsView(
    cards: List<CardEntity>,
    onCollapseToCarousel: () -> Unit,
    onEditCard: (CardEntity) -> Unit,
    onDeleteCard: (CardEntity) -> Unit,
    onToggleFavorite: (CardEntity) -> Unit,
    onReorderCards: (List<CardEntity>) -> Unit = {},
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val density = LocalDensity.current

    var selectedCardId by remember { mutableStateOf<Long?>(null) }
    var orderedCards by remember(cards) { mutableStateOf(cards) }
    var draggingCardId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    var isNumberRevealed by remember { mutableStateOf(false) }
    var isCvvRevealed by remember { mutableStateOf(false) }

    val activeCard = if (selectedCardId != null) orderedCards.find { it.id == selectedCardId } else null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Minimal Return to Carousel Icon Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    haptics?.stackCollapse()
                    onCollapseToCarousel()
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Return to Carousel",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Overlapping Stack of Cards with Drag-to-Reorder physics
        val totalStackHeight = 210.dp + ((orderedCards.size - 1).coerceAtLeast(0) * 72).dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalStackHeight)
        ) {
            orderedCards.forEachIndexed { index, card ->
                val isBeingDragged = draggingCardId == card.id
                val isSelected = selectedCardId == card.id

                val cardScale by animateFloatAsState(
                    targetValue = if (isBeingDragged) 1.05f else if (isSelected) 1.02f else 1f,
                    animationSpec = spring(stiffness = 400f),
                    label = "scale"
                )

                val targetTopOffset = (index * 72).dp
                val animatedTopOffset by animateDpAsState(
                    targetValue = targetTopOffset,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.8f),
                    label = "cardPos"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(
                            y = if (isBeingDragged) (animatedTopOffset + with(density) { dragOffsetY.toDp() }) else animatedTopOffset
                        )
                        .zIndex(if (isBeingDragged) 1000f else (if (isSelected) 100f else index.toFloat()))
                        .graphicsLayer {
                            scaleX = cardScale
                            scaleY = cardScale
                            shadowElevation = if (isBeingDragged) 28f else (if (isSelected) 12f else 4f)
                        }
                        .pointerInput(card.id, orderedCards.size) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    draggingCardId = card.id
                                    dragOffsetY = 0f
                                    haptics?.cardDraw()
                                },
                                onDragEnd = {
                                    if (draggingCardId != null) {
                                        onReorderCards(orderedCards)
                                        haptics?.success()
                                    }
                                    draggingCardId = null
                                    dragOffsetY = 0f
                                },
                                onDragCancel = {
                                    draggingCardId = null
                                    dragOffsetY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetY += dragAmount.y
                                    val curIdx = orderedCards.indexOfFirst { it.id == card.id }
                                    if (curIdx != -1) {
                                        val stepPx = with(density) { 72.dp.toPx() }
                                        val targetIdx = (curIdx + (dragOffsetY / stepPx).roundToInt())
                                            .coerceIn(0, orderedCards.lastIndex)
                                        if (targetIdx != curIdx) {
                                            val updated = orderedCards.toMutableList()
                                            val item = updated.removeAt(curIdx)
                                            updated.add(targetIdx, item)
                                            orderedCards = updated
                                            dragOffsetY -= (targetIdx - curIdx) * stepPx
                                            haptics?.cardSlide()
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    CreditCardItem(
                        card = card,
                        enableTiltSensor = isSelected,
                        haptics = haptics,
                        onCardClick = {
                            if (draggingCardId == null) {
                                haptics?.cardDraw()
                                selectedCardId = if (selectedCardId == card.id) null else card.id
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= SELECTED CARD DETAILS (Shown ONLY when a particular card is selected) =================
        if (activeCard != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column {
                    // Card Title & Issuer Header + Quick Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = activeCard.title.ifBlank { "Card Details" },
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${activeCard.bankOrIssuer.ifBlank { "Bank" }} • ${activeCard.cardType}",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Favorite Toggle Button
                            IconButton(
                                onClick = {
                                    haptics?.cardSelect()
                                    onToggleFavorite(activeCard.copy(isFavorite = !activeCard.isFavorite))
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = if (activeCard.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (activeCard.isFavorite) Color(0xFFE5A93C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Close / Deselect Details Button
                            IconButton(
                                onClick = {
                                    haptics?.stackCollapse()
                                    selectedCardId = null
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Card Number Row with Mask/Unmask and Copy
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CARD NUMBER",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isNumberRevealed) activeCard.cardNumber.ifBlank { "•••• •••• •••• ••••" } else activeCard.maskedNumber,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    haptics?.cardSelect()
                                    isNumberRevealed = !isNumberRevealed
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isNumberRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isNumberRevealed) "Hide" else "Show",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    haptics?.cardSelect()
                                    clipboardManager.setText(AnnotatedString(activeCard.cardNumber))
                                    Toast.makeText(context, "Card number copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Information Grid: Cardholder, Expiry, CVV
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Cardholder
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    if (activeCard.cardholderName.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(activeCard.cardholderName))
                                        Toast.makeText(context, "Cardholder copied", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text("CARDHOLDER", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = activeCard.cardholderName.ifBlank { "Not set" },
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Expiry
                        Column(
                            modifier = Modifier
                                .weight(0.9f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    if (activeCard.expiryDate.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(activeCard.expiryDate))
                                        Toast.makeText(context, "Expiry copied", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text("EXPIRES", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = activeCard.expiryDate.ifBlank { "Not set" },
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // CVV
                        Column(
                            modifier = Modifier
                                .weight(0.9f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    if (activeCard.cvv.isNotBlank()) {
                                        isCvvRevealed = !isCvvRevealed
                                        clipboardManager.setText(AnnotatedString(activeCard.cvv))
                                        Toast.makeText(context, "CVV copied", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text("CVV / CVC", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (activeCard.cvv.isBlank()) "Not set" else if (isCvvRevealed) activeCard.cvv else "•••",
                                color = if (isCvvRevealed && activeCard.cvv.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    // NFC Tag Telemetry if present
                    if (activeCard.nfcTagUid.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
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

                    // Quick Edit & Delete actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                haptics?.cardSelect()
                                onEditCard(activeCard)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Card & Artwork",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit Card & Artwork",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        TextButton(
                            onClick = {
                                haptics?.cardSelect()
                                onDeleteCard(activeCard)
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
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

