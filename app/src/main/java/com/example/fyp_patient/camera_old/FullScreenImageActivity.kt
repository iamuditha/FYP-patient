package com.example.fyp_patient.camera_old

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.fyp_patient.R

class FullScreenImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

//        val myImagesList = ImageURIHolder.uriArrayList()
//        val myArray = ArrayList<Uri>()
//
//        for (uri in myImagesList){
//            myArray.add(uri)
//            Log.i("myitem",myArray.size.toString())
//        }

        val myIntent: Intent = intent
        val position = myIntent.getIntExtra("position", 0)
//        val urls  = myIntent.getStringArrayListExtra("url") as ArrayList<String>
//        Log.i("full screen",position.toString())

        val viewPager = findViewById<ViewPager>(R.id.pager)
        val adapter = ViewPageAdapter(applicationContext, ImageURIHolder.uriArrayList(), position)
        viewPager.adapter = adapter

    }

    class ViewPageAdapter(private val context: Context, private var url: ArrayList<Uri>, private var position: Int):PagerAdapter(){

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return url.size
        }

        override fun instantiateItem(container: ViewGroup, p: Int): Any {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)as LayoutInflater
            var view  = layoutInflater.inflate(R.layout.image_slider, null)
            var image = view.findViewById<View>(R.id.imageView1) as  ImageView
            image.setImageURI(url[position])
            Log.i("myitemshow", position.toString())
            Log.i("myitemshow", url[position].toString())
//            val viewPager = container as ViewPager
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val viewPager = container as ViewPager
            val view = `object` as View
            viewPager.removeView(view)
        }
    }
}
