package jp.ogiwara.kotlin.aileen2.fragment


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.PermissionChecker
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeRequestInitializer
import com.squareup.picasso.Picasso
import jp.ogiwara.kotlin.aileen2.NormalVideolistActivity

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

        val listItems = ArrayList<AccountItem>()
        listItems.add(AccountItem(R.mipmap.history,getString(R.string.history),getString(R.string.history_hint)))
        listItems.add(AccountItem(R.mipmap.label,getString(R.string.labeled),getString(R.string.labeled_hint)))

        listView.adapter = AccountAdapter(context,R.layout.account_list_view_item,listItems)

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

    class AccountAdapter(context: Context, val resource: Int, val items: ArrayList<AccountItem>) : ArrayAdapter<AccountItem>(context,resource,items){

        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = layoutInflater.inflate(resource,null)

            val element = getItem(position)

            val image = view.findViewById(R.id.item_icon) as ImageView
            val message = view.findViewById(R.id.item_message) as TextView
            val hint = view.findViewById(R.id.item_hint) as TextView

            image.setImageResource(element.icon)
            message.text = element.message
            hint.text = element.hint

            view.setOnClickListener {

                val intent = Intent(context,NormalVideolistActivity::class.java)

                when(element.message){
                    context.getString(R.string.history) -> {
                        intent.putExtra("videoIds",ArrayList(Settings.history.toArray().toList()))
                    }
                    context.getString(R.string.labeled) -> {
                        intent.putExtra("videoIds",ArrayList(Settings.labeled.toArray().toList()))
                    }
                    else -> {
                        assert(false)
                    }
                }
                intent.putExtra("title",element.message)
                context.startActivity(intent)
            }

            return view
        }
    }
}

data class AccountItem(val icon: Int,val message: String,val hint: String)
