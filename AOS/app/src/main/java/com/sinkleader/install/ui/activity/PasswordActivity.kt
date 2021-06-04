package com.sinkleader.install.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import cz.msebera.android.httpclient.entity.StringEntity
import com.sinkleader.install.R
import com.sinkleader.install.network.HttpRequestHelper
import com.sinkleader.install.network.HttpRequestHelper.Companion.instance
import com.sinkleader.install.network.JSONParser
import com.sinkleader.install.ui.view.ConfirmDialog
import com.sinkleader.install.ui.view.ConfirmDialogTwo
import com.sinkleader.install.util.Constant
import com.sinkleader.install.util.Util
import cz.msebera.android.httpclient.protocol.HTTP
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class PasswordActivity : BaseActivity(){
    var activity: PasswordActivity? = null
    private var sms_auth_id = ""
    private var limit_date = ""
    private var gTimer: Timer? = null
    private var limitTime: Long = 0

    var limit: TextView? = null
    var btnRequest: Button? = null
    var btnCheck: Button? = null
    var btnNext: Button? = null

    var gEditID: EditText? = null
    var gEditPASS: EditText? = null

    var gEditPhone: EditText? = null
    var gEditAuth: EditText? = null

    var gOauthPopup : RelativeLayout? = null //popup_password
    var gOauthMain : RelativeLayout? = null //oauthmain_password

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)
        activity = this
        initUI()
        resetData()
    }

    override fun onDestroy() {
        if (gTimer != null) gTimer!!.cancel()
        super.onDestroy()
    }

    private fun initUI() {
        val back : ImageView = findViewById(R.id.back_password)
        back.setOnClickListener { finish() }

        gEditID = findViewById(R.id.edit_id_findpass)
        gEditPhone = findViewById(R.id.edit_phone_password)
        btnRequest = findViewById(R.id.btn_phone_password)


        limit = findViewById(R.id.txt_limit_password)
        gEditAuth = findViewById(R.id.edit_oauth_password)
        btnCheck = findViewById(R.id.btn_oauth_password)

        btnNext = findViewById(R.id.button_password)
        btnNext?.isEnabled = false

        gEditPASS = findViewById(R.id.edit_pw_password)
        gEditPASS?.isEnabled = false

        btnRequest?.setOnClickListener{
            requestFindSMS()
        }

        btnCheck?.setOnClickListener{
            requestAuthSMS()
        }

        btnNext?.setOnClickListener {
            var errText = ""
            if (gEditPASS!!.text.length < 6) {
                errText = "비밀번호를 다시 입력하세요."
            }

            if (errText != "") {
                val dialog = ConfirmDialog(activity, errText, "확인", null)
                dialog.show()
            }else{
                ConfirmDialogTwo(this,"비밀번호를 변경하시겠습니까?","취소", "확인", object : ConfirmDialogTwo.ConfirmDialogListener{
                    override fun onConfirm1() {
                    }

                    override fun onConfirm2() {
                        requestNewPass()
                    }
                }).show()
            }
        }

        gOauthMain =  findViewById(R.id.oauthmain_password)
        gOauthPopup = findViewById(R.id.popup_password)
        gOauthPopup?.visibility = View.INVISIBLE
    }

    private fun changeStatus() {

    }

    private fun resetData() {
        if (gTimer != null) gTimer!!.cancel()
        limit!!.text = ""
        btnCheck!!.isEnabled = true
        btnRequest!!.isEnabled = true
        gEditID?.setText("")
        gEditPhone?.setText("")
        gEditAuth?.setText("")
    }

    ///////////////////////////////////
    // 연동부
    ///////////////////////////////////
    private fun requestFindSMS() {
        var entity : StringEntity
        var obj = JSONObject()

        var type = "SEARCH_PASSWORD"
        var id = gEditID?.text.toString()
        val phone = gEditPhone?.text.toString()

        var errText = ""

        if (id.length == 0) {
            errText = "아이디를 입력하세요."
        }else if (phone.length == 0) {
            errText = "휴대폰 번호를 입력하세요."
        }

        if (errText != "") {
            val dialog = ConfirmDialog(activity, errText, "확인", null)
            dialog.show()
            return
        }

        obj.put("auth_type", type)
        obj.put("cell_phone", phone)
        obj.put("user_id", id)
        entity = StringEntity(obj.toString(), StandardCharsets.UTF_8)
        entity.setContentType("application/json")


        val url: String = Constant.Serverlist.get(Util.ServerIndex) + "/v1/common/sms/send"
        val httpReqHelper: HttpRequestHelper? = instance
        httpReqHelper?.init(this, this, object : HttpRequestHelper.OnParseResponseListener {
            override fun onSuccessResponse(apiName: String?, response: JSONObject?) {
                Log.d(apiName, response.toString())
                val result: Int = JSONParser.getInt(response!!, "result")
                if (result == 0) {
                    val data: JSONObject = JSONParser.getJSONObject(response, "data")
                    sms_auth_id = JSONParser.getString(data, "sms_auth_id")
                    limit_date = JSONParser.getString(data, "limit_date")
                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    try {
                        val date = format.parse(limit_date)
                        val curDate = Date()
                        limitTime = date.time - curDate.time / 1000 * 1000


                        gEditID?.isEnabled = false
                        gEditPhone?.isEnabled = false

                        btnRequest?.setText("재발급")

                        btnCheck?.isEnabled = true

                        val dialog = ConfirmDialog(activity, "인증번호를 전송하였습니다.",  "확인", null)
                        dialog.show()
                        setTimer()
                    } catch (e: ParseException) {
                        e.printStackTrace()
                    }
                } else {
                    val message: String = JSONParser.getString(response, "message")
                    val dialog = ConfirmDialog(activity, message, "확인", null)
                    dialog.show()
                }
            }

            override fun onError(code: Int, msg: String?) {
                Log.e("onError", "$msg $code")
                activity?.runOnUiThread {
                    val dialog = ConfirmDialog(activity, "$msg", "확인", null)
                    dialog.show()
                }
            }
        }, "requestFindSMS", false)
        httpReqHelper?.postJsonRequest(activity, url, entity)
    }

    private fun requestAuthSMS() {
        var entity : StringEntity
        var obj = JSONObject()
        val strNum = gEditAuth!!.text.toString()
        if (strNum == "") {
            val dialog = ConfirmDialog(activity, "인증번호를 입력하세요", "확인", null)
            dialog.show()
            return
        }
        val num = strNum.toInt()

        obj.put("auth_type", "SEARCH_PASSWORD")
        obj.put("sms_auth_id", sms_auth_id)
        obj.put("auth_number", num)
        entity = StringEntity(obj.toString(), "UTF-8")

        val url: String = Constant.Serverlist.get(Util.ServerIndex) + "/v1/common/sms/check"
        val httpReqHelper: HttpRequestHelper? = instance
        httpReqHelper?.init(this, this, object : HttpRequestHelper.OnParseResponseListener {
            override fun onSuccessResponse(apiName: String?, response: JSONObject?) {
                Log.d(apiName, response.toString())
                val result: Int = JSONParser.getInt(response!!, "result")
                val data: JSONObject = JSONParser.getJSONObject(response, "data")
                if (result == 0) {
                    if (data.getBoolean("auth")) {
                        gTimer!!.cancel()
                        limit!!.text = ""
                        btnCheck!!.isEnabled = false

                        gOauthPopup?.visibility = View.VISIBLE //popup_password
                        gOauthMain?.visibility = View.INVISIBLE //oauthmain_password

                        btnNext?.isEnabled = true
                        gEditPASS?.isEnabled = true

                    } else{
                        val dialog = ConfirmDialog(activity, "인증번호가 틀렸습니다.", "확인", null)
                        dialog.show()
                    }
                } else {
                    val message: String = JSONParser.getString(response, "message")
                    val dialog = ConfirmDialog(activity, message, "확인", null)
                    dialog.show()
                }
            }

            override fun onError(code: Int, msg: String?) {
                Log.e("onError", "$msg")
            }
        }, "requestAuthSMS", false)
        httpReqHelper?.putJsonRequest(activity, url, entity)
    }

    private fun requestNewPass() {
        val entity : StringEntity
        val obj = JSONObject()

        val pass = gEditPASS?.text.toString()
        val phone = gEditPhone?.text.toString()
        val id = gEditID?.text.toString()

        obj.put("cell_phone", phone)
        obj.put("password_new", pass)
        obj.put("sms_auth_id", sms_auth_id)
        obj.put("user_id", id)
        entity = StringEntity(obj.toString(), "UTF-8")
        entity.setContentType("application/json")

        val url: String = Constant.Serverlist.get(Util.ServerIndex) + "/v1/user/find/password"
        val httpReqHelper: HttpRequestHelper? = instance
        httpReqHelper?.init(this, this, object : HttpRequestHelper.OnParseResponseListener {
            override fun onSuccessResponse(apiName: String?, response: JSONObject?) {
                Log.d(apiName, response.toString())
                val result: Int = JSONParser.getInt(response!!, "result")
                if (result == 0) {
                    var message: String = JSONParser.getString(response, "message")

                    if (message.length == 0){
                        message = "변경되었습니다."
                    }

                    val dialog = ConfirmDialog(activity, message, "확인"){
                        activity?.finish()
                    }
                    dialog.show()
                } else {
                    val message: String = JSONParser.getString(response, "message")
                    val dialog = ConfirmDialog(activity, message, "확인", null)
                    dialog.show()
                }
            }

            override fun onError(code: Int, msg: String?) {
                Log.e("onError", "$msg $code")

                activity?.runOnUiThread {
                    val dialog = ConfirmDialog(activity, "$msg", "확인", null)
                    dialog.show()
                }
            }
        }, "requestNewPass", false)
        httpReqHelper?.putJsonRequest(activity, url, entity)
    }

    fun setTimer() {
        if (gTimer != null) gTimer!!.cancel()
        gTimer = Timer()
        gTimer!!.schedule(object : TimerTask() {
            override fun run() {
                limitTime = limitTime - 1000
                val min = (limitTime / (1000 * 60)).toInt()
                val sec = (limitTime.toInt() - min * 60 * 1000) / 1000
                runOnUiThread { limit!!.text = min.toString() + ":" + String.format("%02d", sec) }
                if (limitTime == 0L) {
                    gTimer!!.cancel()
                    runOnUiThread {
                        limit!!.text = ""
                        val dialog = ConfirmDialog(activity, "인증 시간이 초과하였습니다", "확인", {})
                        dialog.show()
                        btnRequest!!.isEnabled = true
                        gEditID?.isEnabled = true
                        gEditPhone?.isEnabled = true
                    }
                }
            }
        }, 1000, 1000)
    }
}