package com.example.fyp_patient

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import crypto.VC.CredentialSubject
import crypto.VC.VCCover

class DisplayDoctorDetailsActivity : AppCompatActivity() {

    lateinit var vcCover: VCCover

    lateinit var fNameTV:TextView
    lateinit var lNameTV:TextView
    lateinit var hospitalTV:TextView
    lateinit var specialityTV:TextView
    lateinit var genderTV:TextView
    lateinit var continueBtn:Button
    lateinit var cancelBtn:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_doctor_details)
        val intent = intent;
        val vcString =  intent.getStringExtra("vcString")
        val gson: Gson = Gson()
        vcCover = gson.fromJson(vcString, VCCover::class.java)

        //initialize xml
        fNameTV = findViewById(R.id.fName)
        lNameTV = findViewById(R.id.lName)
        hospitalTV = findViewById(R.id.hospital)
        specialityTV = findViewById(R.id.speciality)
        genderTV = findViewById(R.id.gender)
        continueBtn = findViewById(R.id.continueBtn)
        cancelBtn = findViewById(R.id.cancelBtn)


        //set activity screen size
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)

        val width = dm.widthPixels
        val height = dm.heightPixels

        window.setLayout((width * .7).toInt(), (height * .8).toInt())

        setDisplayTexts()
        onContinueBtnPressed(continueBtn)
        onCancelButtonPressed(cancelBtn)

    }

    private fun setDisplayTexts() {
        val credentialSubject:CredentialSubject = vcCover.verifiableClaim.credentialSubject
        fNameTV.text = credentialSubject.firstName
        lNameTV.text = credentialSubject.lastName
        hospitalTV.text = credentialSubject.hospital;
        specialityTV.text = credentialSubject.speciality;
        genderTV.text = credentialSubject.gender;
    }
    private fun onContinueBtnPressed(btn: Button) {
        //challange response protocal
        btn.setOnClickListener {

        }
    }
    private fun onCancelButtonPressed(btn: Button) {
        //return to main screen
        btn.setOnClickListener{
            val intent:Intent = Intent(applicationContext, BarCodeReaderActivity::class.java)
            startActivity(intent)
        }
    }


}