package com.example.fyp_patient.camera_old

import android.net.Uri
import android.util.Log

object ImageURIHolder {

    private val uriArrayList = ArrayList<Uri>()

    fun addUri(uri: Uri): ArrayList<Uri> {
        uriArrayList.add(uri)
        Log.i("full","item added")
        return uriArrayList
    }

    fun uriArrayList(): ArrayList<Uri> {
        return uriArrayList
    }
}