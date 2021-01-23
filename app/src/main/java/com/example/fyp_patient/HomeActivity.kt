package com.example.fyp_patient

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.fyp_patient.camera_old.CameraImageRecycleViewActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.toolbar.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //toolbar and drawer setup
        (R.id.toolbar_main)
        setSupportActionBar(toolbar_main)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

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

        val navigationView: NavigationView = findViewById<View>(R.id.nav_view) as NavigationView
        val headerView: View = navigationView.getHeaderView(0)
        val navUsername = headerView.findViewById(R.id.doctorName) as TextView
        val navUserEmail = headerView.findViewById(R.id.doctorEmail) as TextView
        val navUserImage = headerView.findViewById(R.id.doctorImage) as ImageView


        navUsername.text = name
        navUserEmail.text = email
        Glide.with(this).load(url).apply(RequestOptions.circleCropTransform()).into(navUserImage)

        uploadMyRecords.setOnClickListener {
            val intent = Intent(this,CameraImageRecycleViewActivity::class.java)
            startActivity(intent)
        }

    }
}