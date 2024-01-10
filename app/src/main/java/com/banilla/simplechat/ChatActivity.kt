package com.banilla.simplechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ChatActivity : AppCompatActivity() {
    lateinit var textViewMessages: TextView
    lateinit var textViewUser: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        textViewUser = findViewById(R.id.textViewUser)

        val currentUser = intent.getStringExtra("user")
        textViewUser.text = currentUser.toString().split("@")[1]
    }

    fun deauth(view: View){
        Firebase.auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        Toast.makeText(
            baseContext,
            "Log out successful!",
            Toast.LENGTH_SHORT,
        ).show()
        finish()
    }
}