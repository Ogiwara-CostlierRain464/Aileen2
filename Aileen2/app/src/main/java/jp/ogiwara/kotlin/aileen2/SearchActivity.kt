package jp.ogiwara.kotlin.aileen2

import android.app.Activity
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import jp.ogiwara.java.aileen_alpha.utils.cutEnd
import jp.ogiwara.kotlin.aileen2.adapter.CommonRecyclerAdapter
import jp.ogiwara.kotlin.aileen2.manager.AccountManager
import jp.ogiwara.kotlin.aileen2.model.Video
import jp.ogiwara.kotlin.aileen2.task.LoadIdsTask
import jp.ogiwara.kotlin.aileen2.utils.APP_NAME
import jp.ogiwara.kotlin.aileen2.utils.switchVisibly
import java.io.IOException

/**
 *@intent search_word : 検索キーワード
 */
class SearchActivity : VideolistActivity() {

    override val TAG = SearchActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        val searchWord = intent.getStringExtra("search_word")
        setTitle(searchWord)

        Log.i(TAG,"Searching for $searchWord")

        progress.switchVisibly(true)
        SearchTask(searchWord).execute()
    }
    override fun setVideos(videos: ArrayList<Video>) {
        progress.switchVisibly(false)
        recycler.adapter = SearchRecyclerAdapter(this,videos)
    }

    class SearchRecyclerAdapter(activity: Activity, data: ArrayList<Video>): CommonRecyclerAdapter(activity,data){
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.small_video_item,parent,false))
        }
    }

    inner class SearchTask(val query: String) : AsyncTask<Unit,Unit,String>(){

        override fun doInBackground(vararg params: Unit?): String {
            val stringBuilder = StringBuilder()
            try{
                val youTube = YouTube.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        JacksonFactory(),
                        AccountManager.credential
                ).setApplicationName(APP_NAME).build()

                val search = youTube.search().list("id")
                search.part = "items(id/videoId)"
                search.forMine = true
                search.type = "video"
                search.q = query

                val res = search.execute()
                res.items.forEach {
                    stringBuilder.append("${it.id.videoId},")
                }
            }catch (io: IOException){
                io.printStackTrace()
            }
            return stringBuilder.toString().cutEnd()
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            LoadIdsTask(result,this@SearchActivity::setVideos,applicationContext).execute()
        }
    }
}
