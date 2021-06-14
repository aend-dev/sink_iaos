package com.sinkleader.install.ui.activity

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.multidex.BuildConfig
import com.sinkleader.install.R
import com.sinkleader.install.util.Constant
import com.sinkleader.install.util.GpsInfo


open class BaseActivity : AppCompatActivity(), Constant {
    private var m_bFinishAppWhenPressedBackKey = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //initSecureWindow();
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
    }

    override fun startActivity(paramIntent: Intent) {
        super.startActivity(paramIntent)
    }

    override fun onResume() {
        super.onResume()
        // LocalBroadcastManager.getInstance(this).registerReceiver(PushReceiver, new IntentFilter(BroadcastNm.base.value()));
    }

    override fun onPause() { // LocalBroadcastManager.getInstance(this).unregisterReceiver(PushReceiver);
        super.onPause()
    }

    override fun onDestroy() { // LocalBroadcastManager.getInstance(this).unregisterReceiver(PushReceiver);
        super.onDestroy()
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
    }

    private var m_bFinish = false
    override fun onBackPressed() {
        if (m_bFinishAppWhenPressedBackKey) {
            if (!m_bFinish) {
                m_bFinish = true
                Toast.makeText(
                    this,
                    resources.getString(R.string.app_finish_message),
                    Toast.LENGTH_SHORT
                ).show()
                Handler().postDelayed({ m_bFinish = false }, 2000)
            } else {
                finish()
            }
            return
        }
        super.onBackPressed()
    }

    /************************************************************
     * Helper
     */
    open fun getCurPosition(): String {
        var gps_str = "0,0"
        try {
            val gps = GpsInfo(this@BaseActivity)

            if (gps.isGetLocation) {
                gps_str = gps.longitude.toString() + "," + gps.latitude
            }
        } catch (e: Exception) {
            Log.d("getCurPosition", e.message)
        }
        return gps_str
    }


    fun setFinishAppWhenPressedBackKey(bFlag: Boolean) {
        m_bFinishAppWhenPressedBackKey = bFlag
    }

    private fun initSecureWindow() {
        if (!BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    fun hasPermission(permissions: Array<String>): Boolean {
        for (permission in permissions) if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(
                this,
                permission!!
            )) return false
        return true
    }

    fun isPermisionsRevoked(permissions: Array<String>): Boolean {
        var isRevoked = false
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission!!) == PackageManager.PERMISSION_DENIED &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permission) == false) {
                isRevoked = true
                break
            }
        }
        return isRevoked
    }

    open fun gotoSetting(activity: Activity, request: Int) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivityForResult(intent, request)
    }

    fun requestPermission(
        p_context: Activity?,
        p_requiredPermissions: Array<String>,
        requestCode: Int,
    ) {
        ActivityCompat.requestPermissions(p_context!!, p_requiredPermissions, requestCode)
    }

    fun isNotificationChannelEnabled(channelId: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!channelId.isEmpty()) {
                val manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = manager.getNotificationChannel(channelId) ?: return false
                return channel.importance != NotificationManager.IMPORTANCE_NONE
            }
            false
        } else {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        }
    }

    fun openNotificationSettings() {
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        //for Android 5-7
        intent.putExtra("app_package", packageName)
        intent.putExtra("app_uid", applicationInfo.uid)
        // for Android O
        intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
        startActivity(intent)
    }
}