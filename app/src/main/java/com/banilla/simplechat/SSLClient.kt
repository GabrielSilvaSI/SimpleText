
package com.banilla.simplechat

import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.BufferedOutputStream
import java.io.OutputStreamWriter
import java.net.Socket
import javax.net.ssl.SSLSocketFactory

class SSLClient(private val serverAddress: String, private val port: Int) {

    fun sendPublicKey(publicKeyString: String, callback: (Boolean) -> Unit) {
        AsyncTask.execute {
            try {
                // Configura a conexão SSL/TLS
                val sslSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
                val sslSocket = sslSocketFactory.createSocket(serverAddress, port)

                // Envia a publicKeyString através da conexão
                val outputStream = BufferedOutputStream(sslSocket.getOutputStream())
                val outputStreamWriter = OutputStreamWriter(outputStream)
                outputStreamWriter.write(publicKeyString)
                outputStreamWriter.flush()

                // Fecha a conexão
                sslSocket.close()

                callback(true) // Callback de sucesso
            } catch (e: Exception) {
                Log.e("SSLClient", "Erro ao enviar a chave pública: ${e.message}")
                callback(false) // Callback de falha
            }
        }
    }
}
