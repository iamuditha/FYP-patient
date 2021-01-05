package com.example.fyp_patient.drive

import com.google.api.services.drive.model.File

object DriveFileList {

    private val uploadedFileList = ArrayList<File>()

    fun addFile(file: File): ArrayList<File> {
        uploadedFileList.add(file)
        return uploadedFileList
    }

    fun driveFileList(): ArrayList<File> {
        return uploadedFileList
    }

    fun isFileAvailable(fileName : String): Boolean {
        var isFileAvailable = false
        for (file in uploadedFileList){
            if(file.name == fileName){
                isFileAvailable = true
                break
            }
        }
        return isFileAvailable
    }
}