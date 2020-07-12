package com.example.fyp_patient

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_imagerecycleview.*


class MainActivity : AppCompatActivity() {

    private val uriArrayList = ArrayList<Uri>()
    private val arrayList = ArrayList<Model>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imagerecycleview)

        fab.setOnClickListener {
            getPermission()
        }
    }
    /******************************************************************************/
    /************************************this should be transferred to another class in future******************************************/
    /******************************************************************************/

    /************************* starts camera functions*****************************/
    private val REQUEST_IMAGE_CAPTURE: Int = 100
    private var image_uri : Uri? = null
    private val IMAGE_CAPTURE_CODE: Int = 101

    /***********get the permission from the device to access the camera***********/
    private fun getPermission(){
        //if the system is marshmallow or above get the run time permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED  ||
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED   ){
                //permission was not enabled
                val permission = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                //show popup to request permission
                requestPermissions(permission, REQUEST_IMAGE_CAPTURE)
            }
            else {
                //permission already granted
                openCamera()
            }
        }
        else {
            //system os < marshmallow
            openCamera()
        }
    }
    /************open the device camera*************/
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "this is an images")
        image_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        //camera Intent
        val  cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri)
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
    }
    /*************call when user clicks on the permission request dialog********************/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //called when user allow or deny from permission request
        when(requestCode){
            REQUEST_IMAGE_CAPTURE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission from pop up was granted
                    openCamera()
                }
                else{
                    //permission from pop up was denied
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    /************called when an image is captured*************/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //called when image is captured from camera intent
        if (resultCode == Activity.RESULT_OK){
            //set the image to the image view
            image_uri?.let { uriArrayList.add(it) }
            arrayList.add((image_uri?.let { Model("My title","My description", it) }!!))

            Log.i("check123", arrayList.toString())
            val adapter = Adapter(arrayList,this)

            recycleView.layoutManager = GridLayoutManager(this,2)
            recycleView.adapter = adapter

        }
    }



}
