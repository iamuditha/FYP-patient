package com.example.fyp_patient.camera_old

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.fyp_patient.R

class FullScreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val myIntent: Intent = intent
        val position = myIntent.getIntExtra("position",0)
        val urls  = myIntent.getStringArrayListExtra("url") as ArrayList<String>
        Log.i("full screen",position.toString())

        val viewPager = findViewById<ViewPager>(R.id.pager)
        val adapter = ViewPageAdapter(this,urls,position)
        viewPager.adapter = adapter

    }

    class ViewPageAdapter(private val context: Context,private val url:ArrayList<String>, var position: Int):PagerAdapter(){

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return url.count()
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            Log.i("full screen",position.toString())

            var count = 1
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)as LayoutInflater
            val view  = layoutInflater.inflate(R.layout.image_slider, null)
            val image = view.findViewById<View>(R.id.imageView1) as  ImageView
            Log.i("full screen",url.toString() + position)
            image.setImageURI(Uri.parse(url[position]))

            val viewPager = container as ViewPager
            viewPager.addView(view, position.toInt())
            return view
        }
    }
}
