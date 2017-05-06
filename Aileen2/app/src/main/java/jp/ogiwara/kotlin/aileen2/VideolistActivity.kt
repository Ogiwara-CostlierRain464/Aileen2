package jp.ogiwara.kotlin.aileen2

import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.widget.ProgressBar
import jp.ogiwara.kotlin.aileen2.model.Video
import kotlin.properties.Delegates

abstract class VideolistActivity : AppCompatActivity() {

    open val TAG = VideolistActivity::class.simpleName

    var toolbar: Toolbar by Delegates.notNull<Toolbar>()
    var recycler: RecyclerView by Delegates.notNull<RecyclerView>()
    var progress: ProgressBar by Delegates.notNull<ProgressBar>()


    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        setContentView(R.layout.video_list_activity)

        Log.i(TAG,"onCreate")

        toolbar = findViewById(R.id.toolbar) as Toolbar
        recycler = findViewById(R.id.video_container_layout).findViewById(R.id.recycler_view) as RecyclerView
        progress = findViewById(R.id.progress_bar_layout).findViewById(R.id.progress_bar) as ProgressBar

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    abstract fun setVideos(videos: ArrayList<Video>)

    fun setTitle(title: String){
        toolbar.title = title
    }
}