package jp.ogiwara.kotlin.aileen2.fragment


import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import jp.ogiwara.java.aileen_alpha.utils.cutEnd

import jp.ogiwara.kotlin.aileen2.R
import jp.ogiwara.kotlin.aileen2.adapter.CommonRecyclerAdapter
import jp.ogiwara.kotlin.aileen2.manager.AccountManager
import jp.ogiwara.kotlin.aileen2.model.Video
import jp.ogiwara.kotlin.aileen2.task.LoadIdsTask
import jp.ogiwara.kotlin.aileen2.utils.APP_NAME
import jp.ogiwara.kotlin.aileen2.utils.MAX_ITEM
import jp.ogiwara.kotlin.aileen2.utils.switchVisibly
import java.io.IOException


/**
 * A simple [Fragment] subclass.
 */
class SubscribeFragment : BaseFragment() {

    val TAG = SubscribeFragment::class.simpleName

    override fun onStart() {
        super.onStart()
        loadView()
    }

    override fun onRefresh() {
        loadView()
    }

    fun loadView(){
        if(AccountManager.isLogin){
            switchVisibly(true)
            SubscribeTask().execute()
        }else{
            switchVisibly(false)
            refresh.isRefreshing = false
        }
    }

    override fun setVideos(videos: ArrayList<Video>) {
        progress.switchVisibly(false)

        val adapter = SubscribeRecyclerAdapter(activity,videos)

        recycler.adapter = adapter
        refresh.isRefreshing = false
    }

    class SubscribeRecyclerAdapter(activity: Activity, data: ArrayList<Video>): CommonRecyclerAdapter(activity,data){
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.big_video_item,parent,false))
        }
    }

    inner class SubscribeTask(): AsyncTask<Unit, Unit, String>(){

        override fun doInBackground(vararg params: Unit?): String{
            val stringBuilder = StringBuilder()
            try{
                val youTube = YouTube.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        JacksonFactory(),
                        AccountManager.credential
                ).setApplicationName(APP_NAME).build()

                val videos = youTube.activities().list("id,contentDetails")
                videos.home = true
                videos.maxResults = MAX_ITEM
                videos.fields = "items(contentDetails/upload/videoId)"

                val res = videos.execute()
                Log.d(TAG,res.toString())

                res.items.forEach {
                    stringBuilder.append("${it.contentDetails.upload.videoId},")
                }
            }catch (e: IOException){
                e.printStackTrace()
            }
            return stringBuilder.toString().cutEnd()
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            LoadIdsTask(result,this@SubscribeFragment::setVideos,context).execute()
        }
    }

}// Required empty public constructor
