package com.banilla.simplechat

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.net.http.UrlRequest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telecom.Call
import android.util.Log
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

import java.security.KeyPair
import java.security.KeyStore

import com.google.android.gms.common.api.Response
import org.json.JSONObject

import okhttp3.*
import java.io.IOException
import okhttp3.Request


class ChatActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var dbRef: DatabaseReference
    private lateinit var dbMes: DatabaseReference
    private lateinit var storageImages: StorageReference
    private lateinit var userName: String
    private lateinit var inputTextMessage: EditText
    private lateinit var key: SecretKey
    private lateinit var keyPair: KeyPair
    private lateinit var userId: String
    private lateinit var lastName: String
    private lateinit var messageBox: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var cryptoManager: CryptoManager

    private lateinit var sharedSecret: ByteArray

    private lateinit var diffieHellmanHelper: DiffieHellmanHelper

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


        val keyString = getKeyString(key)
        Log.d("Chave", "Chave em string: $keyString")
        updateUser()
        updateMessages()
    }

    fun sendImage(view: View){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }


    fun getKeyString(key: SecretKey): String {
        return try {
            Base.encodeToString(key.encoded, Base.DEFAULT)
        } catch (e: Exception) {
            Log.e("Error", "Erro ao obter a string da chave: ${e.message}")
            ""
        }
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
                    val encryptedMessage = messageSnapshot.child("userMessage").getValue(String::class.java).toString()
                    val ivString = messageSnapshot.child("iv").getValue(String::class.java).toString()
                    print(ivString)
                    val ivByteArray = Base.decode(ivString, 1)
                    //val decryptedMessage = cryptoManager.decryptMessage(encryptedMessage, key, ivByteArray)
                    val tempImageUrl = messageSnapshot.child("userImage").getValue(String::class.java).toString()
                    buildMessages(tempName, encryptedMessage, tempImageUrl)
                }
                scrollView.post {
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
    fun buttonSendMessage(view: View) {
        val message = inputTextMessage.text.toString()
        //val (encryptedMessage, iv) = cryptoManager.encryptMessage(message, key)
        //val ivString = Base.encodeToString(iv, Base.DEFAULT)

        if (message.isNotBlank()) {
            val messageRef = dbMes.push()

            val hashMap: HashMap<String, Any> = HashMap()
            hashMap["userId"] = currentUser.uid
            hashMap["userName"] = userName
            hashMap["userMessage"] = message
            hashMap["userImage"] = ""
            //hashMap["iv"] = ivString

            messageRef.setValue(hashMap).addOnCompleteListener(this) { task ->
                if (!task.isSuccessful) {
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

    private fun getServerPublicKey() {
        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .url("http://127.0.0.1:5000/public-key")
            .build()

        client.newCall(request).enqueue(object : Callback {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseData = response.body?.string()
                val serverPublicKeyString = JSONObject(responseData).getString("public_key")

                // Continua com o cálculo do segredo compartilhado
                calculateSharedSecret(serverPublicKeyString)
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }
        })
    }


    private fun calculateSharedSecret(publicKey: String) {
        val client = OkHttpClient.Builder().build()
        val requestBody = FormBody.Builder()
            .add("public_key", publicKey)
            .build()

        val request = Request.Builder()
            .url("http://127.0.0.1:5000/shared-secret")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val jsonObject = JSONObject(responseData)
                    val sharedSecretString = jsonObject.getString("shared_secret")

                    // Converte o segredo compartilhado de Base64 para ByteArray
                    sharedSecret = Base64.getDecoder().decode(sharedSecretString)

                    // Aqui você pode prosseguir com a utilização do segredo compartilhado
                } else {
                    // Se a resposta não for bem-sucedida, imprima o código de erro
                    println("Erro na solicitação: ${response.code}")
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // Em caso de falha na solicitação, imprima o erro
                e.printStackTrace()
            }
        })
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun startKeyExchange() {
        getServerPublicKey()
    }



    fun closeChat(view: View){
        finish()
    }

}
