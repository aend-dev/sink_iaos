package com.sinkleader.install.util

import android.content.Context
import android.util.Log
import com.sinkleader.install.fcm.FcmLibrary
import com.sinkleader.install.ui.view.WebViewCustom
import org.json.JSONObject

object Device {
    fun getInfo(context: Context?): JSONObject {
        val obj = JSONObject()
        try {
            obj.put("agent", WebViewCustom.mUserAgent);
            obj.put("dev_model", Util.getDeviceModel(context))
            obj.put("os", "AOS") //
            obj.put("os_version", Util.androidVersion)
            obj.put("dev_ip", Util.iPAddress)
            obj.put("dev_token", FcmLibrary.instance?.token)
            obj.put("app_version", Util.version)

//            if (Build.VERSION.SDK_INT < 29)
//                object.put("uuid", Util.getDeviceId(context));
//            else
//                object.put("uuid", FcmLibrary.getInstance().getToken().substring(0,31));
        } catch (e: Exception) {
            Log.d("DeviceInfo", e.message)
        }
        return obj
    }
}