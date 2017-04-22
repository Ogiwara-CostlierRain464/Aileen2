package jp.ogiwara.kotlin.aileen2.utils

import android.view.View

fun View.switchVisibly(visibly: Boolean){
    visibility = if(visibly) View.VISIBLE else View.INVISIBLE
}
