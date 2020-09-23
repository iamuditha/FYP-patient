package com.example.fyp_patient

import android.os.Handler
import android.os.Looper
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.math.floor

class ChallengeResponse(private val did: String) {

    private var isValidated : Boolean = false

    fun challengeResponse(){

        val opts = IO.Options()
        opts.query = "type=patient"


        val challenge = getRandomString()?.let { MessageObject(MessageType.CHALLENGE, it) }!!
        val challengeString = MessageSerializerHandler.instance?.serialize(challenge)
        val jsonObject = challengeString?.let { createMessage(did, it) }
        val socket = IO.socket("http://10.10.7.52:8083",opts)

        socket.on(Socket.EVENT_CONNECT, Emitter.Listener {
            fun call(vararg objects: Any?) {
                socket.emit("sendTo",jsonObject)
            }

        }).on("fromServer", Emitter.Listener {
            fun call(vararg  objects:Any?) {
                val myJSON = objects[0] as JSONObject
                val id = myJSON.get("id")
                val msg = myJSON.get("msg")
                val messageObject : MessageObject = MessageSerializerHandler.instance?.deserialize(msg as String) as MessageObject
                when(messageObject.getType()){
                    MessageType.CHALLENGE -> Log.i("message","Invalid...........")
                    MessageType.RESPONSE -> responseHandler(messageObject.getMessage(),challenge.getMessage())
                    MessageType.SECRET_KEY -> secretKeyHandler()
                    MessageType.TERMINATE -> mTerminate(socket)
                    MessageType.PING -> ping(socket)
                }
            }
        })
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

    private fun responseHandler(message: String, challenge: String) {
        if (message.equals(challenge)){
            isValidated = true
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


}