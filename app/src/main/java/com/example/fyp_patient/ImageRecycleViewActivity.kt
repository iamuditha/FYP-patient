package com.example.fyp_patient

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_imagerecycleview.*
import kotlinx.android.synthetic.main.image_list_item.view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imagerecycleview)

        val arrayList = ArrayList<Model>()

        arrayList.add(Model("Image one","this is image one", R.drawable.one))
        arrayList.add(Model("Image two","this is image two", R.drawable.two))
        arrayList.add(Model("Image three","this is image three", R.drawable.three))
        arrayList.add(Model("Image four","this is image four", R.drawable.four))
        arrayList.add(Model("Image five","this is image five", R.drawable.five))

        val adapter = Adapter(arrayList,this)

        recycleView.layoutManager = LinearLayoutManager(this)
        recycleView.adapter = adapter
    }

}
