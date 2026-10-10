package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppUpdateService
import com.example.nfc.NfcCardReaderManager
import com.example.security.BiometricAuthManager
import com.example.ui.AppScreen
import com.example.ui.WalletViewModel
import com.example.ui.components.BiometricLockOverlay
import com.example.ui.screens.AddCardChooserBottomSheet
import com.example.ui.screens.AndroidLockScreenPinSetupScreen
import com.example.ui.screens.CardDetailBottomSheet
import com.example.ui.screens.EditCardDialog
import com.example.ui.screens.EditCardScreen
import com.example.ui.screens.NfcCardDetectedDialog
import com.example.ui.screens.NfcScanScreen
import com.example.ui.screens.WalletHomeScreen
import com.example.ui.screens.ScanCardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SyncBackupScreen
import com.example.ui.screens.ThemeMode
import com.example.ui.theme.VaultFolioTheme


class MainActivity : FragmentActivity() {
    private lateinit var nfcManager: NfcCardReaderManager
    private var activeViewModel: WalletViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcManager = NfcCardReaderManager(this)
        enableEdgeToEdge()

        setContent {
            val viewModel: WalletViewModel = viewModel()
            activeViewModel = viewModel
            val themeMode by viewModel.themeMode.collectAsState()
            val pixelAccent by viewModel.pixelAccent.collectAsState()

            val isDarkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            VaultFolioTheme(
                darkTheme = isDarkTheme,
                accentColor = pixelAccent
            ) {
                MainAppContent(
                    activity = this@MainActivity,
                    viewModel = viewModel
                )
            }
        }

        intent?.let { processNfcIntent(it) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        processNfcIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (::nfcManager.isInitialized) {
            nfcManager.enableForegroundDispatch(this)
        }
    }

    override fun onPause() {
        super.onPause()
        if (::nfcManager.isInitialized) {
            nfcManager.disableForegroundDispatch(this)
        }
    }

    override fun onStop() {
        super.onStop()
        // Automatically re-lock wallet when backgrounded if biometric lock is active
        if (activeViewModel?.isBiometricEnabled?.value == true) {
            activeViewModel?.lockWallet()
        }
    }

    private fun processNfcIntent(intent: Intent) {

        if (!::nfcManager.isInitialized) return
        val result = nfcManager.parseIntent(intent)
        if (result?.suggestedCard != null) {
            activeViewModel?.let { vm ->
                vm.haptics.success()
                vm.onPhysicalNfcCardScanned(result.suggestedCard)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    activity: FragmentActivity,
    viewModel: WalletViewModel
) {
    val cards by viewModel.cards.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val masterPin by viewModel.masterPin.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val pixelAccent by viewModel.pixelAccent.collectAsState()
    val leatherFinish by viewModel.leatherFinish.collectAsState()
    val isHapticsEnabled by viewModel.isHapticsEnabled.collectAsState()
    val hapticSensitivity by viewModel.hapticSensitivity.collectAsState()
    val scannedPhysicalCard by viewModel.scannedPhysicalCard.collectAsState()
    val showLockSuggestionDialog by viewModel.showLockSuggestionDialog.collectAsState()
    val isOnlineCardArtEnabled by viewModel.isOnlineCardArtEnabled.collectAsState()
    val selectedCardForEdit by viewModel.selectedCardForEdit.collectAsState()
    val selectedCardForDetail by viewModel.selectedCardForDetail.collectAsState()
    val autoCheckUpdates by viewModel.autoCheckUpdates.collectAsState()
    val appUpdateInfo by viewModel.appUpdateInfo.collectAsState()
    val isCheckingForUpdates by viewModel.isCheckingForUpdates.collectAsState()
    val updateStatusMessage by viewModel.updateStatusMessage.collectAsState()
    val showUpdatePromptOnLaunch by viewModel.showUpdatePromptOnLaunch.collectAsState()
    var showUpdateDetailsDialog by remember { mutableStateOf(false) }

    var showAddCardChooser by remember { mutableStateOf(false) }
    var startInManualMode by remember { mutableStateOf(false) }
    var biometricErrorMessage by remember { mutableStateOf<String?>(null) }

    val haptics = if (isHapticsEnabled) viewModel.haptics else null

    // Hardware back navigation handler
    BackHandler(enabled = currentScreen != AppScreen.WALLET_HOME) {
        viewModel.navigateTo(AppScreen.WALLET_HOME)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLocked && isBiometricEnabled) {
            // Absolute Security Shield: Cards are completely hidden behind biometric lock
            BiometricLockOverlay(
                masterPin = masterPin,
                onUnlockSuccess = {
                    haptics?.success()
                    biometricErrorMessage = null
                    viewModel.unlockWallet()
                },
                onTriggerSystemBiometric = {
                    BiometricAuthManager.promptBiometric(
                        activity = activity,
                        title = "Unlock VaultFolio",
                        subtitle = "Verify fingerprint or device credentials to view your cards",
                        onSuccess = {
                            haptics?.success()
                            biometricErrorMessage = null
                            viewModel.unlockWallet()
                        },
                        onError = { err ->
                            biometricErrorMessage = err
                        }
                    )
                },
                onSetMasterPin = { newPin ->
                    viewModel.setMasterPin(newPin)
                },
                biometricError = biometricErrorMessage,
                haptics = haptics
            )
        } else {
            // Cards are ONLY visible and interactive after successful biometric verification
            when (currentScreen) {
                AppScreen.WALLET_HOME -> {
                    WalletHomeScreen(
                        cards = cards,
                        onAddNewCard = {
                            haptics?.cardSelect()
                            showAddCardChooser = true
                        },
                        onNavigateToSettings = {
                            viewModel.navigateTo(AppScreen.SETTINGS)
                        },
                        onNavigateToSync = {
                            viewModel.navigateTo(AppScreen.SYNC_BACKUP)
                        },
                        onLockWallet = {
                            viewModel.lockWallet()
                        },
                        onDeleteCard = {
                            viewModel.deleteCard(it)
                        },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(it)
                        },
                        onEditCard = {
                            viewModel.openCardEdit(it)
                        },
                        onReorderCards = {
                            viewModel.reorderCards(it)
                        },
                        leatherFinish = leatherFinish,
                        isOverlayOpen = (selectedCardForEdit != null || selectedCardForDetail != null || showAddCardChooser || scannedPhysicalCard != null),
                        haptics = haptics
                    )
                }

                AppScreen.SCAN_CAMERA -> {
                    ScanCardScreen(
                        onNavigateBack = { viewModel.navigateTo(AppScreen.WALLET_HOME) },
                        onCardSaved = { viewModel.saveCard(it) },
                        initialManualMode = startInManualMode,
                        isOnlineCardArtEnabled = isOnlineCardArtEnabled,
                        onEnableOnlineCardArt = { viewModel.toggleOnlineCardArt(true) },
                        haptics = haptics
                    )
                }

                AppScreen.SCAN_NFC -> {
                    NfcScanScreen(
                        onNavigateBack = { viewModel.navigateTo(AppScreen.WALLET_HOME) },
                        onCardSaved = { viewModel.saveCard(it) },
                        onNavigateToCameraScan = { viewModel.navigateTo(AppScreen.SCAN_CAMERA) },
                        isOnlineCardArtEnabled = isOnlineCardArtEnabled,
                        onEnableOnlineCardArt = { viewModel.toggleOnlineCardArt(true) },
                        haptics = haptics
                    )
                }

                AppScreen.SYNC_BACKUP -> {
                    SyncBackupScreen(
                        cards = cards,
                        repository = viewModel.repository,
                        onNavigateBack = { viewModel.navigateTo(AppScreen.WALLET_HOME) },
                        haptics = haptics
                    )
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        currentThemeMode = themeMode,
                        onThemeModeChange = { viewModel.setThemeMode(it) },
                        currentAccentColor = pixelAccent,
                        onAccentColorChange = { viewModel.setPixelAccent(it) },
                        isBiometricLockEnabled = isBiometricEnabled,
                        onToggleBiometricLock = { viewModel.toggleBiometricEnabled(it) },
                        masterPin = masterPin,
                        onUpdateMasterPin = { viewModel.setMasterPin(it) },
                        isHapticsEnabled = isHapticsEnabled,
                        onToggleHaptics = { viewModel.toggleHaptics(it) },
                        hapticSensitivity = hapticSensitivity,
                        onHapticSensitivityChange = { viewModel.setHapticSensitivity(it) },
                        isOnlineCardArtEnabled = isOnlineCardArtEnabled,
                        onToggleOnlineCardArt = { viewModel.toggleOnlineCardArt(it) },
                        autoCheckUpdates = autoCheckUpdates,
                        onToggleAutoCheckUpdates = { viewModel.setAutoCheckUpdates(it) },
                        isCheckingForUpdates = isCheckingForUpdates,
                        updateStatusMessage = updateStatusMessage,
                        onCheckForUpdates = { viewModel.checkForAppUpdates(manual = true) },
                        appUpdateInfo = appUpdateInfo,
                        onShowUpdateDetails = { showUpdateDetailsDialog = true },
                        onNavigateBack = { viewModel.navigateTo(AppScreen.WALLET_HOME) },
                        haptics = haptics
                    )
                }
            }

            // Add Card Chooser Bottom Sheet (Camera AI vs NFC vs Manual)
            if (showAddCardChooser) {
                AddCardChooserBottomSheet(
                    onDismiss = { showAddCardChooser = false },
                    onChooseCameraScan = {
                        showAddCardChooser = false
                        startInManualMode = false
                        viewModel.navigateTo(AppScreen.SCAN_CAMERA)
                    },
                    onChooseNfcScan = {
                        showAddCardChooser = false
                        viewModel.navigateTo(AppScreen.SCAN_NFC)
                    },
                    onChooseManualEntry = {
                        showAddCardChooser = false
                        startInManualMode = true
                        viewModel.navigateTo(AppScreen.SCAN_CAMERA)
                    },
                    haptics = haptics
                )
            }

            // Physical NFC Card Detected Modal Dialog (Hardware NFC Scanning)
            if (scannedPhysicalCard != null) {
                NfcCardDetectedDialog(
                    card = scannedPhysicalCard!!,
                    onDismiss = { viewModel.dismissPhysicalNfcCardPrompt() },
                    onSaveToWallet = {
                        viewModel.saveCard(it)
                        viewModel.dismissPhysicalNfcCardPrompt()
                    },
                    onScanWithCamera = {
                        viewModel.dismissPhysicalNfcCardPrompt()
                        viewModel.navigateTo(AppScreen.SCAN_CAMERA)
                    },
                    haptics = haptics
                )
            }

            // First-load opt-in suggestion dialog: suggest setting up lock
            if (showLockSuggestionDialog) {
                var showSetupPinPrompt by remember { mutableStateOf(false) }
                var initialPinInput by remember { mutableStateOf("") }

                if (!showSetupPinPrompt) {
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissLockSuggestion() },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Enable App Lock?",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        },
                        text = {
                            Text(
                                text = "Protect your cards with fingerprint or a personal master PIN.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showSetupPinPrompt = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Set PIN & Enable", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { viewModel.dismissLockSuggestion() }
                            ) {
                                Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(22.dp)
                    )
                } else {
                    AndroidLockScreenPinSetupScreen(
                        currentPin = masterPin,
                        onPinSaved = { newPin ->
                            viewModel.setMasterPin(newPin)
                            viewModel.enableLockFromSuggestion()
                            showSetupPinPrompt = false
                        },
                        onDismiss = {
                            showSetupPinPrompt = false
                            viewModel.dismissLockSuggestion()
                        },
                        haptics = haptics
                    )
                }
            }

            // Dedicated Full-Page Screen for Card & Artwork Editing
            if (selectedCardForEdit != null) {
                EditCardScreen(
                    card = selectedCardForEdit!!,
                    isOnlineCardArtEnabled = isOnlineCardArtEnabled,
                    onDismiss = { viewModel.closeCardEdit() },
                    onSaveCard = { viewModel.updateCard(it) },
                    onEnableOnlineCardArt = { viewModel.toggleOnlineCardArt(true) },
                    haptics = haptics
                )
            }

            // Card Detail Bottom Sheet
            if (selectedCardForDetail != null) {
                CardDetailBottomSheet(
                    card = selectedCardForDetail!!,
                    onDismiss = { viewModel.closeCardDetail() },
                    onDeleteCard = { viewModel.deleteCard(it) },
                    onToggleFavorite = { viewModel.toggleFavorite(it) },
                    onEditCard = { viewModel.openCardEdit(it) },
                    haptics = haptics
                )
            }

            // ================= 7. APP UPDATE PROMPT ON LAUNCH / DETECTED =================
            if (showUpdatePromptOnLaunch && appUpdateInfo?.isUpdateAvailable == true) {
                val update = appUpdateInfo!!
                AlertDialog(
                    onDismissRequest = { viewModel.dismissUpdatePrompt(rememberDismissed = false) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "New Update Available!",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 19.sp
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Simple Wallet v${update.latestVersionName} is now ready to download (Current: v${update.currentVersionName}).",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "What's new in this release:",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    update.releaseNotes.take(3).forEach { note ->
                                        Text(
                                            text = "• $note",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                haptics?.success()
                                viewModel.dismissUpdatePrompt(rememberDismissed = true)
                                AppUpdateService.openDownloadPage(activity, update.downloadUrl)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download Now", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                viewModel.dismissUpdatePrompt(rememberDismissed = true)
                            }
                        ) {
                            Text("Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // Update Details / Release Notes Modal Dialog
            if (showUpdateDetailsDialog && appUpdateInfo != null) {
                val update = appUpdateInfo!!
                AlertDialog(
                    onDismissRequest = { showUpdateDetailsDialog = false },
                    title = {
                        Text(
                            text = "Release Notes v${update.latestVersionName}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Package Size: ${update.apkSizeMb} • Released: ${update.releaseDate}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            update.releaseNotes.forEach { note ->
                                Text(
                                    text = "• $note",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showUpdateDetailsDialog = false
                                AppUpdateService.openDownloadPage(activity, update.downloadUrl)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Download")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showUpdateDetailsDialog = false }) {
                            Text("Close")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }

}

