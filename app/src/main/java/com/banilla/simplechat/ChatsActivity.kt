package com.banilla.simplechat

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.*

class ChatsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var editTextContact: EditText
    private lateinit var chatList: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        auth = Firebase.auth
        database = Firebase.database.reference

        editTextContact = findViewById(R.id.editTextContactEmail)
        chatList = findViewById(R.id.chatList)

        listChats()
    }

    private fun listChats() {
        database = FirebaseDatabase.getInstance().getReference("Users").child(auth.currentUser!!.uid).child("userChats")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chatList.removeAllViews()
                for (messageSnapshot in dataSnapshot.children) {
                    val chatId = messageSnapshot.getValue(String::class.java).toString()

                    // Obter o email do usuário com quem o usuário atual está conversando
                    val chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("participants")
                    chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(participantsSnapshot: DataSnapshot) {
                            val currentUser = auth.currentUser
                            val currentUserId = currentUser?.uid
                            var otherUserId = ""
                            for (participantSnapshot in participantsSnapshot.children) {
                                val userId = participantSnapshot.key
                                if (userId != currentUserId) {
                                    otherUserId = userId ?: ""
                                    break
                                }
                            }

                            // Obter o email do usuário com o ID obtido
                            val otherUserRef = FirebaseDatabase.getInstance().getReference("Users").child(otherUserId)
                            otherUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val otherUserEmail = userSnapshot.child("userEmail").getValue(String::class.java)

                                    // Criar e configurar o botão com o email do outro usuário
                                    val chatButton = Button(this@ChatsActivity)
                                    chatButton.text = otherUserEmail
                                    chatButton.id = View.generateViewId()

                                    chatButton.setOnClickListener {
                                        val intent = Intent(this@ChatsActivity, ChatActivity::class.java)
                                        intent.putExtra("chatId", chatId)
                                        startActivity(intent)
                                    }

                                    chatList.addView(chatButton)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Tratar erro de leitura
                                }
                            })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Tratar erro de leitura
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // MENSAGEM DE ERRO
            }
        })
    }


    fun createChat(view: View) {
        val currentUser = auth.currentUser
        database = Firebase.database.reference
        val email = editTextContact.text.toString().trim()
        if (currentUser != null) {

            val currentUserId = currentUser.uid
            val usersRef = database.child("Users")
            usersRef.orderByChild("userEmail").equalTo(email).get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val userKey = dataSnapshot.children.first().key // Obtém a chave do usuário
                    if (userKey != null) {
                        val chatRef = database.child("chats").push()
                        chatRef.child("participants").child(currentUserId).setValue(true)
                        chatRef.child("participants").child(userKey).setValue(true)

                        val currentUserChatRef = database.child("Users").child(currentUserId).child("userChats").push()
                        currentUserChatRef.setValue(chatRef.key)

                        val otherUserChatRef = database.child("Users").child(userKey).child("userChats").push()
                        otherUserChatRef.setValue(chatRef.key)

                        editTextContact.text.clear()
                        Toast.makeText(this, "New chat created", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Server operation fail", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun options(view: View){
        val intent = Intent(this, OptionsActivity::class.java)
        startActivity(intent)
    }
}