package com.sinkleader.install.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.loopj.android.http.AsyncHttpClient
import com.sinkleader.install.R
import com.sinkleader.install.util.PrefMgr
import org.json.JSONObject

class FcmLibrary {
    interface OnResponseListener {
        fun onSuccessResponse(json: JSONObject?)
        fun onError(code: Int, msg: String?)
    }

    var asyncHttpClient = AsyncHttpClient(true, 80, 443)
    var mContext: Context? = null
    var token: String? = ""
    var serverURL = ""
    fun init(context: Context?) {
        mContext = context
        val token = PrefMgr.instance.getString("fcm_token", "")
        if (token != "") {
            this.token = token
        }
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener{task ->
                    if (!task.isSuccessful) {
                        Log.w("onComplete", "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    val token : String = task.result!!

                    // Log and toast
                    val msg = mContext!!.getString(R.string.msg_token_fmt, token)
                    Log.d("onComplete", msg)
                    PrefMgr.instance.put("fcm_token", token)
                    this.token = token
                }
            )
        FirebaseMessaging.getInstance().isAutoInitEnabled = true
        setHttpClientProperty()
    }

    private fun setHttpClientProperty() {
        asyncHttpClient.maxConnections = AsyncHttpClient.DEFAULT_MAX_CONNECTIONS
        asyncHttpClient.setMaxRetriesAndTimeout(AsyncHttpClient.DEFAULT_MAX_RETRIES, MAX_TIMEOUT)
        //        asyncHttpClient.getHttpClient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
    }

    fun setNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                mContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel = NotificationChannel(
                PUSH_CHANNEL_ID,
                PUSH_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = "channel description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationChannel.vibrationPattern = longArrayOf(100, 200, 100, 200)
            notificationChannel.importance = NotificationManager.IMPORTANCE_HIGH
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            manager.createNotificationChannel(notificationChannel)
        }
    }

    fun isNotificationChannelEnabled(channelId: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!channelId.isEmpty()) {
                val manager =
                    mContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = manager.getNotificationChannel(channelId) ?: return false
                return channel.importance != NotificationManager.IMPORTANCE_NONE
            }
            false
        } else {
            NotificationManagerCompat.from(mContext!!).areNotificationsEnabled()
        }
    }

    fun setNotificationChannelEnabled(channelId: String, isONOFF: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!channelId.isEmpty()) {
                val manager =
                    mContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = manager.getNotificationChannel(channelId) ?: return
                //                return  != NotificationManager.IMPORTANCE_NONE;
                if (isONOFF) {
                    channel.importance = NotificationManager.IMPORTANCE_HIGH
                } else {
                    channel.importance = NotificationManager.IMPORTANCE_NONE
                }
                manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            }
        } else {
        }
    }

    companion object {
        private var mLibrary: FcmLibrary? = null
        @JvmStatic
        val instance: FcmLibrary?
            get() {
                if (mLibrary == null) {
                    mLibrary = FcmLibrary()
                }
                return mLibrary
            }
        const val PUSH_CHANNEL_ID = "channel_01"
        const val PUSH_CHANNEL_NAME = "Push Channel"
        var IconResorce = 0
        private const val MAX_TIMEOUT = 10 * 60 * 1000 // 5min
    }
}