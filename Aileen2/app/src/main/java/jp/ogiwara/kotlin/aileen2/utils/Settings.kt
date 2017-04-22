package jp.ogiwara.kotlin.aileen2.utils

import android.content.Context
import android.preference.PreferenceManager
import kotlin.reflect.jvm.internal.impl.javax.inject.Singleton

/**
 *設定情報保存するシングルトン
 *
 * アプリケーションの起動時に{@link Settings.init}
 * 終了時に{@link Settings.fin}を呼び出す必要あり
 */
@Singleton
object Settings{
    var accountName: String? = null
    var labeled = CustomQueue(MAX_ITEM.toInt())
    var history = CustomQueue(MAX_ITEM.toInt())

    enum class ThumbnailQuality{
        INVISIBLE,
        LOW,
        NORMAL
    }

    enum class VideoQuality{
        LOW,
        MIDDLE,
        HIGH
    }

    fun init(context: Context){
        val preferences = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        accountName = preferences.getString(ACCOUNT_NAME_KEY,null)
        labeled = CustomQueue(MAX_ITEM.toInt(),preferences.getString(LABELED_KEY,""))
        history = CustomQueue(MAX_ITEM.toInt(),preferences.getString(HISTORY_KEY,""))
    }

    private fun thumbnailFromInt(int: Int) = when(int){
        0->ThumbnailQuality.INVISIBLE
        1->ThumbnailQuality.LOW
        2->ThumbnailQuality.NORMAL
        else->ThumbnailQuality.LOW
    }

    private fun videoFromInt(int: Int) = when(int){
        0->VideoQuality.LOW
        1->VideoQuality.MIDDLE
        2->VideoQuality.HIGH
        else->VideoQuality.MIDDLE
    }

    fun getThumbnailQuality(context: Context): ThumbnailQuality{
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return thumbnailFromInt(pref.getString("thumbnail_quality","2").toInt())
    }

    fun getVideoQuality(context: Context): VideoQuality{
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return videoFromInt(pref.getString("video_quality","1").toInt())
    }

    fun fin(context: Context){
        val editor = context.getSharedPreferences(PREFERENCE_FILE_NAME,Context.MODE_PRIVATE).edit()
        editor.putString(ACCOUNT_NAME_KEY, accountName)
        editor.putString(LABELED_KEY, labeled.toPrettyString())
        editor.putString(HISTORY_KEY, history.toPrettyString())
        editor.apply()
    }
}