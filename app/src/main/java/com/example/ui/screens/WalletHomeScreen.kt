package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.data.CardEntity
import com.example.security.HapticsHelper
import com.example.ui.components.CreditCardItem
import com.example.ui.components.LeatherFinish
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.MaterialYouThemeEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

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
        val radius = with(density) { 30.dp.toPx() }
        val cy = with(density) { 32.dp.toPx() }

        val path = Path().apply {
            moveTo(0f, h)
            cubicTo(
                w * 0.12f, h,
                cx - radius * 1.15f, h * 0.65f,
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
                cx + radius * 1.15f, h * 0.65f,
                w * 0.88f, h,
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
 * Creates the iconic scalloped floral / clover border inspired by the Google Pixel
 * Material You Clock Widget, with 8 smooth organic lobes.
 */
fun createGoogleClockBorderPath(width: Float, height: Float, lobes: Int = 8, lobeDepthRatio: Float = 0.15f): Path {
    val path = Path()
    val cx = width / 2f
    val cy = height / 2f
    val baseR = min(cx, cy) * 0.90f
    val steps = 180
    val stepAngle = (2.0 * PI) / steps

    var first = true
    for (i in 0..steps) {
        val angle = i * stepAngle
        val r = baseR - (baseR * lobeDepthRatio * (0.5f - 0.5f * cos(lobes * angle).toFloat()))
        val x = cx + r * cos(angle).toFloat()
        val y = cy + r * sin(angle).toFloat()
        if (first) {
            path.moveTo(x, y)
            first = false
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}

/**
 * Material 3 Expressive Wallet Home Screen in Jetpack Compose.
 * - M3 TopAppBar with tonal icon actions & dropdown menu
 * - Zero-shadow 3D card carousel & Samsung Wallet vertical stack
 * - Active card Material You actions with quick Copy button in the favorite action slot
 * - Rotating Google Pixel clock scalloped border "+ Add" button
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    val isPlusSignOn = !isOverlayOpen && !isVerticalStackMode && !isCardSelected && cardToDelete == null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(isPlusSignOn) {
                if (!isPlusSignOn) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                    var totalDragY = 0f
                    var totalDragX = 0f
                    var isDraggingUp = false

                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break

                        val delta = change.positionChange()
                        totalDragY += delta.y
                        totalDragX += delta.x

                        // Drag from down to up anywhere on the home screen
                        if (!isDraggingUp && totalDragY < -10f && abs(totalDragY) > abs(totalDragX) * 1.1f) {
                            isDraggingUp = true
                        }

                        if (isDraggingUp) {
                            change.consume()
                            val nextPull = (addCardDragOffset.value - delta.y * 1.15f).coerceIn(0f, 260f)
                            coroutineScope.launch {
                                addCardDragOffset.snapTo(nextPull)
                            }
                            if (nextPull - lastAddCardHapticMilestone >= 16f) {
                                haptics?.cardDragTick()
                                lastAddCardHapticMilestone = nextPull
                            }
                        }
                    }

                    if (isDraggingUp) {
                        val currentPull = addCardDragOffset.value
                        if (currentPull >= 50f) {
                            haptics?.cardDraw()
                            coroutineScope.launch {
                                addCardDragOffset.animateTo(
                                    240f,
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
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ================= 1. MATERIAL 3 TOP APP BAR =================
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Wallet",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (cards.isNotEmpty()) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "${cards.size}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (cards.size > 1) {
                        FilledTonalIconButton(
                            onClick = {
                                haptics?.stackExpand()
                                isVerticalStackMode = !isVerticalStackMode
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (isVerticalStackMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = if (isVerticalStackMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = if (isVerticalStackMode) "Carousel View" else "Stack View"
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Box {
                        FilledTonalIconButton(
                            onClick = {
                                haptics?.cardSelect()
                                showMenu = true
                            },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            shape = MaterialTheme.shapes.large,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Settings",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Backup & Sync",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSync()
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Lock Wallet",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onLockWallet()
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )

            // ================= 2. MAIN SCROLLABLE CONTENT =================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                if (cards.isEmpty()) {
                    // Material 3 Expressive Empty Wallet State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 28.dp, vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    modifier = Modifier.size(88.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(42.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "No Cards in Wallet",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Swipe up from the bottom or tap the + button to scan a card with AI Camera, NFC, or manual entry.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                FilledTonalButton(
                                    onClick = onAddNewCard,
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Add First Card",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
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
                                // Material 3 subtle drag pill handle
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                        .size(width = 36.dp, height = 4.dp)
                                        .clickable {
                                            haptics?.stackExpand()
                                            isVerticalStackMode = true
                                        }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

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
                                                        shadowElevation = 0f
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
                                                    shadowElevation = 0f
                                                }
                                        ) {
                                            CreditCardItem(card = nextCard, enableTiltSensor = false)
                                        }
                                    }

                                    // Active front card carousel with physical offset (Zero shadow in animation)
                                    HorizontalPager(
                                        state = pagerState,
                                        contentPadding = PaddingValues(horizontal = 28.dp),
                                        pageSpacing = 16.dp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset { IntOffset(0, cardDragOffset.value.roundToInt()) }
                                    ) { page ->
                                        val card = cards[page]
                                        val rawOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                                        val absOffset = rawOffset.absoluteValue.coerceIn(0f, 1f)

                                        // Smooth continuous card transformation during swipe (shadowless)
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
                                                    shadowElevation = 0f
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

                                Spacer(modifier = Modifier.height(16.dp))

                                // Material 3 Animated Pill Indicators
                                if (cards.size > 1) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        repeat(cards.size) { index ->
                                            val isSelected = pagerState.currentPage == index
                                            val dotWidth by animateDpAsState(
                                                targetValue = if (isSelected) 24.dp else 8.dp,
                                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                                label = "dotWidth"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 4.dp)
                                                    .height(8.dp)
                                                    .width(dotWidth)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) (activeM3Theme?.primary ?: MaterialTheme.colorScheme.primary)
                                                        else MaterialTheme.colorScheme.surfaceContainerHighest
                                                    )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                // ================= 4. MATERIAL 3 EXPRESSIVE CARD ACTIONS & QUICK COPY TAB =================
                                AnimatedVisibility(
                                    visible = isCardSelected && activeCard != null,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    if (activeCard != null) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp),
                                            shape = MaterialTheme.shapes.extraLarge,
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                            ),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp)
                                            ) {
                                                // Selected Card Status Pill & Collapse button
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    AssistChip(
                                                        onClick = {},
                                                        label = {
                                                            Text(
                                                                text = activeCard.title.ifBlank { "Card Selected" },
                                                                style = MaterialTheme.typography.labelMedium
                                                            )
                                                        },
                                                        leadingIcon = {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(8.dp)
                                                                    .clip(CircleShape)
                                                                    .background(activeM3Theme?.primary ?: MaterialTheme.colorScheme.primary)
                                                            )
                                                        },
                                                        shape = CircleShape,
                                                        colors = AssistChipDefaults.assistChipColors(
                                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                            labelColor = MaterialTheme.colorScheme.onSurface
                                                        ),
                                                        border = null
                                                    )

                                                    TextButton(
                                                        onClick = {
                                                            haptics?.cardSelect()
                                                            isCardSelected = false
                                                        },
                                                        colors = ButtonDefaults.textButtonColors(
                                                            contentColor = MaterialTheme.colorScheme.primary
                                                        )
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Collapse",
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Collapse",
                                                            style = MaterialTheme.typography.labelLarge
                                                        )
                                                    }
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Primary Contactless Pay Button
                                                    Button(
                                                        onClick = {
                                                            haptics?.success()
                                                            isNfcPayingAnimation = true
                                                            coroutineScope.launch {
                                                                delay(2400)
                                                                isNfcPayingAnimation = false
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .weight(1.25f)
                                                            .height(50.dp),
                                                        shape = MaterialTheme.shapes.medium,
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = if (isNfcPayingAnimation) MaterialTheme.colorScheme.tertiary else (activeM3Theme?.primary ?: MaterialTheme.colorScheme.primary),
                                                            contentColor = if (isNfcPayingAnimation) MaterialTheme.colorScheme.onTertiary else (activeM3Theme?.onPrimary ?: MaterialTheme.colorScheme.onPrimary)
                                                        )
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isNfcPayingAnimation) Icons.Default.Check else Icons.Default.Contactless,
                                                            contentDescription = "Pay",
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = if (isNfcPayingAnimation) "Ready" else "Pay / Tap",
                                                            style = MaterialTheme.typography.labelLarge,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
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
                                                        shape = MaterialTheme.shapes.medium,
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                                        colors = ButtonDefaults.filledTonalButtonColors(
                                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isCardRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                            contentDescription = "Show Details",
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = if (isCardRevealed) "Hide" else "CVV",
                                                            style = MaterialTheme.typography.labelLarge,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    // Quick Copy Card Number Tab Button (Replaces favorite star mark while keeping the tab slot)
                                                    FilledTonalButton(
                                                        onClick = {
                                                            haptics?.cardSelect()
                                                            clipboardManager.setText(AnnotatedString(activeCard.cardNumber))
                                                            Toast.makeText(context, "Card number copied", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.size(50.dp),
                                                        shape = MaterialTheme.shapes.medium,
                                                        contentPadding = PaddingValues(0.dp),
                                                        colors = ButtonDefaults.filledTonalButtonColors(
                                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.ContentCopy,
                                                            contentDescription = "Copy Card Number",
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }

                                                    // Edit Card & Artwork Button
                                                    FilledTonalButton(
                                                        onClick = {
                                                            haptics?.cardSelect()
                                                            onEditCard(activeCard)
                                                        },
                                                        modifier = Modifier.size(50.dp),
                                                        shape = MaterialTheme.shapes.medium,
                                                        contentPadding = PaddingValues(0.dp),
                                                        colors = ButtonDefaults.filledTonalButtonColors(
                                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                                            contentColor = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Edit,
                                                            contentDescription = "Edit Card",
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }

                                                // Revealed CVV / Number quick pill when toggled
                                                AnimatedVisibility(visible = isCardRevealed) {
                                                    Surface(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(top = 12.dp),
                                                        shape = MaterialTheme.shapes.medium,
                                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column {
                                                                Text(
                                                                    text = "NUMBER & SECURITY CODE",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                                Spacer(modifier = Modifier.height(2.dp))
                                                                Text(
                                                                    text = "${activeCard.formattedNumber.ifBlank { activeCard.cardNumber }}  •  CVV ${activeCard.cvv.ifBlank { "•••" }}",
                                                                    style = MaterialTheme.typography.bodyMedium,
                                                                    fontFamily = JetBrainsMonoFamily,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.onSurface
                                                                )
                                                            }
                                                            IconButton(
                                                                onClick = {
                                                                    haptics?.cardSelect()
                                                                    clipboardManager.setText(AnnotatedString(activeCard.cardNumber))
                                                                    Toast.makeText(context, "Card number copied", Toast.LENGTH_SHORT).show()
                                                                }
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.ContentCopy,
                                                                    contentDescription = "Copy",
                                                                    tint = MaterialTheme.colorScheme.primary,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
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
                    // Emerging animated credit card pulling up from bottom slot (Zero shadow)
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
                                    shadowElevation = 0f
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.92f)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primaryContainer,
                                                MaterialTheme.colorScheme.surfaceContainerHigh,
                                                MaterialTheme.colorScheme.surfaceContainer
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        brush = Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
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
                                            style = MaterialTheme.typography.labelLarge,
                                            letterSpacing = 2.sp
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Contactless,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "•••• •••• •••• ••••",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 14.sp,
                                            letterSpacing = 2.sp,
                                            fontFamily = JetBrainsMonoFamily
                                        )
                                        Text(
                                            text = if (addCardDragOffset.value >= 60f) "RELEASE TO ADD" else "SWIPE UP",
                                            color = if (addCardDragOffset.value >= 60f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val infiniteTransition = rememberInfiniteTransition(label = "GoogleClockBorderTransition")
                    val idleClockRotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 16000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "idleClockRotation"
                    )

                    val currentRotation = idleClockRotation + (addCardDragOffset.value * 1.5f)
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val density = LocalDensity.current
                    val halfCoverOffsetPx = with(density) { 34.dp.toPx() }
                    val currentYOffset = (halfCoverOffsetPx - (addCardDragOffset.value * 0.7f)).roundToInt()

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(0, currentYOffset) }
                            .size(68.dp)
                            .clickable {
                                haptics?.cardDraw()
                                coroutineScope.launch {
                                    addCardDragOffset.animateTo(
                                        200f,
                                        spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)
                                    )
                                    onAddNewCard()
                                    addCardDragOffset.snapTo(0f)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Google Widget Clock scalloped border rotating when idle + centered plus sign (No outer or inner circles)
                        Box(
                            modifier = Modifier.size(64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Rotating Scalloped Floral Border (Google Pixel Material You Clock Widget)
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val cx = w / 2f
                                val cy = h / 2f

                                rotate(currentRotation, pivot = Offset(cx, cy)) {
                                    val clockPath = createGoogleClockBorderPath(w, h, lobes = 8, lobeDepthRatio = 0.15f)

                                    // Material 3 Primary themed border stroke
                                    drawPath(
                                        path = clockPath,
                                        color = primaryColor.copy(alpha = 0.85f),
                                        style = Stroke(width = 2.2.dp.toPx())
                                    )
                                }
                            }

                            // Bold Plus Sign in Material 3 Primary color directly inside rotating border
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Card",
                                tint = primaryColor,
                                modifier = Modifier.size(30.dp)
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
                title = {
                    Text(
                        text = "Remove Card",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to remove ${cardToDelete!!.title} from your wallet?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            haptics?.cardSelect()
                            onDeleteCard(cardToDelete!!)
                            cardToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Remove", style = MaterialTheme.typography.labelLarge)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { cardToDelete = null }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.extraLarge
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
 * Samsung Wallet style vertical stacked cards view in Material 3 Compose.
 * Supports long-press dragging to reorder cards (shadowless), and tapping to inspect details.
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
        // Material 3 Return to Carousel Chip
        AssistChip(
            onClick = {
                haptics?.stackCollapse()
                onCollapseToCarousel()
            },
            label = {
                Text(
                    text = "Collapse Stack",
                    style = MaterialTheme.typography.labelMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Return to Carousel",
                    modifier = Modifier.size(18.dp)
                )
            },
            shape = CircleShape,
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                leadingIconContentColor = MaterialTheme.colorScheme.primary
            ),
            border = null
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Overlapping Stack of Cards with Drag-to-Reorder physics (Zero shadow)
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
                            shadowElevation = 0f
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

        // ================= SELECTED CARD DETAILS (Material 3 Tonal Card) =================
        if (activeCard != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Card Title & Issuer Header + Quick Actions (Copy replaces Favorite Star)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(
                                text = activeCard.title.ifBlank { "Card Details" },
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${activeCard.bankOrIssuer.ifBlank { "Bank" }} • ${activeCard.cardType}",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Copy Card Number Button (replaces favorite star mark)
                            FilledTonalIconButton(
                                onClick = {
                                    haptics?.cardSelect()
                                    clipboardManager.setText(AnnotatedString(activeCard.cardNumber))
                                    Toast.makeText(context, "Card number copied", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(38.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Card Number",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Close / Deselect Details Button
                            FilledTonalIconButton(
                                onClick = {
                                    haptics?.stackCollapse()
                                    selectedCardId = null
                                },
                                modifier = Modifier.size(38.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Details",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Card Number Row with Mask/Unmask and Copy
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "CARD NUMBER",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isNumberRevealed) activeCard.cardNumber.ifBlank { "•••• •••• •••• ••••" } else activeCard.maskedNumber,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontFamily = JetBrainsMonoFamily,
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
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Information Grid: Cardholder, Expiry, CVV
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cardholder
                        Surface(
                            modifier = Modifier
                                .weight(1.1f)
                                .clickable {
                                    if (activeCard.cardholderName.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(activeCard.cardholderName))
                                        Toast.makeText(context, "Cardholder copied", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text(
                                    text = "CARDHOLDER",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = activeCard.cardholderName.ifBlank { "Not set" },
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Expiry
                        Surface(
                            modifier = Modifier
                                .weight(0.9f)
                                .clickable {
                                    if (activeCard.expiryDate.isNotBlank()) {
                                        clipboardManager.setText(AnnotatedString(activeCard.expiryDate))
                                        Toast.makeText(context, "Expiry copied", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text(
                                    text = "EXPIRES",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = activeCard.expiryDate.ifBlank { "Not set" },
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                        }

                        // CVV
                        Surface(
                            modifier = Modifier
                                .weight(0.9f)
                                .clickable {
                                    if (activeCard.cvv.isNotBlank()) {
                                        isCvvRevealed = !isCvvRevealed
                                        clipboardManager.setText(AnnotatedString(activeCard.cvv))
                                        Toast.makeText(context, "CVV copied", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text(
                                    text = "CVV / CVC",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (activeCard.cvv.isBlank()) "Not set" else if (isCvvRevealed) activeCard.cvv else "•••",
                                    color = if (isCvvRevealed && activeCard.cvv.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // NFC Tag Telemetry if present
                    if (activeCard.nfcTagUid.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "NFC Tag UID: ${activeCard.nfcTagUid}",
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 11.sp,
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Edit & Delete actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = {
                                haptics?.cardSelect()
                                onEditCard(activeCard)
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Card & Artwork",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit Card & Artwork",
                                style = MaterialTheme.typography.labelLarge
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
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Remove Card",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
