package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.security.HapticsHelper
import kotlin.math.PI
import kotlin.math.cos

/**
 * Builds the scalloped/ridged top contour path for the "Add Card to Wallet" tab
 * so that its top edge has organic sinusoidal ridges matching the Google Pixel Clock
 * scalloped border of the '+' button.
 */
fun createRidgedTabTopPath(
    width: Float,
    height: Float,
    topInsetY: Float,
    ridgeAmplitude: Float,
    ridgeCount: Int = 12
): Path {
    val path = Path()
    val steps = 240
    path.moveTo(0f, height)
    path.lineTo(0f, topInsetY + ridgeAmplitude)

    for (i in 0..steps) {
        val t = i.toFloat() / steps.toFloat()
        val x = t * width
        // Smoothly taper ridge amplitude near the left/right edges for clean rounded corners
        val edgeEnvelope = kotlin.math.sin(PI * t).toFloat()
        val wave = 0.5f - 0.5f * cos(ridgeCount * 2.0 * PI * t).toFloat()
        val y = topInsetY + (ridgeAmplitude * wave * edgeEnvelope)
        path.lineTo(x, y)
    }

    path.lineTo(width, height)
    path.close()
    return path
}

/**
 * Builds the open stroke path along the ridged top edge of the "Add Card to Wallet" tab
 * so it shares the exact primary ridge stroke style as the expanding '+' sign.
 */
fun createRidgedTabTopStrokePath(
    width: Float,
    topInsetY: Float,
    ridgeAmplitude: Float,
    ridgeCount: Int = 12
): Path {
    val path = Path()
    val steps = 240
    for (i in 0..steps) {
        val t = i.toFloat() / steps.toFloat()
        val x = t * width
        val edgeEnvelope = kotlin.math.sin(PI * t).toFloat()
        val wave = 0.5f - 0.5f * cos(ridgeCount * 2.0 * PI * t).toFloat()
        val y = topInsetY + (ridgeAmplitude * wave * edgeEnvelope)
        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    return path
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardChooserBottomSheet(
    onDismiss: () -> Unit,
    onChooseCameraScan: () -> Unit,
    onChooseNfcScan: () -> Unit,
    onChooseManualEntry: () -> Unit = onChooseCameraScan,
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val infiniteTransition = rememberInfiniteTransition(label = "SheetPlusClockRotation")
    val idleClockRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheetClockRotation"
    )

    val sheetSurfaceColor = MaterialTheme.colorScheme.surfaceContainerLow
    val primaryColor = MaterialTheme.colorScheme.primary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .drawBehind {
                    val topInsetPx = 56.dp.toPx()
                    val ridgeAmpPx = 10.dp.toPx()
                    val fillPath = createRidgedTabTopPath(
                        width = size.width,
                        height = size.height,
                        topInsetY = topInsetPx,
                        ridgeAmplitude = ridgeAmpPx,
                        ridgeCount = 12
                    )
                    drawPath(
                        path = fillPath,
                        color = sheetSurfaceColor,
                        style = Fill
                    )

                    val strokePath = createRidgedTabTopStrokePath(
                        width = size.width,
                        topInsetY = topInsetPx,
                        ridgeAmplitude = ridgeAmpPx,
                        ridgeCount = 12
                    )
                    drawPath(
                        path = strokePath,
                        color = primaryColor.copy(alpha = 0.85f),
                        style = Stroke(width = 2.4.dp.toPx())
                    )
                }
        ) {
            // Enlarged Scalloped/Ridged '+' Sign overlapping the top of the Ridged Add Card Tab
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(116.dp)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f

                    rotate(idleClockRotation, pivot = Offset(cx, cy)) {
                        val clockPath = createGoogleClockBorderPath(
                            width = w,
                            height = h,
                            lobes = 8,
                            lobeDepthRatio = 0.15f
                        )
                        // Filled interior matching the tab surface so the plus sign and tab fuse seamlessly
                        drawPath(
                            path = clockPath,
                            color = sheetSurfaceColor,
                            style = Fill
                        )
                        // Ridged primary border matching the tab's top ridge stroke
                        drawPath(
                            path = clockPath,
                            color = primaryColor.copy(alpha = 0.9f),
                            style = Stroke(width = 2.6.dp.toPx())
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Card",
                    tint = primaryColor,
                    modifier = Modifier.size(52.dp)
                )
            }

            // Tab Content positioned below the overlapping enlarged '+' crest
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 112.dp)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add Card to Wallet",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Choose how you would like to add your card",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    FilledTonalIconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Option 1: Camera Scan
                ScanMethodItem(
                    title = "Camera Scan",
                    subtitle = "Instant AI & OCR recognition from card photo",
                    icon = Icons.Default.CameraAlt,
                    iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = {
                        haptics?.cardSelect()
                        onDismiss()
                        onChooseCameraScan()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: NFC Tap
                ScanMethodItem(
                    title = "NFC Tap",
                    subtitle = "Hold contactless card to the back of your phone",
                    icon = Icons.Default.Sensors,
                    iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = {
                        haptics?.cardSelect()
                        onDismiss()
                        onChooseNfcScan()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Option 3: Manual Entry
                ScanMethodItem(
                    title = "Manual Entry",
                    subtitle = "Type card details and choose official card art",
                    icon = Icons.Default.Edit,
                    iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = {
                        haptics?.cardSelect()
                        onDismiss()
                        onChooseManualEntry()
                    }
                )
            }
        }
    }
}

@Composable
private fun ScanMethodItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = iconContainerColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
