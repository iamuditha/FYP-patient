package com.example.fyp_patient

import ContractorHandlers.IAMContractorHandler
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.util.*

class VerifierActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verifier)

        val intent = intent;
        val didDoctor =  intent.getStringExtra("did")
        Log.i("blockChainInVerifier",didDoctor!!)

        verifyWithBlockChain(didDoctor)
    }

    //verify the email
    private fun verifyWithBlockChain(didDoctor:String){

        val thread = Thread{
                    val web3j: Web3j = Web3j.build(HttpService("https://c4375655a390.ngrok.io"))
//            val web3j : Web3j = EthFunctions.connect("https://c4375655a390.ngrok.io")
            val credentials = WalletUtils.loadBip39Credentials("123456", UUID.randomUUID().toString())
            val iamContract = IAMContractorHandler.getInstance().getWrapperForContractor(web3j,getString(R.string.main_contractor_address),credentials)
            val doctorDetails = IAMContractorHandler.getInstance().getDoctorDetails(iamContract,didDoctor)

//            val didLink = doctorDetails[0]
//            val vcLink = doctorDetails[0]
            Log.i("blockChainInVerifier", doctorDetails.size.toString())
//            Log.i("blockChain", vcLink)
//
//            didLink.saveTo(filesDir.absolutePath+"/DoctorDid.json")
//            vcLink.saveTo(filesDir.absolutePath+"/DoctorVc.json")

        }
        thread.start()


    }
}