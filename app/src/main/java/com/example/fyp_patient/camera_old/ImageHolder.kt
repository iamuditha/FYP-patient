package com.example.fyp_patient.camera_old

object ImageHolder {

    private val imageArrayList = ArrayList<CameraImagesModel>()

    fun addImage(imagesModel: CameraImagesModel): ArrayList<CameraImagesModel> {
        imageArrayList.add(imagesModel)
        return imageArrayList
    }

    fun removeImage(position: Int): ArrayList<CameraImagesModel> {
        imageArrayList.removeAt(position)
        return imageArrayList
    }

    fun imageArrayList(): ArrayList<CameraImagesModel> {
        return imageArrayList
    }

}