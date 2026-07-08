package com.example.data

import android.util.Base64
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupEncryptor {

    private val SALT = byteArrayOf(
        0x43, 0x79, 0x62, 0x65,
        0x72, 0x47, 0x75, 0x61,
        0x72, 0x64, 0x53, 0x61,
        0x6C, 0x74, 0x32, 0x36
    ) // "CyberGuardSalt26"
    private const val ITERATION_COUNT = 1000
    private const val KEY_LENGTH = 256

    fun encrypt(plainText: String, password: CharArray): String {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec: KeySpec = PBEKeySpec(password, SALT, ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        val secret = SecretKeySpec(tmp.encoded, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secret)
        
        val iv = cipher.iv
        val encryptedTextBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        // Prepend IV to encrypted bytes so we can read it on decryption
        val combinedBytes = ByteArray(iv.size + encryptedTextBytes.size)
        System.arraycopy(iv, 0, combinedBytes, 0, iv.size)
        System.arraycopy(encryptedTextBytes, 0, combinedBytes, iv.size, encryptedTextBytes.size)

        return Base64.encodeToString(combinedBytes, Base64.NO_WRAP)
    }

    fun decrypt(encryptedBase64: String, password: CharArray): String {
        val combinedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)

        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec: KeySpec = PBEKeySpec(password, SALT, ITERATION_COUNT, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        val secret = SecretKeySpec(tmp.encoded, "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivSize = cipher.blockSize
        val iv = ByteArray(ivSize)
        System.arraycopy(combinedBytes, 0, iv, 0, ivSize)

        val encryptedTextBytes = ByteArray(combinedBytes.size - ivSize)
        System.arraycopy(combinedBytes, ivSize, encryptedTextBytes, 0, encryptedTextBytes.size)

        cipher.init(Cipher.DECRYPT_MODE, secret, IvParameterSpec(iv))
        val decryptedBytes = cipher.doFinal(encryptedTextBytes)

        return String(decryptedBytes, Charsets.UTF_8)
    }
}
