package com.sinkleader.install

import android.util.Log
import androidx.multidex.MultiDexApplication
import com.sinkleader.install.util.Util
import com.sinkleader.install.fcm.FcmLibrary
import com.sinkleader.install.network.HttpRequestHelper
import com.sinkleader.install.util.PrefMgr

class MyApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        globalApplicationContext = this

        HttpRequestHelper.instance.checkNetworkAvailable(this, true)

        //FCM
        FcmLibrary.instance?.init(applicationContext)
        FcmLibrary.instance?.setNotificationChannel()
        FcmLibrary.instance?.serverURL = ""

        val release = BuildConfig.BUILD_TYPE

        if(release.equals("release")){
            Util.ServerIndex = 0
            Util.FrontIndex = 0
        }else{
            val index = PrefMgr.instance.getString("ServerIndex", "1")
            val s = Integer.parseInt(index!!)
            Log.d("ServerIndex", index.toString())
            Util.ServerIndex = s
            Util.FrontIndex = s
        }
    }

    override fun onTerminate() {
        HttpRequestHelper.instance.terminateNetworkCallback(this)
        super.onTerminate()
    }

    companion object {
        /**
         * singleton 애플리케이션 객체를 얻는다.
         *
         * @return singleton 애플리케이션 객체
         */
        @Volatile
        var globalApplicationContext: MyApplication? = null
    }
}