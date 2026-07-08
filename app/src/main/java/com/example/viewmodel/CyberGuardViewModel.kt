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

    // Emergency Contact StateFlows
    private val _contact1Name = MutableStateFlow(prefs.getString("emergency_contact_1_name", "") ?: "")
    val contact1Name: StateFlow<String> = _contact1Name.asStateFlow()

    private val _contact1Phone = MutableStateFlow(prefs.getString("emergency_contact_1_phone", "") ?: "")
    val contact1Phone: StateFlow<String> = _contact1Phone.asStateFlow()

    private val _contact2Name = MutableStateFlow(prefs.getString("emergency_contact_2_name", "") ?: "")
    val contact2Name: StateFlow<String> = _contact2Name.asStateFlow()

    private val _contact2Phone = MutableStateFlow(prefs.getString("emergency_contact_2_phone", "") ?: "")
    val contact2Phone: StateFlow<String> = _contact2Phone.asStateFlow()

    private val _contact3Name = MutableStateFlow(prefs.getString("emergency_contact_3_name", "") ?: "")
    val contact3Name: StateFlow<String> = _contact3Name.asStateFlow()

    private val _contact3Phone = MutableStateFlow(prefs.getString("emergency_contact_3_phone", "") ?: "")
    val contact3Phone: StateFlow<String> = _contact3Phone.asStateFlow()

    private val _customMessage = MutableStateFlow(prefs.getString("emergency_custom_message", "") ?: "")
    val customMessage: StateFlow<String> = _customMessage.asStateFlow()

    private val _isHighContrast = MutableStateFlow(prefs.getBoolean("is_high_contrast", false))
    val isHighContrast: StateFlow<Boolean> = _isHighContrast.asStateFlow()

    fun setHighContrast(enabled: Boolean) {
        prefs.edit().putBoolean("is_high_contrast", enabled).apply()
        _isHighContrast.value = enabled
    }

    init {
        checkListenerStatus()
        updateDynamicShortcut()
    }

    fun updateContacts(
        c1Name: String, c1Phone: String,
        c2Name: String, c2Phone: String,
        c3Name: String, c3Phone: String,
        msg: String
    ) {
        prefs.edit().apply {
            putString("emergency_contact_1_name", c1Name)
            putString("emergency_contact_1_phone", c1Phone)
            putString("emergency_contact_2_name", c2Name)
            putString("emergency_contact_2_phone", c2Phone)
            putString("emergency_contact_3_name", c3Name)
            putString("emergency_contact_3_phone", c3Phone)
            putString("emergency_custom_message", msg)
            apply()
        }
        _contact1Name.value = c1Name
        _contact1Phone.value = c1Phone
        _contact2Name.value = c2Name
        _contact2Phone.value = c2Phone
        _contact3Name.value = c3Name
        _contact3Phone.value = c3Phone
        _customMessage.value = msg

        updateDynamicShortcut()
    }

    fun updateDynamicShortcut() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE) as android.content.pm.ShortcutManager
            
            val intent = Intent(context, com.example.MainActivity::class.java).apply {
                action = "com.example.action.PANIC_TRIGGER"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val shortcut = android.content.pm.ShortcutInfo.Builder(context, "sos_panic_shortcut")
                .setShortLabel("SOS Panic Alert")
                .setLongLabel("Trigger CyberGuard emergency broadcast")
                .setIcon(android.graphics.drawable.Icon.createWithResource(context, android.R.drawable.ic_dialog_alert))
                .setIntent(intent)
                .build()

            try {
                shortcutManager.dynamicShortcuts = listOf(shortcut)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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

    @android.annotation.SuppressLint("NotificationPermission")
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

    // Trigger direct background SMS SOS if permission is granted, otherwise open standard composer
    fun triggerPanicSOSDirectly() {
        val message = getCompiledEmergencyMessage()
        val phones = listOf(contact1Phone.value, contact2Phone.value, contact3Phone.value)
            .filter { it.isNotBlank() }

        if (phones.isEmpty()) {
            initiatePhoneCall("112")
            android.widget.Toast.makeText(context, "No emergency contacts configured! Calling 112 as backup.", android.widget.Toast.LENGTH_LONG).show()
            return
        }

        val hasSmsPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.SEND_SMS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasSmsPermission) {
            try {
                val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    context.getSystemService(android.telephony.SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    android.telephony.SmsManager.getDefault()
                }

                phones.forEach { phone ->
                    val parts = smsManager.divideMessage(message)
                    if (parts.size > 1) {
                        smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
                    } else {
                        smsManager.sendTextMessage(phone, null, message, null, null)
                    }
                }
                android.widget.Toast.makeText(context, "SOS alert dispatched to all ${phones.size} emergency contacts!", android.widget.Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                launchSmsAppComposer(phones.joinToString(","), message)
            }
        } else {
            launchSmsAppComposer(phones.joinToString(","), message)
        }
    }

    private fun launchSmsAppComposer(phonesJoined: String, msg: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phonesJoined")
            putExtra("sms_body", msg)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, msg)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(shareIntent, "Send SOS alert via").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    fun getCompiledEmergencyMessage(): String {
        val customText = customMessage.value.ifBlank { "EMERGENCY: I need immediate assistance, please check on me." }
        val threats = allThreats.value
        val activeThreats = threats.size
        val scanTime = lastScanTime.value
        val scanInfo = if (scanTime > 0L) {
            "Last local scan: " + java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(scanTime))
        } else {
            "No local scan executed"
        }

        return "$customText\n\n[My Security Shield Status]\nPending threats: $activeThreats\n$scanInfo\nStatus: ${if (activeThreats > 0) "WARNING (Threats Pending)" else "Secured Local Shield"}"
    }

    // Export local Room threat log history to a plain-text file and launch share chooser
    fun exportThreatLogs(context: Context) {
        viewModelScope.launch {
            val threats = allThreats.value
            val textBuilder = java.lang.StringBuilder()
            textBuilder.append("========================================\n")
            textBuilder.append("CYBERGUARD SHIELD - SECURITY AUDIT REPORT\n")
            textBuilder.append("Generated on: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n")
            textBuilder.append("========================================\n\n")

            if (threats.isEmpty()) {
                textBuilder.append("No local security threat events logged. Device secure.\n")
            } else {
                threats.forEachIndexed { index, threat ->
                    textBuilder.append("${index + 1}. [${threat.severity}] - ${threat.title}\n")
                    textBuilder.append("   Date/Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(threat.timestamp))}\n")
                    textBuilder.append("   Source App: ${threat.appName} (${threat.packageName})\n")
                    textBuilder.append("   Message Snip: ${threat.messageSnippet}\n")
                    textBuilder.append("   Threat Classification: ${threat.fraudType}\n")
                    textBuilder.append("   Recommended Steps: ${threat.stepsToTake}\n")
                    textBuilder.append("   ----------------------------------------\n\n")
                }
            }

            try {
                val file = java.io.File(context.cacheDir, "cyberguard_threat_log.txt")
                file.writeText(textBuilder.toString())

                val fileUri: Uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "CyberGuard Threat History Log")
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                val chooserIntent = Intent.createChooser(shareIntent, "Export Security Audit Report").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(chooserIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                // Plaintext fallback
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "CyberGuard Threat History Log")
                    putExtra(Intent.EXTRA_TEXT, textBuilder.toString())
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(shareIntent, "Export Threat Log").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }
    }

    fun exportSecureStorage(password: String, onSuccess: (Uri) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val backupJson = org.json.JSONObject()

                // 1. Export Preferences
                val prefsJson = org.json.JSONObject().apply {
                    put("onboarding_completed", prefs.getBoolean("onboarding_completed", false))
                    put("emergency_contact_1_name", prefs.getString("emergency_contact_1_name", ""))
                    put("emergency_contact_1_phone", prefs.getString("emergency_contact_1_phone", ""))
                    put("emergency_contact_2_name", prefs.getString("emergency_contact_2_name", ""))
                    put("emergency_contact_2_phone", prefs.getString("emergency_contact_2_phone", ""))
                    put("emergency_contact_3_name", prefs.getString("emergency_contact_3_name", ""))
                    put("emergency_contact_3_phone", prefs.getString("emergency_contact_3_phone", ""))
                    put("emergency_custom_message", prefs.getString("emergency_custom_message", ""))
                    put("last_scan_time", prefs.getLong("last_scan_time", 0L))
                    put("is_high_contrast", prefs.getBoolean("is_high_contrast", false))
                }
                backupJson.put("preferences", prefsJson)

                // 2. Export Threat Logs
                val threatsArray = org.json.JSONArray()
                allThreats.value.forEach { threat ->
                    val threatJson = org.json.JSONObject().apply {
                        put("appName", threat.appName)
                        put("packageName", threat.packageName)
                        put("title", threat.title)
                        put("messageSnippet", threat.messageSnippet)
                        put("fraudType", threat.fraudType)
                        put("severity", threat.severity)
                        put("timestamp", threat.timestamp)
                        put("stepsToTake", threat.stepsToTake)
                    }
                    threatsArray.put(threatJson)
                }
                backupJson.put("threat_alerts", threatsArray)

                val plainText = backupJson.toString()
                val encryptedBase64 = com.example.data.BackupEncryptor.encrypt(plainText, password.toCharArray())

                // Write to a temporary file in cache and return the Uri to share
                val cacheDir = context.cacheDir
                val file = java.io.File(cacheDir, "cyberguard_backup.enc")
                file.writeText(encryptedBase64, Charsets.UTF_8)

                val fileUri: Uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                onSuccess(fileUri)
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun importSecureStorage(uri: Uri, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val encryptedBase64 = inputStream?.bufferedReader()?.use { it.readText() }
                if (encryptedBase64.isNullOrBlank()) {
                    onError("Empty or invalid backup file")
                    return@launch
                }

                val decryptedText = com.example.data.BackupEncryptor.decrypt(encryptedBase64.trim(), password.toCharArray())
                val backupJson = org.json.JSONObject(decryptedText)

                // 1. Import Preferences
                if (backupJson.has("preferences")) {
                    val prefsJson = backupJson.getJSONObject("preferences")
                    prefs.edit().apply {
                        if (prefsJson.has("onboarding_completed")) {
                            val value = prefsJson.getBoolean("onboarding_completed")
                            putBoolean("onboarding_completed", value)
                            _isOnboardingCompleted.value = value
                        }
                        if (prefsJson.has("emergency_contact_1_name")) {
                            val value = prefsJson.getString("emergency_contact_1_name")
                            putString("emergency_contact_1_name", value)
                            _contact1Name.value = value
                        }
                        if (prefsJson.has("emergency_contact_1_phone")) {
                            val value = prefsJson.getString("emergency_contact_1_phone")
                            putString("emergency_contact_1_phone", value)
                            _contact1Phone.value = value
                        }
                        if (prefsJson.has("emergency_contact_2_name")) {
                            val value = prefsJson.getString("emergency_contact_2_name")
                            putString("emergency_contact_2_name", value)
                            _contact2Name.value = value
                        }
                        if (prefsJson.has("emergency_contact_2_phone")) {
                            val value = prefsJson.getString("emergency_contact_2_phone")
                            putString("emergency_contact_2_phone", value)
                            _contact2Phone.value = value
                        }
                        if (prefsJson.has("emergency_contact_3_name")) {
                            val value = prefsJson.getString("emergency_contact_3_name")
                            putString("emergency_contact_3_name", value)
                            _contact3Name.value = value
                        }
                        if (prefsJson.has("emergency_contact_3_phone")) {
                            val value = prefsJson.getString("emergency_contact_3_phone")
                            putString("emergency_contact_3_phone", value)
                            _contact3Phone.value = value
                        }
                        if (prefsJson.has("emergency_custom_message")) {
                            val value = prefsJson.getString("emergency_custom_message")
                            putString("emergency_custom_message", value)
                            _customMessage.value = value
                        }
                        if (prefsJson.has("last_scan_time")) {
                            val value = prefsJson.getLong("last_scan_time")
                            putLong("last_scan_time", value)
                            _lastScanTime.value = value
                        }
                        if (prefsJson.has("is_high_contrast")) {
                            val value = prefsJson.getBoolean("is_high_contrast")
                            putBoolean("is_high_contrast", value)
                            _isHighContrast.value = value
                        }
                        apply()
                    }
                }

                // 2. Import Threat Logs
                if (backupJson.has("threat_alerts")) {
                    val threatsArray = backupJson.getJSONArray("threat_alerts")
                    
                    // Clear existing database logs
                    dao.clearAllThreats()
                    
                    val restoredThreats = mutableListOf<ThreatAlert>()
                    for (i in 0 until threatsArray.length()) {
                        val threatJson = threatsArray.getJSONObject(i)
                        restoredThreats.add(
                            ThreatAlert(
                                appName = threatJson.optString("appName", ""),
                                packageName = threatJson.optString("packageName", ""),
                                title = threatJson.optString("title", ""),
                                messageSnippet = threatJson.optString("messageSnippet", ""),
                                fraudType = threatJson.optString("fraudType", ""),
                                severity = threatJson.optString("severity", ""),
                                timestamp = threatJson.optLong("timestamp", System.currentTimeMillis()),
                                stepsToTake = threatJson.optString("stepsToTake", "")
                            )
                        )
                    }
                    restoredThreats.forEach { dao.insertThreat(it) }
                }

                updateDynamicShortcut()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.localizedMessage ?: "Decryption failed. Please check your password.")
            }
        }
    }
}

enum class ScanUiState {
    IDLE, SCANNING, COMPLETED
}
