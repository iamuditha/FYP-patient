package com.example.fyp_patient

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class DriveServiceHelper(private val mDriveService: Drive) {
    private val mExecutor: Executor =
        Executors.newSingleThreadExecutor()
    private val TAG = "DRIVE_TAG"

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    fun createFile(
        folderId: String?,
        filename: String?
    ): Task<GoogleDriveFileHolder> {
        return Tasks.call<GoogleDriveFileHolder>(mExecutor,
            Callable<GoogleDriveFileHolder> {
                val googleDriveFileHolder = GoogleDriveFileHolder()
                val root: List<String>
                root = folderId?.let { listOf(it) } ?: listOf("root")
                val metadata =
                    File()
                        .setParents(root)
                        .setMimeType("text/plain")
                        .setName(filename)
                val googleFile =
                    mDriveService.files().create(metadata).execute()
                        ?: throw IOException("Null result when requesting file creation.")
                googleDriveFileHolder.id=(googleFile.id)
                googleDriveFileHolder
            }
        )
    }

    // TO CREATE A FOLDER
    fun createFolder(
        folderName: String?,
        folderId: String?
    ): Task<GoogleDriveFileHolder> {
        return Tasks.call<GoogleDriveFileHolder>(mExecutor,
            Callable<GoogleDriveFileHolder> {
                val googleDriveFileHolder = GoogleDriveFileHolder()
                val root: List<String>
                root = folderId?.let { listOf(it) } ?: listOf("root")
                val metadata =
                    File()
                        .setParents(root)
                        .setMimeType("application/vnd.google-apps.folder")
                        .setName(folderName)
                val googleFile =
                    mDriveService.files().create(metadata).execute()
                        ?: throw IOException("Null result when requesting file creation.")
                googleDriveFileHolder.id=(googleFile.id)
                googleDriveFileHolder
            }
        )
    }

//    fun downloadFile(
//        targetFile: java.io.File?,
//        fileId: String?
//    ): Task<Void?> {
//        return Tasks.call(
//            mExecutor,
//            Callable {
//
//
//                // Retrieve the metadata as a File object.
//                val outputStream: OutputStream = FileOutputStream(targetFile)
//                mDriveService.files()[fileId].executeMediaAndDownloadTo(outputStream)
//                null
//            }
//        )
//    }

//    fun deleteFolderFile(fileId: String?): Task<Void?> {
//        return Tasks.call(
//            mExecutor,
//            Callable {
//
//
//                // Retrieve the metadata as a File object.
//                if (fileId != null) {
//                    mDriveService.files().delete(fileId).execute()
//                }
//                null
//            }
//        )
//    }

    // TO LIST FILES
    @Throws(IOException::class)
    fun listDriveImageFiles(): List<File> {
        var result: FileList
        var pageToken: String? = null
        do {
            result = mDriveService.files()
                .list() /*.setQ("mimeType='image/png' or mimeType='text/plain'")This si to list both image and text files. Mind the type of image(png or jpeg).setQ("mimeType='image/png' or mimeType='text/plain'") */
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name)")
                .setPageToken(pageToken)
                .execute()
            pageToken = result.nextPageToken
        } while (pageToken != null)
        return result.files
    }

    // TO UPLOAD A FILE ONTO DRIVE
    fun uploadFile(
        localFile: java.io.File,
        mimeType: String?, folderId: String?
    ): Task<Any>? {
        return Tasks.call(mExecutor, Callable<Any> { // Retrieve the metadata as a File object.
            val root: List<String>
            root = folderId?.let { listOf(it) } ?: listOf("root")
            val metadata =
                File()
                    .setParents(root)
                    .setMimeType(mimeType)
                    .setName(localFile.name)
            val fileContent = FileContent(mimeType, localFile)
            val fileMeta =
                mDriveService.files().create(
                    metadata,
                    fileContent
                ).execute()
            val googleDriveFileHolder = GoogleDriveFileHolder()
            googleDriveFileHolder.id=(fileMeta.id)
            googleDriveFileHolder.name=(fileMeta.name)
            googleDriveFileHolder
        })
    }
//
//    private val mExecutor: Executor =
//        Executors.newSingleThreadExecutor()
//    private var mDriveService: Drive? = null


    fun createFilePdf(filePath: String?): Task<String>? {
        return Tasks.call(mExecutor, Callable {
            val fileMetaData =
                File()
            fileMetaData.name = "MyPDFFile"
            val file = java.io.File(filePath)
            val mediaContent = FileContent("image/jpeg", file)
            var myFile: File? = null
            try {
                myFile = mDriveService.files().create(fileMetaData, mediaContent).execute()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("myissue", e.message)
            }
            if (myFile == null) {
                throw IOException("Null result when requesting file creation")
            }
            myFile.id
        })
    }

}