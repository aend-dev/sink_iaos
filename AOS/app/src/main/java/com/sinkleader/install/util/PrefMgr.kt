package com.sinkleader.install.util

import android.content.Context
import android.content.SharedPreferences
import com.sinkleader.install.MyApplication


class PrefMgr {
    var prefs: SharedPreferences? = null

    init {
        prefs = MyApplication.globalApplicationContext?.getSharedPreferences("SLInstall_Preference", Context.MODE_PRIVATE)
    }

    fun put(key: String?, value: Any) {
        val editor = prefs!!.edit()
        if (value.javaClass == String::class.java) {
            editor.putString(key, value as String)
        } else if (value.javaClass == Boolean::class.java) {
            editor.putBoolean(key, (value as Boolean))
        } else if (value.javaClass == Int::class.java) {
            editor.putInt(key, (value as Int))
        } else if (value.javaClass == Float::class.java || value.javaClass == Double::class.java) {
            editor.putFloat(key, (value as Float))
        }
        editor.commit()
    }

    fun getString(key: String?, defaultValue: String?): String? {
        return prefs!!.getString(key, defaultValue)
    }

    fun getFloat(key: String?, defaultValue: Float): Float {
        return prefs!!.getFloat(key, defaultValue)
    }

    fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
        return prefs!!.getBoolean(key, defaultValue)
    }

    fun getInt(key: String?, defaultValue: Int): Int {
        return prefs!!.getInt(key, defaultValue)
    }

    companion object {
        @kotlin.jvm.JvmField
        var instance: PrefMgr = PrefMgr()
    }
}