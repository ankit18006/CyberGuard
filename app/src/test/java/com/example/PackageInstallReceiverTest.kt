package com.example

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.ThreatDatabase
import com.example.receiver.PackageInstallReceiver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PackageInstallReceiverTest {

    private lateinit var context: Context
    private lateinit var database: ThreatDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = ThreatDatabase.getDatabase(context)
        runBlocking {
            database.threatDao().clearAllThreats()
        }
    }

    @Test
    fun testOnReceive_SafeAppInstalled_NoThreatSaved() = runBlocking {
        val shadowPackageManager = Shadows.shadowOf(context.packageManager)
        val packageInfo = PackageInfo().apply {
            packageName = "com.safe.calculator"
            applicationInfo = ApplicationInfo().apply {
                packageName = "com.safe.calculator"
                flags = 0
            }
        }
        shadowPackageManager.addPackage(packageInfo)

        val receiver = PackageInstallReceiver()
        val intent = Intent(Intent.ACTION_PACKAGE_ADDED).apply {
            data = Uri.parse("package:com.safe.calculator")
        }

        receiver.onReceive(context, intent)

        Thread.sleep(500)

        val threats = database.threatDao().getAllThreats().first()
        assertTrue(threats.isEmpty())
    }

    @Test
    fun testOnReceive_SuspiciousAppInstalled_ThreatSaved() = runBlocking {
        val shadowPackageManager = Shadows.shadowOf(context.packageManager)
        val packageInfo = PackageInfo().apply {
            packageName = "com.rustdesk.rustdesk"
            applicationInfo = ApplicationInfo().apply {
                packageName = "com.rustdesk.rustdesk"
                flags = 0
            }
        }
        shadowPackageManager.addPackage(packageInfo)

        val receiver = PackageInstallReceiver()
        val intent = Intent(Intent.ACTION_PACKAGE_ADDED).apply {
            data = Uri.parse("package:com.rustdesk.rustdesk")
        }

        receiver.onReceive(context, intent)

        Thread.sleep(500)

        val threats = database.threatDao().getAllThreats().first()
        assertEquals(1, threats.size)
        assertEquals("com.rustdesk.rustdesk", threats[0].packageName)
        assertEquals("Malware / Remote Access Tool", threats[0].fraudType)
        assertEquals("CRITICAL", threats[0].severity)
    }
}
