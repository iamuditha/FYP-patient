package com.example.fyp_patient

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.image_list_item.view.*

class Adapter(val arrayList: ArrayList<Model>,val context:Context) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {

    class ViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView){

        fun bindItems (model: Model){
            itemView.title.text = model.title
            itemView.description.text = model.des
            itemView.imageIv.setImageURI(model.uri)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.image_list_item, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(arrayList[position])

        holder.itemView.imageIv.setOnClickListener {
            Toast.makeText(context,position.toString(), Toast.LENGTH_SHORT).show()
        }
    }

}