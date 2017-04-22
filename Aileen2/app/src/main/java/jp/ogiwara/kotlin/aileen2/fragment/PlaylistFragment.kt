package jp.ogiwara.kotlin.aileen2.fragment


import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import com.squareup.picasso.Picasso
import jp.ogiwara.kotlin.aileen2.PlaylistActivity

import jp.ogiwara.kotlin.aileen2.R
import jp.ogiwara.kotlin.aileen2.adapter.CommonRecyclerAdapter
import jp.ogiwara.kotlin.aileen2.manager.AccountManager
import jp.ogiwara.kotlin.aileen2.model.PlaylistItem
import jp.ogiwara.kotlin.aileen2.model.Video
import jp.ogiwara.kotlin.aileen2.utils.API_KEY
import jp.ogiwara.kotlin.aileen2.utils.APP_NAME
import jp.ogiwara.kotlin.aileen2.utils.MAX_ITEM
import jp.ogiwara.kotlin.aileen2.utils.switchVisibly
import java.io.IOException


/**
 * A simple [Fragment] subclass.
 */
class PlaylistFragment : BaseFragment() {

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
            PlaylistTask().execute()
        }else{
            switchVisibly(false)
            refresh.isRefreshing = false
        }
    }

    override fun setVideos(videos: ArrayList<Video>) {

    }

    class PlaylistRecyclerAdapter(val activity: Activity,val data: ArrayList<PlaylistItem>): RecyclerView.Adapter<PlaylistRecyclerAdapter.ViewHolder>(){


        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val title = holder?.itemView?.findViewById(R.id.playlist_title) as TextView
            val thumb1 = holder.itemView?.findViewById(R.id.playlist_thumbnail) as ImageView
            val thumb2 = holder.itemView?.findViewById(R.id.thumbnail_2) as ImageView
            val thumb3 = holder.itemView?.findViewById(R.id.thumbnail_3) as ImageView
            val thumb4 = holder.itemView?.findViewById(R.id.thumbnail_4) as ImageView
            val thumb5 = holder.itemView?.findViewById(R.id.thumbnail_5) as ImageView
            val thumb6 = holder.itemView?.findViewById(R.id.thumbnail_6) as ImageView

            val cardView = holder.itemView?.findViewById(R.id.grid_video_item) as CardView

            title.text = data[position].title
            Picasso.with(activity).load(data[position].playlistThumbnailUrl).into(thumb1)
            if(data[position].thumbnail1Url != null){
                Picasso.with(activity).load(data[position].thumbnail1Url).into(thumb2)
            }
            if(data[position].thumbnail2Url != null){
                Picasso.with(activity).load(data[position].thumbnail2Url).into(thumb3)
            }
            if(data[position].thumbnail3Url != null){
                Picasso.with(activity).load(data[position].thumbnail3Url).into(thumb4)
            }
            if(data[position].thumbnail4Url != null){
                Picasso.with(activity).load(data[position].thumbnail4Url).into(thumb5)
            }
            if(data[position].thumbnail5Url != null){
                Picasso.with(activity).load(data[position].thumbnail5Url).into(thumb6)
            }

            cardView.setOnClickListener {
                val intent = Intent(activity,PlaylistActivity::class.java)
                //TODO intent.putExtra()
                activity.startActivity(intent)
            }
        }

        override fun getItemCount() = data.size

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.grid_video_item,parent,false))
        }

        class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
    }

    inner class PlaylistTask(): AsyncTask<Unit, Unit, ArrayList<PlaylistItem>>() {

        override fun doInBackground(vararg params: Unit?): ArrayList<PlaylistItem> {
            val result = ArrayList<PlaylistItem>()
            try {
                val youTube = YouTube.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        JacksonFactory(),
                        AccountManager.credential
                ).setYouTubeRequestInitializer(
                        YouTubeRequestInitializer(API_KEY)
                ).setApplicationName(APP_NAME)
                        .build()

                val list = youTube.playlists().list("id,snippet,contentDetails")
                list.fields = "items"
                list.mine = true
                list.maxResults = MAX_ITEM
                val res = list.execute()
                for (e in res.items) {
                    val title = e.snippet.title
                    val id = e.id
                    val thumbnail = e.snippet.thumbnails.high.url//ここはhigh固定???

                    val listItem = youTube.playlistItems().list("id,snippet")
                    listItem.fields = "items(snippet/thumbnails/high)"
                    listItem.playlistId = e.id
                    listItem.maxResults = MAX_ITEM
                    val playlistItems = listItem.execute().items
                    val lists = ArrayList<String?>()
                    for(i in 1..(playlistItems.size)){

                        lists.add(playlistItems[i -1].snippet.thumbnails.high.url)
                        if(i >= 7) {
                            break
                        }
                    }

                    while (lists.size <= 6){
                        lists.add(null)
                    }

                    result.add(PlaylistItem(title = title, id = id, playlistThumbnailUrl = thumbnail, thumbnail1Url = lists[0], thumbnail2Url = lists[1], thumbnail3Url = lists[2], thumbnail4Url = lists[3], thumbnail5Url = lists[4], thumbnail6Url = lists[5]))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return result
        }


        override fun onPostExecute(result: ArrayList<PlaylistItem>) {
            super.onPostExecute(result)
            progress.switchVisibly(false)
            recycler.adapter = PlaylistRecyclerAdapter(activity,result)
            refresh.isRefreshing = false
        }
    }
}// Required empty public constructor
