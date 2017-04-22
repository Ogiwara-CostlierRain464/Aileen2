package jp.ogiwara.kotlin.aileen2.utils

import java.io.Serializable
import java.util.*
import java.util.ArrayList

/**
 * 要素がなかったら一番最初に戻る
 */
class PositionQueue<T>: ArrayList<T>(),Serializable{

    var position: Int = 0
        set(value) {
            if(value < 0)
                return

            position = value
        }

    fun next(): T{
        position += 1
        if(size <= position)
            position = 0

        return this.get(position)
    }

    fun current(): T{
        return get(position)
    }

    fun previous(): T{
        position -= 1
        if(position <= -1)
            position = 0

        return this.get(position)
    }
}