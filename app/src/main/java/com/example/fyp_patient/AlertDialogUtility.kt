package com.example.fyp_patient

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.airbnb.lottie.LottieAnimationView
import kotlinx.android.synthetic.main.alert_resource_layout.view.*

class AlertDialogUtility {

    companion object {
        fun alertDialog(context: Context, alertText: String): Dialog {
//            val layoutBuilder = LayoutInflater.from(context).inflate(R.layout.alert_resource_layout, null)

            val dialog = Dialog(context, R.style.Theme_AppCompat_Dialog)
            dialog.setContentView(R.layout.alert_resource_layout)
            dialog.findViewById<TextView>(R.id.tv_alert).text = alertText
            dialog.findViewById<LottieAnimationView>(R.id.lottie_anim).loop(true)
            dialog.findViewById<LottieAnimationView>(R.id.lottie_anim).playAnimation()
//
//            layoutBuilder.lottie_anim.loop(true)
//            layoutBuilder.lottie_anim.playAnimation()
            return dialog

//            layoutBuilder.btn_ok.setOnClickListener {
//                Toast.makeText(context, "Ok Bye "/* + layoutBuilder.et_name.text.toString()*/, Toast.LENGTH_SHORT).show()
//                alertDialog.dismiss()
//            }
        }
    }
}