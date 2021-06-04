package com.sinkleader.install.ui.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.*
import com.sinkleader.install.BuildConfig
import com.sinkleader.install.R
import com.sinkleader.install.network.HttpRequestHelper
import com.sinkleader.install.network.JSONParser
import com.sinkleader.install.ui.view.ConfirmDialog
import com.sinkleader.install.util.Constant
import com.sinkleader.install.util.Device
import com.sinkleader.install.util.PrefMgr
import com.sinkleader.install.util.Util
import cz.msebera.android.httpclient.entity.StringEntity
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : BaseActivity() {
    var editID : EditText? = null
    var editPASS : EditText? = null

    var checkID : CheckBox? = null
    var checkAuto : CheckBox? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setFinishAppWhenPressedBackKey(true)

        val cookieManager = CookieManager.getInstance()
        cookieManager.removeAllCookies { isRemove: Boolean? -> }

        WebStorage.getInstance().deleteAllData()

        initView()
        if (! hasPermission(Constant.Permission_Login)){
            requestPermission(this, Constant.Permission_Login, Constant.RC_PERMISSION_LOGIN)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults == null) {
            Toast.makeText(this, "$requestCode 권한 결과 값을 받아오지 못하였습니다.", Toast.LENGTH_LONG).show()
            return
        }
    }

    fun initView(){
        val id = PrefMgr.instance.getString("user_id", "")!!
        editID = findViewById(R.id.edit_id_login)
        editID?.setText(id)
        editPASS = findViewById(R.id.edit_pw_login)

        checkID = findViewById(R.id.check_save_login)
        if (id.length != 0){
            checkID?.isChecked = true
        }
        checkAuto = findViewById(R.id.check_auto_login)

        val signIn : Button = findViewById(R.id.button_login)
        signIn.setOnClickListener {
            requestLOGIN(this)
        }

        val findpass = findViewById<RelativeLayout>(R.id.layout_find_login)
        findpass.setOnClickListener {
            val intent = Intent(this, PasswordActivity::class.java)
            startActivity(intent)
        }

        val txt_call = findViewById<TextView>(R.id.txt_phone_login)
        txt_call.setOnClickListener {
//            val intent = Intent(this, QRscenActivity::class.java)
//            startActivity(intent)
            val tel = getString(R.string.phone_number)
            val tt = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
            startActivity(tt)
        }

        val img_call = findViewById<ImageView>(R.id.img_phone_login)
        img_call.setOnClickListener {
            val tel = getString(R.string.phone_number)
            val tt = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
            startActivity(tt)
        }
    }

    private fun requestLOGIN(activity: Activity) {
        var entity = StringEntity("")
        val jsonEntity = JSONObject()
        try {
            val device : JSONObject = Device.getInfo(this)

            var id = editID?.text.toString()
            var pass = editPASS?.text.toString()

//            id = "aend001"
//            pass = "admin1234!!"

            var errText = ""
            if (id.length == 0) {
                errText = "아이디를 입력하세요."
            }

            if (errText != "") {
                ConfirmDialog(this, errText, "확인", {}).show()
                return
            }

            jsonEntity.put("user_id", id)
            jsonEntity.put("password", pass)
            jsonEntity.put("userDevice", device)

            entity = StringEntity(jsonEntity.toString(), "UTF-8")

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val url: String = Constant.Serverlist.get(Util.ServerIndex) + "/v1/login/login"
        val httpReqHelper: HttpRequestHelper = HttpRequestHelper.instance
        httpReqHelper.init(activity, this, object : HttpRequestHelper.OnParseResponseListener {
            @Throws(JSONException::class)
            override fun onSuccessResponse(apiName: String?, response: JSONObject?) {
                Log.d(apiName, response.toString())
                val result: Int = JSONParser.getInt(response!!, "result")
                if (result == 0) {
                    val data: JSONObject = JSONParser.getJSONObject(response, "data")
                    val user: JSONObject = JSONParser.getJSONObject(data, "user")

                    val token: String = JSONParser.getString(user, "token")
                    if (token == "") {

                    } else {
                        val user_sno = JSONParser.getInt(user, "user_seq")

                        if (checkID?.isChecked!!){
                            PrefMgr.instance.put("user_id", editID?.text!!.toString())
                        }

                        if (checkAuto?.isChecked!!){
                            PrefMgr.instance.put("token", token)
                            PrefMgr.instance.put("user_sno", String.format("%d", user_sno))
                        }

                        Util.moveWebPage(this@LoginActivity, user, "")
                    }
                } else {
                    val message: String = JSONParser.getString(response, "message")
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(code: Int, msg: String?) {
                Log.e("onError", "$msg $code")
                activity.runOnUiThread {
                    val dialog = ConfirmDialog(activity, msg, "확인", null)
                    dialog.show()
                }

            }
        }, "requestLOGIN", false)

        httpReqHelper.postJsonRequest(activity, url, entity)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (BuildConfig.BUILD_TYPE == "debug") {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if (Util.FrontIndex < Constant.FrontUrls.size - 1) {
                    Util.FrontIndex += 1
                    Toast.makeText(
                        this,
                        "Server 변경 : " + Constant.Serverlist.get(Util.FrontIndex),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Util.FrontIndex = 0
                    Toast.makeText(
                        this,
                        "Server 변경 : " + Constant.Serverlist.get(Util.FrontIndex),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Util.changeServer()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}