package jp.ogiwara.kotlin.aileen2.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.squareup.picasso.Picasso
import jp.ogiwara.kotlin.aileen2.MainActivity
import jp.ogiwara.kotlin.aileen2.R
import jp.ogiwara.kotlin.aileen2.model.Video
import jp.ogiwara.kotlin.aileen2.service.BackgroundAudioService
import jp.ogiwara.kotlin.aileen2.utils.PositionQueue
import jp.ogiwara.kotlin.aileen2.utils.Settings
import java.io.Serializable

abstract class CommonRecyclerAdapter(val activity: Activity, val data: ArrayList<Video>) : RecyclerView.Adapter<CommonRecyclerAdapter.ViewHolder>(){

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val cardView = holder?.itemView?.findViewById(R.id.video_item) as CardView
        val title = holder.itemView.findViewById(R.id.video_title) as TextView
        val thumbnail = holder.itemView.findViewById(R.id.video_thumbnail) as ImageView
        val duration = holder.itemView.findViewById(R.id.video_duration) as TextView
        val viewCount = holder.itemView.findViewById(R.id.video_views) as TextView
        val channelName = holder.itemView.findViewById(R.id.video_channel_name) as TextView
        val moreButton = holder.itemView.findViewById(R.id.more_button) as ImageButton

        title.text = data[position].title
        Picasso.with(activity).load(data[position].thumbnail).into(thumbnail)
        duration.text = data[position].duration
        viewCount.text = data[position].viewCount
        channelName.text = data[position].channelName
        cardView.setOnClickListener {
            Settings.history.add(data[position].id)

            //TODO 再生
            val intent = Intent(activity,BackgroundAudioService::class.java)
            intent.action = BackgroundAudioService.ACTIONS.ACTION_PLAY
            intent.putExtra(BackgroundAudioService.SerializeAbleString.YOUTUBE_TYPE,BackgroundAudioService.ItemType.YOUTUBE_MEDIA_TYPE_VIDEO_LIST)
            val list = PositionQueue<Video>()
            list.add(data[position])
            intent.putExtra(BackgroundAudioService.SerializeAbleString.YOUTUBE_TYPE_VIDEO_LIST,list)
            BackgroundAudioService.GUILT.list = list
            activity.startService(intent)
        }
        moreButton.setOnClickListener {
            val popup = PopupMenu(activity,it)
            popup.menuInflater.inflate(R.menu.more_button,popup.menu)
            popup.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.share -> {
                        //TODO share
                    }
                    R.id.download -> {
                        //TODO Download
                    }
                    R.id.add_labeled -> {
                        Settings.labeled.add(data[position].id)
                        Toast.makeText(activity,activity.getString(R.string.added_labeled),Toast.LENGTH_SHORT)
                    }
                }
                true
            }
            popup.show()
        }
    }

    override fun getItemCount() = data.size

    abstract override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder
        //return ViewHolder(LayoutInflater.from(context).inflate(R.layout.small_video_item,parent,false))

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

}

