package com.banilla.simplechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var textInputEmail: EditText
    private lateinit var textInputPassword: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        textInputEmail = findViewById(R.id.editTextEmailLogin)
        textInputPassword = findViewById(R.id.editTextPasswordLogin)
        startChat()
    }

    fun buttonLogin(view: View){
        val tempEmail = textInputEmail.text.toString()
        val tempPassword = textInputPassword.text.toString()
        if(TextUtils.isEmpty(tempEmail)){
            Toast.makeText(applicationContext, "A email is required", Toast.LENGTH_SHORT).show()
        }else if(TextUtils.isEmpty(tempPassword)){
            Toast.makeText(applicationContext, "A password is required", Toast.LENGTH_SHORT).show()
        }else{
            loginAuth(tempEmail, tempPassword)
        }
    }

    private fun loginAuth(tempEmail: String, tempPassword: String){
        if(tempEmail!="" && tempPassword!="") {
            auth.signInWithEmailAndPassword(tempEmail, tempPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(
                            baseContext,
                            "Login successful!",
                            Toast.LENGTH_SHORT,
                        ).show()
                        startChat()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
                        textInputPassword.setText(null)
                    }
                }
        }
    }

    private fun startChat() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, ChatsActivity::class.java)
            intent.putExtra("user", currentUser)
            startActivity(intent)
            finish()
        }
    }

    fun buttonCreateAccount(view: View){
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
        finish()
    }
}