package jp.ogiwara.kotlin.aileen2.utils

import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import jp.ogiwara.kotlin.aileen2.MainActivity
import jp.ogiwara.kotlin.aileen2.R

fun MainActivity.dialog(title: String,message: String,icon: Int? = null){
    val dialog = AlertDialog.Builder(this).create()
    dialog.setTitle(title)
    dialog.setMessage(message)
    if(icon != null)
        dialog.setIcon(icon)

    dialog.show()
}

fun MainActivity.snack(message: String){
    val coordinator = findViewById(R.id.coordinator) as CoordinatorLayout
    Snackbar.make(coordinator,message,Snackbar.LENGTH_SHORT).show()
}
