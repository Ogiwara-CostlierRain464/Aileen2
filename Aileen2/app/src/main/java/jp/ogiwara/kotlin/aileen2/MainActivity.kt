package jp.ogiwara.kotlin.aileen2

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.MenuItemCompat
import android.support.v4.view.ViewPager
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeScopes
import jp.ogiwara.kotlin.aileen2.fragment.AccountFragment
import jp.ogiwara.kotlin.aileen2.fragment.PlaylistFragment
import jp.ogiwara.kotlin.aileen2.fragment.SubscribeFragment
import jp.ogiwara.kotlin.aileen2.fragment.TopChartFragment
import jp.ogiwara.kotlin.aileen2.manager.AccountManager
import jp.ogiwara.kotlin.aileen2.utils.API_ACCESS_SCOPE
import jp.ogiwara.kotlin.aileen2.utils.Settings
import jp.ogiwara.kotlin.aileen2.utils.dialog
import jp.ogiwara.kotlin.aileen2.utils.snack
import java.io.IOException
import java.util.*

const val REQUEST_PERMISSION = 0
const val REQUEST_ACCOUNT_PICKER = 1
const val REQUEST_GOOGLE_PLAY_SERVICES = 2
const val REQUEST_AUTHORIZATION = 3

class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.simpleName

    object CASH {
        var ACCOUNT_NAME: String? = null
    }

    //region override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Settings.init(applicationContext)
        AccountManager.init(this)

        if(savedInstanceState == null && Intent.ACTION_SEND == intent.action
                && intent.type != null && "text/plain" == intent.type)
            handleIntent(intent)

        initView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.tool_bar,menu)
        initSearch(menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        handleMenu(item)
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        Settings.fin(applicationContext)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        class LoginChecker(val accountName: String): AsyncTask<Unit, Unit, Unit>(){

            override fun doInBackground(vararg params: Unit?) {
                try{
                    val youTube = YouTube.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            JacksonFactory(),
                            GoogleAccountCredential.usingOAuth2(this@MainActivity, Arrays.asList(API_ACCESS_SCOPE))
                                    .setBackOff(ExponentialBackOff())
                                    .setSelectedAccountName(accountName)
                    ).setApplicationName(getString(R.string.app_name)).build()

                    val lists = youTube.playlists().list("id")
                    lists.fields = "items(id)"
                    lists.mine = true
                    lists.execute()
                }catch (availabilityException: GooglePlayServicesAvailabilityException){
                    this@MainActivity.runOnUiThread {
                        GooglePlayServicesUtil.getErrorDialog(
                                availabilityException.connectionStatusCode,this@MainActivity, REQUEST_GOOGLE_PLAY_SERVICES
                        ).show()
                    }
                    CASH.ACCOUNT_NAME = accountName
                    cancel(true)
                }catch (userRecover: UserRecoverableAuthIOException){
                    this@MainActivity.runOnUiThread {
                        this@MainActivity.startActivityForResult(userRecover.intent, REQUEST_AUTHORIZATION)
                    }
                    CASH.ACCOUNT_NAME = accountName
                    cancel(true)
                }catch (io: IOException){
                    io.printStackTrace()
                    this@MainActivity.runOnUiThread {
                        this@MainActivity.dialog(getString(R.string.connection_error),io.javaClass.simpleName)
                    }
                    cancel(true)
                }
            }

            override fun onPostExecute(result: Unit?) {
                super.onPostExecute(result)
                AccountManager.confirmLogin(accountName)
            }
        }

        when(requestCode){
            REQUEST_ACCOUNT_PICKER -> {
                if(resultCode == Activity.RESULT_OK && data != null && data.extras != null){
                    val account = data.extras.getString(android.accounts.AccountManager.KEY_ACCOUNT_NAME)
                    if(account != null){
                        LoginChecker(account).execute()
                    }
                }
            }
            REQUEST_GOOGLE_PLAY_SERVICES -> {
                if(resultCode == Activity.RESULT_OK)
                    LoginChecker(CASH.ACCOUNT_NAME as String).execute()
            }
            REQUEST_AUTHORIZATION -> {
                if(resultCode == Activity.RESULT_OK)
                    LoginChecker(CASH.ACCOUNT_NAME as String).execute()
            }
        }
    }
    //endregion

    //region view
    fun initView(){
        class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm){
            override fun getItem(position: Int): Fragment {
                return when(position){
                    0 -> TopChartFragment()
                    1 -> SubscribeFragment()
                    2 -> PlaylistFragment()
                    3-> AccountFragment()
                    else -> AccountFragment()
                }
            }
            override fun getCount() = 4
        }

        val tabLayout = findViewById(R.id.tab_layout) as TabLayout
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        val toolbarTitle = findViewById(R.id.toolbar_title) as TextView
        val viewpager = findViewById(R.id.view_pager) as ViewPager

        toolbar.title = ""
        toolbarTitle.text = getString(R.string.top_chart)
        viewpager.adapter = ViewPagerAdapter(supportFragmentManager)
        viewpager.offscreenPageLimit = 4//キャッシュ

        tabLayout.setupWithViewPager(viewpager)
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                when(tab.position){
                    0 -> toolbarTitle.text = getString(R.string.top_chart)
                    1 -> toolbarTitle.text = getString(R.string.subscribe)
                    2 -> toolbarTitle.text = getString(R.string.playlist)
                    3 -> toolbarTitle.text = getString(R.string.account)
                    else -> toolbarTitle.text = getString(R.string.account)
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })
        setSupportActionBar(toolbar)

        tabLayout.getTabAt(0)?.setIcon(R.mipmap.top_chart)
        tabLayout.getTabAt(1)?.setIcon(R.mipmap.subscriptions)
        tabLayout.getTabAt(2)?.setIcon(R.mipmap.list)
        tabLayout.getTabAt(3)?.setIcon(R.mipmap.account)
    }

    fun initSearch(menu: Menu){
        val menuItem = menu.findItem(R.id.action_search)

        val searchView = MenuItemCompat.getActionView(menuItem) as SearchView
        searchView.setIconifiedByDefault(true)
        searchView.isSubmitButtonEnabled = false
        searchView.queryHint = getString(R.string.search_hint)

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                val intent = Intent(applicationContext,SearchActivity::class.java)
                intent.putExtra("search_word",query)
                startActivity(intent)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }
    //endregion

    //region handler
    fun handleIntent(intent: Intent){
        val ytLink = intent.getStringExtra(Intent.EXTRA_TEXT)
        Log.i(TAG,"handle intent for $ytLink")
        if(ytLink != null && (ytLink.contains("://youtu.be/") || ytLink.contains("youtube.com/watch?v="))){
            //TODO start video
        }else{
            snack(getString(R.string.invalid_link))
        }
    }

    fun handleMenu(item: MenuItem){
        when(item.itemId){
            R.id.settings -> {
                val intent = Intent(applicationContext,SettingActivity::class.java)
                startActivity(intent)
            }
            R.id.about -> {
                dialog(getString(R.string.app_name),getString(R.string.author),R.mipmap.aileen2)
            }
        }
    }
    //endregion
}
