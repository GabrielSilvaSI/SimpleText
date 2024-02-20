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

    fun createKey(alias: String): SecretKey {
        return KeyGenerator.getInstance(FERNET).apply {
            init(
                KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    fun getKey(alias: String): SecretKey {
        val existingKey = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey(alias)
    }

    fun getOrCreateKeyForChat(chatId: String): SecretKey {
        val alias = "${chatId}_key"

        return if (keyStore.containsAlias(alias)) {
            getKey(alias)
        } else {
            createKey(alias)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun encryptMessage(message: String, key: SecretKey): Pair<String, ByteArray> {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encryptedBytes: ByteArray = cipher.doFinal(message.toByteArray())
        return Pair(Base64.getEncoder().encodeToString(encryptedBytes), iv)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decryptMessage(encryptedMessage: String, key: SecretKey, iv: ByteArray): String {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        val decryptedBytes: ByteArray = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage))
        return String(decryptedBytes, charset("UTF-8"))
    }

    companion object{
        private const val FERNET = KeyProperties.KEY_ALGORITHM_AES
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val CIPHER_TRANSFORMATION = "AES/CBC/PKCS7Padding"
    }
}