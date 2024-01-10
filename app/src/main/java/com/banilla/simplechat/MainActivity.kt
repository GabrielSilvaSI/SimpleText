package com.banilla.simplechat

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var textInputEmail: EditText
    private lateinit var textInputPassword: EditText
    private lateinit var textViewStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        textInputEmail = findViewById(R.id.editTextEmail)
        textInputPassword = findViewById(R.id.editTextPassword)
        textViewStatus = findViewById(R.id.textViewStatus)
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        startChat()
    }

    fun signinAuth(view: View){
        val tempUser = textInputEmail.text.toString()
        val tempPassword = textInputPassword.text.toString()
        if(tempUser!="" && tempPassword!=""){
            auth.createUserWithEmailAndPassword(tempUser, tempPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        textInputEmail.setText("")
                        textInputPassword.setText("")
                        startChat()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        textViewStatus.text = "The email account is already registered!"
                        textViewStatus.isVisible = true
                        textInputPassword.setText("")
                    }
                }
        }
    }

    fun loginAuth(view: View){
        val tempUser = textInputEmail.text.toString()
        val tempPassword = textInputPassword.text.toString()
        if(tempUser!="" && tempPassword!="") {
            auth.signInWithEmailAndPassword(tempUser, tempPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        startChat()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        textViewStatus.text = "Failed to log in. Nonexistent account or wrong password!"
                        textViewStatus.isVisible = true
                        textInputPassword.setText("")
                    }
                }
        }
    }

    private fun startChat() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("user", currentUser.toString())
            startActivity(intent)
            Toast.makeText(
                baseContext,
                "Login successful!",
                Toast.LENGTH_SHORT,
            ).show()
            finish()

        }
    }
}