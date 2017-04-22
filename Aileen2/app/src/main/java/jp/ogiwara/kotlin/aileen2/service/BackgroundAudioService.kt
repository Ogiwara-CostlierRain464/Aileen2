package jp.ogiwara.kotlin.aileen2.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import android.os.RemoteException
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.util.SparseArray
import android.widget.Toast
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import jp.ogiwara.kotlin.aileen2.MainActivity
import jp.ogiwara.kotlin.aileen2.R
import jp.ogiwara.kotlin.aileen2.adapter.CommonRecyclerAdapter
import jp.ogiwara.kotlin.aileen2.model.Video
import jp.ogiwara.kotlin.aileen2.service.BackgroundAudioService.ACTIONS.ACTION_NEXT
import jp.ogiwara.kotlin.aileen2.service.BackgroundAudioService.ACTIONS.ACTION_PAUSE
import jp.ogiwara.kotlin.aileen2.service.BackgroundAudioService.ACTIONS.ACTION_PLAY
import jp.ogiwara.kotlin.aileen2.service.BackgroundAudioService.ACTIONS.ACTION_PREVIOUS
import jp.ogiwara.kotlin.aileen2.service.BackgroundAudioService.ACTIONS.ACTION_STOP
import jp.ogiwara.kotlin.aileen2.service.BackgroundAudioService.SerializeAbleString.YOUTUBE_TYPE
import jp.ogiwara.kotlin.aileen2.service.BackgroundAudioService.SerializeAbleString.YOUTUBE_TYPE_VIDEO_LIST
import jp.ogiwara.kotlin.aileen2.utils.Networker
import jp.ogiwara.kotlin.aileen2.utils.PositionQueue
import jp.ogiwara.kotlin.aileen2.utils.Settings
import jp.ogiwara.kotlin.aileen2.utils.YOUTUBE_BASE_URL
import java.io.IOException

/**
 * ビデオ再生サービス。
 * 単一の動画や、プレイリストの再生にも対応
 *
 * Intent内容
 * action play,pause,next..
 * SerializeAble
 * -ItemType at YOUTUBE_TYPE
 * -PositionQueue at YOUTUBE_TYPE_VIDEO_LIST
 */
class BackgroundAudioService: Service() {

    val TAG = BackgroundAudioService::class.simpleName

    //region constants
    object SerializeAbleString{
        val YOUTUBE_TYPE = "YT_MEDIA_TYPE"
        val YOUTUBE_TYPE_VIDEO_LIST = "YT_VIDEO_LIST"
    }

    object ACTIONS{
        val ACTION_PLAY = "action_play"
        val ACTION_PAUSE = "action_pause"
        val ACTION_NEXT = "action_next"
        val ACTION_PREVIOUS = "action_previous"
        val ACTION_STOP = "action_stop"
    }

    enum class ItemType{
        YOUTUBE_MEDIA_NONE,
        YOUTUBE_MEDIA_TYPE_VIDEO_LIST,
    }
    //endregion

    var playQueue = PositionQueue<Video>()

    val mediaPlayer = MediaPlayer()
    var session: MediaSessionCompat? = null
    var controller: MediaControllerCompat? = null

    override fun onBind(intent: Intent?) = null

    override fun onCreate() {
        super.onCreate()
        initMediaPlayer()
        initMediaSessions()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleIntent(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    fun handleIntent(intent: Intent?){
        if(intent == null || intent.action == null )
            return

        val action = intent.action
        when(action){
            ACTION_PLAY -> {
                handleMedia(intent)
                controller?.transportControls?.play()
            }
            ACTION_PAUSE -> {
                controller?.transportControls?.pause()
            }
            ACTION_NEXT -> {
                controller?.transportControls?.skipToNext()
            }
            ACTION_PREVIOUS -> {
                controller?.transportControls?.skipToPrevious()
            }
            ACTION_STOP -> {
                controller?.transportControls?.stop()
            }
        }
    }

    fun handleMedia(intent: Intent){
        var intentMediaType = ItemType.YOUTUBE_MEDIA_NONE
        if(intent.getSerializableExtra(YOUTUBE_TYPE) != null)
            intentMediaType = intent.getSerializableExtra(YOUTUBE_TYPE) as ItemType

        when(intentMediaType){
            ItemType.YOUTUBE_MEDIA_NONE -> {
                mediaPlayer.start()
            }
            ItemType.YOUTUBE_MEDIA_TYPE_VIDEO_LIST-> {
                //playQueue = @Suppress("UNCHECKED_CAST")(intent.getSerializableExtra(YOUTUBE_TYPE_VIDEO_LIST) as PositionQueue<Video>)
                playQueue = CommonRecyclerAdapter.TEST.list
                playVideo()
            }
            else -> {
                Log.w(TAG,"Unknown command")
            }
        }
    }

    //region init
    fun initMediaPlayer(){
        mediaPlayer.setOnCompletionListener {
            playNext()
        }
    }

    fun initMediaSessions(){
        mediaPlayer.setWakeMode(applicationContext,PowerManager.PARTIAL_WAKE_LOCK)

        val buttonReceiveIntent = PendingIntent.getBroadcast(
                applicationContext,0,
                Intent(Intent.ACTION_MEDIA_BUTTON),
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        session = MediaSessionCompat(applicationContext,"simple player session",
                null,buttonReceiveIntent)

        try{
            controller = MediaControllerCompat(applicationContext,session!!.sessionToken)

            session?.setCallback(object : MediaSessionCompat.Callback(){
                override fun onPlay() {
                    super.onPlay()
                    playVideo()
                    buildNotification(generateAction(android.R.drawable.ic_media_pause,"Pause",ACTION_PAUSE))
                }

                override fun onPause() {
                    super.onPause()
                    pauseVideo()
                    buildNotification(generateAction(android.R.drawable.ic_media_play,"Play",ACTION_PLAY))
                }

                override fun onStop() {
                    super.onStop()
                    stopVideo()
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(1)
                    val intent = Intent(applicationContext,BackgroundAudioService::class.java)
                    stopService(intent)
                }

                override fun onSkipToNext() {
                    super.onSkipToNext()
                    playNext()
                    buildNotification(generateAction(android.R.drawable.ic_media_pause,"Pause",ACTION_PAUSE))
                }

                override fun onSkipToPrevious() {
                    super.onSkipToPrevious()
                    playPrevious()
                    buildNotification(generateAction(android.R.drawable.ic_media_pause,"Pause",ACTION_PAUSE))
                }
            })
        }catch (re: RemoteException){
            re.printStackTrace()
        }
    }
    //endregion

    //region video controller
    fun playVideo(){
        extractUrlAndPlay(playQueue.current())
    }

    fun pauseVideo(){
        mediaPlayer.pause()
    }

    fun resumeVideo(){
        mediaPlayer.start()
    }

    fun stopVideo(){
        mediaPlayer.stop()
    }

    fun playNext(){
        extractUrlAndPlay(playQueue.next())
    }

    fun playPrevious(){
        extractUrlAndPlay(playQueue.previous())
    }
    //endregion

    //region notification
    fun buildNotification(action: NotificationCompat.Action){
        val style = android.support.v7.app.NotificationCompat.MediaStyle()

        val intent = Intent(applicationContext,BackgroundAudioService::class.java)
        intent.action = ACTION_STOP
        val stopPendingIntent = PendingIntent.getService(applicationContext,1,intent,0)

        val clickIntent = Intent(this,MainActivity::class.java)
        clickIntent.action = Intent.ACTION_MAIN
        clickIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val clickPendingIntent = PendingIntent.getActivity(applicationContext,0,clickIntent,0)

        style.setShowActionsInCompactView(0)
        val builder = NotificationCompat.Builder(applicationContext)
        builder.setSmallIcon(R.mipmap.aileen2)
        //TODO Replace with other text
        builder.setContentTitle("TITLE")
        builder.setContentInfo("INFO")
        builder.setShowWhen(false)
        builder.setContentIntent(clickPendingIntent)
        builder.setDeleteIntent(stopPendingIntent)
        builder.setOngoing(false)
        builder.setSubText("SUB TEXT")
        builder.setStyle(style)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1,builder.build())
    }

    fun generateAction(icon: Int,title: String,intentAction: String): NotificationCompat.Action{
        val intent = Intent(applicationContext,BackgroundAudioService::class.java)
        intent.action = intentAction
        val pendingIntent = PendingIntent.getService(applicationContext,1,intent,0)
        return NotificationCompat.Action.Builder(icon,title,pendingIntent).build()
    }
    //endregion

    fun extractUrlAndPlay(video: Video){
        val link = YOUTUBE_BASE_URL + video.id
        Log.i(TAG,"Start playing for: ${video.title}")
        object: YouTubeExtractor(this){
            override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, videoMeta: VideoMeta?) {
                if(ytFiles == null){
                    Toast.makeText(applicationContext,"ERROR Play",Toast.LENGTH_SHORT).show()
                    return
                }
                val ytFile = getQuality(ytFiles)
                try{
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(ytFile.url)
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    mediaPlayer.prepare()
                    mediaPlayer.start()

                    Toast.makeText(baseContext,videoMeta?.title,Toast.LENGTH_SHORT).show()
                }catch (io: IOException){
                    io.printStackTrace()
                }
            }
        }.execute(link)
    }

    fun getQuality(ytFiles: SparseArray<YtFile>): YtFile{
        if(Networker.isMobileInternet(applicationContext)){
            val quality = Settings.getVideoQuality(applicationContext)
            when(quality){
                Settings.VideoQuality.HIGH -> {
                    return Networker.getAsHigherQuality(ytFiles)
                }
                Settings.VideoQuality.MIDDLE -> {
                    return ytFiles.get(251) ?: ytFiles.get(141) ?: ytFiles.get(17)
                }
                Settings.VideoQuality.LOW -> {
                    return ytFiles.get(17)
                }
                else -> {
                    return ytFiles.get(17)
                }
            }
        }else{
            return Networker.getAsHigherQuality(ytFiles)
        }
    }
}