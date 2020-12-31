package com.example.fyp_patient.camera_old

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fyp_patient.AlertDialogUtility
import com.example.fyp_patient.R
import kotlinx.android.synthetic.main.image_list_item.view.*

class CameraImagesAdapter(private val arrayList: ArrayList<CameraImagesModel>, private val context: Context) :
    RecyclerView.Adapter<CameraImagesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, private val context: Context) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        fun bindItems(cameraImagesModel: CameraImagesModel) {
            itemView.title.text = cameraImagesModel.title
            itemView.date.text = cameraImagesModel.des
//            itemView.recordImage.setImageURI(cameraImagesModel.uri)

//            itemView.checkbox.setOnClickListener(this.)
            itemView.uploadButton.setOnClickListener(this)
            itemView.deleteButton.setOnClickListener(this)
            itemView.recordImage.setOnClickListener(this)
            Glide.with(context).load(cameraImagesModel.uri).into(itemView.recordImage)
        }

        override fun onClick(p0: View?) {
//            if (p0!!.id == itemView.recordImage.id){
//                (context as CameraImageRecycleViewActivity).tapped(adapterPosition)
////                Toast.makeText(context,"clicked",Toast.LENGTH_SHORT).show()
//
//            }
            if (p0!!.id == itemView.deleteButton.id){
                (context as CameraImageRecycleViewActivity).removeItem(adapterPosition)
            }
//            (context as CameraImageRecycleViewActivity).prepareSelection(p0, adapterPosition)
            if (p0!!.id == itemView.uploadButton.id){
                Log.i("checkdelay","called 0")
//                val context = context as CameraImageRecycleViewActivity
//                val uploading = AlertDialogUtility.alertDialog(context as CameraImageRecycleViewActivity, "Uploading....", R.raw.progress_animation)

                Log.i("checkdelay","called 1")

                (context as CameraImageRecycleViewActivity).uploadImageIntoDrive(adapterPosition)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.image_list_item, parent, false)
        return ViewHolder(v, context)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(arrayList[position])

        if (!(context as CameraImageRecycleViewActivity).isInActionMode) {
            holder.itemView.checkbox.visibility = View.GONE
        } else {
            holder.itemView.checkbox.visibility = View.VISIBLE
        }

        holder.itemView.recordImage.setOnClickListener {
            tapped(position)
        }
        holder
//        holder.itemView.uploadButton.setOnClickListener {
//            (context as CameraImageRecycleViewActivity).uplo
//        }

//        holder.itemView.setOnLongClickListener(context)
////        holder.itemView.checkbox.setOnClickListener(this)
    }

    fun updateAdapter(list: ArrayList<CameraImagesModel>) {
        for (cameraImagesModel: CameraImagesModel in list) {
            arrayList.remove(cameraImagesModel)
        }
        notifyDataSetChanged()
    }

     fun tapped(position: Int) {
        val intent = Intent(context, FullScreenImageActivity::class.java)
        intent.putExtra("position", position)
//        intent.putStringArrayListExtra("url", uriList(arrayList))
        context.startActivity(intent)
    }

    private fun uriList(list: ArrayList<CameraImagesModel>): ArrayList<String> {
        val myList = ArrayList<String>()
        for (model in list) {
            myList.add(model.uri.toString())
        }
        return myList
    }

}