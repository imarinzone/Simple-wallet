package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardEntity
import com.example.nfc.NfcCardReaderManager
import com.example.nfc.NfcReadResult
import com.example.security.HapticsHelper
import com.example.ui.components.CreditCardItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScanScreen(
    onNavigateBack: () -> Unit,
    onCardSaved: (CardEntity) -> Unit,
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val nfcManager = remember { NfcCardReaderManager(context) }

    var readResult by remember { mutableStateOf<NfcReadResult?>(null) }
    var detectedCard by remember { mutableStateOf<CardEntity?>(null) }
    var isReading by remember { mutableStateOf(false) }

    // Start real NFC hardware listener
    DisposableEffect(Unit) {
        if (activity != null) {
            nfcManager.startListening(activity) { result ->
                haptics?.success()
                readResult = result
                detectedCard = result.suggestedCard
            }
        }
        onDispose {
            if (activity != null) {
                nfcManager.stopListening(activity)
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "nfcWaves")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )
    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NFC Contactless Reader",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Radar Pulse NFC Animation Box
            Box(
                modifier = Modifier
                    .size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Concentric Waves
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(waveScale)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8).copy(alpha = waveAlpha * 0.3f))
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(waveScale * 0.9f)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7).copy(alpha = waveAlpha * 0.5f))
                )

                // Center NFC Pad
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(16.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF38BDF8),
                                    Color(0xFF0369A1),
                                    Color(0xFF0C4A6E)
                                )
                            )
                        )
                        .border(3.dp, Color(0xFF7DD3FC), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "NFC Sensor",
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (detectedCard != null) "CARD DETECTED VIA NFC" else "READY TO SCAN",
                color = if (detectedCard != null) Color(0xFF34D399) else Color(0xFF38BDF8),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (detectedCard != null)
                    "Contactless ISO-14443 tag UID: ${readResult?.tagUidHex ?: "READ_OK"}"
                else
                    "Hold any contactless payment card or transit card firmly against the back of your device",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Test NFC Simulator Buttons (Important for emulator and demo environments)
            Text(
                text = "EMULATOR & DEMO NFC CONTROLS",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptics?.success()
                        val sim = nfcManager.simulateNfcTap("VISA")
                        readResult = sim
                        detectedCard = sim.suggestedCard
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tap Visa", fontSize = 12.sp, color = Color(0xFF7DD3FC))
                }

                OutlinedButton(
                    onClick = {
                        haptics?.success()
                        val sim = nfcManager.simulateNfcTap("AMEX")
                        readResult = sim
                        detectedCard = sim.suggestedCard
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tap Amex", fontSize = 12.sp, color = Color(0xFF7DD3FC))
                }

                OutlinedButton(
                    onClick = {
                        haptics?.success()
                        val sim = nfcManager.simulateNfcTap("TRANSIT")
                        readResult = sim
                        detectedCard = sim.suggestedCard
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tap Transit", fontSize = 12.sp, color = Color(0xFF7DD3FC))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display Detected Card
            AnimatedVisibility(visible = detectedCard != null) {
                if (detectedCard != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "EXTRACTED NFC CARD",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        CreditCardItem(
                            card = detectedCard!!,
                            haptics = haptics
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // NFC Diagnostic Specs Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1E293B))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "NFC PAYLOAD TELEMETRY",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "UID: ${readResult?.tagUidHex ?: "UNKNOWN"}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Protocol: ISO/IEC 14443 Type A (106 kbps)",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "EMV AID: ${readResult?.emvAid ?: "PPSE 2PAY.SYS.DDF01"}",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        ElevatedButton(
                            onClick = {
                                haptics?.success()
                                onCardSaved(detectedCard!!)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = Color(0xFF0284C7),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Store Card in Wallet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}
