package jp.ogiwara.java.aileen_alpha.utils

fun String.cutEnd(): String{

    if(length <= 1)
        return this

    return this.substring(0,length-1)
}