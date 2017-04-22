package jp.ogiwara.kotlin.aileen2.task

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import jp.ogiwara.kotlin.aileen2.manager.AccountManager
import jp.ogiwara.kotlin.aileen2.model.Video
import jp.ogiwara.kotlin.aileen2.utils.*
import jp.ogiwara.kotlin.aileen2.utils.Settings.getThumbnailQuality
import java.io.IOException


class LoadIdsTask(val idList: String,val onPost: (ArrayList<Video>)->Unit,val context: Context) : AsyncTask<Unit,Unit,ArrayList<Video>>(){

    val TAG = LoadIdsTask::class.simpleName

    override fun doInBackground(vararg params: Unit?) : ArrayList<Video>{
        val result = ArrayList<Video>()
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

            val videoList = youtube.videos().list("id,snippet,statistics,contentDetails")
            videoList.key = API_KEY
            videoList.fields = "items(id,snippet/channelTitle,snippet/title,snippet/thumbnails/default/url,snippet/thumbnails/high/url,contentDetails/duration,statistics/viewCount)"
            videoList.maxResults = MAX_ITEM
            videoList.id = idList
            Log.i(TAG,"loading videos: $idList")

            val res = videoList.execute()

            for(e: com.google.api.services.youtube.model.Video in res.items){

                val isoTime = e.contentDetails?.duration ?: "PT5M42S"
                val time = ISO8601DurationConverter.convert(isoTime)

                val thumbnail = when(getThumbnailQuality(context)){
                    Settings.ThumbnailQuality.INVISIBLE->null
                    Settings.ThumbnailQuality.LOW->e.snippet.thumbnails.default.url
                    Settings.ThumbnailQuality.NORMAL->e.snippet.thumbnails.high.url
                    else->e.snippet.thumbnails.default.url
                }

                //ぬルポ対策
                result.add(Video(id = e.id,
                                    title = e.snippet?.title ?: "",
                                    channelName = e.snippet?.channelTitle ?: "",
                                    thumbnail = thumbnail,
                                    duration = time,
                                    viewCount = e.statistics?.viewCount?.toString() ?: "" ))
            }
        }catch (e: IOException){
            e.printStackTrace()
            //TODO Connection error
        }
        return result
    }

    override fun onPostExecute(result: ArrayList<Video>) {
        super.onPostExecute(result)
        onPost(result)
    }
}