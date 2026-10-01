package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardEntity
import com.example.security.HapticsHelper
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class LeatherFinish(
    val name: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val stitchColor: Color,
    val hardwareColor: Color
)

val LeatherFinishes = listOf(
    LeatherFinish(
        name = "Cognac Saddle",
        primaryColor = Color(0xFF5A2A18),
        secondaryColor = Color(0xFF38180D),
        stitchColor = Color(0xFFD49B55),
        hardwareColor = Color(0xFFE5A93C)
    ),
    LeatherFinish(
        name = "Obsidian Noir",
        primaryColor = Color(0xFF1E1D1C),
        secondaryColor = Color(0xFF0F0E0E),
        stitchColor = Color(0xFF716C68),
        hardwareColor = Color(0xFFD4AF37)
    ),
    LeatherFinish(
        name = "Midnight Navy",
        primaryColor = Color(0xFF132035),
        secondaryColor = Color(0xFF0A121E),
        stitchColor = Color(0xFF6B8AB8),
        hardwareColor = Color(0xFFE2E8F0)
    ),
    LeatherFinish(
        name = "Royal Emerald",
        primaryColor = Color(0xFF123524),
        secondaryColor = Color(0xFF081C13),
        stitchColor = Color(0xFF68B289),
        hardwareColor = Color(0xFFF3C569)
    )
)

@Composable
fun RealisticLeatherWallet(
    cards: List<CardEntity>,
    isWalletOpen: Boolean,
    onToggleWalletOpen: () -> Unit,
    onCardSelected: (CardEntity) -> Unit,
    onAddNewCard: () -> Unit,
    onScanWithNfc: () -> Unit,
    onScanWithCamera: () -> Unit,
    leatherFinish: LeatherFinish = LeatherFinishes[0],
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var activeSlidCardId by remember { mutableStateOf<Long?>(cards.firstOrNull()?.id) }

    // Synchronize active card if list updates
    LaunchedEffect(cards) {
        if (activeSlidCardId == null || cards.none { it.id == activeSlidCardId }) {
            activeSlidCardId = cards.firstOrNull()?.id
        }
    }

    // 3D Flap opening animation
    val openProgress by animateFloatAsState(
        targetValue = if (isWalletOpen) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "walletUnfold"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (!isWalletOpen) {
            // ================= CLOSED PHYSICAL WALLET =================
            ClosedWalletView(
                leatherFinish = leatherFinish,
                cardCount = cards.size,
                openProgress = openProgress,
                onOpen = {
                    haptics?.walletOpen()
                    onToggleWalletOpen()
                }
            )
        } else {
            // ================= OPEN UNFOLDED WALLET =================
            OpenWalletView(
                cards = cards,
                leatherFinish = leatherFinish,
                activeSlidCardId = activeSlidCardId,
                openProgress = openProgress,
                haptics = haptics,
                onCloseWallet = {
                    haptics?.walletClose()
                    onToggleWalletOpen()
                },
                onSlideCardOut = { card ->
                    haptics?.cardSlide()
                    activeSlidCardId = card.id
                },
                onCardClick = { card ->
                    haptics?.cardSelect()
                    onCardSelected(card)
                },
                onAddNewCard = onAddNewCard,
                onScanWithNfc = onScanWithNfc,
                onScanWithCamera = onScanWithCamera
            )
        }
    }
}

/**
 * Realistic Leather Exterior when wallet is closed
 */
@Composable
private fun ClosedWalletView(
    leatherFinish: LeatherFinish,
    cardCount: Int,
    openProgress: Float,
    onOpen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Main Leather Bi-fold Cover
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = leatherFinish.primaryColor.copy(alpha = 0.8f)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            leatherFinish.primaryColor,
                            leatherFinish.secondaryColor
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .clickable { onOpen() }
        ) {
            // Leather textured stitching along the borders
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 1.8f
                val margin = 16.dp.toPx()
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)

                // Outer perimeter stitching line
                drawRoundRect(
                    color = leatherFinish.stitchColor,
                    topLeft = Offset(margin, margin),
                    size = androidx.compose.ui.geometry.Size(size.width - 2 * margin, size.height - 2 * margin),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeW,
                        pathEffect = dashEffect
                    )
                )

                // Center spine fold crease
                drawLine(
                    color = Color.Black.copy(alpha = 0.45f),
                    start = Offset(size.width / 2, margin),
                    end = Offset(size.width / 2, size.height - margin),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.12f),
                    start = Offset(size.width / 2 + 2, margin),
                    end = Offset(size.width / 2 + 2, size.height - margin),
                    strokeWidth = 1.5f
                )
            }

            // Wallet details: Clasp and Gold Crest
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Embossed Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Secured Vault",
                        tint = leatherFinish.hardwareColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENCRYPTED VAULT",
                        color = leatherFinish.hardwareColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }

                // Center Golden Brass Snap Clasp
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(10.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFF1C2),
                                    leatherFinish.hardwareColor,
                                    Color(0xFF8C5815)
                                )
                            )
                        )
                        .border(2.dp, Color(0xFF52330A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Wallet Clasp",
                            tint = Color(0xFF422606),
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "OPEN",
                            color = Color(0xFF422606),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Bottom Branding & Card Counter
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "VAULTFOLIO",
                        color = leatherFinish.hardwareColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 3.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$cardCount Secured Cards Inside • Handcrafted Leather",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Open Wallet Action Button
        ElevatedButton(
            onClick = onOpen,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.elevatedButtonColors(
                containerColor = leatherFinish.hardwareColor,
                contentColor = Color(0xFF2A1702)
            ),
            elevation = ButtonDefaults.elevatedButtonElevation(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Unfold Leather Wallet",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Inside the unfolded Leather Wallet with sliding card slots
 */
@Composable
private fun OpenWalletView(
    cards: List<CardEntity>,
    leatherFinish: LeatherFinish,
    activeSlidCardId: Long?,
    openProgress: Float,
    haptics: HapticsHelper?,
    onCloseWallet: () -> Unit,
    onSlideCardOut: (CardEntity) -> Unit,
    onCardClick: (CardEntity) -> Unit,
    onAddNewCard: () -> Unit,
    onScanWithNfc: () -> Unit,
    onScanWithCamera: () -> Unit
) {
    val activeCard = cards.find { it.id == activeSlidCardId } ?: cards.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = openProgress
            }
    ) {
        // Header Controls: Fold Wallet & Quick Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onCloseWallet,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = leatherFinish.hardwareColor
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, leatherFinish.hardwareColor.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Close Wallet",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Fold Wallet", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            ElevatedButton(
                onClick = onAddNewCard,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = leatherFinish.hardwareColor,
                    contentColor = Color(0xFF281603)
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Card",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Card", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }


        Spacer(modifier = Modifier.height(10.dp))

        // Active Card Presentation (The card slid out of the wallet)
        if (activeCard != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE CARD • TAP TO FLIP",
                        color = leatherFinish.hardwareColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Tap for Details ❯",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        modifier = Modifier.clickable { onCardClick(activeCard) }
                    )
                }

                // Interactive 3D Card
                CreditCardItem(
                    card = activeCard,
                    haptics = haptics,
                    onCardClick = { onCardClick(activeCard) }
                )
            }
        }

        // Leather Card Organizer Slots (Cards sliding in from sides/slots)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(16.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            leatherFinish.primaryColor,
                            leatherFinish.secondaryColor
                        )
                    )
                )
                .border(
                    1.5.dp,
                    leatherFinish.stitchColor.copy(alpha = 0.6f),
                    RoundedCornerShape(22.dp)
                )
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Tier Label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WALLET POCKETS (${cards.size} CARDS)",
                        color = leatherFinish.stitchColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Text(
                        text = "Swipe side to slide out",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable tiered card slots with side-slide gesture controls
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(cards) { index, card ->
                        LeatherCardSlotItem(
                            card = card,
                            slotNumber = index + 1,
                            isActive = card.id == activeSlidCardId,
                            leatherFinish = leatherFinish,
                            haptics = haptics,
                            onSlideOut = { onSlideCardOut(card) },
                            onClick = {
                                onSlideCardOut(card)
                                onCardClick(card)
                            }
                        )
                    }

                    item {
                        // Empty slot prompt to add another card
                        AddCardSlotPrompt(
                            leatherFinish = leatherFinish,
                            onAdd = onAddNewCard
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Leather Pocket Tier where a card is stored.
 * Supports horizontal swipe gestures to slide card out from the side!
 */
@Composable
fun LeatherCardSlotItem(
    card: CardEntity,
    slotNumber: Int,
    isActive: Boolean,
    leatherFinish: LeatherFinish,
    haptics: HapticsHelper?,
    onSlideOut: () -> Unit,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val baseCardColor = parseHexColor(card.themeColorHex, Color(0xFF1E293B))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.35f))
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isActive) leatherFinish.hardwareColor else leatherFinish.stitchColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        // The Sliding Card Body inside the pocket
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            baseCardColor,
                            baseCardColor.copy(alpha = 0.85f),
                            parseHexColor(card.gradientEndHex, Color(0xFF0F172A))
                        )
                    )
                )
                .pointerInput(card.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                if (offsetX.value > 60f || offsetX.value < -60f) {
                                    // Trigger slide out & snap back
                                    haptics?.cardSlide()
                                    onSlideOut()
                                    offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                                } else {
                                    offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            coroutineScope.launch {
                                offsetX.snapTo(offsetX.value + dragAmount)
                            }
                        }
                    )
                }
                .clickable {
                    onSlideOut()
                    onClick()
                }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pocket Slot Number & EMV Mini Chip
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#$slotNumber",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = card.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = card.maskedNumber,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CardNetworkLogoBadge(cardType = card.cardType)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Slide card",
                        tint = if (isActive) leatherFinish.hardwareColor else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddCardSlotPrompt(
    leatherFinish: LeatherFinish,
    onAdd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .border(
                width = 1.dp,
                color = leatherFinish.stitchColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onAdd() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Card",
                tint = leatherFinish.hardwareColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add New Card to Slot",
                color = leatherFinish.hardwareColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
