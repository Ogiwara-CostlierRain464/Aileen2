package jp.ogiwara.kotlin.aileen2

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import jp.ogiwara.java.aileen_alpha.utils.cutEnd
import jp.ogiwara.kotlin.aileen2.adapter.CommonRecyclerAdapter
import jp.ogiwara.kotlin.aileen2.model.Video
import jp.ogiwara.kotlin.aileen2.task.LoadIdsTask
import jp.ogiwara.kotlin.aileen2.utils.switchVisibly

/**
 * Created by ogiwara on 2017/04/25.
 *
 * 検索Activity未完の状態で作成。
 * //TODO FIX
 *
 * @intent videoIds : ArrayList<String>
 * @intent title : String
 */
class NormalVideolistActivity() : VideolistActivity() {

    override val TAG = NormalVideolistActivity::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val videoIds = intent.getStringArrayListExtra("videoIds")
        val title = intent.getStringExtra("title")

        setTitle(title)

        progress.switchVisibly(true)

        val stringBuilder = StringBuilder()
        videoIds.forEach {
            stringBuilder.append("$it,")
        }
        LoadIdsTask(stringBuilder.toString().cutEnd(),this::setVideos,this).execute()
    }

    override fun setVideos(videos: ArrayList<Video>) {
        progress.switchVisibly(false)
        recycler.adapter = SearchActivity.SearchRecyclerAdapter(this,videos)
    }

    class NormalVideolistRecyclerAdapter(activity: Activity, data: ArrayList<Video>): CommonRecyclerAdapter(activity,data){
        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.small_video_item,parent,false))
        }
    }
}