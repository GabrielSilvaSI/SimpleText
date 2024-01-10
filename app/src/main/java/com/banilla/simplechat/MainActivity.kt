package com.banilla.simplechat

import android.content.ContentValues.TAG
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
import com.google.firebase.auth.FirebaseUser
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

    fun signinAuth(view: View){
        auth.createUserWithEmailAndPassword(textInputEmail.text.toString(), textInputPassword.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    textViewStatus.text = "New user registered!"
                    textViewStatus.isVisible = true
                    textInputEmail.setText("")
                    textInputPassword.setText("")
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