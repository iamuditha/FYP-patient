package com.example.fyp_patient

import ContractorHandlers.IAMContractorHandler
import ContractorHandlers.MainContractorHandler
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.gson.Gson
import crypto.VC.VCCover
import crypto.did.DID
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import javaethereum.contracts.generated.MainContract
import kotlinx.android.synthetic.main.activity_bar_code_reader.*
import kotlinx.android.synthetic.main.toolbar.*
import org.json.JSONException
import org.json.JSONObject
import org.web3j.protocol.Web3j
import utilities.EthFunctions
import java.util.*
import kotlin.math.floor


class BarCodeReaderActivity : BaseActivity() {

    private var isFirstTime = true

    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource : CameraSource
    private lateinit var detector : BarcodeDetector
    private var did : String? = null

    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar_code_reader)

        setSupportActionBar(toolbar_main)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)


        checkPermissionAndOpenCamera()

//        if (ContextCompat.checkSelfPermission(
//                this@BarCodeReaderActivity,
//                android.Manifest.permission.CAMERA
//            ) != PackageManager.PERMISSION_GRANTED
//        ){
//            askForCameraPermission()
//        }else {
//            checkPermissionAndOpenCamera()
//        }
//
//        AlertDialogUtility.alertDialog(this, "Uploading....").show()



    }

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
            requestPermissions(permission, 191)
        } else {
            //permission already granted
            setupControls()
        }
    }

    private fun setupControls() {
        detector = BarcodeDetector.Builder(this@BarCodeReaderActivity).build()
        cameraSource = CameraSource.Builder(this@BarCodeReaderActivity, detector)
            .setAutoFocusEnabled(true)
            .build()
        cameraSurfaceView.holder.addCallback(surfaceCallBack)
        detector.setProcessor(processor)
    }

//    private fun askForCameraPermission() {
//        ActivityCompat.requestPermissions(
//            this@BarCodeReaderActivity,
//            arrayOf(android.Manifest.permission.CAMERA),
//            requestCodeCameraPermission
//        )
//        setupControls()
//
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
                Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()

            }else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val surfaceCallBack = object : SurfaceHolder.Callback {
        override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
        }
        override fun surfaceDestroyed(p0: SurfaceHolder?) {
            cameraSource.stop()
        }

        override fun surfaceCreated(surfaceHolder: SurfaceHolder?) {
            try {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    checkPermissionAndOpenCamera()
                    return
                }
                cameraSource.start(surfaceHolder)
            } catch (exception: Exception){
                Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private val processor = object : Detector.Processor<Barcode>{
        override fun release() {
        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>?) {

            if (detections != null && detections.detectedItems != null){

                val qrCodes: SparseArray<Barcode> = detections.detectedItems
                val code = qrCodes.valueAt(0)
                textScanResult.text = code.displayValue
                did = code.displayValue

                Log.i("myQRCode",did)

                if (did != null){
//                    cameraSource.stop()

                    Log.i("did", "i am called $did")
//                    cameraSurfaceView.visibility = View.INVISIBLE
//                    Toast.makeText(this@BarCodeReaderActivity, "scanned", Toast.LENGTH_SHORT).show()
//                    val web3j :Web3j = EthFunctions.connect("https://mainnet.infura.io/v3/898d9e570ec143d6ada30bfdeab9572c")
                    val iamContractorHandler = IAMContractorHandler.getInstance()
//
//                    val credentials = createWallet("123", filesDir.absolutePath) /******* make a temporary wallet *********/
//                    val mainContract: MainContract = mainContractorHandler.getWrapperForMainContractor(
//                            web3j, getString(
//                                R.string.main_contractor_address
//                            ), credentials
//                        )
//
//                    val  doctorDetails = mainContract.getDoctorDetails(did)
//                    val didDocumentLink = doctorDetails.send().value1
//                    val verifiableClaimLink = doctorDetails.send().value2
//                    val claimIssuerLink = "TO DO"
                    /****** Download these files using the links ************/
                    val vc = "";
                    val didDoc = "";

//                    val isValidate : Boolean = mainContract.validateDoctor(did, "profile hash", "claim hash").send()

                    val isValidate = true
                    if (isValidate && count == 0){
                        count++
//                        detector.release()
                        val intent: Intent = Intent(applicationContext, VerifierActivity::class.java)
                        intent.putExtra("did", did)
                        startActivity(intent)

                    }else{
                        validationFailed()
                    }

                }
            } else {
                textScanResult.text = ""
                did = ""
            }
        }
    }
    fun validationFailed(){}


    fun validated(did: String){
        //Read Public Key /******TO DO **************/
        Log.i("did", "validate function called")




    }





}
