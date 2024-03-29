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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var textInputUser: EditText
    private lateinit var textInputEmail: EditText
    private lateinit var textInputPassword: EditText
    private lateinit var textInputConfirmPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = Firebase.auth

        textInputUser = findViewById(R.id.editTextUser)
        textInputEmail = findViewById(R.id.editTextEmail)
        textInputPassword = findViewById(R.id.editTextPassword)
        textInputConfirmPassword = findViewById(R.id.editTextConfirmPassword)
    }

    public override fun onStart() {
        super.onStart()
    }

    fun buttonSignup(view: View){
        val tempUser = textInputUser.text.toString()
        val tempEmail = textInputEmail.text.toString()
        val tempPassword = textInputPassword.text.toString()
        val tempConfirmPassword = textInputConfirmPassword.text.toString()
        if(TextUtils.isEmpty(tempUser)){
            Toast.makeText(applicationContext, "A username is required", Toast.LENGTH_SHORT).show()
        }else
        if(TextUtils.isEmpty(tempEmail)){
            Toast.makeText(applicationContext, "A email is required", Toast.LENGTH_SHORT).show()
        }else
        if(TextUtils.isEmpty(tempPassword)){
            Toast.makeText(applicationContext, "A password is required", Toast.LENGTH_SHORT).show()
        }else
        if(TextUtils.isEmpty(tempConfirmPassword)){
            Toast.makeText(applicationContext, "A confirm password is required", Toast.LENGTH_SHORT).show()
        }else
        if(tempPassword != tempConfirmPassword){
            Toast.makeText(applicationContext, "Passwords not match", Toast.LENGTH_SHORT).show()
        }else{
            signupAuth(tempUser, tempEmail, tempPassword)
        }

    }

    private fun signupAuth(tempUser: String,tempEmail: String,tempPassword: String){
        auth.createUserWithEmailAndPassword(tempEmail, tempPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user: FirebaseUser? = auth.currentUser
                    val userId: String = user!!.uid
                    dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                    val hashMap: HashMap<String,String> = HashMap()
                    hashMap.put("userId",userId)
                    hashMap.put("userName",tempUser)
                    hashMap.put("userEmail",tempEmail)
                    dbRef.setValue(hashMap).addOnCompleteListener(this){
                        if(it.isSuccessful){
                            val user = auth.currentUser
                            user!!.sendEmailVerification()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            baseContext,
                                            "Confirmation email sent",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                        val intent = Intent(this, LoginActivity::class.java)
                                        startActivity(intent)
                                        auth.signOut()
                                        finish()
                                    }
                                }
                        }
                    }
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

    fun buttonLoginAccount(view: View){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}