package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.HapticsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardChooserBottomSheet(
    onDismiss: () -> Unit,
    onChooseCameraScan: () -> Unit,
    onChooseNfcScan: () -> Unit,
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF171310),
        tonalElevation = 10.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Add Card to Wallet",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose your preferred scanning method",
                        color = Color(0xFFE5A93C),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Option 1: AI Camera Scan (Gemini 3.1 Pro OCR)
            ScanMethodCard(
                title = "AI Camera Scan",
                badge = "Gemini 3.1 Pro",
                description = "Point your camera or upload photo of physical card. Automatically detects numbers, names, expiry, and issuer.",
                icon = Icons.Default.CameraAlt,
                accentColor = Color(0xFFE5A93C),
                iconBgGradient = listOf(Color(0xFFE5A93C), Color(0xFF925A0B)),
                onClick = {
                    haptics?.cardSelect()
                    onDismiss()
                    onChooseCameraScan()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Option 2: NFC Contactless Tap
            ScanMethodCard(
                title = "NFC Contactless Tap",
                badge = "ISO-14443 / EMV",
                description = "Hold your contactless card against the back of your phone to read the NFC chip instantly.",
                icon = Icons.Default.Sensors,
                accentColor = Color(0xFF38BDF8),
                iconBgGradient = listOf(Color(0xFF38BDF8), Color(0xFF0369A1)),
                onClick = {
                    haptics?.cardSelect()
                    onDismiss()
                    onChooseNfcScan()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Option 3: Manual Entry
            ScanMethodCard(
                title = "Manual Card Entry",
                badge = "Direct Input",
                description = "Type card numbers and custom details manually without taking a picture or using NFC.",
                icon = Icons.Default.Edit,
                accentColor = Color(0xFF94A3B8),
                iconBgGradient = listOf(Color(0xFF475569), Color(0xFF1E293B)),
                onClick = {
                    haptics?.cardSelect()
                    onDismiss()
                    onChooseCameraScan()
                }
            )
        }
    }
}

@Composable
private fun ScanMethodCard(
    title: String,
    badge: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    iconBgGradient: List<Color>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF221C18))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glowing Icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(iconBgGradient)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            color = accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
