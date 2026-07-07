package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ThreatAlert
import com.example.data.ThreatDatabase
import com.example.service.CyberGuardListenerService
import com.example.service.MalwareScanner
import com.example.service.SuspiciousApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CyberGuardViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()
    private val database = ThreatDatabase.getDatabase(context)
    private val dao = database.threatDao()

    private val prefs: SharedPreferences = context.getSharedPreferences("cyberguard_prefs", Context.MODE_PRIVATE)

    // Onboarding status
    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    // Service connection state
    private val _isListenerEnabled = MutableStateFlow(false)
    val isListenerEnabled: StateFlow<Boolean> = _isListenerEnabled.asStateFlow()

    // Last scan time
    private val _lastScanTime = MutableStateFlow(prefs.getLong("last_scan_time", 0L))
    val lastScanTime: StateFlow<Long> = _lastScanTime.asStateFlow()

    // Malware Scanner UI state
    private val _scanState = MutableStateFlow(ScanUiState.IDLE)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _currentScanningAppName = MutableStateFlow("")
    val currentScanningAppName: StateFlow<String> = _currentScanningAppName.asStateFlow()

    private val _scanResults = MutableStateFlow<List<SuspiciousApp>>(emptyList())
    val scanResults: StateFlow<List<SuspiciousApp>> = _scanResults.asStateFlow()

    // Threats from Room
    val allThreats: StateFlow<List<ThreatAlert>> = dao.getAllThreats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        checkListenerStatus()
    }

    fun checkListenerStatus() {
        val enabled = CyberGuardListenerService.isNotificationServiceEnabled(context)
        _isListenerEnabled.value = enabled
    }

    fun completeOnboarding() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        _isOnboardingCompleted.value = true
    }

    fun resetOnboarding() {
        prefs.edit().putBoolean("onboarding_completed", false).apply()
        _isOnboardingCompleted.value = false
    }

    fun triggerTestNotification() {
        // Post a standard mock notification to let the system read and detect it!
        // This simulates a realistic OTP threat arriving in SMS
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        
        // Create channel first
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "sms_mock_channel",
                "Messages (Simulated)",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = androidx.core.app.NotificationCompat.Builder(context, "sms_mock_channel")
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("+91 98765 43210")
            .setContentText("CRITICAL Alert: SBI NetBanking password expired. Please click https://bit.ly/sbi-kyc-scam to update your KYC and prevent digital arrest.")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(999, builder.build())
    }

    // Malware scan initiation
    fun startMalwareScan() {
        viewModelScope.launch {
            _scanState.value = ScanUiState.SCANNING
            _scanProgress.value = 0f
            _currentScanningAppName.value = "Initializing Scanner..."
            _scanResults.value = emptyList()

            val detected = MalwareScanner.scanInstalledApps(context) { current, total, appName ->
                _currentScanningAppName.value = appName
                _scanProgress.value = current.toFloat() / total.toFloat()
            }

            _scanResults.value = detected
            _scanState.value = ScanUiState.COMPLETED
            
            val now = System.currentTimeMillis()
            prefs.edit().putLong("last_scan_time", now).apply()
            _lastScanTime.value = now
        }
    }

    fun clearAllThreats() {
        viewModelScope.launch {
            dao.clearAllThreats()
        }
    }

    fun deleteThreat(id: Int) {
        viewModelScope.launch {
            dao.deleteThreatById(id)
        }
    }

    fun openNotificationAccessSettings() {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun requestBatteryOptimizationExemption() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to battery settings if direct request fails or is blocked
                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
            }
        }
    }

    fun initiatePhoneCall(number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openReportWebsite() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://cybercrime.gov.in")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun uninstallApp(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:$packageName")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

enum class ScanUiState {
    IDLE, SCANNING, COMPLETED
}
