package com.banilla.simplechat

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.*
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import android.util.Base64 as Base
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.storage
import javax.crypto.spec.SecretKeySpec
import com.bumptech.glide.Glide

class ChatActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var dbRef: DatabaseReference
    private lateinit var dbMes: DatabaseReference
    private lateinit var storageImages: StorageReference
    private lateinit var userName: String
    private lateinit var inputTextMessage: EditText
    private lateinit var key: SecretKey
    private lateinit var userId: String
    private lateinit var lastName: String
    private lateinit var messageBox: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var cryptoManager: CryptoManager
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageUri: Uri? = null
    private var chatId: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = Firebase.auth
        currentUser = auth.currentUser!!

        cryptoManager = CryptoManager()

        // Obter o ID do chat passado pelo Intent
        chatId = intent.getStringExtra("chatId")

        // Criar a referência do banco de dados usando o ID do chat
        dbMes = FirebaseDatabase.getInstance().getReference("chats").child(chatId ?: "").child("Messages")

        inputTextMessage = findViewById(R.id.editTextMessage)
        messageBox = findViewById(R.id.messagesBox)
        scrollView = findViewById(R.id.scrollView)
        userId= currentUser.uid

        storageImages = Firebase.storage.getReference().child("images")

        selectedImageUri = null

        key = cryptoManager.createKey()

        updateUser()
        updateMessages()
    }

    fun sendImage(view: View){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            // Aqui você pode lidar com a Uri selecionada, como fazer o upload para o Firebase Storage
            uploadImage()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadImage() {
        if (selectedImageUri != null) {
            val imageRef = storageImages.child("${System.currentTimeMillis()}.jpg")
            val uploadTask: UploadTask = imageRef.putFile(selectedImageUri!!)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                // Agora, obtemos a URL da imagem corretamente
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl: String = uri.toString()

                    // Enviamos a URL da imagem para o banco de dados junto com outras informações
                    sendImageMessage(imageUrl)
                }.addOnFailureListener {
                    Toast.makeText(baseContext, "Erro ao obter a URL da imagem", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(baseContext, "Erro no upload da imagem", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendImageMessage(imageUrl: String) {
        val message = inputTextMessage.text.toString()
        val userId: String = currentUser.uid
        val messageRef = dbMes.push()

        val hashMap: HashMap<String, String> = HashMap()
        hashMap["userId"] = userId
        hashMap["userName"] = userName
        hashMap["userMessage"] = message
        hashMap["userImage"] = imageUrl

        messageRef.setValue(hashMap).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                inputTextMessage.text = null
                Toast.makeText(
                    baseContext,
                    "Imagem enviada com sucesso!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    baseContext,
                    "Erro, imagem não enviada!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Limpar a seleção da imagem após o envio
            selectedImageUri = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateMessages() {
        dbMes.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                messageBox.removeAllViews()
                lastName = ""
                for (messageSnapshot in dataSnapshot.children) {
                    val tempName = messageSnapshot.child("userName").getValue(String::class.java).toString()
                    val tempMessage = messageSnapshot.child("userMessage").getValue(String::class.java).toString()
                    val tempImageUrl = messageSnapshot.child("userImage").getValue(String::class.java).toString()
                    buildMessages(tempName, tempMessage, tempImageUrl)
                }
                scrollView.post{
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                    inputTextMessage.restoreDefaultFocus()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // MENSAGEM DE ERRO
            }
        })
    }

    private fun buildMessages(name: String, message: String, image: String) {
        val contextMessageBox = ContextThemeWrapper(this, R.style.message_style)
        val messageLayout = LinearLayout(contextMessageBox)
        messageLayout.orientation = LinearLayout.VERTICAL

        if (name != lastName) {
            val nameTextView = TextView(this)
            nameTextView.text = name
            nameTextView.setTextColor(ContextCompat.getColor(this, R.color.blue))
            nameTextView.setTypeface(null, Typeface.BOLD)
            lastName = name
            messageLayout.addView(nameTextView)
        }

        if (!image.isNullOrEmpty()) {
            val imageView = ImageView(this)
            Glide.with(this)
                .load(image)
                .into(imageView)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 4, 0, 4)
            val displayMetrics = resources.displayMetrics
            params.width = (displayMetrics.widthPixels * 0.5).toInt()
            imageView.layoutParams = params
            messageLayout.addView(imageView)
        }

        if (!message.isNullOrEmpty()) {
            val messageTextView = TextView(this)
            messageTextView.text = message
            messageTextView.setTextColor(ContextCompat.getColor(this, R.color.gray_dark))

            messageLayout.addView(messageTextView)
        }
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 4, 0, 4)
        messageLayout.layoutParams = params
        messageBox.addView(messageLayout)
    }


    private fun updateUser() {
        dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userName = dataSnapshot.child("userName").getValue(String::class.java).toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                // MENSAGEM DE ERRO
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun buttonSendMessage(view: View){
        val message = inputTextMessage.text.toString()
        if(message.isNotBlank()){
            val messageRef = dbMes.push()

            val hashMap: HashMap<String, String> = HashMap()
            hashMap.put("userId", currentUser.uid)
            hashMap.put("userName", userName)
            hashMap.put("userMessage", cryptoManager.encryptMessage(message, key))
            hashMap.put("userImage", "")

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
            selectedImageUri = null
        }

    }

    fun closeChat(view: View){
        finish()
    }

}
