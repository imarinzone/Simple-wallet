package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.HapticsHelper
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class PinSetupStep {
    CHOOSE,
    CONFIRM
}

/**
 * Full-screen Android native lock screen PIN setup experience.
 * Matches Android AOSP / Pixel Settings "Set a screen lock PIN":
 * - Full-screen layout with immersive system bar padding
 * - Authentic circular numeric Keyguard keypad with letters underneath
 * - Step 1: "Choose your PIN" (at least 4 digits)
 * - Step 2: "Confirm your PIN" (must match Step 1)
 * - Visual masked dots with shake animation on mismatch error
 */
@Composable
fun AndroidLockScreenPinSetupScreen(
    currentPin: String = "",
    onPinSaved: (String) -> Unit,
    onDismiss: () -> Unit,
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(PinSetupStep.CHOOSE) }
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    val currentEnteredDigits = if (step == PinSetupStep.CHOOSE) firstPin else confirmPin

    // Handle system back button
    BackHandler {
        if (step == PinSetupStep.CONFIRM) {
            haptics?.cardSelect()
            step = PinSetupStep.CHOOSE
            confirmPin = ""
            errorMessage = null
        } else {
            onDismiss()
        }
    }

    fun triggerShakeAnimation() {
        coroutineScope.launch {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 24f,
                animationSpec = spring(stiffness = Spring.StiffnessHigh, dampingRatio = 0.2f)
            )
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.5f)
            )
        }
    }

    fun onDigitPress(digit: String) {
        haptics?.cardSlide()
        errorMessage = null

        if (step == PinSetupStep.CHOOSE) {
            if (firstPin.length < 6) {
                firstPin += digit
            }
        } else {
            if (confirmPin.length < 6) {
                val nextConfirm = confirmPin + digit
                confirmPin = nextConfirm

                // If user entered same length as firstPin
                if (nextConfirm.length == firstPin.length) {
                    if (nextConfirm == firstPin) {
                        // Match!
                        haptics?.success()
                        onPinSaved(nextConfirm)
                        onDismiss()
                    } else {
                        // Mismatch!
                        haptics?.error()
                        errorMessage = "PINs didn't match. Try again."
                        triggerShakeAnimation()
                        confirmPin = ""
                    }
                }
            }
        }
    }

    fun onBackspace() {
        haptics?.cardSlide()
        errorMessage = null
        if (step == PinSetupStep.CHOOSE) {
            if (firstPin.isNotEmpty()) {
                firstPin = firstPin.dropLast(1)
            }
        } else {
            if (confirmPin.isNotEmpty()) {
                confirmPin = confirmPin.dropLast(1)
            }
        }
    }

    fun onProceedNext() {
        if (step == PinSetupStep.CHOOSE) {
            if (firstPin.length in 4..6) {
                haptics?.cardSelect()
                step = PinSetupStep.CONFIRM
                confirmPin = ""
                errorMessage = null
            } else {
                haptics?.error()
                errorMessage = "PIN must be at least 4 digits"
                triggerShakeAnimation()
            }
        } else {
            if (confirmPin == firstPin) {
                haptics?.success()
                onPinSaved(confirmPin)
                onDismiss()
            } else {
                haptics?.error()
                errorMessage = "PINs didn't match. Try again."
                triggerShakeAnimation()
                confirmPin = ""
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ================= 1. HEADER & TOP BAR =================
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (step == PinSetupStep.CONFIRM) {
                                haptics?.cardSelect()
                                step = PinSetupStep.CHOOSE
                                confirmPin = ""
                                errorMessage = null
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("pin_setup_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = if (step == PinSetupStep.CHOOSE) "Set a screen lock PIN" else "Confirm your PIN",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = if (step == PinSetupStep.CHOOSE) {
                        "PIN must be at least 4 digits"
                    } else {
                        "Enter your PIN again to confirm"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Masked Dots Indicator with Shake Animation
                Row(
                    modifier = Modifier
                        .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val count = maxOf(4, currentEnteredDigits.length)
                    repeat(count) { index ->
                        val isFilled = index < currentEnteredDigits.length
                        val dotColor = if (errorMessage != null) {
                            MaterialTheme.colorScheme.error
                        } else if (isFilled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                        }

                        Box(
                            modifier = Modifier
                                .size(if (isFilled) 18.dp else 14.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(
                                    width = 1.dp,
                                    color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Error Message
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ================= 2. AUTHENTIC ANDROID LOCK SCREEN KEYPAD =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val keypadRows = listOf(
                    listOf(KeypadKey("1", ""), KeypadKey("2", "ABC"), KeypadKey("3", "DEF")),
                    listOf(KeypadKey("4", "GHI"), KeypadKey("5", "JKL"), KeypadKey("6", "MNO")),
                    listOf(KeypadKey("7", "PQRS"), KeypadKey("8", "TUV"), KeypadKey("9", "WXYZ")),
                    listOf(KeypadKey("CLEAR", ""), KeypadKey("0", "+"), KeypadKey("BACKSPACE", ""))
                )

                for (row in keypadRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (key in row) {
                            when (key.digit) {
                                "CLEAR" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .clickable(
                                                enabled = currentEnteredDigits.isNotEmpty(),
                                                onClick = {
                                                    haptics?.cardSlide()
                                                    if (step == PinSetupStep.CHOOSE) firstPin = "" else confirmPin = ""
                                                    errorMessage = null
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (currentEnteredDigits.isNotEmpty()) {
                                            Text(
                                                text = "Clear",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                                "BACKSPACE" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .clickable(
                                                enabled = currentEnteredDigits.isNotEmpty(),
                                                onClick = { onBackspace() }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (currentEnteredDigits.isNotEmpty()) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    AndroidKeyguardPinButton(
                                        digit = key.digit,
                                        subtext = key.subtext,
                                        onClick = { onDigitPress(key.digit) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ================= 3. BOTTOM ACTIONS (CANCEL / NEXT) =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("pin_setup_cancel_button")
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { onProceedNext() },
                    enabled = currentEnteredDigits.length >= 4,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("pin_setup_next_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (step == PinSetupStep.CHOOSE) "Next" else "Confirm",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        if (step == PinSetupStep.CONFIRM) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class KeypadKey(val digit: String, val subtext: String)

/**
 * Authentic Android Keyguard / Lock Screen circular button.
 * Prominent digit on top, subtle uppercase alphabet subtext underneath.
 */
@Composable
private fun AndroidKeyguardPinButton(
    digit: String,
    subtext: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .testTag("pin_key_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 32.sp
            )
            if (subtext.isNotEmpty()) {
                Text(
                    text = subtext,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    lineHeight = 12.sp
                )
            }
        }
    }
}
