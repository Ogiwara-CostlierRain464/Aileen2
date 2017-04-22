package jp.ogiwara.kotlin.aileen2.manager

import android.app.Activity
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTubeScopes
import jp.ogiwara.kotlin.aileen2.REQUEST_ACCOUNT_PICKER
import jp.ogiwara.kotlin.aileen2.utils.API_ACCESS_SCOPE
import jp.ogiwara.kotlin.aileen2.utils.Settings
import java.util.*
import kotlin.properties.Delegates
import kotlin.reflect.jvm.internal.impl.javax.inject.Singleton

/**
 * アカウントマネージャー
 * アプリケーションの起動時に{@link AccountManger.init}を実行せよ
 */
@Singleton
object AccountManager{

    var activity: Activity by Delegates.notNull<Activity>()
    var credential = HttpRequestInitializer {}
    var isLogin = Settings.accountName != null

    fun init(anActivity: Activity){
        activity = anActivity
        if(isLogin){
            credential = GoogleAccountCredential.usingOAuth2(activity, Arrays.asList(API_ACCESS_SCOPE))
                    .setBackOff(ExponentialBackOff())
                    .setSelectedAccountName(Settings.accountName)
        }
    }

    fun login(){
        if(isLogin || Settings.accountName != null)
            return

        val testCredential = GoogleAccountCredential.usingOAuth2(activity,Arrays.asList(API_ACCESS_SCOPE))
                .setBackOff(ExponentialBackOff())

        activity.startActivityForResult(testCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
    }

    /**
     * onActivityResultでログインに成功した場合にcredentialとisLoginを更新
     */
    fun confirmLogin(name: String){
        credential = GoogleAccountCredential.usingOAuth2(activity, Arrays.asList(YouTubeScopes.YOUTUBE))
                .setBackOff(ExponentialBackOff())
                .setSelectedAccountName(name)
        Settings.accountName = name
        isLogin = true
    }
}
