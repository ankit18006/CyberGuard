package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.data.ThreatAlert
import com.example.ui.theme.CriticalColor
import com.example.ui.theme.DangerousColor
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.SafeGreen
import com.example.ui.theme.SuspiciousColor
import com.example.viewmodel.CyberGuardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreatLogScreen(
    viewModel: CyberGuardViewModel,
    onThreatClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val allThreats by viewModel.allThreats.collectAsState()
    var selectedTab by remember { mutableStateOf("All") }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val tabs = listOf("All", "Critical", "Dangerous", "Suspicious")

    val filteredThreats = when (selectedTab) {
        "Critical" -> allThreats.filter { it.severity == "CRITICAL" }
        "Dangerous" -> allThreats.filter { it.severity == "DANGEROUS" }
        "Suspicious" -> allThreats.filter { it.severity == "SUSPICIOUS" }
        else -> allThreats
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Threat History Logs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (allThreats.isNotEmpty()) {
                        IconButton(onClick = { showConfirmationDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All Logs",
                                tint = CriticalColor
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab row to filter
            TabRow(
                selectedTabIndex = tabs.indexOf(selectedTab),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredThreats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SafeGreen,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Zero Threat Logs Found",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "No threats matched the '$selectedTab' severity filter in offline database audits.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredThreats) { threat ->
                        ThreatItemCard(threat = threat, onClick = { onThreatClick(threat.id) })
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }

        // Confirmation Dialog
        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = {
                    Text(text = "Clear Threat Logs?", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(text = "Are you sure you want to permanently clear all localized threat event logs from your device storage? This action is non-reversible.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllThreats()
                            showConfirmationDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CriticalColor)
                    ) {
                        Text(text = "Clear All", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmationDialog = false }) {
                        Text(text = "Cancel")
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}
