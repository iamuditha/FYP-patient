package com.example.fyp_patient.challange_response

import ChallengeResponse.MessageObject
import ChallengeResponse.MessageSerializerHandler
import ChallengeResponse.MessageType
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import crypto.AsymmetricEncDec
import crypto.KeyHandler
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import java.security.PublicKey
import java.util.*
import kotlin.math.floor

class ChallengeResponse (private val did: String, private val id: String)  {

    private var isValidated : Boolean = false
    private var publicKeyString : String = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAj8SmQnY22Mfus5H1Vd6NqaJnVGVXgRncMdSWx1sVo8VQowTrTLz9VqOD9foorGDzIKnPxP7HC9kmuqTsKlOus2GcN8F01PqJVlvR2TGGDLAXdg9k2uokHEvnC5A56VVvSHgrpmloSyWc3VCRhFlzVW0LRYf9Ksp+NsPpoxrGM4S5VdVguzIurdoKNwpZIYlEgm+lzSQlJjc/H2zHC7TxGGjJe1zC/AgdiSMaw1M4QFX7yR3hTtJv+tmNGBIF7GCdjB+bHOIODhg+gdeW0Zk+1wlXHe1ZITfz/qe1Aq5Uh4G0RUb02D+hi7wGR10B0shCNWwKOVXRnsIPZ5Spmote7QIDAQAB"



    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun challengeResponse(){

        val opts = IO.Options()
        opts.query = "type=patient&&id=${id}"

        val challengeString:String = getRandomString()
        val publicKey:PublicKey = KeyHandler.getInstance().loadRSAPublicFromPlainText(publicKeyString)
        val encodedChallengeString : String = AsymmetricEncDec.getInstance().encryptString(challengeString, publicKey)
        val messageObject: MessageObject = MessageObject(MessageType.CHALLENGE, encodedChallengeString)

        val serializerHandler: MessageSerializerHandler? = MessageSerializerHandler.getInstance();
        var serializedMessageObjectA: String? = serializerHandler?.serialize(messageObject)
//        val keyGenerator: KeyGenerator = KeyGenerator.getInstance("DES");
//        val secureRandom: SecureRandom = SecureRandom()

//        keyGenerator.init(secureRandom)
//        var secretKey: SecretKey = keyGenerator.generateKey();

//        val cipher: Cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//        var challengeString1 = cipher.doFinal(challengeString!!.toByteArray())
//        val messageString: String = String(challengeString1, StandardCharsets.UTF_8);
//        val secretKeyString : String = String(secretKey.encoded, StandardCharsets.UTF_8)
        val jsonObject = serializedMessageObjectA?.let { createMessage(did, it) }
        Log.i("clg",jsonObject.toString())
//        val jsonObject = createMessage(did, messageString)

//        cipher.init(Cipher.DECRYPT_MODE, secretKey)
//
//        val dec = String(cipher.doFinal(challengeString1),StandardCharsets.UTF_8)
//
//        val obj2 = serializerHandler?.deserialize(challengeString)
//
//        val obj = serializerHandler?.deserialize(dec) as MessageObject
        val socket = IO.socket("https://d38ecdb4ac41.ngrok.io",opts)

        socket.on(Socket.EVENT_CONNECT, Emitter.Listener {
            socket.emit("sendTo",jsonObject)

        }).on("fromServer") { parameters ->

            val myJSON = parameters[0] as JSONObject
            val id = myJSON.get("id")
            val msg = myJSON.get("msg")

            val messageObject: MessageObject = MessageSerializerHandler.getInstance()
                ?.deserialize(msg as String) as MessageObject
            when (messageObject.messageType) {
                MessageType.CHALLENGE -> Log.i("message", "Invalid...........")
                MessageType.RESPONSE -> responseHandler(
                    socket,
                    messageObject.msg,
                    challengeString
                )
                MessageType.DECRYPTION_KEY -> Log.i("message", "Invalid...........")
                MessageType.TERMINATE -> Log.i("message", "Invalid...........")
                MessageType.PING -> ping(socket)
                MessageType.VALIDATION -> Log.i("message", "Invalid.........")
            }

        }
        socket.on(Socket.EVENT_DISCONNECT) { Log.i("msg", "asdfghjhgfds") }
        socket.connect()
    }


    private fun getRandomString(): String {
        val length = floor(Math.random()*10+20).toInt()
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
        val salt = StringBuilder()
        val rnd = Random()
        while (salt.length < length) { // length of the random string.
            val index = (rnd.nextFloat() * chars.length).toInt()
            salt.append(chars[index])
        }
        return salt.toString()
    }

    private fun responseHandler(socket: Socket,message: String, challenge: String) {
        Log.i("tag1","works")
        if (message.equals(challenge)){
            isValidated = true
            val decryptionKey = "decryption key"
            val publicKey:PublicKey = KeyHandler.getInstance().loadRSAPublicFromPlainText(publicKeyString)
            val encodedDecryptionKey : String = AsymmetricEncDec.getInstance().encryptString(decryptionKey, publicKey)

            socket.emit("sendTo", createMessage(did,
                MessageSerializerHandler.getInstance()?.serialize(MessageObject(MessageType.DECRYPTION_KEY,encodedDecryptionKey))!!
            ))
            Log.i("clg","response handler works")
        }else{
            socket.emit("sendTo", createMessage(did,
                MessageSerializerHandler.getInstance()?.serialize(MessageObject(MessageType.VALIDATION,"False"))!!
            ))
            Log.i("clg","response handler failed")
        }
    }

    private fun secretKeyHandler() {

    }

    private fun mTerminate(socket: Socket) {
        isValidated =false
        socket.emit("sendTo", createMessage(did,
            MessageSerializerHandler.getInstance()?.serialize(MessageObject(MessageType.TERMINATE,"terminate"))!!
        ))
        socket.disconnect()
    }

    private fun ping(socket: Socket) {
        Handler(Looper.getMainLooper()).postDelayed({
            socket.emit("sendTo", createMessage(did,
                MessageSerializerHandler.getInstance()?.serialize(MessageObject(MessageType.PING,"ping"))!!
            ))        }, 10*1000)
    }

    private fun createMessage(id:String,message: String): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("id",id)
            jsonObject.put("msg",message)
        }catch (e : JSONException){
            Log.i("message",e.printStackTrace().toString())
        }
        return jsonObject
    }
    private fun createMessage(id:String,message: String, secretKey:String): JSONObject {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("id",id)
            jsonObject.put("msg",message)
            jsonObject.put("secretKey",secretKey)
        }catch (e : JSONException){
            Log.i("message",e.printStackTrace().toString())
        }
        return jsonObject
    }



}