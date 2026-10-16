package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CardEntity
import com.example.data.WalletRepository
import com.example.security.HapticsHelper
import com.example.ui.theme.JetBrainsMonoFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncBackupScreen(
    cards: List<CardEntity>,
    repository: WalletRepository,
    onNavigateBack: () -> Unit,
    haptics: HapticsHelper? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    var exportPassphrase by remember { mutableStateOf("") }
    var showExportPassphrase by remember { mutableStateOf(false) }

    var importPassphrase by remember { mutableStateOf("") }
    var showImportPassphrase by remember { mutableStateOf(false) }

    var generatedBackupPayload by remember { mutableStateOf<String?>(null) }
    var importInputText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Backup & Sync",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            if (statusMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = if (isSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                    border = BorderStroke(
                        1.dp,
                        if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        text = statusMessage ?: "",
                        color = if (isSuccess) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // SECTION 1: EXPORT (AES-GCM ENCRYPTED)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Export Encrypted Backup",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Securely encrypt all cards using your personal secret passphrase (AES-GCM).",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = exportPassphrase,
                        onValueChange = { exportPassphrase = it },
                        label = { Text("Encryption Passphrase") },
                        placeholder = { Text("Enter a secret passphrase") },
                        visualTransformation = if (showExportPassphrase) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showExportPassphrase = !showExportPassphrase }) {
                                Icon(
                                    imageVector = if (showExportPassphrase) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Passphrase Visibility",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (exportPassphrase.isBlank()) {
                                statusMessage = "Please enter an encryption passphrase"
                                isSuccess = false
                                return@Button
                            }
                            coroutineScope.launch {
                                val encryptedJson = repository.exportEncryptedBackupJson(cards, exportPassphrase)
                                generatedBackupPayload = encryptedJson
                                clipboardManager.setText(AnnotatedString(encryptedJson))
                                haptics?.success()
                                statusMessage = "Encrypted backup created and copied to clipboard"
                                isSuccess = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (cards.isEmpty()) "Generate Encrypted Backup" else "Generate Encrypted Backup (${cards.size})",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }

                    AnimatedVisibility(visible = generatedBackupPayload != null) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = generatedBackupPayload ?: "",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    fontFamily = JetBrainsMonoFamily,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .verticalScroll(rememberScrollState())
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            FilledTonalButton(
                                onClick = {
                                    if (generatedBackupPayload != null) {
                                        clipboardManager.setText(AnnotatedString(generatedBackupPayload!!))
                                        haptics?.success()
                                        statusMessage = "Encrypted backup copied to clipboard"
                                        isSuccess = true
                                    }
                                },
                                modifier = Modifier.align(Alignment.End),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Code", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // SECTION 2: RESTORE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Restore Encrypted Backup",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Paste backup code and enter your secret passphrase to decrypt and restore.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        label = { Text("Backup Payload") },
                        placeholder = { Text("Paste encrypted VaultFolio JSON code here") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = MaterialTheme.shapes.medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = importPassphrase,
                        onValueChange = { importPassphrase = it },
                        label = { Text("Decryption Passphrase") },
                        placeholder = { Text("Enter passphrase used during export") },
                        visualTransformation = if (showImportPassphrase) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showImportPassphrase = !showImportPassphrase }) {
                                Icon(
                                    imageVector = if (showImportPassphrase) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Passphrase Visibility",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    importInputText = clip
                                    haptics?.cardSelect()
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paste", style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = {
                                if (importInputText.isBlank()) {
                                    statusMessage = "Please paste backup code"
                                    isSuccess = false
                                    return@Button
                                }

                                coroutineScope.launch {
                                    val result = repository.restoreEncryptedBackupJson(
                                        payload = importInputText,
                                        passphrase = importPassphrase,
                                        replaceExisting = false
                                    )
                                    result.onSuccess { count ->
                                        haptics?.success()
                                        statusMessage = "Successfully decrypted and restored $count card(s)"
                                        isSuccess = true
                                        importInputText = ""
                                        importPassphrase = ""
                                    }.onFailure { err ->
                                        haptics?.error()
                                        statusMessage = err.message ?: "Failed to decrypt backup"
                                        isSuccess = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1.3f).height(50.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Decrypt & Restore", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
