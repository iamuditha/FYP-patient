package com.example.fyp_patient.camera_old


import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.SparseArray
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
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
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CameraImageRecycleViewActivity : BaseActivity(), MenuItem.OnMenuItemClickListener,
    NavigationView.OnNavigationItemSelectedListener {

    var isInActionMode = false

    //    private val uriArrayList = ArrayList<Uri>()
//    val arrayList = ArrayList<CameraImagesModel>()
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

    var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imagerecycleview)


        //toolbar and drawer setup
        (R.id.toolbar_main)
        setSupportActionBar(toolbar_main)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

//    checklist()
        val checklength=upgradedHammingDist("iver Profile","iver Profile".length,"Lipid Profile","Lipid Profile".length)

        Log.i("distance", checklength.toString())
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

        val checkTestList = dataRepos.testNamesList()
        if (checkTestList.contains("cTnI")){
            Log.i("myocrtest", "found an item")
        }

        listFilesInDrive()

//        mDriveServiceHelper?.createFolder("MEDICO", null)
//            ?.addOnSuccessListener(OnSuccessListener<Any> { googleDriveFileHolder ->
//                progressDialog!!.dismiss()
//                Log.i(
//                    "creatingfol",
//                    "Successfully Uploaded. File Id :$googleDriveFileHolder"
//                )
//            })
//            ?.addOnFailureListener { e ->
//                progressDialog!!.dismiss()
//                Log.i(
//                    "creatingfol",
//                    "Failed to Upload. File Id :" + e.message
//                )
//            }
    }

    //check the permissions and open the camera
    private fun checkPermissionAndOpenCamera() {
        //if the system is marshmallow or above get the run time permission
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
        ) {
            //permission was not enabled
            val permission = arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            //show popup to request permission
            requestPermissions(permission, REQUEST_IMAGE_CAPTURE)
        } else {
            //permission already granted
            openCamera()
        }
    }

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
        if (data!!.clipData != null){
            val count = data.clipData!!.itemCount
            var i = 0;
            while (i < count) {
                val uri = data.clipData!!.getItemAt(i).uri
                ImageURIHolder.addUri(uri!!)
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                readText(bitmap)
                ImageHolder.addImage(CameraImagesModel("title", getCurrentDate(), uri))
                i++
            }
        }else if (data.data != null){
            val uri = data?.data
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
        if(ImageHolder.imageArrayList().size != 0){
            imageDisplayWhenEmpty.visibility = View.GONE
            emptyTextView.visibility = View.GONE
        }else{
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
                    Log.i("myfilelist", i.toString())
                }
                val check = DriveFileList.isFileAvailable("MyPDFFile")
                Log.i("myfilelist", check.toString())


            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        thread.start()



    }

    //
    fun uploadImageIntoDrive(position: Int) {
        val thread1 = Thread {
            runOnUiThread {
                uploading_animation_cover.visibility = View.VISIBLE
                uploading_animation.visibility = View.VISIBLE
            }
        }
        thread1.start()
        thread1.join()

        val TAG = "image upload"
        val bitmap = MediaStore.Images.Media.getBitmap(
            this.contentResolver,
            ImageHolder.imageArrayList()[position].uri
        )

        try {
            if (bitmap == null) {
                Log.i(TAG, "Bitmap is null")
                return
            }
            val file = File(
                applicationContext.filesDir, UUID.randomUUID().toString().substring(
                    0,
                    5
                )
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
            val encryptedFile = EncryptAndDecrypt().encryptFile(inputStream)
            mDriveServiceHelper?.uploadFile(
                compressedImageFile,
                "application/octet-stream",
                "1l_mz2QPAAO-GkrgRaEfnO454qjOFIhiN"
            )
                ?.addOnSuccessListener(OnSuccessListener<Any> { googleDriveFileHolder ->
                    removeItem(position)
                    uploading_animation_cover.visibility = View.GONE
                    uploading_animation.visibility = View.GONE
                    Log.i(
                        TAG,
                        "Successfully Uploaded. File Id :$googleDriveFileHolder"
                    )
                })
                ?.addOnFailureListener { e ->
                    uploading_animation_cover.visibility = View.GONE
                    uploading_animation.visibility = View.GONE
                    Log.i(
                        TAG,
                        "Failed to Upload. File Id :" + e.message
                    )
                }
        } catch (e: Exception) {
            Log.i(TAG, "Exception : " + e.message)
        }
    }

    private fun getPath(uri: Uri): String? {
        val projection =
            arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor = contentResolver.query(uri, projection, null, null, null)!!
        val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
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

    fun prepareSelection(view: View?, position: Int?) {
        if ((view as CheckBox).isChecked) {
            selection_list.add(ImageHolder.imageArrayList()[position!!])
            Toast.makeText(applicationContext, position.toString(), Toast.LENGTH_SHORT).show()
            counter += 1
            updateCounter(counter)
        } else {
            selection_list.remove(ImageHolder.imageArrayList()[position!!])
            Toast.makeText(applicationContext, position.toString(), Toast.LENGTH_SHORT).show()
            counter -= 1
            updateCounter(counter)
        }

    }

    private fun updateCounter(counter: Int) {
        if (counter == 0) {
            counter_text.text = "0 items are selected"
        } else {
            counter_text.text = "$counter items selected"
        }
    }

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

    private fun deleteImage(path: String) {
        val delete = File(path)
        if (delete.exists()) {
            if (delete.delete()) {
                println("file Deleted :$path")
            } else {
                println("file not Deleted :$path")
            }
        }
    }

    private fun getCurrentDate(): String {
        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        return df.format(c)
    }

     fun readText(imageBitmap: Bitmap) {
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

                        for (j in 0 until singleWordArrayList.size-2){
                            doubleWordArrayList.add(singleWordArrayList[j] + " " + singleWordArrayList[j+1])
                            tripleWordArrayList.add(singleWordArrayList[j] + " " + singleWordArrayList[j+1] + " " +singleWordArrayList[j+2])
                        }
//                        doubleWordArrayList.add(singleWordArrayList[singleWordArrayList.size-2]+ " " + singleWordArrayList[singleWordArrayList.size-1])
                    }
                }

            }

        }
         val match = findMatchingWords(singleWordArrayList,doubleWordArrayList,tripleWordArrayList)
         Log.i("matchingword",match)
    }

    fun findMatchingWords(singleList: java.util.ArrayList<String>,doubleList: java.util.ArrayList<String>,tripleList: java.util.ArrayList<String>):String?{
        for (word in tripleList){
            if (testData.threeWord.containsKey(word)){
//                Log.i("tagname",word)
                return word
            }
        }
        for (word in doubleList){
            if (testData.twoWords.containsKey(word)){
                return word
            }
        }
        for (word in singleList){
            if (testData.oneWord.containsKey(word)){
                return word
            }
        }

        for (word in tripleList){
          for (key in testData.threeWord.keys){
              if(key.length <= word.length+2 && key.length  >= word.length-2){
                  val hLength = upgradedHammingDist(word,word.length,key,key.length)
                  if (hLength <= 2){
                      return key
                  }
              }
          }
        }
        for (word in doubleList){
            for (key in testData.twoWords.keys){
                if(key.length <= word.length+2 && key.length  >= word.length-2){
                    val hLength = upgradedHammingDist(word,word.length,key,key.length)
                    if (hLength <= 2){
                        return key
                    }
                }
            }
        }
        for (word in singleList){
            for (key in testData.oneWord.keys){
                if(key.length <= word.length+2 && key.length  >= word.length-2){
                    val hLength = upgradedHammingDist(word,word.length,key,key.length)
                    if (hLength <= 2){
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
//        singleWordArrayList.add("heyoullo")
//        singleWordArrayList.add("hello")
//
//        for (i in 0 until singleWordArrayList.size-2){
//            doubleWordArrayList.add(singleWordArrayList[i] + " " + singleWordArrayList[i+1])
//            tripleWordArrayList.add(singleWordArrayList[i] + " " + singleWordArrayList[i+1] + " " +singleWordArrayList[i+2])
//        }
//        doubleWordArrayList.add(singleWordArrayList[singleWordArrayList.size-2]+ " " + singleWordArrayList[singleWordArrayList.size-1])
//
//        for (word in tripleWordArrayList){
//            Log.i("listwords",word)
//        }
//    }


    fun toGrayscale(bmpOriginal: Bitmap): Bitmap? {
        val height: Int = bmpOriginal.height
        val width: Int = bmpOriginal.width
        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0F)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(bmpOriginal, 0F, 0F, paint)
        return bmpGrayscale
    }


    fun tapped(position: Int) {
        val intent = Intent(applicationContext, FullScreenImageActivity::class.java)
        intent.putExtra("position", position)
        startActivity(intent)

    }

    fun detectRotation(bitmap: Bitmap, uri: Uri) {

        val photoPath = getPath(uri)
        val ei = ExifInterface(photoPath)
        val orientation: Int = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        var rotatedBitmap: Bitmap? = null
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
            ExifInterface.ORIENTATION_NORMAL -> bitmap
        }
    }

    private fun rotateImage(source: Bitmap, angle: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        Log.i("drawerfunctions", "asdfghj")
        when (item!!.itemId) {
            R.id.logout1 -> {
                Toast.makeText(this, "Logout Successfully hahaha", Toast.LENGTH_SHORT).show()
                Log.i("drawerfunctions", "I am clicked")
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
        val id = item.itemId
        when (id) {
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

    fun upgradedHammingDist(str1: String, str1_length: Int, str2: String, str2_length: Int): Int {
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
        return count + Math.abs((str1_length - str2_length) / 2)
    }

}
