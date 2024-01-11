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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


class ChatActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var dbRef: DatabaseReference
    private lateinit var dbMes: DatabaseReference
    private lateinit var userName: String
    private lateinit var textViewMessages: TextView
    private lateinit var textViewUser: TextView
    private lateinit var inputTextMessage: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = Firebase.auth
        currentUser = auth.currentUser!!
        dbMes = FirebaseDatabase.getInstance().getReference("Messages")

        textViewUser = findViewById(R.id.textViewUser)
        textViewMessages = findViewById(R.id.textViewMessages)
        inputTextMessage = findViewById(R.id.textInputMessage)

        val userId: String = currentUser.uid
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userName = dataSnapshot.child("userName").getValue(String::class.java).toString()
                textViewUser.text = userName
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // MENSAGEM DE ERRO
            }
        })
        dbMes.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val messagesBuilder = StringBuilder()

                for (messageSnapshot in dataSnapshot.children) {
                    val tempName = messageSnapshot.child("userName").getValue(String::class.java).toString()
                    val tempMessage = messageSnapshot.child("userMessage").getValue(String::class.java).toString()

                    messagesBuilder.append("$tempName: $tempMessage\n")
                }

                textViewMessages.text = messagesBuilder.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // MENSAGEM DE ERRO
            }
        })
    }

    fun buttonSendMessage(view: View){
        val message = inputTextMessage.text.toString()
        val userId: String = currentUser.uid
        val messageRef = dbMes.push()

        val hashMap: HashMap<String, String> = HashMap()
        hashMap.put("userId", userId)
        hashMap.put("userName", userName)
        hashMap.put("userMessage", message)

        messageRef.setValue(hashMap).addOnCompleteListener(this){
            if(!it.isSuccessful){
                Toast.makeText(
                    baseContext,
                    "Error, message not sent!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                inputTextMessage.text = null
            }
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

}