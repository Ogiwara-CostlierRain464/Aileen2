package jp.ogiwara.kotlin.aileen2.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import jp.ogiwara.kotlin.aileen2.R
import jp.ogiwara.kotlin.aileen2.model.Video
import jp.ogiwara.kotlin.aileen2.utils.switchVisibly
import kotlin.properties.Delegates

abstract class BaseFragment: Fragment(),SwipeRefreshLayout.OnRefreshListener{

    var needLogin: LinearLayout by Delegates.notNull<LinearLayout>()
    var progress: ProgressBar by Delegates.notNull<ProgressBar>()
    var recycler: RecyclerView by Delegates.notNull<RecyclerView>()
    var refresh: SwipeRefreshLayout by Delegates.notNull<SwipeRefreshLayout>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val row = inflater.inflate(R.layout.fragment_common, container, false)

        needLogin = row.findViewById(R.id.need_login_layout) as LinearLayout
        progress = row.findViewById(R.id.progress_bar_layout).findViewById(R.id.progress_bar) as ProgressBar
        recycler = row.findViewById(R.id.video_container_layout).findViewById(R.id.recycler_view) as RecyclerView
        refresh = row.findViewById(R.id.video_container_layout) as SwipeRefreshLayout

        refresh.setColorSchemeColors(R.color.black)
        refresh.setOnRefreshListener(this)

        recycler.layoutManager = LinearLayoutManager(context)

        return row
    }

    abstract override fun onRefresh()

    /**
     * Videoをビューに反映する
     */
    abstract fun setVideos(videos: ArrayList<Video>)
    /**
     * Viewの可視を切り替え
     * @param visibly ログイン時にtrue(Except TopChart)
     */
    fun switchVisibly(visibly: Boolean){
        if(visibly){
            needLogin.switchVisibly(false)
            progress.switchVisibly(true)
            recycler.switchVisibly(true)
        }else{
            needLogin.switchVisibly(true)
            progress.switchVisibly(false)
            recycler.switchVisibly(false)
        }
    }

}