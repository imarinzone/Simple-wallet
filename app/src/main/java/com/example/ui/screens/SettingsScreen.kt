package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AppUpdateInfo
import com.example.data.AppUpdateService
import com.example.security.HapticSensitivity
import com.example.security.HapticsHelper
import com.example.ui.theme.PixelAccentColor

enum class ThemeMode {
    DARK, LIGHT, SYSTEM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    currentAccentColor: PixelAccentColor = PixelAccentColor.BLUE,
    onAccentColorChange: (PixelAccentColor) -> Unit = {},
    isBiometricLockEnabled: Boolean,
    onToggleBiometricLock: (Boolean) -> Unit,
    masterPin: String,
    onUpdateMasterPin: (String) -> Unit,
    isHapticsEnabled: Boolean,
    onToggleHaptics: (Boolean) -> Unit,
    hapticSensitivity: HapticSensitivity = HapticSensitivity.MEDIUM,
    onHapticSensitivityChange: (HapticSensitivity) -> Unit = {},
    isOnlineCardArtEnabled: Boolean = false,
    onToggleOnlineCardArt: (Boolean) -> Unit = {},
    autoCheckUpdates: Boolean = true,
    onToggleAutoCheckUpdates: (Boolean) -> Unit = {},
    isCheckingForUpdates: Boolean = false,
    updateStatusMessage: String? = null,
    onCheckForUpdates: () -> Unit = {},
    appUpdateInfo: AppUpdateInfo? = null,
    onShowUpdateDetails: () -> Unit = {},
    onNavigateBack: () -> Unit,
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    var showPinDialog by remember { mutableStateOf(false) }

    if (showPinDialog) {
        AndroidLockScreenPinSetupScreen(
            currentPin = masterPin,
            onPinSaved = { newPin ->
                onUpdateMasterPin(newPin)
                showPinDialog = false
            },
            onDismiss = { showPinDialog = false },
            haptics = haptics
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // 1. APPEARANCE & MATERIAL YOU DYNAMIC COLOR
            SettingsSectionTitle("Appearance & Material You")

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "Color Mode",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Material 3 SingleChoiceSegmentedButtonRow for Theme Mode
                    val themeOptions = listOf(
                        Triple("Light", Icons.Default.LightMode, ThemeMode.LIGHT),
                        Triple("Dark", Icons.Default.DarkMode, ThemeMode.DARK),
                        Triple("System", Icons.Default.Palette, ThemeMode.SYSTEM)
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        themeOptions.forEachIndexed { index, (label, icon, mode) ->
                            val isSelected = currentThemeMode == mode
                            SegmentedButton(
                                selected = isSelected,
                                onClick = {
                                    haptics?.cardSelect()
                                    onThemeModeChange(mode)
                                },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = themeOptions.size
                                ),
                                icon = {
                                    SegmentedButtonDefaults.Icon(active = isSelected) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Accent Palette",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = currentAccentColor.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (accent in PixelAccentColor.values()) {
                                val isSelected = currentAccentColor == accent
                                val dynamicGradient = Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF38BDF8),
                                        Color(0xFF818CF8),
                                        Color(0xFFEC4899),
                                        Color(0xFFF97316),
                                        Color(0xFF10B981),
                                        Color(0xFF38BDF8)
                                    )
                                )
                                val solidColor = Color(android.graphics.Color.parseColor(accent.hex))

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (accent.isDynamic) Modifier.background(dynamicGradient)
                                            else Modifier.background(solidColor)
                                        )
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            haptics?.cardSelect()
                                            onAccentColorChange(accent)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = accent.label,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. SECURITY
            SettingsSectionTitle("Security & Lock")

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Screen Lock & Fingerprint",
                                style = MaterialTheme.typography.titleSmall
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "Always require biometric or PIN verification when opening the wallet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            Switch(
                                checked = isBiometricLockEnabled,
                                onCheckedChange = {
                                    haptics?.cardSelect()
                                    onToggleBiometricLock(it)
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    if (isBiometricLockEnabled) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        ListItem(
                            modifier = Modifier.clickable { showPinDialog = true },
                            headlineContent = {
                                Text(
                                    text = "In-App Backup PIN",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = "Emergency fallback if biometrics are unavailable",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Pin,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            },
                            trailingContent = {
                                FilledTonalButton(
                                    onClick = { showPinDialog = true },
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text(
                                        text = if (masterPin.isNotBlank()) "Change PIN" else "Set PIN",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }

            // 3. HAPTICS
            SettingsSectionTitle("Tactile Haptics")

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(bottom = if (isHapticsEnabled) 16.dp else 0.dp)) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = "Haptic Feedback",
                                style = MaterialTheme.typography.titleSmall
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "Tactile response when flipping, sliding, or scanning cards",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            Switch(
                                checked = isHapticsEnabled,
                                onCheckedChange = {
                                    onToggleHaptics(it)
                                    if (it) haptics?.cardSelect()
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    if (isHapticsEnabled) {
                        val sensitivityOptions = listOf(
                            Pair("Light", HapticSensitivity.LIGHT),
                            Pair("Medium", HapticSensitivity.MEDIUM),
                            Pair("Strong", HapticSensitivity.STRONG)
                        )
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            sensitivityOptions.forEachIndexed { index, (label, sensitivity) ->
                                val isSelected = hapticSensitivity == sensitivity
                                SegmentedButton(
                                    selected = isSelected,
                                    onClick = { onHapticSensitivityChange(sensitivity) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = sensitivityOptions.size
                                    ),
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 4. CARD ARTWORK & OFFICIAL DESIGNS
            SettingsSectionTitle("Official Card Artwork")

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = "Show Official Card Artwork",
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "Display official card designs for HDFC, SBI, Chase, Amex, Apple Card, and more.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = isOnlineCardArtEnabled,
                            onCheckedChange = {
                                haptics?.cardSelect()
                                onToggleOnlineCardArt(it)
                            }
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            // 5. APP UPDATES & VERSION
            val context = LocalContext.current
            SettingsSectionTitle("App Updates")

            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Check for Updates",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Current version: v${AppUpdateService.CURRENT_VERSION_NAME}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "github.com/imarinzone/Simple-wallet/releases",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                haptics?.cardSelect()
                                onCheckForUpdates()
                            },
                            enabled = !isCheckingForUpdates,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (isCheckingForUpdates) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Checking…", style = MaterialTheme.typography.labelLarge)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Check", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }

                    // Status message or update banner
                    if (updateStatusMessage != null || appUpdateInfo != null) {
                        val isAvailable = appUpdateInfo?.isUpdateAvailable == true
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = if (isAvailable)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isAvailable)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = updateStatusMessage ?: if (isAvailable) "New release v${appUpdateInfo?.latestVersionName} available!" else "You are up to date! (v${appUpdateInfo?.currentVersionName})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isAvailable) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )

                                if (appUpdateInfo != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Installed: v${appUpdateInfo.currentVersionName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isAvailable) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Latest: v${appUpdateInfo.latestVersionName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (appUpdateInfo != null && isAvailable) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                haptics?.success()
                                                AppUpdateService.openDownloadPage(context, appUpdateInfo.downloadUrl)
                                            },
                                            shape = MaterialTheme.shapes.medium,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Download v${appUpdateInfo.latestVersionName}",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                haptics?.cardSelect()
                                                onShowUpdateDetails()
                                            },
                                            shape = MaterialTheme.shapes.medium,
                                            modifier = Modifier.weight(0.7f)
                                        ) {
                                            Text("Details", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Auto-check updates toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Auto Check on Launch",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Notify immediately when an app update is ready",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoCheckUpdates,
                            onCheckedChange = {
                                haptics?.cardSelect()
                                onToggleAutoCheckUpdates(it)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp)
    )
}
