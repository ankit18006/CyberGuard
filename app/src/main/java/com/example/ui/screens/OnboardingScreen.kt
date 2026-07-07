package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryRed
import com.example.viewmodel.CyberGuardViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: CyberGuardViewModel,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    val isListenerEnabled by viewModel.isListenerEnabled.collectAsState()
    
    val steps = listOf(
        OnboardingStep(
            title = "CyberGuard",
            subtitle = "Sentry of India's Mobile Security",
            description = "Real-time fraud and malware detector working 100% locally to protect your finances and privacy.",
            icon = Icons.Default.Shield,
            color = PrimaryRed
        ),
        OnboardingStep(
            title = "Notification Fraud Guard",
            subtitle = "WhatsApp, SMS & Telegram Scan",
            description = "Reads incoming notifications silently. Fires an emergency alarm the moment any UPI OTP fraud, fake CBI threat, or remote control link is detected.",
            icon = Icons.Default.NotificationImportant,
            color = Color(0xFFF59E0B)
        ),
        OnboardingStep(
            title = "Deep Malware Scanner",
            subtitle = "Find Spyware & Predatory Apps",
            description = "Scans all installed applications against signatures of known malicious clones, unauthorized remote tools (AnyDesk/TeamViewer), and predatory loan apps.",
            icon = Icons.Default.Security,
            color = Color(0xFF3B82F6)
        ),
        OnboardingStep(
            title = "100% Private. No Server.",
            subtitle = "Zero Internet Required",
            description = "All fraud parsing runs inside your phone. No server, no APIs, and absolutely zero personal data ever leaves your device. Fully secure and battery optimized.",
            icon = Icons.Default.Lock,
            color = Color(0xFF10B981)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top branding skip line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CyberGuard",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryRed,
                fontWeight = FontWeight.Bold
            )
            
            if (currentStep < 3) {
                IconButton(onClick = { currentStep = 3 }) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(1.dp))
            }
        }

        // Animated Body Card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    fadeIn() with fadeOut()
                }
            ) { stepIdx ->
                val step = steps[stepIdx]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    // Feature Icon with gradient pulse
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(step.color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = step.title,
                            tint = step.color,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = step.subtitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = step.color,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
        }

        // Bottom Controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                repeat(4) { idx ->
                    Box(
                        modifier = Modifier
                            .size(if (idx == currentStep) 16.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (idx == currentStep) PrimaryRed else MaterialTheme.colorScheme.onBackground.copy(
                                    alpha = 0.2f
                                )
                            )
                    )
                }
            }

            // Buttons / Permission setup
            if (currentStep < 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        IconButton(
                            onClick = { currentStep-- },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(56.dp))
                    }

                    Button(
                        onClick = { currentStep++ },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Next",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
            } else {
                // Final Screen: Permission Setup
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Setup Security Permissions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed
                        )

                        // 1. Notification Listener Access Status
                        PermissionRow(
                            title = "1. Real-time Fraud Monitor",
                            desc = "Required to silently read incoming notifications and catch scams instantly.",
                            isEnabled = isListenerEnabled,
                            onGrantClick = { viewModel.openNotificationAccessSettings() }
                        )

                        // 2. Battery optimization exemption status
                        PermissionRow(
                            title = "2. Background Life Assurance",
                            desc = "Required to bypass heavy brand killing (OnePlus/Xiaomi) and stay alert.",
                            isEnabled = true, // SharedPreferences or user clicked
                            onGrantClick = { viewModel.requestBatteryOptimizationExemption() }
                        )
                    }
                }

                // Finish Button
                Button(
                    onClick = {
                        viewModel.completeOnboarding()
                        onComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isListenerEnabled) Color(0xFF10B981) else PrimaryRed),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isListenerEnabled) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Start Protection Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Continue Anyway (Grant Later)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRow(
    title: String,
    desc: String,
    isEnabled: Boolean,
    onGrantClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        Spacer(modifier = Modifier.size(12.dp))
        Button(
            onClick = onGrantClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEnabled) Color(0xFF10B981).copy(alpha = 0.15f) else PrimaryRed,
                contentColor = if (isEnabled) Color(0xFF10B981) else Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = if (isEnabled) "Active" else "Grant", fontWeight = FontWeight.Bold)
        }
    }
}

data class OnboardingStep(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)
