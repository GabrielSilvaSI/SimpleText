package com.banilla.simplechat

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class ChatActivity : AppCompatActivity() {
    private lateinit var textViewMessages: TextInputEditText
    private lateinit var textViewUser: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        textViewUser = findViewById(R.id.textViewUser)
        textViewMessages = findViewById(R.id.textInputMessage)

        val currentUser = intent.getStringExtra("user")
        textViewUser.text = currentUser.toString().split("@")[1]

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun btSendAction(view: View) {
        try {
            // Generate a secret key
            val key: SecretKey = generateKey()

            // Get input from the user
            val userInput = textViewMessages.text.toString()

            // Encrypt the input
            val encryptedInput: String = encryptMessage(userInput, key)

            val currentUser = intent.getStringExtra("user")
            val databaseReference = Firebase.database.reference.child("messages")
            val messageData = MessageData(currentUser, encryptedInput)


            databaseReference.push().setValue(messageData)

            addMessage("Encrypted message: $encryptedInput")

            // Decrypt the input
            val decryptedInput: String = decryptMessage(encryptedInput, key)
            addMessage("Decrypted message: $decryptedInput")



        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deauth(view: View){
        Firebase.auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        Toast.makeText(
            baseContext,
            "Log out successful!",
            Toast.LENGTH_SHORT,
        ).show()
        startActivity(intent)
        finish()
    }

    @Throws(Exception::class)
    private fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        return keyGenerator.generateKey()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(Exception::class)
    private fun encryptMessage(message: String, key: SecretKey): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes: ByteArray = cipher.doFinal(message.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(Exception::class)
    private fun decryptMessage(encryptedMessage: String, key: SecretKey): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decryptedBytes: ByteArray = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage))
        return String(decryptedBytes, charset("UTF-8"))
    }

    private fun addMessage(text : String){
        textViewMessages.setText("")
        textViewUser.text = buildString {
            append(textViewUser.text.toString())
            append(text)
            append("\n")
        }

    }
}