package com.example.fyp_patient.challange_response


import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log

class ChallengeResponseService : Service(){

    override fun onBind(intent: Intent?): IBinder? {
        return null;
    }

    override fun onCreate() {
        super.onCreate()
        Log.i("tag", "service creating for god sake")



    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("tag","start service??")
        val bundle: Bundle? = intent?.extras
        val did : String = bundle!!.getString("did").toString()
        var id : String = bundle.getString("id").toString()
        val challengeResponse = ChallengeResponse(did, id)
        challengeResponse?.challengeResponse()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        Log.i("tag", "service ends")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun stopService(name: Intent?): Boolean {
        stopSelf()
        return super.stopService(name)

    }

}