package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.HapticsHelper
import com.example.ui.components.LeatherFinish
import com.example.ui.components.LeatherFinishes

enum class ThemeMode {
    DARK, LIGHT, SYSTEM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    currentLeatherFinish: LeatherFinish,
    onLeatherFinishChange: (LeatherFinish) -> Unit,
    isBiometricLockEnabled: Boolean,
    onToggleBiometricLock: (Boolean) -> Unit,
    masterPin: String,
    onUpdateMasterPin: (String) -> Unit,
    isHapticsEnabled: Boolean,
    onToggleHaptics: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    var newPinInput by remember { mutableStateOf(masterPin) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wallet Settings & Themes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF13100E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF13100E)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // THEME MODE SELECTOR
            SettingSection(title = "APPEARANCE & DARK MODE", icon = Icons.Default.DarkMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemeOptionButton(
                        title = "Dark Mode",
                        icon = Icons.Default.DarkMode,
                        isSelected = currentThemeMode == ThemeMode.DARK,
                        onClick = {
                            haptics?.cardSelect()
                            onThemeModeChange(ThemeMode.DARK)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        title = "Light Mode",
                        icon = Icons.Default.LightMode,
                        isSelected = currentThemeMode == ThemeMode.LIGHT,
                        onClick = {
                            haptics?.cardSelect()
                            onThemeModeChange(ThemeMode.LIGHT)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionButton(
                        title = "System",
                        icon = Icons.Default.Palette,
                        isSelected = currentThemeMode == ThemeMode.SYSTEM,
                        onClick = {
                            haptics?.cardSelect()
                            onThemeModeChange(ThemeMode.SYSTEM)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // LEATHER FINISH SELECTOR
            SettingSection(title = "LEATHER WALLET FINISH", icon = Icons.Default.Palette) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (finish in LeatherFinishes) {
                        val isSelected = finish.name == currentLeatherFinish.name
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1F1A17))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) finish.hardwareColor else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    haptics?.cardSelect()
                                    onLeatherFinishChange(finish)
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(finish.primaryColor)
                                        .border(2.dp, finish.hardwareColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = finish.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Top-grain crafted leather texture",
                                        color = Color.White.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(finish.hardwareColor)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        color = Color(0xFF261502),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // BIOMETRIC & SECURITY SETTINGS
            SettingSection(title = "BIOMETRICS & SECURITY LOCK", icon = Icons.Default.Fingerprint) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Require Biometric Authentication",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Prompt for Fingerprint or Face ID on opening",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = isBiometricLockEnabled,
                            onCheckedChange = {
                                haptics?.cardSelect()
                                onToggleBiometricLock(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFE5A93C),
                                checkedTrackColor = Color(0xFF5A3B18)
                            )
                        )
                    }

                    // Master PIN setting
                    OutlinedTextField(
                        value = newPinInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                newPinInput = it
                                onUpdateMasterPin(it)
                            }
                        },
                        label = { Text("Master Wallet PIN") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE5A93C),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }

            // HAPTICS & TACTILE FEEDBACK
            SettingSection(title = "TACTILE HAPTIC FEEDBACK", icon = Icons.Default.Vibration) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Haptic Interactions",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Vibrate on wallet unfold, card slide, and tap",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = isHapticsEnabled,
                        onCheckedChange = {
                            onToggleHaptics(it)
                            if (it) haptics?.walletOpen()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFE5A93C),
                            checkedTrackColor = Color(0xFF5A3B18)
                        )
                    )
                }
            }

            // GEMINI AI ENGINE SPEC
            SettingSection(title = "AI VISION ENGINE", icon = Icons.Default.AutoAwesome) {
                Column {
                    Text(
                        text = "Google Gemini 3.1 Pro OCR",
                        color = Color(0xFFF3C569),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Utilizes multimodal gemini-3.1-pro-preview with structured JSON outputs for financial card OCR, issuer verification, and security layout classification.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFE5A93C),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = Color(0xFFE5A93C),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1B1714))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun ThemeOptionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFE5A93C) else Color(0xFF26201C))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF261502) else Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = if (isSelected) Color(0xFF261502) else Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
