package com.example.fyp_patient.camera_old


import android.app.Activity
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.SparseArray
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.fyp_patient.*
import com.example.fyp_patient.OCR.OcrCaptureActivity
import com.example.fyp_patient.drive.DriveFileList
import com.example.fyp_patient.drive.DriveServiceHelper
import com.example.fyp_patient.signIn.SignInActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.navigation.NavigationView
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_imagerecycleview.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class CameraImageRecycleViewActivity : BaseActivity(), MenuItem.OnMenuItemClickListener,
    NavigationView.OnNavigationItemSelectedListener {

    var isInActionMode = false

    private var driveServiceHelper: DriveServiceHelper? = null
    private var RC_AUTHORIZE_DRIVE = 101
    private val REQUEST_IMAGE_CAPTURE: Int = 100
    private val REQUEST_IMAGE_SELECT: Int = 104

    private var image_uri: Uri? = null
    private val IMAGE_CAPTURE_CODE: Int = 103
    private val IMAGE_SELECT_CODE: Int = 102
    private var ACCESS_DRIVE_SCOPE = Scope(Scopes.DRIVE_FILE)
    private var SCOPE_EMAIL = Scope(Scopes.EMAIL)
    var SCOPE_APP_DATA = Scope(Scopes.DRIVE_APPFOLDER)

    lateinit var googleDriveService: Drive
    var mDriveServiceHelper: DriveServiceHelper? = null
    var adapter = CameraImagesAdapter(ImageHolder.imageArrayList(), this)

    private var selection_list = ArrayList<CameraImagesModel>()
    var counter = 0

    var gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build()
    var uploading: AlertDialog? = null

    lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imagerecycleview)

        progressDialog = displayLoading(this,"File is Uploading... Please Wait......")


        //toolbar and drawer setup
        (R.id.toolbar_main)
        setSupportActionBar(toolbar_main)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

//        verifyWithBlockChain()


        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar_main,
            R.string.open,
            R.string.close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setHomeButtonEnabled(true)

        val prefs: SharedPreferences = getSharedPreferences("PROFILE_DATA", MODE_PRIVATE)
        val name: String? = prefs.getString("name", "No name defined")
        val email: String? = prefs.getString("email", "no email")
        val url: String? = prefs.getString("url", "no url")

        val navigationView: NavigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView: View = navigationView.getHeaderView(0)
        val navUsername = headerView.findViewById(R.id.doctorName) as TextView
        val navUserEmail = headerView.findViewById(R.id.doctorEmail) as TextView
        val navUserImage = headerView.findViewById(R.id.doctorImage) as ImageView

        navUsername.text = name
        navUserEmail.text = email
        Glide.with(this).load(url).apply(RequestOptions.circleCropTransform()).into(navUserImage)

        val intent = intent
        val bundle = intent.extras
        if (bundle != null) {
            val uri = bundle.get("ocrImageURI")
            ImageHolder.addImage(CameraImagesModel("title", getCurrentDate(), uri as Uri))
            updateView()
        }

        updateView()

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        GoogleSignIn.getClient(this, gso)
        checkForGooglePermissions()


        openCamera.setOnClickListener {
            val ocrIntent = Intent(this, OcrCaptureActivity::class.java)
            startActivity(ocrIntent)
        }

        openGallery.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        navigationView.setNavigationItemSelectedListener(this)


    }

    //display loading dialog
    private fun displayLoading(context: Context, message: String): ProgressDialog {
        val progress = ProgressDialog(context)
        progress.setMessage(message)
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progress.isIndeterminate = true
        progress.setCancelable(false)
        return progress
    }

    fun uploadImages(position: Int) {
//        val thread1 = Thread {
//            listFilesInDrive()
//        }
//        thread1.start()

//        val thread2 = Thread {
//            thread1.join()
//            if (DriveFileList.isFileAvailable("finalmedfol")) {
//                Log.i("uploadingImages", DriveFileList.getFolderId("finalmedfol")!!)
//                val folderId = DriveFileList.getFolderId("finalmedfol")
//                uploadImageIntoDrive(position, folderId!!)
//            } else {
//                createFolderInDrive("finalmedfol", position)
//            }
//        }
//        thread2.start()

    }

    fun createFolderInDrive(folderName: String, position: Int) {
        mDriveServiceHelper?.createFolder(folderName, null)
            ?.addOnSuccessListener(OnSuccessListener<Any> { googleDriveFileHolder ->
                uploadImages(position)
                Log.i(
                    "creatingfol",
                    "Successfully Uploaded. File Id :$googleDriveFileHolder"
                )
            })
            ?.addOnFailureListener { e ->
                Log.i(
                    "creatingfol",
                    "Failed to Upload. File Id :" + e.message
                )
            }
    }

//    private fun verifyWithBlockChain() {
//
//        val thread = Thread {
//            val web3j: Web3j = Web3j.build(HttpService("https://c4375655a390.ngrok.io"))
////            val web3j : Web3j = EthFunctions.connect("https://c4375655a390.ngrok.io")
//            val credentials = WalletUtils.loadBip39Credentials(
//                "123456",
//                UUID.randomUUID().toString()
//            )
//            val iamContract = IAMContractorHandler.getInstance().getWrapperForContractor(
//                web3j, getString(
//                    R.string.main_contractor_address
//                ), credentials
//            )
//            val doctorDetails = IAMContractorHandler.getInstance().getDoctorDetails(
//                iamContract,
//                "did:medico:zDAHazRBj7KcVFHRtwdEimmpyZoq9TAQGELM="
//            )
//
//            val hash = IAMContractorHandler.getInstance().getDoctorVerifyingDetails(
//                iamContract,
//                "did:medico:zDAHazRBj7KcVFHRtwdEimmpyZoq9TAQGELM="
//            )
//            val mydidHash = hash[0]
//            val didLink = doctorDetails[0]
//            val vcLink = doctorDetails[0]
//            Log.i("blockChainInVerifier", doctorDetails.size.toString())
//            Log.i("blockChain", vcLink)
//            Log.i("blockChain", didLink)
//            Log.i("blockChain", mydidHash)
//
//            val mystrign =
//                "{\"created\":\"Jan 22, 2021 14:59:38\",\"id\":\"did:medico:zDAHazRBj7KcVFHRtwdEimmpyZoq9TAQGELM=\",\"publicKeys\":[{\"controller\":\"did:medico:zDAHazRBj7KcVFHRtwdEimmpyZoq9TAQGELM=\",\"id\":\"pubKeyzicbmenbnhhhymmtjnjtegjnfmlqqxwbnylcdjcjjokgdgbdnhfqnccvzpmehkzh\",\"publicKey\":\"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0BllnxNkAsxy8CCOfvKkYD+EFcmgh/v1EquAglRWyVQodsXfA1iHiNDqgonPcep+u/1Vgi7kyckou1QjeJoHIOKMENufNGmDxp30Bw9A3Pj+c/kIZkQceCGRiD2YNyS3LQ3IycilRdyNZ1aetamX8pM5jLgfLu6EPIQw9OxcLjjHvCk1GAKPy5xc7wUG7qIINyhFM6Gn7+1XE9OzFJH3I7hElvsOc90CVo4zjBoSIQTiFQW9o7ReOVyxXU9bRlUglswUe5Z74CSUbi82Dc94UM1Hf4IpOYlRBw/SBzcDDgCgQhEnnCv4sJkEQBv8kRwQ0ik4DTLV1XlvXItGmLpPawIDAQAB\",\"type\":\"RSA\"}],\"service\":[{\"endPoint\":\"-\",\"id\":\"did:medico:zDAHazRBj7KcVFHRtwdEimmpyZoq9TAQGELM=#medico\",\"publicKeyId\":\"pubKeyzicbmenbnhhhymmtjnjtegjnfmlqqxwbnylcdjcjjokgdgbdnhfqnccvzpmehkzh\",\"serviceName\":\"medico\",\"type\":\"MedicalDataSharingApplication\"}],\"updated\":\"Jan 22, 2021 14:59:38\"}"
//
//            val messageDigestf = MessageDigest.getInstance("sha-512")
//            val h = Base64.getEncoder().encodeToString(mystrign.toByteArray())
//            Log.i("blockChain", h)
//
//
//            val input: InputStream = URL(didLink).openStream()
//            val imageBytes: ByteArray =
//                com.google.android.gms.common.util.IOUtils.toByteArray(input)
//
//            try {
//                val textBuilder = StringBuilder()
//                BufferedReader(
//                    InputStreamReader(
//                        input,
//                        Charset.forName(StandardCharsets.UTF_8.name())
//                    )
//                ).use { reader ->
//                    var c = 0
//                    while (reader.read().also { c = it } != -1) {
//                        textBuilder.append(c.toChar())
//                        Log.i("blockChain", "result " + c.toChar())
//
//                    }
//                }
//            } catch (e: Exception) {
//                Log.i("blockChain", "result " + e.toString())
//
//            }
//
//
//            val obj = Base64.getEncoder().encodeToString(imageBytes)
////            val newobj = String(Base64.getDecoder().decode(imageBytes))
//            Log.i("blockChain", "obj : " + obj)
////            Log.i("blockChain", "newobj : " + newobj)
//
//            val anotheobh = String(imageBytes)
//            Log.i("blockChain", "anotherobh : " + anotheobh)
//
//
//            val messageDigest: MessageDigest = MessageDigest.getInstance("sha-512")
//            val didHash =
//                Base64.getEncoder().encodeToString(messageDigest.digest(anotheobh.toByteArray()))
//
//
//
//            Log.i("blockChain", didHash)
//
//            Log.i("blockChain", (mydidHash == didHash).toString())
//
//
////            didLink.saveTo(filesDir.absolutePath+"/DoctorDid.json")
////            vcLink.saveTo(filesDir.absolutePath+"/DoctorVc.json")
//
//        }
//        thread.start()
//
//
//    }

//    //check the permissions and open the camera
//    private fun checkPermissionAndOpenCamera() {
//        //if the system is marshmallow or above get the run time permission
//        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
//            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
//        ) {
//            //permission was not enabled
//            val permission = arrayOf(
//                android.Manifest.permission.CAMERA,
//                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
//            )
//            //show popup to request permission
//            requestPermissions(permission, REQUEST_IMAGE_CAPTURE)
//        } else {
//            //permission already granted
//            openCamera()
//        }
//    }

    //check the permissions and open the gallery
    private fun checkPermissionAndOpenGallery() {
        //if the system is marshmallow or above get the run time permission
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            //permission was not enabled
            val permission = arrayOf(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            //show popup to request permission
            requestPermissions(permission, REQUEST_IMAGE_SELECT)
        } else {
            //permission already granted
            openGallery()
        }
    }

    //open the camera and capture the image
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "this is an images")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        //camera Intent
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }

    //open gallery and select the image
    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Pictures: "), IMAGE_SELECT_CODE)
    }

    //call when user clicks on the permission request dialog
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //called when user allow or deny from permission request
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from pop up was granted
                    openCamera()
                } else {
                    //permission from pop up was denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_IMAGE_SELECT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from pop up was granted
                    openGallery()
                } else {
                    //permission from pop up was denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //called when image is captured from camera intent
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == Activity.RESULT_OK) {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, image_uri)
            readText(bitmap)
            //set the image to the image view
            ImageURIHolder.addUri(image_uri!!)
            ImageHolder.addImage(CameraImagesModel("title", getCurrentDate(), image_uri!!))
            Log.i("check123", image_uri.toString())
            updateView()
        }

        //call when image is selected from the gallery
        if (requestCode == IMAGE_SELECT_CODE && resultCode == Activity.RESULT_OK) {
            if (data!!.clipData != null) {
                val count = data.clipData!!.itemCount
                var i = 0
                while (i < count) {
                    val uri = data.clipData!!.getItemAt(i).uri
                    ImageURIHolder.addUri(uri!!)
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    readText(bitmap)
                    ImageHolder.addImage(CameraImagesModel("title", getCurrentDate(), uri))
                    i++
                }
            } else if (data.data != null) {
                val uri = data.data
                ImageURIHolder.addUri(uri!!)
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                readText(bitmap)
                ImageHolder.addImage(CameraImagesModel("title", getCurrentDate(), uri))
            }

            updateView()

        }
    }

    //update the recycle view when new items are added
    private fun updateView() {
        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.adapter = adapter
        if (ImageHolder.imageArrayList().size != 0) {
            imageDisplayWhenEmpty.visibility = View.GONE
            emptyTextView.visibility = View.GONE
        } else {
            imageDisplayWhenEmpty.visibility = View.VISIBLE
            emptyTextView.visibility = View.VISIBLE
        }
    }

    //list the files in the drive
    private fun listFilesInDrive() {

        val thread = Thread(Runnable {
            try {
                if (mDriveServiceHelper == null) {
                    checkForGooglePermissions()
                }
                val thread1 = Thread {
                    val fileList123456 = mDriveServiceHelper?.listDriveImageFiles()
                    if (fileList123456 != null) {
                        for (i in fileList123456) {
                            DriveFileList.addFile(i)
                        }
                    }
                }
                thread1.start()
                thread1.join()

                for (i in DriveFileList.driveFileList()) {
                    Log.i("myFileList", i.toString())
                }
                val check = DriveFileList.isFileAvailable("MyPDFFile")
                Log.i("myFileList", check.toString())


            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        thread.start()


    }

    //uploading an image ti the drive
     fun uploadImageIntoDrive(position: Int) {
        progressDialog!!.show()

        val tag = "imageUploading"
        val bitmap = MediaStore.Images.Media.getBitmap(
            this.contentResolver,
            ImageHolder.imageArrayList()[position].uri
        )

        try {
            if (bitmap == null) {
                Log.i(tag, "Bitmap is null")
                return
            }
            val file = File(
                applicationContext.filesDir, UUID.randomUUID().toString().substring(0, 5)
            )
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
            val bitmapData = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapData)
            fos.flush()
            fos.close()
            val compressedImageFile = Compressor(this).compressToFile(file);
            val inputStream = FileInputStream(compressedImageFile)
            val encryptedFile = EncryptAndDecrypt().encryptFile(inputStream, "123456789")
            mDriveServiceHelper?.uploadFile(
                compressedImageFile,
                "application/octet-stream",
                null
            )
                ?.addOnSuccessListener(OnSuccessListener<Any> { googleDriveFileHolder ->
                    removeItem(position)
                    Log.i(tag, "Successfully Uploaded. File Id :$googleDriveFileHolder")
                    progressDialog!!.dismiss()
                })
                ?.addOnFailureListener { e ->
                    Log.i(
                        tag,
                        "Failed to Upload. File Id :" + e.message
                    )
                    progressDialog!!.dismiss()
                }
        } catch (e: Exception) {
            Log.i(tag, "Exception : " + e.message)
        }
    }

    private fun getPath(uri: Uri): String? {
        val projection =
            arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor = contentResolver.query(uri, projection, null, null, null)!!
        val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
    }


    private fun checkForGooglePermissions() {
        if (!GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(applicationContext),
                ACCESS_DRIVE_SCOPE,
                SCOPE_EMAIL,
                SCOPE_APP_DATA
            )
        ) {
            GoogleSignIn.requestPermissions(
                this,
                RC_AUTHORIZE_DRIVE,
                GoogleSignIn.getLastSignedInAccount(applicationContext),
                ACCESS_DRIVE_SCOPE,
                SCOPE_EMAIL,
                SCOPE_APP_DATA
            )
        } else {
            Toast.makeText(
                this,
                "Permission to access Drive and Email has been granted",
                Toast.LENGTH_SHORT
            ).show()
            driveSetUp()
        }
    }

    private fun driveSetUp() {
        val mAccount =
            GoogleSignIn.getLastSignedInAccount(this)
        val credential = GoogleAccountCredential.usingOAuth2(
            applicationContext, setOf(Scopes.DRIVE_FILE)
        )
        credential.selectedAccount = mAccount!!.account
        googleDriveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("GoogleDriveIntegration 3")
            .build()
        mDriveServiceHelper = DriveServiceHelper(googleDriveService)
    }

//    override fun onLongClick(view: View?): Boolean {
//        if (toolbar == null) {
//            Log.i("check", "toolbar is null")
//        }
//        toolbar.menu.clear()
//        toolbar.inflateMenu(R.menu.menu_action_mode)
//        counter_text.visibility = View.VISIBLE
//        isInActionMode = true
//        adapter.notifyDataSetChanged()
//        supportActionBar?.setDisplayShowHomeEnabled(true)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        return true
//    }

//    fun prepareSelection(view: View?, position: Int?) {
//        if ((view as CheckBox).isChecked) {
//            selection_list.add(ImageHolder.imageArrayList()[position!!])
//            Toast.makeText(applicationContext, position.toString(), Toast.LENGTH_SHORT).show()
//            counter += 1
//            updateCounter(counter)
//        } else {
//            selection_list.remove(ImageHolder.imageArrayList()[position!!])
//            Toast.makeText(applicationContext, position.toString(), Toast.LENGTH_SHORT).show()
//            counter -= 1
//            updateCounter(counter)
//        }
//
//    }

//    private fun updateCounter(counter: Int) {
//        if (counter == 0) {
//            counter_text.text = "0 items are selected"
//        } else {
//            counter_text.text = "$counter items selected"
//        }
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.item_delete) {
//            adapter.updateAdapter(selection_list)
//            clearActionMode()
//        } else if (item.itemId == android.R.id.home) {
//            clearActionMode()
//            adapter.notifyDataSetChanged()
//        }
//        return true
//    }
//
//    private fun clearActionMode() {
//        isInActionMode = false
//        toolbar.menu.clear()
//        toolbar.inflateMenu(R.menu.main)
//        supportActionBar?.setDisplayHomeAsUpEnabled(false)
//        for (cameraImagesModel: CameraImagesModel in selection_list) {
//            getPath(cameraImagesModel.uri)?.let { deleteImage(it) }
//        }
//        counter_text.visibility = View.GONE
//        counter_text.text = "0 items selected"
//        checkbox.isChecked = false
//        counter = 0
//        selection_list.clear()
//    }

    override fun onBackPressed() {
        if (isInActionMode) {
//            clearActionMode()
            adapter.notifyDataSetChanged()
        } else {
            super.onBackPressed()
        }
    }

//    private fun deleteImage(path: String) {
//        val delete = File(path)
//        if (delete.exists()) {
//            if (delete.delete()) {
//                println("file Deleted :$path")
//            } else {
//                println("file not Deleted :$path")
//            }
//        }
//    }

    private fun getCurrentDate(): String {
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        return df.format(c)
    }

    private fun readText(imageBitmap: Bitmap) {
        val singleWordArrayList = java.util.ArrayList<String>()
        val doubleWordArrayList = java.util.ArrayList<String>()
        val tripleWordArrayList = java.util.ArrayList<String>()


        val LOG_TAG = "detectedText"
        // imageBitmap is the Bitmap image you're trying to process for text
        if (imageBitmap != null) {
            val textRecognizer: TextRecognizer = TextRecognizer.Builder(applicationContext).build()
            if (!textRecognizer.isOperational) {
                Log.w(LOG_TAG, "Detector dependencies are not yet available.")

                // Check for low storage.  If there is low storage, the native library will not be
                // downloaded, so detection will not become operational.
                val lowstorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
                val hasLowStorage = registerReceiver(null, lowstorageFilter) != null
                if (hasLowStorage) {
                    Toast.makeText(this, "Low Storage", Toast.LENGTH_LONG).show()
                    Log.w(LOG_TAG, "Low Storage")
                }
            }
            val imageFrame = Frame.Builder()
                .setBitmap(imageBitmap)
                .build()

            val textBlocks: SparseArray<TextBlock> = textRecognizer.detect(imageFrame)
            for (i in 0 until textBlocks.size()) {
                val item: TextBlock = textBlocks.valueAt(i)
                if (item.value != null) {
                    if (item.value != null) {
//                        Log.d(LOG_TAG, "Text detected! " + item.value)
//                        Log.d(LOG_TAG, "Text detected! " + item.value.length)
                        for (word in item.value.split(" ".toRegex()).toTypedArray()) {
                            Log.d(LOG_TAG, "Text detected! $word")

                            singleWordArrayList.add(word)
                        }

                        for (j in 0 until singleWordArrayList.size - 2) {
                            doubleWordArrayList.add(singleWordArrayList[j] + " " + singleWordArrayList[j + 1])
                            tripleWordArrayList.add(singleWordArrayList[j] + " " + singleWordArrayList[j + 1] + " " + singleWordArrayList[j + 2])
                        }
//                        doubleWordArrayList.add(singleWordArrayList[singleWordArrayList.size-2]+ " " + singleWordArrayList[singleWordArrayList.size-1])
                    }
                }

            }

        }
        val match = findMatchingWords(
            singleWordArrayList,
            doubleWordArrayList,
            tripleWordArrayList
        )
//         Log.i("matchingWord",match)
    }

    private fun findMatchingWords(
        singleList: java.util.ArrayList<String>,
        doubleList: java.util.ArrayList<String>,
        tripleList: java.util.ArrayList<String>
    ): String? {
        for (word in tripleList) {
            if (testData.threeWord.containsKey(word)) {
//                Log.i("tagName",word)
                return word
            }
        }
        for (word in doubleList) {
            if (testData.twoWords.containsKey(word)) {
                return word
            }
        }
        for (word in singleList) {
            if (testData.oneWord.containsKey(word)) {
                return word
            }
        }

        for (word in tripleList) {
            for (key in testData.threeWord.keys) {
                if (key.length <= word.length + 2 && key.length >= word.length - 2) {
                    val hLength = upgradedHammingDist(word, word.length, key, key.length)
                    if (hLength <= 2) {
                        return key
                    }
                }
            }
        }
        for (word in doubleList) {
            for (key in testData.twoWords.keys) {
                if (key.length <= word.length + 2 && key.length >= word.length - 2) {
                    val hLength = upgradedHammingDist(word, word.length, key, key.length)
                    if (hLength <= 2) {
                        return key
                    }
                }
            }
        }
        for (word in singleList) {
            for (key in testData.oneWord.keys) {
                if (key.length <= word.length + 2 && key.length >= word.length - 2) {
                    val hLength = upgradedHammingDist(word, word.length, key, key.length)
                    if (hLength <= 2) {
                        return key
                    }
                }
            }
        }

        return null
    }


//    fun checklist(){
//        val singleWordArrayList = java.util.ArrayList<String>()
//        val doubleWordArrayList = java.util.ArrayList<String>()
//        val tripleWordArrayList = java.util.ArrayList<String>()
//        singleWordArrayList.add("hello")
//        singleWordArrayList.add("world")
//        singleWordArrayList.add("i")
//        singleWordArrayList.add("am")
//        singleWordArrayList.add("good")
//        singleWordArrayList.add("how")
//        singleWordArrayList.add("are")
//        singleWordArrayList.add("hello")
//
//        for (i in 0 until singleWordArrayList.size-2){
//            doubleWordArrayList.add(singleWordArrayList[i] + " " + singleWordArrayList[i+1])
//            tripleWordArrayList.add(singleWordArrayList[i] + " " + singleWordArrayList[i+1] + " " +singleWordArrayList[i+2])
//        }
//        doubleWordArrayList.add(singleWordArrayList[singleWordArrayList.size-2]+ " " + singleWordArrayList[singleWordArrayList.size-1])
//
//        for (word in tripleWordArrayList){
//            Log.i("listWords",word)
//        }
//    }


//    fun toGrayscale(bmpOriginal: Bitmap): Bitmap? {
//        val height: Int = bmpOriginal.height
//        val width: Int = bmpOriginal.width
//        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//        val c = Canvas(bmpGrayscale)
//        val paint = Paint()
//        val cm = ColorMatrix()
//        cm.setSaturation(0F)
//        val f = ColorMatrixColorFilter(cm)
//        paint.colorFilter = f
//        c.drawBitmap(bmpOriginal, 0F, 0F, paint)
//        return bmpGrayscale
//    }


//    fun tapped(position: Int) {
//        val intent = Intent(applicationContext, FullScreenImageActivity::class.java)
//        intent.putExtra("position", position)
//        startActivity(intent)
//
//    }

//    fun detectRotation(bitmap: Bitmap, uri: Uri) {
//
//        val photoPath = getPath(uri)
//        val ei = ExifInterface(photoPath)
//        val orientation: Int = ei.getAttributeInt(
//            ExifInterface.TAG_ORIENTATION,
//            ExifInterface.ORIENTATION_UNDEFINED
//        )
//
//        var rotatedBitmap: Bitmap? = null
//        when (orientation) {
//            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
//            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
//            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
//            ExifInterface.ORIENTATION_NORMAL -> bitmap
//        }
//    }

//    private fun rotateImage(source: Bitmap, angle: Int): Bitmap? {
//        val matrix = Matrix()
//        matrix.postRotate(angle.toFloat())
//        return Bitmap.createBitmap(
//            source, 0, 0, source.width, source.height,
//            matrix, true
//        )
//    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        Log.i("drawerFunctions", "clicked")
        when (item!!.itemId) {
            R.id.logout1 -> {
                Toast.makeText(this, "Logout Successfully here", Toast.LENGTH_SHORT).show()
                Log.i("drawerFunctions", "I am clicked")
                return true
            }

        }
        return true
    }

    fun removeItem(position: Int) {
        ImageHolder.removeImage(position)
        ImageURIHolder.removeUri(position)
        updateView()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            (R.id.logout1) -> {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build()
                GoogleSignIn.getClient(this, gso).signOut()
                    .addOnCompleteListener(this, OnCompleteListener<Void?> {
                        val intent = Intent(applicationContext, SignInActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(this, "Logout Successfully", Toast.LENGTH_SHORT).show()
                    }).addOnFailureListener {
                        Toast.makeText(this, "Issue with Logout", Toast.LENGTH_SHORT).show()
                    }
                Toast.makeText(this, "I am clicked", Toast.LENGTH_SHORT).show()
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.camera -> {
                val intent = Intent(this, OcrCaptureActivity::class.java)
                startActivity(intent)
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.gallery -> {
                checkPermissionAndOpenGallery()
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            R.id.barcode -> {
                val intent = Intent(this, BarCodeReaderActivity::class.java)
                startActivity(intent)
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
            else -> {
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
            }
        }

    }

    private fun upgradedHammingDist(str1: String, str1_length: Int, str2: String, str2_length: Int): Int {
        var i = 0
        var j = 0
        var count = 0
        var direction = 1
        var backwardIndex1 = str1_length - 1
        var backwardIndex2 = str2_length - 1
        var forwardIndex1 = 0
        var forwardIndex2 = 0
        while (i < str1_length && j < str2_length) {
            if (direction == 1) {
                direction = if (str1[forwardIndex1] != str2[forwardIndex2]) {
                    count++
                    -1
                } else {
                    1
                }
                forwardIndex1 += 1
                forwardIndex2 += 1
            } else {
                direction = if (str1[backwardIndex1] != str2[backwardIndex2]) {
                    count++
                    1
                } else {
                    -1
                }
                backwardIndex1 -= 1
                backwardIndex2 -= 1
            }
            i++
            j++
        }
        return count + abs((str1_length - str2_length) / 2)
    }


}
