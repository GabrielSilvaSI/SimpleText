package com.banilla.simplechat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ChatActivity : AppCompatActivity() {
    lateinit var textViewMessages: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        textViewMessages = findViewById(R.id.textViewMessages)
        val currentUser = intent.getStringExtra("user")
        textViewMessages.text = currentUser.toString()
    }
}