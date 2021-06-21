package com.sinkleader.install.network

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.jvm.Throws

object JSONParser {
    @Throws(JSONException::class)
    fun getJSONObject(json: JSONObject, paramName: String): JSONObject {

        if (!json.has(paramName)) { //            throw new JSONException("have no this parameter");
            return JSONObject()
        }
        return if (json[paramName].javaClass == JSONObject::class.java) {
            json.getJSONObject(paramName)
        } else {
            JSONObject()
        }
    }

    @JvmStatic
    @Throws(JSONException::class)
    fun getInt(json: JSONObject, paramName: String): Int {
        if (!json.has(paramName)) { //            throw new JSONException("have no this parameter");
            return 0
        }
        return if (json[paramName].javaClass == Integer::class.java) {
            json.getInt(paramName)
        } else if (json[paramName].javaClass == String::class.java) {
            try {
                getString(json, paramName).toInt()
            } catch (e: Exception) {
                0
            }
        } else {
            0
        }
    }

    @JvmStatic
    @Throws(JSONException::class)
    fun getString(json: JSONObject, paramName: String): String {
        if (!json.has(paramName)) { //            throw new JSONException("have no this parameter");
            return ""
        }
        return if (json[paramName].javaClass == String::class.java) {
            json.getString(paramName)
        } else {
            ""
        }
    }

    @Throws(JSONException::class)
    fun getJSONArray(json: JSONObject, paramName: String): JSONArray {
        val jsonArray = JSONArray()
        if (!json.has(paramName)) { //            throw new JSONException("have no this parameter");
            return jsonArray
        }
        return if (json[paramName].javaClass == JSONArray::class.java) {
            json.getJSONArray(paramName)
        } else {
            jsonArray
        }
    }

    @Throws(JSONException::class)
    fun getFloat(json: JSONObject, paramName: String): Float {
        if (!json.has(paramName)) { //            throw new JSONException("have no this parameter");
            return 0.0f
        }
        return if (json[paramName].javaClass == String::class.java) {
            try {
                getString(json, paramName).toFloat()
            } catch (e: Exception) {
                0
            } as Float
        } else {
            json.getDouble(paramName).toFloat()
        }
    }

    @Throws(JSONException::class)
    fun getBoolean(json: JSONObject, paramName: String): Boolean {
        if (!json.has(paramName)) {
            return false
        }
        return json.getBoolean(paramName)
    }
}