package com.example.fyp_patient.signIn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.fyp_patient.R
import com.example.fyp_patient.camera_old.CameraImageRecycleViewActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_signin.*


class SignInActivity : AppCompatActivity() {

    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var RC_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        // Configure sign-in to request the user's ID, email address, and basic profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                 .requestEmail()
                 .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        sign_in_button.setOnClickListener { v ->
            when (v.id) {
                R.id.sign_in_button -> signIn()
            }
        }
    }

    override fun onStart() {
        // Check for existing Google Sign In account, if the user is already signed in the GoogleSignInAccount will be non-null.
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        account?.let { getProfileData(it) }
        if (account != null) {
            val intent = Intent(this, CameraImageRecycleViewActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                getProfileData(account)
            }
            // Signed in successfully, show authenticated UI.
            Log.i("loginInfo", "successfully logged in to $account")
            val intent = Intent(this, CameraImageRecycleViewActivity::class.java)
            startActivity(intent)
        } catch (e: ApiException) {
            Log.w("loginInfo", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun getProfileData(account: GoogleSignInAccount) {
        sharedPreference(account)
    }

    private fun sharedPreference(account: GoogleSignInAccount){
        val editor = getSharedPreferences("PROFILE_DATA", MODE_PRIVATE).edit()
        editor.putString("email", account.email)
        editor.putString("name", account.displayName)
        editor.putString("url", account.photoUrl.toString())
        editor.apply()
    }
}
