package com.example

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MalwareScanScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ThreatDetailScreen
import com.example.ui.screens.ThreatLogScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CyberGuardViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CyberGuardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntentAction(intent?.action)

        setContent {
            val isHighContrast by viewModel.isHighContrast.collectAsState()
            MyApplicationTheme(isHighContrast = isHighContrast) {
                val navController = rememberNavController()
                val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()

                // Check for threat deep link from status notification click
                val threatIdFromNotification = intent?.getIntExtra("THREAT_ID", -1) ?: -1

                LaunchedEffect(isOnboardingCompleted) {
                    if (isOnboardingCompleted) {
                        if (threatIdFromNotification != -1) {
                            navController.navigate("threat_detail/$threatIdFromNotification") {
                                popUpTo("home") { inclusive = false }
                            }
                        } else {
                            navController.navigate("home") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        }
                    } else {
                        navController.navigate("onboarding") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                // Check listener service status when resuming the activity
                LaunchedEffect(Unit) {
                    viewModel.checkListenerStatus()
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (isOnboardingCompleted) "home" else "onboarding",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("onboarding") {
                            OnboardingScreen(
                                viewModel = viewModel,
                                onComplete = {
                                    navController.navigate("home") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onScanAppsClick = {
                                    navController.navigate("malware_scan")
                                },
                                onThreatLogClick = {
                                    navController.navigate("threat_log")
                                },
                                onThreatClick = { threatId ->
                                    navController.navigate("threat_detail/$threatId")
                                },
                                onSettingsClick = {
                                    navController.navigate("settings")
                                }
                            )
                        }

                        composable("threat_log") {
                            ThreatLogScreen(
                                viewModel = viewModel,
                                onThreatClick = { threatId ->
                                    navController.navigate("threat_detail/$threatId")
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = "threat_detail/{threatId}",
                            arguments = listOf(navArgument("threatId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val threatId = backStackEntry.arguments?.getInt("threatId") ?: -1
                            ThreatDetailScreen(
                                threatId = threatId,
                                viewModel = viewModel,
                                onBackClick = {
                                    if (navController.previousBackStackEntry != null) {
                                        navController.popBackStack()
                                    } else {
                                        navController.navigate("home") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }

                        composable("malware_scan") {
                            MalwareScanScreen(
                                viewModel = viewModel,
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("settings") {
                            com.example.ui.screens.SettingsScreen(
                                viewModel = viewModel,
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkListenerStatus()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntentAction(intent.action)
    }

    private fun handleIntentAction(action: String?) {
        if (action == "com.example.action.PANIC_TRIGGER") {
            viewModel.triggerPanicSOSDirectly()
        }
    }
}
