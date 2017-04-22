package jp.ogiwara.kotlin.aileen2.utils

import jp.ogiwara.java.aileen_alpha.utils.cutEnd
import java.io.Serializable
import java.util.*

/**
 *カスタムキュー
 *
 *{@link maxSize}を超えたら一番最後に追加した要素を削除
 * また、既に同じ要素がある場合は追加しない
 */
class CustomQueue(val maxSize: Int): ArrayDeque<String>(maxSize),Serializable {

    constructor(size: Int,string: String): this(size){
        if(string == "")
            return

        for(e in string.split(","))
            add(e)
    }

    fun toPrettyString(): String{
        if(size == 0)
            return ""

        val stringBuilder = StringBuilder()
        for(e in this){
            stringBuilder.append("$e,")
        }
        return stringBuilder.toString().cutEnd()
    }

    override fun add(element: String): Boolean {
        if(maxSize <= size)
            removeLast()

        if(contains(element))
            return false

        return super.add(element)
    }

}