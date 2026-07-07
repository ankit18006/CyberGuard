package com.example.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.util.Log
import com.example.service.CyberGuardListenerService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("CyberGuardBootReceiver", "Reboot completed. Rebinding CyberGuardListenerService...")
            
            // Rebind the notification listener service on boot to ensure real-time protection is up
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    val componentName = ComponentName(context, CyberGuardListenerService::class.java)
                    NotificationListenerService.requestRebind(componentName)
                    Log.d("CyberGuardBootReceiver", "Rebind requested successfully.")
                } catch (e: Exception) {
                    Log.e("CyberGuardBootReceiver", "Error requesting rebind: ${e.message}")
                }
            }
        }
    }
}
