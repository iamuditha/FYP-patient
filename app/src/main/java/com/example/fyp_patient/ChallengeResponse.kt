package com.example.fyp_patient

import android.database.StaleDataException
import android.os.Handler
import android.os.Looper
import android.util.Log
import crypto.AsymmetricEncDec
import crypto.KeyHandler
import crypto.PublicPrivateKeyPairGenerator
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.PublicKey
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor

class ChallengeResponse(private val did: String, private val id: String
                        ) {

    private var isValidated : Boolean = false
    private var publicKeyString : String = "-----BEGIN RSA PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAj8SmQnY22Mfus5H1Vd6NqaJnVGVXgRncMdSWx1sVo8VQowTrTLz9VqOD9foorGDzIKnPxP7HC9kmuqTsKlOus2GcN8F01PqJVlvR2TGGDLAXdg9k2uokHEvnC5A56VVvSHgrpmloSyWc3VCRhFlzVW0LRYf9Ksp+NsPpoxrGM4S5VdVguzIurdoKNwpZIYlEgm+lzSQlJjc/H2zHC7TxGGjJe1zC/AgdiSMaw1M4QFX7yR3hTtJv+tmNGBIF7GCdjB+bHOIODhg+gdeW0Zk+1wlXHe1ZITfz/qe1Aq5Uh4G0RUb02D+hi7wGR10B0shCNWwKOVXRnsIPZ5Spmote7QIDAQAB------END RSA PUBLIC KEY-----"



    fun challengeResponse(){

        val opts = IO.Options()
        opts.query = "type=patient&&id=${id}"


        val challenge = getRandomString()?.let { MessageObject(MessageType.CHALLENGE, it) }!!
        Log.i("clg", challenge.getMessage() + " my message")
        val serializerHandler: MessageSerializerHandler? = MessageSerializerHandler.instance;
        var challengeString = serializerHandler?.serialize(challenge)
        val keyGenerator: KeyGenerator = KeyGenerator.getInstance("DES");
        val secureRandom: SecureRandom = SecureRandom()
        Log.i("clg",  " my message1")
        keyGenerator.init(secureRandom)
        var secretKey: SecretKey = keyGenerator.generateKey();

        val cipher: Cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        var challengeString1 = cipher.doFinal(challengeString!!.toByteArray())
        val messageString: String = String(challengeString1, StandardCharsets.UTF_8);
        val secretKeyString : String = String(secretKey.encoded, StandardCharsets.UTF_8)
        val publicKey:PublicKey = KeyHandler.getInstance().loadRSAPublicFromPlainText(publicKeyString)
        val encodedSecretKeyString : String = AsymmetricEncDec.getInstance().encryptString(secretKeyString, publicKey)
        val jsonObject = createMessage(did, messageString,encodedSecretKeyString)
//        val jsonObject = createMessage(did, messageString)

//        cipher.init(Cipher.DECRYPT_MODE, secretKey)
//
//        val dec = String(cipher.doFinal(challengeString1),StandardCharsets.UTF_8)
//
//        val obj2 = serializerHandler?.deserialize(challengeString)
//
//        val obj = serializerHandler?.deserialize(dec) as MessageObject
        val socket = IO.socket("https://fe071c124e4b.ngrok.io",opts)

        socket.on(Socket.EVENT_CONNECT, Emitter.Listener {
                socket.emit("sendTo",jsonObject)

        }).on("fromServer", Emitter.Listener {
            fun call(vararg  objects:Any?) {
                val myJSON = objects[0] as JSONObject
                val id = myJSON.get("id")
                val msg = myJSON.get("msg")

                val messageObject : MessageObject = MessageSerializerHandler.instance?.deserialize(msg as String) as MessageObject
                when(messageObject.getType()){
                    MessageType.CHALLENGE -> Log.i("message","Invalid...........")
                    MessageType.RESPONSE -> responseHandler(socket,messageObject.getMessage(),challenge.getMessage())
                    MessageType.DECRYPTION_KEY -> Log.i("message","Invalid...........")
                    MessageType.TERMINATE -> Log.i("message","Invalid...........")
                    MessageType.PING -> ping(socket)
                    MessageType.VALIDATION -> Log.i("message", "Invalid.........")
                }
            }
        })
        socket.on(Socket.EVENT_DISCONNECT) { Log.i("msg", "asdfghjhgfds") }
        socket.connect()
    }


    private fun getRandomString(): String? {
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
        if (message.equals(challenge)){
            isValidated = true
            socket.emit("sendTo", createMessage(did,
                MessageSerializerHandler.instance?.serialize(MessageObject(MessageType.VALIDATION,"True"))!!
            ))
            Log.i("did","response handler works")
        }else{
            socket.emit("sendTo", createMessage(did,
                MessageSerializerHandler.instance?.serialize(MessageObject(MessageType.VALIDATION,"False"))!!
            ))
        }
    }

    private fun secretKeyHandler() {

    }

    private fun mTerminate(socket: Socket) {
        isValidated =false
        socket.emit("sendTo", createMessage(did,
            MessageSerializerHandler.instance?.serialize(MessageObject(MessageType.TERMINATE,"terminate"))!!
        ))
        socket.disconnect()
    }

    private fun ping(socket: Socket) {
        Handler(Looper.getMainLooper()).postDelayed({
            socket.emit("sendTo", createMessage(did,
                MessageSerializerHandler.instance?.serialize(MessageObject(MessageType.PING,"ping"))!!
            ))        }, 5*60*1000)
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