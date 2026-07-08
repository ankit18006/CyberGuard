package com.example

import com.example.data.BackupEncryptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackupEncryptionTest {

    @Test
    fun testEncryptionDecryption_Success() {
        val originalText = "{\"preferences\":{\"is_high_contrast\":true,\"emergency_contact_1_name\":\"Batman\"},\"threat_alerts\":[]}"
        val password = "MySecurePassword123".toCharArray()

        // 1. Encrypt
        val encryptedBase64 = BackupEncryptor.encrypt(originalText, password)
        assertNotEquals(originalText, encryptedBase64)
        
        // 2. Decrypt
        val decryptedText = BackupEncryptor.decrypt(encryptedBase64, password)
        assertEquals(originalText, decryptedText)
    }

    @Test
    fun testEncryptionDecryption_WrongPassword_Fails() {
        val originalText = "Sensitive security payload"
        val password = "CorrectPassword".toCharArray()
        val wrongPassword = "WrongPassword".toCharArray()

        // Encrypt with correct password
        val encryptedBase64 = BackupEncryptor.encrypt(originalText, password)

        // Attempt decryption with wrong password
        assertThrows(Exception::class.java) {
            BackupEncryptor.decrypt(encryptedBase64, wrongPassword)
        }
    }
}
