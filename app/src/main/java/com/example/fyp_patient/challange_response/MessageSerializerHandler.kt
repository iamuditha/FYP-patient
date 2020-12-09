package com.example.fyp_patient.challange_response

import java.io.*
import java.util.*

class MessageSerializerHandler private constructor() {
    @Throws(IOException::class)
    fun serialize(msg: Any?): String {
        val bo = ByteArrayOutputStream()
        val so = ObjectOutputStream(bo)
        so.writeObject(msg)
        so.flush()
        return String(Base64.getEncoder().encode(bo.toByteArray()))
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    fun deserialize(serializedMsg: String): Any {
        val b: ByteArray = Base64.getDecoder().decode(serializedMsg.toByteArray())
        val bi = ByteArrayInputStream(b)
        val si = ObjectInputStream(bi)
        return si.readObject()
    }

    companion object {
        var instance: MessageSerializerHandler? = null
            get() {
                if (field == null) {
                    synchronized(MessageSerializerHandler::class.java) {
                        if (field == null) {
                            field = MessageSerializerHandler()
                        }
                    }
                }
                return field
            }
            private set
    }
}