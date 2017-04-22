package jp.ogiwara.kotlin.aileen2.utils

import android.app.Activity
import android.support.v4.app.ActivityCompat
import jp.ogiwara.kotlin.aileen2.REQUEST_PERMISSION

fun getPermission(activity: Activity, permission: String){
    ActivityCompat.requestPermissions(activity, arrayOf(permission), REQUEST_PERMISSION)
    //TODO we don't check result
}