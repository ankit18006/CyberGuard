package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.CyberGuardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: CyberGuardViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 1. Accessibility State
    val isHighContrast by viewModel.isHighContrast.collectAsState()

    // 2. Emergency Contacts State
    val contact1Name by viewModel.contact1Name.collectAsState()
    val contact1Phone by viewModel.contact1Phone.collectAsState()
    val contact2Name by viewModel.contact2Name.collectAsState()
    val contact2Phone by viewModel.contact2Phone.collectAsState()
    val contact3Name by viewModel.contact3Name.collectAsState()
    val contact3Phone by viewModel.contact3Phone.collectAsState()
    val customMessage by viewModel.customMessage.collectAsState()

    // Temp screen inputs for contacts configuration
    var c1Name by remember(contact1Name) { mutableStateOf(contact1Name) }
    var c1Phone by remember(contact1Phone) { mutableStateOf(contact1Phone) }
    var c2Name by remember(contact2Name) { mutableStateOf(contact2Name) }
    var c2Phone by remember(contact2Phone) { mutableStateOf(contact2Phone) }
    var c3Name by remember(contact3Name) { mutableStateOf(contact3Name) }
    var c3Phone by remember(contact3Phone) { mutableStateOf(contact3Phone) }
    var msgText by remember(customMessage) { mutableStateOf(customMessage) }

    // 3. Backup Dialog States
    var showExportPasswordDialog by remember { mutableStateOf(false) }
    var showImportPasswordDialog by remember { mutableStateOf(false) }
    var exportPassword by remember { mutableStateOf("") }
    var importPassword by remember { mutableStateOf("") }
    var selectedImportUri by remember { mutableStateOf<Uri?>(null) }

    // Backup Document Pickers
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImportUri = uri
            showImportPasswordDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "System Settings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: ACCESSIBILITY & GLOBAL THEME SWITCHER
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Display & Visual Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Toggle between our high-performance dark crimson theme and a standardized high-contrast mode designed for visual assistance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "High-Contrast Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isHighContrast) "Yellow/Cyan High Accessibility Theme" else "Standard Crimson Dark Theme",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Switch(
                            checked = isHighContrast,
                            onCheckedChange = { viewModel.setHighContrast(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // Section 2: EMERGENCY PANIC BUTTON CONTACTS
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContactPhone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Family Emergency Shield",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Configure up to 3 trusted family members or contacts. When you tap the home screen PANIC trigger, CyberGuard will automatically broadcast your distress alert to these numbers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    // Contact 1
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Primary Emergency Contact",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = c1Name,
                                onValueChange = { c1Name = it },
                                label = { Text("Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = c1Phone,
                                onValueChange = { c1Phone = it },
                                label = { Text("Phone Number") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Contact 2
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Secondary Emergency Contact",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = c2Name,
                                onValueChange = { c2Name = it },
                                label = { Text("Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = c2Phone,
                                onValueChange = { c2Phone = it },
                                label = { Text("Phone Number") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Contact 3
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Tertiary Emergency Contact",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = c3Name,
                                onValueChange = { c3Name = it },
                                label = { Text("Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = c3Phone,
                                onValueChange = { c3Phone = it },
                                label = { Text("Phone Number") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Custom Message
                    OutlinedTextField(
                        value = msgText,
                        onValueChange = { msgText = it },
                        label = { Text("Custom Panic Broadcast Message") },
                        placeholder = { Text("HELP! I received a critical security threat alert.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.updateContacts(c1Name, c1Phone, c2Name, c2Phone, c3Name, c3Phone, msgText)
                            Toast.makeText(context, "Emergency Contacts updated successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Shield Configuration", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Section 3: SECURE ENCRYPTED BACKUP & RESTORE
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Secure Cryptographic Backup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Export or import your full application context. Your configuration, contacts, settings, and local security audit logs will be packed and securely encrypted with an offline key derived from your custom password.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Export Button
                        Button(
                            onClick = { showExportPasswordDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // Import Button
                        OutlinedButton(
                            onClick = { importLauncher.launch("*/*") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.DownloadForOffline, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // Export Master Password Picker Dialog
    if (showExportPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showExportPasswordDialog = false
                exportPassword = ""
            },
            title = {
                Text(
                    text = "Encrypt Security Backup",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter a password to encrypt your backup file. You must remember this password to decrypt and restore your settings on another device.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = exportPassword,
                        onValueChange = { exportPassword = it },
                        label = { Text("Backup Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (exportPassword.isBlank()) {
                            Toast.makeText(context, "Password cannot be blank!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.exportSecureStorage(
                                password = exportPassword,
                                onSuccess = { fileUri ->
                                    showExportPasswordDialog = false
                                    exportPassword = ""
                                    // Trigger file sharing intent
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/octet-stream"
                                        putExtra(Intent.EXTRA_SUBJECT, "CyberGuard Encrypted Security Backup")
                                        putExtra(Intent.EXTRA_STREAM, fileUri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    val chooser = Intent.createChooser(sendIntent, "Save encrypted backup file").apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(chooser)
                                },
                                onError = { errorMsg ->
                                    Toast.makeText(context, "Export error: $errorMsg", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                ) {
                    Text("Secure & Share")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExportPasswordDialog = false
                        exportPassword = ""
                    }
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Import Password Entry Dialog
    if (showImportPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportPasswordDialog = false
                importPassword = ""
                selectedImportUri = null
            },
            title = {
                Text(
                    text = "Decrypt Security Backup",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter the encryption password for this backup file to safely restore your settings and logs.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = importPassword,
                        onValueChange = { importPassword = it },
                        label = { Text("Decryption Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = selectedImportUri
                        if (uri == null) {
                            Toast.makeText(context, "No backup file selected", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (importPassword.isBlank()) {
                            Toast.makeText(context, "Password cannot be blank", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.importSecureStorage(
                                uri = uri,
                                password = importPassword,
                                onSuccess = {
                                    showImportPasswordDialog = false
                                    importPassword = ""
                                    selectedImportUri = null
                                    Toast.makeText(context, "Security settings and Threat logs successfully restored!", Toast.LENGTH_LONG).show()
                                },
                                onError = { errorMsg ->
                                    Toast.makeText(context, "Restoration error: $errorMsg", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                ) {
                    Text("Decrypt & Restore")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showImportPasswordDialog = false
                        importPassword = ""
                        selectedImportUri = null
                    }
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
