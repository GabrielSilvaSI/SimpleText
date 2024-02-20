package com.banilla.simplechat

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CryptoManager {

    private val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    fun createKey(): SecretKey{
        return KeyGenerator.getInstance(FERNET).apply {
            init(
                KeyGenParameterSpec.Builder("secret", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    fun getKey(): SecretKey{
        val existingKey = keyStore.getEntry("secret" , null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encryptMessage(message: String, key: SecretKey): String {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes: ByteArray = cipher.doFinal(message.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decryptMessage(encryptedMessage: String, key: SecretKey): String {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decryptedBytes: ByteArray = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage))
        return String(decryptedBytes, charset("UTF-8"))
    }

    companion object{
        private const val FERNET = KeyProperties.KEY_ALGORITHM_AES
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding"
    }
}