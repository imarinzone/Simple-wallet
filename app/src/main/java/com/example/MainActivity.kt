package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.example.nfc.NfcCardReaderManager
import com.example.security.BiometricAuthManager
import com.example.ui.AppScreen
import com.example.ui.WalletViewModel
import com.example.ui.components.BiometricLockOverlay
import com.example.ui.screens.AddCardChooserBottomSheet
import com.example.ui.screens.CardDetailBottomSheet
import com.example.ui.screens.NfcCardDetectedDialog
import com.example.ui.screens.NfcScanScreen
import com.example.ui.screens.SamsungWalletHomeScreen
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

            val isDarkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            VaultFolioTheme(darkTheme = isDarkTheme) {
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
    val leatherFinish by viewModel.leatherFinish.collectAsState()
    val isHapticsEnabled by viewModel.isHapticsEnabled.collectAsState()
    val scannedPhysicalCard by viewModel.scannedPhysicalCard.collectAsState()
    val showLockSuggestionDialog by viewModel.showLockSuggestionDialog.collectAsState()

    var showAddCardChooser by remember { mutableStateOf(false) }
    var biometricErrorMessage by remember { mutableStateOf<String?>(null) }

    val haptics = if (isHapticsEnabled) viewModel.haptics else null

    // Hardware back navigation handler
    BackHandler(enabled = currentScreen != AppScreen.WALLET_HOME) {
        viewModel.navigateTo(AppScreen.WALLET_HOME)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0E0D))
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
                biometricError = biometricErrorMessage,
                haptics = haptics
            )
        } else {
            // Cards are ONLY visible and interactive after successful biometric verification
            when (currentScreen) {
                AppScreen.WALLET_HOME -> {
                    SamsungWalletHomeScreen(
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
                        leatherFinish = leatherFinish,
                        haptics = haptics
                    )
                }

                AppScreen.SCAN_CAMERA -> {
                    ScanCardScreen(
                        onNavigateBack = { viewModel.navigateTo(AppScreen.WALLET_HOME) },
                        onCardSaved = { viewModel.saveCard(it) },
                        haptics = haptics
                    )
                }

                AppScreen.SCAN_NFC -> {
                    NfcScanScreen(
                        onNavigateBack = { viewModel.navigateTo(AppScreen.WALLET_HOME) },
                        onCardSaved = { viewModel.saveCard(it) },
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
                        currentLeatherFinish = leatherFinish,
                        onLeatherFinishChange = { viewModel.setLeatherFinish(it) },
                        isBiometricLockEnabled = isBiometricEnabled,
                        onToggleBiometricLock = { viewModel.toggleBiometricEnabled(it) },
                        masterPin = masterPin,
                        onUpdateMasterPin = { viewModel.setMasterPin(it) },
                        isHapticsEnabled = isHapticsEnabled,
                        onToggleHaptics = { viewModel.toggleHaptics(it) },
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
                        viewModel.navigateTo(AppScreen.SCAN_CAMERA)
                    },
                    onChooseNfcScan = {
                        showAddCardChooser = false
                        viewModel.navigateTo(AppScreen.SCAN_NFC)
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
                    haptics = haptics
                )
            }

            // First-load opt-in suggestion dialog: suggest setting up lock
            if (showLockSuggestionDialog) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissLockSuggestion() },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE5A93C).copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = Color(0xFFE5A93C),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    title = {
                        Text(
                            text = "Add Fingerprint or App Lock?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    },
                    text = {
                        Text(
                            text = "Keep your payment cards and digital wallet safe. You can secure VaultFolio with your device's fingerprint or PIN. Lock is completely optional and can be turned on or off anytime in Settings.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.enableLockFromSuggestion()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE5A93C),
                                contentColor = Color(0xFF1E1002)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Turn On Lock", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { viewModel.dismissLockSuggestion() }
                        ) {
                            Text("Not Now", color = Color.White.copy(alpha = 0.65f))
                        }
                    },
                    containerColor = Color(0xFF1E1A17),
                    shape = RoundedCornerShape(22.dp)
                )
            }
        }
    }

}

