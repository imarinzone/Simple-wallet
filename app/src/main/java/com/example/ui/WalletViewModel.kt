package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CardEntity
import com.example.data.WalletRepository
import com.example.security.HapticsHelper
import com.example.ui.components.LeatherFinish
import com.example.ui.components.LeatherFinishes
import com.example.ui.screens.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    WALLET_HOME,
    SCAN_CAMERA,
    SCAN_NFC,
    SYNC_BACKUP,
    SETTINGS
}

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val repository = WalletRepository(database.cardDao())
    val haptics = HapticsHelper(application)
    private val prefs = application.getSharedPreferences("wallet_security_prefs", Context.MODE_PRIVATE)

    val cards: StateFlow<List<CardEntity>> = repository.allCards
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentScreen = MutableStateFlow(AppScreen.WALLET_HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _isWalletOpen = MutableStateFlow(false)
    val isWalletOpen: StateFlow<Boolean> = _isWalletOpen.asStateFlow()

    private val _selectedCardForDetail = MutableStateFlow<CardEntity?>(null)
    val selectedCardForDetail: StateFlow<CardEntity?> = _selectedCardForDetail.asStateFlow()

    // Finger lock and app lock are strictly OPT-IN (default false). App does not start locked.
    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean("pref_biometric_enabled", false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _hasSuggestedLock = MutableStateFlow(prefs.getBoolean("pref_has_suggested_lock", false))
    val hasSuggestedLock: StateFlow<Boolean> = _hasSuggestedLock.asStateFlow()

    // Show suggestion only when app is loaded for the very first time and lock is not yet enabled
    private val _showLockSuggestionDialog = MutableStateFlow(!_hasSuggestedLock.value && !_isBiometricEnabled.value)
    val showLockSuggestionDialog: StateFlow<Boolean> = _showLockSuggestionDialog.asStateFlow()

    private val _masterPin = MutableStateFlow(prefs.getString("pref_master_pin", "1234") ?: "1234")
    val masterPin: StateFlow<String> = _masterPin.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _leatherFinish = MutableStateFlow(LeatherFinishes[0])
    val leatherFinish: StateFlow<LeatherFinish> = _leatherFinish.asStateFlow()

    private val _isHapticsEnabled = MutableStateFlow(true)
    val isHapticsEnabled: StateFlow<Boolean> = _isHapticsEnabled.asStateFlow()

    private val _scannedPhysicalCard = MutableStateFlow<CardEntity?>(null)
    val scannedPhysicalCard: StateFlow<CardEntity?> = _scannedPhysicalCard.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialCardsIfEmpty()
        }
    }

    fun onPhysicalNfcCardScanned(card: CardEntity) {
        _scannedPhysicalCard.value = card
        _isWalletOpen.value = true
        _isLocked.value = false
    }

    fun dismissPhysicalNfcCardPrompt() {
        _scannedPhysicalCard.value = null
    }


    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun toggleWalletOpen() {
        _isWalletOpen.value = !_isWalletOpen.value
    }

    fun openCardDetail(card: CardEntity) {
        _selectedCardForDetail.value = card
    }

    fun closeCardDetail() {
        _selectedCardForDetail.value = null
    }

    fun unlockWallet() {
        _isLocked.value = false
        _isWalletOpen.value = true
    }

    fun lockWallet() {
        if (_isBiometricEnabled.value) {
            _isLocked.value = true
            _isWalletOpen.value = false
        }
    }

    fun dismissLockSuggestion() {
        _showLockSuggestionDialog.value = false
        _hasSuggestedLock.value = true
        prefs.edit().putBoolean("pref_has_suggested_lock", true).apply()
    }

    fun enableLockFromSuggestion() {
        _isBiometricEnabled.value = true
        prefs.edit().putBoolean("pref_biometric_enabled", true).apply()
        _showLockSuggestionDialog.value = false
        _hasSuggestedLock.value = true
        prefs.edit().putBoolean("pref_has_suggested_lock", true).apply()
        haptics.success()
    }

    fun saveCard(card: CardEntity) {
        viewModelScope.launch {
            repository.saveCard(card)
            _currentScreen.value = AppScreen.WALLET_HOME
            _isWalletOpen.value = true
        }
    }

    fun deleteCard(card: CardEntity) {
        viewModelScope.launch {
            repository.deleteCard(card)
            if (_selectedCardForDetail.value?.id == card.id) {
                _selectedCardForDetail.value = null
            }
        }
    }

    fun toggleFavorite(card: CardEntity) {
        viewModelScope.launch {
            repository.saveCard(card)
            _selectedCardForDetail.value = card
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun setLeatherFinish(finish: LeatherFinish) {
        _leatherFinish.value = finish
    }

    fun toggleBiometricEnabled(enabled: Boolean) {
        _isBiometricEnabled.value = enabled
        prefs.edit().putBoolean("pref_biometric_enabled", enabled).apply()
        if (!enabled) {
            _isLocked.value = false
        }
    }

    fun setMasterPin(pin: String) {
        _masterPin.value = pin
        prefs.edit().putString("pref_master_pin", pin).apply()
    }

    fun toggleHaptics(enabled: Boolean) {
        _isHapticsEnabled.value = enabled
    }
}
