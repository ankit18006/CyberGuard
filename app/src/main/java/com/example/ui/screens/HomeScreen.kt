package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ThreatAlert
import com.example.ui.theme.CriticalColor
import com.example.ui.theme.DangerousColor
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SuspiciousColor
import com.example.viewmodel.CyberGuardViewModel

@Composable
fun HomeScreen(
    viewModel: CyberGuardViewModel,
    onScanAppsClick: () -> Unit,
    onThreatLogClick: () -> Unit,
    onThreatClick: (Int) -> Unit,
    onSettingsClick: () -> Unit
) {
    val isEnabled by viewModel.isListenerEnabled.collectAsState()
    val allThreats by viewModel.allThreats.collectAsState()

    val contact1Name by viewModel.contact1Name.collectAsState()
    val contact1Phone by viewModel.contact1Phone.collectAsState()
    val contact2Name by viewModel.contact2Name.collectAsState()
    val contact2Phone by viewModel.contact2Phone.collectAsState()
    val contact3Name by viewModel.contact3Name.collectAsState()
    val contact3Phone by viewModel.contact3Phone.collectAsState()
    val customMessage by viewModel.customMessage.collectAsState()

    var showContactDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var hasSmsPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.SEND_SMS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasSmsPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Direct background SMS dispatch enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied. Falling back to pre-filled SMS composer.", Toast.LENGTH_SHORT).show()
        }
    }

    // Calculate dynamic stats
    val totalThreatsCount = allThreats.size
    val criticalCount = allThreats.count { it.severity == "CRITICAL" }
    
    val past24Hours = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
    val threatsToday = allThreats.count { it.timestamp > past24Hours }

    val recentThreats = allThreats.take(5)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header spacing
            item { Spacer(modifier = Modifier.height(10.dp)) }

            // Brand Title / Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CyberGuard",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Secure local shield • Mumbai, India",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Settings Screen Button
                        IconButton(
                            onClick = onSettingsClick,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Demo Simulation Trigger
                        Button(
                            onClick = { viewModel.triggerTestNotification() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Test Threat", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Real-Time Listener Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEnabled) SafeGreen.copy(alpha = 0.08f) else CriticalColor.copy(alpha = 0.08f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isEnabled) SafeGreen.copy(alpha = 0.3f) else CriticalColor.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(if (isEnabled) SafeGreen.copy(alpha = 0.15f) else CriticalColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = if (isEnabled) SafeGreen else CriticalColor,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (isEnabled) "Active Protection" else "Protection Suspended",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isEnabled) SafeGreen else CriticalColor
                                )
                                Text(
                                    text = if (isEnabled) "Silent background scans are active." else "Real-time scans cannot read incoming SMS/WhatsApp.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (!isEnabled) {
                            Button(
                                onClick = { viewModel.openNotificationAccessSettings() },
                                colors = ButtonDefaults.buttonColors(containerColor = CriticalColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(text = "FIX", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Stats row (Today, Critical, Total)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Threats Today",
                        count = threatsToday.toString(),
                        color = if (threatsToday > 0) DangerousColor else SafeGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Critical Count",
                        count = criticalCount.toString(),
                        color = if (criticalCount > 0) CriticalColor else SafeGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "All-Time Scan",
                        count = totalThreatsCount.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Actions Title
            item {
                Text(
                    text = "Shield Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Grid of 4 actions: Scan Apps, Threat Log, Call 1930, Report Online
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Scan Apps",
                            desc = "Local Malware Check",
                            icon = Icons.Default.BugReport,
                            color = Color(0xFF3B82F6),
                            onClick = onScanAppsClick,
                            modifier = Modifier.weight(1f)
                        )
                        ActionCard(
                            title = "Threat Log",
                            desc = "History & Security Logs",
                            icon = Icons.Default.History,
                            color = Color(0xFF8B5CF6),
                            onClick = onThreatLogClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionCard(
                            title = "Call 1930",
                            desc = "Cyber Helpline India",
                            icon = Icons.Default.Call,
                            color = Color(0xFFEF4444),
                            onClick = { viewModel.initiatePhoneCall("1930") },
                            modifier = Modifier.weight(1f)
                        )
                        ActionCard(
                            title = "Report Online",
                            desc = "MHA cybercrime.gov.in",
                            icon = Icons.Default.Language,
                            color = Color(0xFF10B981),
                            onClick = { viewModel.openReportWebsite() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Emergency Contacts Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Emergency National Helplines",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EmergencyCallButton(label = "Police 100", number = "100", onDial = { viewModel.initiatePhoneCall("100") })
                            EmergencyCallButton(label = "Cyber 1930", number = "1930", onDial = { viewModel.initiatePhoneCall("1930") })
                            EmergencyCallButton(label = "Emergency 112", number = "112", onDial = { viewModel.initiatePhoneCall("112") })
                            EmergencyCallButton(label = "RBI 14440", number = "14440", onDial = { viewModel.initiatePhoneCall("14440") })
                        }
                    }
                }
            }

            // Family SOS Panic Shield
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Family SOS Panic Shield",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = onSettingsClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Setup Contacts",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Configure", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = CriticalColor.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left part: SOS Action Circle Button
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .background(CriticalColor)
                                        .clickable { viewModel.triggerPanicSOSDirectly() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Trigger SOS",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Text(
                                            text = "PANIC",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap to Alert Contacts",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = CriticalColor,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Right part: Info on Configured Contacts & Auto Send status
                            Column(
                                modifier = Modifier.weight(1.8f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val activeCount = listOf(contact1Phone, contact2Phone, contact3Phone).count { it.isNotBlank() }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ContactPhone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (activeCount > 0) "$activeCount Contacts Active" else "No contacts configured",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeCount > 0) SafeGreen else DangerousColor
                                    )
                                }

                                if (activeCount > 0) {
                                    Text(
                                        text = listOf(
                                            contact1Name.ifBlank { contact1Phone },
                                            contact2Name.ifBlank { contact2Phone },
                                            contact3Name.ifBlank { contact3Phone }
                                        ).filter { it.isNotBlank() }.joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // Permission / Sending status
                                if (hasSmsPermission) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sms,
                                            contentDescription = null,
                                            tint = SafeGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Direct background send active",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = SafeGreen
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { permissionLauncher.launch(android.Manifest.permission.SEND_SMS) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sms,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Authorize Auto-Send",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent 5 threats section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Local Threats",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (recentThreats.isNotEmpty()) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onThreatLogClick() }
                        )
                    }
                }
            }

            if (recentThreats.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = SafeGreen,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Your Device is Secure",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Zero fraud threats or malicious bank app clones detected recently.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            } else {
                items(recentThreats) { threat ->
                    ThreatItemCard(threat = threat, onClick = { onThreatClick(threat.id) })
                }
                item { Spacer(modifier = Modifier.height(10.dp)) }
            }
        }
    }

    if (showContactDialog) {
        var c1Name by remember { mutableStateOf(contact1Name) }
        var c1Phone by remember { mutableStateOf(contact1Phone) }
        var c2Name by remember { mutableStateOf(contact2Name) }
        var c2Phone by remember { mutableStateOf(contact2Phone) }
        var c3Name by remember { mutableStateOf(contact3Name) }
        var c3Phone by remember { mutableStateOf(contact3Phone) }
        var msgText by remember { mutableStateOf(customMessage) }

        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text(text = "Family Emergency Contacts", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Configure up to 3 family contacts to receive alert updates when you trigger the Panic Button.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Contact 1 (Primary)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = c1Name,
                                onValueChange = { c1Name = it },
                                label = { Text("Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = c1Phone,
                                onValueChange = { c1Phone = it },
                                label = { Text("Phone") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Contact 2", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = c2Name,
                                onValueChange = { c2Name = it },
                                label = { Text("Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = c2Phone,
                                onValueChange = { c2Phone = it },
                                label = { Text("Phone") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Contact 3", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = c3Name,
                                onValueChange = { c3Name = it },
                                label = { Text("Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = c3Phone,
                                onValueChange = { c3Phone = it },
                                label = { Text("Phone") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = msgText,
                        onValueChange = { msgText = it },
                        label = { Text("Custom Emergency Message") },
                        placeholder = { Text("e.g. HELP! I need immediate assistance.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateContacts(c1Name, c1Phone, c2Name, c2Phone, c3Name, c3Phone, msgText)
                        showContactDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = desc, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmergencyCallButton(
    label: String,
    number: String,
    onDial: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onDial() }
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CriticalColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Call, contentDescription = label, tint = CriticalColor, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ThreatItemCard(
    threat: ThreatAlert,
    onClick: () -> Unit
) {
    val severityColor = when (threat.severity) {
        "CRITICAL" -> CriticalColor
        "DANGEROUS" -> DangerousColor
        else -> SuspiciousColor
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(severityColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (threat.severity == "CRITICAL") Icons.Default.ErrorOutline else Icons.Default.Warning,
                        contentDescription = null,
                        tint = severityColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = threat.fraudType,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = severityColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(severityColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = threat.severity,
                                style = MaterialTheme.typography.labelSmall,
                                color = severityColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "App: ${threat.appName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = threat.messageSnippet,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
