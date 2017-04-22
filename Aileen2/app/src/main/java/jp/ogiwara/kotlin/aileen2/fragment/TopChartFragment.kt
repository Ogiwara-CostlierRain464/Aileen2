package jp.ogiwara.kotlin.aileen2.fragment


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import jp.ogiwara.java.aileen_alpha.utils.cutEnd

import jp.ogiwara.kotlin.aileen2.R
import jp.ogiwara.kotlin.aileen2.adapter.CommonRecyclerAdapter
import jp.ogiwara.kotlin.aileen2.manager.AccountManager
import jp.ogiwara.kotlin.aileen2.model.Video
import jp.ogiwara.kotlin.aileen2.task.LoadIdsTask
import jp.ogiwara.kotlin.aileen2.utils.API_KEY
import jp.ogiwara.kotlin.aileen2.utils.APP_NAME
import jp.ogiwara.kotlin.aileen2.utils.MAX_ITEM
import jp.ogiwara.kotlin.aileen2.utils.switchVisibly
import java.io.IOException


/**
 * A simple [Fragment] subclass.
 */
class TopChartFragment : BaseFragment(){

    override fun onStart() {
        super.onStart()
        switchVisibly(true)
        TopChartTask().execute()
    }

    override fun onRefresh() {
        TopChartTask().execute()
    }

    override fun setVideos(videos: ArrayList<Video>) {
        progress.switchVisibly(false)

        val adapter = TopChartRecyclerAdapter(activity,videos)
        recycler.adapter = adapter
        refresh.isRefreshing = false
    }

    class TopChartRecyclerAdapter(activity: Activity, data: ArrayList<Video>): CommonRecyclerAdapter(activity,data){

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.small_video_item,parent,false))
        }
    }

    inner class TopChartTask(): AsyncTask<Unit, Unit, String>(){
        override fun doInBackground(vararg params: Unit?): String{
            val stringBuilder = StringBuilder()
            try{
                val youtube = YouTube.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        JacksonFactory(),
                        AccountManager.credential)
                        .setYouTubeRequestInitializer(
                                YouTubeRequestInitializer(API_KEY)
                        )
                        .setApplicationName(APP_NAME)
                        .build()

                val videos = youtube.videos().list("id")
                videos.chart = "mostPopular"
                videos.maxResults = MAX_ITEM
                videos.fields = "items"

                val res = videos.execute()
                res.items.forEach {
                    stringBuilder.append("${it.id},")
                }
            }catch (e: IOException){
                e.printStackTrace()
            }

            return stringBuilder.toString().cutEnd()
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            LoadIdsTask(result,this@TopChartFragment::setVideos,context).execute()
        }
    }
}
