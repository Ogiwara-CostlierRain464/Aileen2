package jp.ogiwara.kotlin.aileen2

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceFragment

class SettingActivity: AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO Toolbar sucks!
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: MutableList<Header>?) {
        loadHeadersFromResource(R.xml.pref_headers,target)
    }

    override fun isValidFragment(fragmentName: String?): Boolean {
        return (DataFragment::class.java.name == fragmentName) or
                (HistoryFragment::class.java.name == fragmentName)
    }

    class DataFragment: PreferenceFragment(){

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_data)
        }

    }

    class HistoryFragment: PreferenceFragment(){
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_history)
        }
    }

}