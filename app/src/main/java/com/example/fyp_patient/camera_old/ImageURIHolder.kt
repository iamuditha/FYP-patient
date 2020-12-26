package com.example.fyp_patient.camera_old

import android.net.Uri

object ImageURIHolder {

    private val uriArrayList = ArrayList<Uri>()

    fun addUri(uri: Uri): ArrayList<Uri> {
        uriArrayList.add(uri)
        return uriArrayList
    }

    fun uriArrayList(): ArrayList<Uri> {
        return uriArrayList
    }
}