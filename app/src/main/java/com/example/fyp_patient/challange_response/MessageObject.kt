package com.example.fyp_patient.challange_response

import java.io.Serializable
import java.security.SecureRandom


class MessageObject(private val type: MessageType, private val msg: String) : Serializable {
    var salt: String = "";
    init{
        salt = SecureRandom().toString()
    }
    fun getType(): MessageType {
        return type
    }

    fun getMessage(): String {
        return msg
    }

}