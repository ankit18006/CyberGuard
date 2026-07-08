package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.ThreatAlert
import com.example.data.ThreatDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CyberGuardListenerService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    private val appTimestamps = mutableMapOf<String, MutableList<Long>>()
    private val totalTimestamps = mutableListOf<Long>()

    companion object {
        private const val TAG = "CyberGuardService"
        private const val CHANNEL_ID = "cyberguard_threat_alerts"
        
        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning

        // Helper to check if Notification Listener is enabled in system settings
        fun isNotificationServiceEnabled(context: Context): Boolean {
            val cn = android.content.ComponentName(context, CyberGuardListenerService::class.java)
            val flat = android.provider.Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            return flat != null && flat.contains(cn.flattenToString())
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isServiceRunning.value = true
        Log.d(TAG, "Notification Listener connected successfully.")
        createNotificationChannel()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isServiceRunning.value = false
        Log.d(TAG, "Notification Listener disconnected.")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        val packageName = sbn.packageName ?: return
        
        // Skip our own notifications to prevent loop alerts
        if (packageName == this.packageName) return
        
        // Rate limiting
        if (shouldRateLimit(packageName)) {
            Log.w(TAG, "Rate limit reached. Dropping notification from $packageName")
            return
        }

        val extras: Bundle = sbn.notification.extras ?: return
        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        
        if (title.isEmpty() && text.isEmpty()) return

        // Fetch application label/name from package manager
        val pm = packageManager
        val appName = try {
            val ai = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(ai).toString()
        } catch (e: Exception) {
            packageName
        }

        // Analyze notification
        val threat = DetectionEngine.analyzeNotification(appName, packageName, title, text)
        if (threat != null) {
            Log.e(TAG, "CRITICAL THREAT DETECTED: ${threat.fraudType} on app $appName")
            saveThreatAndAlertUser(threat)
        }
    }

    private fun shouldRateLimit(packageName: String): Boolean {
        val now = System.currentTimeMillis()
        
        synchronized(this) {
            // Remove timestamps older than 1 second
            totalTimestamps.removeAll { now - it > 1000 }
            val appList = appTimestamps.getOrPut(packageName) { mutableListOf() }
            appList.removeAll { now - it > 1000 }
            
            // Limit checks
            if (totalTimestamps.size >= 20) {
                return true
            }
            if (appList.size >= 10) {
                return true
            }
            
            totalTimestamps.add(now)
            appList.add(now)
            return false
        }
    }

    private fun saveThreatAndAlertUser(threat: ThreatAlert) {
        scope.launch {
            val database = ThreatDatabase.getDatabase(applicationContext)
            val dao = database.threatDao()
            val insertedId = dao.insertThreat(threat)
            
            // Trigger emergency user notification alert
            triggerEmergencyNotification(threat, insertedId.toInt())
        }
    }

    @android.annotation.SuppressLint("NotificationPermission")
    private fun triggerEmergencyNotification(threat: ThreatAlert, threatId: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Deep link pending intent to open app and view threat details
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("THREAT_ID", threatId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            threatId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("🚨 EMERGENCY: ${threat.fraudType} Detected!")
            .setContentText("From ${threat.appName}: \"${threat.messageSnippet.take(50)}...\"")
            .setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle("🚨 Cyber Fraud Alert!")
                .bigText("We detected a ${threat.severity} risk in an incoming message from ${threat.appName}.\n\nMessage: \"${threat.messageSnippet}\"\n\n👉 Tap immediately to see step-by-step security actions!"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setOngoing(false)

        notificationManager.notify(threatId, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "CyberGuard Safety Alerts"
            val descriptionText = "Fires high priority notifications the moment a fraud pattern is detected."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
