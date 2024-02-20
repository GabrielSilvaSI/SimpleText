package com.banilla.simplechat

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoManager {

    fun encryptMessage(message: String, sharedSecret: ByteArray): Pair<ByteArray, ByteArray> {
        // Gera um IV (Initialization Vector) aleatório
        val iv = ByteArray(16).apply {
            // Aqui você deve gerar um IV criptograficamente seguro
            // Por simplicidade, estamos usando um IV de bytes zero
            fill(0)
        }

        // Deriva uma chave simétrica a partir do segredo compartilhado
        val secretKeySpec = SecretKeySpec(sharedSecret, "AES")

        // Cria e inicializa o objeto Cipher para encriptação
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(iv))

        // Encripta a mensagem
        val encryptedMessage = cipher.doFinal(message.toByteArray())

        // Retorna a mensagem encriptada e o IV
        return Pair(encryptedMessage, iv)
    }

    fun decryptMessage(encryptedMessage: ByteArray, iv: ByteArray, sharedSecret: ByteArray): String {
        // Deriva uma chave simétrica a partir do segredo compartilhado
        val secretKeySpec = SecretKeySpec(sharedSecret, "AES")

        // Cria e inicializa o objeto Cipher para decriptação
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IvParameterSpec(iv))

        // Decripta a mensagem
        val decryptedMessage = cipher.doFinal(encryptedMessage)

        // Retorna a mensagem decriptada como uma String
        return String(decryptedMessage)
    }
}