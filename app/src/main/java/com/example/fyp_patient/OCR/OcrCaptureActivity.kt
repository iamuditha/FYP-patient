package com.example.fyp_patient.OCR

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.util.SparseArray
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.fyp_patient.R
import com.example.fyp_patient.camera.CameraSource
import com.example.fyp_patient.camera.CameraSourcePreview
import com.example.fyp_patient.camera.GraphicOverlay
import com.example.fyp_patient.camera_old.CameraImageRecycleViewActivity
import com.example.fyp_patient.camera_old.ImageURIHolder
import com.example.fyp_patient.testData
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_ocr_capture.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class OcrCaptureActivity : AppCompatActivity() {
    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay<OcrGraphic>? = null
    private var capture: Button? = null
    private var imageView: ImageView? = null

    // Helper objects for detecting taps and pinches.
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null

    // A TextToSpeech engine for speaking a String value.
    private var tts: TextToSpeech? = null

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_ocr_capture)
        preview = findViewById(R.id.preview) as CameraSourcePreview?
        graphicOverlay = findViewById(R.id.graphicOverlay) as GraphicOverlay<OcrGraphic>?
//        capture = findViewById(R.id.capture) as ImageButton
//        imageView = findViewById(R.id.cimage) as ImageView


        // Set good defaults for capturing text.
        val autoFocus = true
        val useFlash = false

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        val rc: Int = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash)
        } else {
            requestCameraPermission()
        }
        gestureDetector = GestureDetector(this, CaptureGestureListener())
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
        Snackbar.make(
            graphicOverlay!!, "Tap to Speak. Pinch/Stretch to zoom",
            Snackbar.LENGTH_LONG
        )
            .show()

        // Set up the Text To Speech engine.
        val listener = OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d("OnInitListener", "Text to speech engine started successfully.")
                tts!!.language = Locale.US
            } else {
                Log.d("OnInitListener", "Error starting the text to speech engine.")
            }
        }
        tts = TextToSpeech(this.applicationContext, listener)
        captureImage!!.setOnClickListener {
            cameraSource!!.takePicture(null, object : CameraSource.PictureCallback {
                override fun onPictureTaken(data: ByteArray) {
                    var bm = BitmapFactory.decodeByteArray(data, 0, data.size)
                    var uri = getImageUri(applicationContext, bm)
                    bm = rotateImage(bm, 90)
                    val myWords = readText(bm)
                    for (i in myWords){
                        Log.i("detectedText",i)
                    }
                    val intent = Intent(
                        applicationContext,
                        CameraImageRecycleViewActivity::class.java
                    )

                    intent.putExtra("ocrImageURI", uri)
                    ImageURIHolder.addUri(uri)
                    startActivity(intent)
                }
            })
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

    fun detectRotation(bitmap: Bitmap, photoPath: String): Bitmap? {
        val ei: ExifInterface = ExifInterface(photoPath)
        val orientation = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        Log.i("detectedText", orientation.toString())

        var rotatedBitmap: Bitmap? = null
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = rotateImage(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = rotateImage(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = rotateImage(bitmap, 270)
            ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = bitmap
            else -> rotatedBitmap = bitmap
        }
        return rotatedBitmap
    }

    fun readText(imageBitmap: Bitmap): ArrayList<String> {

//        val LOG_TAG = "detectedText"
//        val wordArrayList = ArrayList<String>()
//        // imageBitmap is the Bitmap image you're trying to process for text
//        if (imageBitmap != null) {
//            val textRecognizer: TextRecognizer = TextRecognizer.Builder(applicationContext).build()
//            if (!textRecognizer.isOperational) {
//                Log.v(LOG_TAG, "Detector dependencies are not yet available.")
//
//                // Check for low storage.  If there is low storage, the native library will not be
//                // downloaded, so detection will not become operational.
//                val lowstorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
//                val hasLowStorage = registerReceiver(null, lowstorageFilter) != null
//                if (hasLowStorage) {
//                    Toast.makeText(this, "Low Storage", Toast.LENGTH_LONG).show()
//                    Log.w(LOG_TAG, "Low Storage")
//                }
//            }
//            val imageFrame = Frame.Builder()
//                .setBitmap(imageBitmap)
//                .build()
//
//            val textBlocks: SparseArray<TextBlock> = textRecognizer.detect(imageFrame)
//            for (i in 0 until textBlocks.size()) {
//                val item: TextBlock = textBlocks.valueAt(i)
//                if (item.value != null) {
//                    Log.d(LOG_TAG, "Text detected! " + item.value)
//                    Log.d(LOG_TAG, "Text detected! " + item.value.length)
//                    for (word in item.value.split(" ".toRegex()).toTypedArray()) {
//                        wordArrayList.add(word)
//                    }
//
//
//                }
//            }
//        }
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
//        Log.i("matchingword",match)
        return singleWordArrayList
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private fun requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission")
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }
        val thisActivity: Activity = this
        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(
                thisActivity, permissions,
                RC_HANDLE_CAMERA_PERM
            )
        }
        Snackbar.make(
            graphicOverlay!!, R.string.permission_camera_rationale,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.ok, listener)
            .show()
    }

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        val b = scaleGestureDetector!!.onTouchEvent(e)
        val c = gestureDetector!!.onTouchEvent(e)
        return b || c || super.onTouchEvent(e)
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private fun createCameraSource(autoFocus: Boolean, useFlash: Boolean) {
        val context = applicationContext

        // A text recognizer is created to find text.  An associated multi-processor instance
        // is set to receive the text recognition results, track the text, and maintain
        // graphics for each text block on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each text block.
        val textRecognizer = TextRecognizer.Builder(context).build()
        textRecognizer.setProcessor(OcrDetectorProcessor(graphicOverlay!!))
        if (!textRecognizer.isOperational) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.")

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            val lowstorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
            val hasLowStorage = registerReceiver(null, lowstorageFilter) != null
            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show()
                Log.w(TAG, getString(R.string.low_storage_error))
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.
        cameraSource = CameraSource.Builder(applicationContext, textRecognizer)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1280, 1024)
            .setRequestedFps(2.0f)
            .setFlashMode(if (useFlash) Camera.Parameters.FLASH_MODE_TORCH else null)
            .setFocusMode(if (autoFocus) Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO else null)
            .build()
    }

    /**
     * Restarts the camera.
     */
    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    /**
     * Stops the camera.
     */
    override fun onPause() {
        super.onPause()
        if (preview != null) {
            preview!!.stop()
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    override fun onDestroy() {
        super.onDestroy()
        if (preview != null) {
            preview!!.release()
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on [.requestPermissions].
     *
     *
     * **Note:** It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     *
     *
     * @param requestCode  The request code passed in [.requestPermissions].
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either [PackageManager.PERMISSION_GRANTED]
     * or [PackageManager.PERMISSION_DENIED]. Never null.
     * @see .requestPermissions
     */
//    fun onRequestPermissionsResult(
//        requestCode: Int,
//        @NonNull permissions: Array<String?>?,
//        @NonNull grantResults: IntArray
//    ) {
//        if (requestCode != RC_HANDLE_CAMERA_PERM) {
//            Log.d(
//                TAG,
//                "Got unexpected permission result: $requestCode"
//            )
//            super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
//            return
//        }
//        if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "Camera permission granted - initialize the camera source")
//            // we have permission, so create the camerasource
//            val autoFocus = intent.getBooleanExtra(AutoFocus, true)
//            val useFlash = intent.getBooleanExtra(UseFlash, false)
//            createCameraSource(autoFocus, useFlash)
//            return
//        }
//        Log.e(
//            TAG, "Permission not granted: results len = " + grantResults.size +
//                    " Result code = " + if (grantResults.size > 0) grantResults[0] else "(empty)"
//        )
//        val listener =
//            DialogInterface.OnClickListener { dialog, id -> finish() }
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle("Multitracker sample")
//            .setMessage(R.string.no_camera_permission)
//            .setPositiveButton(R.string.ok, listener)
//            .show()
//    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    @Throws(SecurityException::class)
    private fun startCameraSource() {
        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            applicationContext
        )
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }
        if (cameraSource != null) {
            try {
                preview!!.start(cameraSource, graphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                cameraSource!!.release()
                cameraSource = null
            }
        }
    }

    /**
     * onTap is called to speak the tapped TextBlock, if any, out loud.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the tap was on a TextBlock
     */
    private fun onTap(rawX: Float, rawY: Float): Boolean {
        val graphic: OcrGraphic? = graphicOverlay!!.getGraphicAtLocation(rawX, rawY)
        var text: TextBlock? = null
        if (graphic != null) {
            text = graphic.getTextBlock()
            if (text != null && text.value != null) {
                Log.d(TAG, "text data is being spoken! " + text.value)
                // Speak the string.
                tts!!.speak(text.value, TextToSpeech.QUEUE_ADD, null, "DEFAULT")
            } else {
                Log.d(TAG, "text data is null")
            }
        } else {
            Log.d(TAG, "no text detected")
        }
        return text != null
    }

    private inner class CaptureGestureListener : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return onTap(e.rawX, e.rawY) || super.onSingleTapConfirmed(e)
        }
    }

    private inner class ScaleListener : OnScaleGestureListener {
        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            return false
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         *
         *
         * Once a scale has ended, [ScaleGestureDetector.getFocusX]
         * and [ScaleGestureDetector.getFocusY] will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         * retrieve extended info about event state.
         */
        override fun onScaleEnd(detector: ScaleGestureDetector) {
            cameraSource?.doZoom(detector.scaleFactor)
        }
    }

    companion object {
        private const val TAG = "OcrCaptureActivity"

        // Intent request code to handle updating play services if needed.
        private const val RC_HANDLE_GMS = 9001

        // Permission request codes need to be < 256
        private const val RC_HANDLE_CAMERA_PERM = 2

        // Constants used to pass extra data in the intent
        const val AutoFocus = "AutoFocus"
        const val UseFlash = "UseFlash"
        const val TextBlockObject = "String"
    }


    private fun getPath(uri: Uri): String? {
        val projection =
            arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor = contentResolver.query(uri, projection, null, null, null)!!
        val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

//    private fun detectRotation(uri: Uri) {
//        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
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

    fun getTheImageUri(context: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
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
