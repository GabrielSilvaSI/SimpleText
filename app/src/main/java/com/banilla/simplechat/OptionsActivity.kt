package com.banilla.simplechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

class OptionsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        auth = Firebase.auth
    }

    fun deleteAccount(view: View){

        val builder = AlertDialog.Builder(this)

        builder.setTitle("Delete account")
        builder.setMessage("Are you sure you want to delete account?")

        builder.setPositiveButton("Delete") { dialog, which ->
            val user = Firebase.auth.currentUser!!
            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            baseContext,
                            "User account deleted",
                            Toast.LENGTH_SHORT,
                        ).show()
                        Firebase.auth.signOut()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(
                            baseContext,
                            "Account delete failed",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            Toast.makeText(
                baseContext,
                "Action canceled",
                Toast.LENGTH_SHORT,
            ).show()
        }

        val dialog = builder.create()
        dialog.show()
    }

    fun changePassword(view: View){
        Firebase.auth.sendPasswordResetEmail(auth.currentUser!!.email!!)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        baseContext,
                        "Change password email sent",
                        Toast.LENGTH_SHORT,
                    ).show()
                    Firebase.auth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
    }

    fun backToChats(view: View){
        finish()
    }

    fun deauth(view: View){
        Firebase.auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        Toast.makeText(
            baseContext,
            "Log out successful",
            Toast.LENGTH_SHORT,
        ).show()
        startActivity(intent)
        finish()
    }
}