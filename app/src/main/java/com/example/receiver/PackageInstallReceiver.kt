package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.ThreatAlert
import com.example.data.ThreatDatabase
import com.example.service.MalwareScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PackageInstallReceiver : BroadcastReceiver() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    companion object {
        private const val TAG = "PackageInstallReceiver"
        private const val CHANNEL_ID = "cyberguard_install_alerts"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "Received system broadcast action: $action")
        
        if (action == Intent.ACTION_PACKAGE_ADDED || action == Intent.ACTION_PACKAGE_REPLACED) {
            val packageName = intent.data?.schemeSpecificPart ?: return
            Log.d(TAG, "New app package detected: $packageName")
            
            // Skip our own package
            if (packageName == context.packageName) return
            
            val pm = context.packageManager
            try {
                val appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                val suspiciousApp = MalwareScanner.analyzeSingleApp(pm, appInfo)
                
                if (suspiciousApp != null) {
                    Log.e(TAG, "SUSPICIOUS APP DETECTED VIA SYSTEM EVENT: ${suspiciousApp.appName} (${suspiciousApp.packageName})")
                    
                    val threat = ThreatAlert(
                        appName = suspiciousApp.appName,
                        packageName = suspiciousApp.packageName,
                        title = "Suspicious App Installed",
                        messageSnippet = "App: ${suspiciousApp.appName} (${suspiciousApp.packageName}) - Reason: ${suspiciousApp.reason}",
                        fraudType = when (suspiciousApp.severity) {
                            "CRITICAL" -> "Malware / Remote Access Tool"
                            "DANGEROUS" -> "Unverified App / Potential Fraud"
                            else -> "Suspicious Behavior App"
                        },
                        severity = suspiciousApp.severity,
                        stepsToTake = "1. UNINSTALL ${suspiciousApp.appName} immediately to secure your device.\n2. Do NOT open any banking or UPI apps while this application remains installed.\n3. Run a full scan in CyberGuard to verify if other dangerous apps exist."
                    )
                    
                    saveThreatAndAlertUser(context, threat)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing package $packageName: ${e.message}")
            }
        }
    }

    private fun saveThreatAndAlertUser(context: Context, threat: ThreatAlert) {
        scope.launch {
            val database = ThreatDatabase.getDatabase(context.applicationContext)
            val dao = database.threatDao()
            val insertedId = dao.insertThreat(threat)
            
            triggerEmergencyNotification(context, threat, insertedId.toInt())
        }
    }

    @android.annotation.SuppressLint("NotificationPermission")
    private fun triggerEmergencyNotification(context: Context, threat: ThreatAlert, threatId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(context, notificationManager)
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("THREAT_ID", threatId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            threatId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("🚨 MALWARE ALERT: ${threat.appName}")
            .setContentText("Detected as high risk: ${threat.fraudType}")
            .setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle("🚨 Dangerous Application Installed!")
                .bigText("We detected a ${threat.severity} threat in the newly installed app '${threat.appName}'.\n\nReason: ${threat.messageSnippet}\n\n👉 Tap immediately to see step-by-step security actions to protect your device!"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(threatId, builder.build())
    }

    private fun createNotificationChannel(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "CyberGuard App Security Alerts"
            val descriptionText = "Fires high priority notifications the moment a suspicious app is installed or updated."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
