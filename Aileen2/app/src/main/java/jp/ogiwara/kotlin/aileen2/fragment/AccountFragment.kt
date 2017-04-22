package jp.ogiwara.kotlin.aileen2.fragment


import android.Manifest
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.PermissionChecker
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import com.squareup.picasso.Picasso

import jp.ogiwara.kotlin.aileen2.R
import jp.ogiwara.kotlin.aileen2.manager.AccountManager
import jp.ogiwara.kotlin.aileen2.utils.*
import java.io.IOException
import kotlin.properties.Delegates


class AccountFragment : Fragment(),SwipeRefreshLayout.OnRefreshListener{

    var listView: ListView by Delegates.notNull<ListView>()
    var refresh: SwipeRefreshLayout by Delegates.notNull<SwipeRefreshLayout>()
    var accountImage: CircleImageView by Delegates.notNull<CircleImageView>()
    var accountName: TextView by Delegates.notNull<TextView>()
    var loginButton: Button by Delegates.notNull<Button>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val row = inflater.inflate(R.layout.fragment_account, container, false)
        listView = row.findViewById(R.id.accountItem) as ListView
        refresh = row.findViewById(R.id.refresh_view) as SwipeRefreshLayout
        accountImage = row.findViewById(R.id.account_icon) as CircleImageView
        accountName = row.findViewById(R.id.account_name) as TextView
        loginButton = row.findViewById(R.id.login) as Button

        refresh.setOnRefreshListener(this)

        loginButton.setOnClickListener {
            if(PermissionChecker.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS)
                    != PackageManager.PERMISSION_GRANTED){
                getPermission(activity,Manifest.permission.GET_ACCOUNTS)
            }else{
                AccountManager.login()
            }
        }
        return row
    }

    override fun onStart() {
        super.onStart()
        loadView()
    }

    override fun onRefresh() {
        loadView()
    }

    fun loadView(){
        if(AccountManager.isLogin){
            AccountInfoTask().execute()
        }else{
            refresh.isRefreshing = false
        }
    }

    inner class AccountInfoTask(): AsyncTask<Unit,Unit,Unit>(){

        var accountName: String? = null
        var accountIconUrl: String? = null

        override fun doInBackground(vararg params: Unit?) {
            try {
                val youTube = YouTube.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        JacksonFactory(),
                        AccountManager.credential
                ).setYouTubeRequestInitializer(
                        YouTubeRequestInitializer(API_KEY)
                ).setApplicationName(APP_NAME)
                        .build()


                val account = youTube.channels().list("id,snippet")
                account.fields = "items"
                account.mine = true
                val res = account.execute()
                val me = res.items.first()
                accountName = me.snippet.title
                accountIconUrl = me.snippet.thumbnails.high.url
            }catch (e: IOException){
                e.printStackTrace()
            }
        }

        override fun onPostExecute(result: Unit?) {
            super.onPostExecute(result)
            this@AccountFragment.accountName.text = accountName
            Picasso.with(this@AccountFragment.context).load(accountIconUrl).into(this@AccountFragment.accountImage )
            loginButton.switchVisibly(false)
            refresh.isRefreshing = false
        }
    }

}// Required empty public constructor
