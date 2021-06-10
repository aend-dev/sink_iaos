package com.sinkleader.install.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import com.sinkleader.install.R
import com.sinkleader.install.network.HttpRequestHelper
import com.sinkleader.install.network.JSONParser
import com.sinkleader.install.ui.view.ConfirmDialog
import com.sinkleader.install.ui.view.ConfirmDialogTwo
import com.sinkleader.install.util.Constant
import com.sinkleader.install.util.Device
import com.sinkleader.install.util.PrefMgr
import com.sinkleader.install.util.Util
import cz.msebera.android.httpclient.entity.StringEntity
import cz.msebera.android.httpclient.protocol.HTTP
import org.json.JSONObject
import java.util.*

class IntroActivity : BaseActivity() {
    var timer: Timer? = null
    var activity : IntroActivity? = null

    var jumpUrl = ""
    var upd_force = "N"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        activity = this

        Util.TOKEN = PrefMgr.instance.getString("token", "").toString()


        if (intent.hasExtra("WEB_URL"))
            jumpUrl = intent.getStringExtra("WEB_URL")

//        getKeyHash(this)

        HttpRequestHelper.instance.checkNetworkAvailable(this, true)
        requestCheckVersion()
//        startTimer()
        //변경예정
    }


    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }

    private fun requestCheckVersion() {
        if (!HttpRequestHelper.instance.m_isNetwork) {
            val dialog = ConfirmDialog(
                activity,
                resources.getString(R.string.network_err),
                "확인",
                {
                    finish()
                })
            dialog.show()
            return
        }

        val url: String = Constant.Serverlist.get(Util.ServerIndex) + "/v1/main/app-version?os=AOS"
        val httpReqHelper: HttpRequestHelper? = HttpRequestHelper.instance
        httpReqHelper?.init(this, this, object : HttpRequestHelper.OnParseResponseListener {
            override fun onSuccessResponse(apiName: String?, response: JSONObject?) {
                Log.d(apiName, response.toString())

                val result: Int = JSONParser.getInt(response!!, "result")
                if (result == 0) {
                    var data = JSONParser.getJSONObject(response, "data")
                    val list = JSONParser.getJSONArray(data, "list")

                    for (i in 0..list.length()-1){
                        var item = list.getJSONObject(i)
                        val os = JSONParser.getString(item, "os")
                        if (os.equals("AOS")){
                            data = item
                            break
                        }
                    }

                    var isVer = compareVersion(JSONParser.getString(data, "version"))
                    var msg = JSONParser.getString(data, "message")
                    msg = msg.replace("\\n", "\n")
                    var update_url = JSONParser.getString(data, "url")

                    //ds_upd_force_yn : Y = 확인버튼 선택시 스토어 이동, C = 버튼2개 팝업 노출, N = 그냥 App 실행
                    upd_force = JSONParser.getString(data, "upd_force_yn")

                    if (isVer) {
                        if (upd_force.equals("Y")) {
                            val dialog = ConfirmDialog(activity, msg, "확인") {
                                val marketLaunch = Intent(Intent.ACTION_VIEW)
                                marketLaunch.data = Uri.parse(update_url)
                                startActivity(marketLaunch)
                                finish()
                            }
                            dialog.show()
                        } else if (upd_force.equals("C")) {
                            val dialog = ConfirmDialogTwo(
                                activity,
                                msg,
                                "취소",
                                "확인",
                                object : ConfirmDialogTwo.ConfirmDialogListener {
                                    override fun onConfirm1() {
                                        startTimer()
                                    }

                                    override fun onConfirm2() {
                                        val marketLaunch = Intent(Intent.ACTION_VIEW)
                                        marketLaunch.data = Uri.parse(update_url)
                                        startActivity(marketLaunch)
                                        finish()
                                    }
                                })
                            dialog.show()
                        } else {
                            startTimer()
                        }
                    } else {
                        startTimer()
                    }

                } else {
                    activity?.runOnUiThread {
                        if (!this@IntroActivity.isFinishing()) {
                            val message: String = JSONParser.getString(response, "message")
                            val dialog = ConfirmDialog(activity, message, "확인", null)
                            dialog.show()
                        }
                    }
                }
            }

            override fun onError(code: Int, msg: String?) {
                Log.e("onError", "$msg $code")

                activity?.runOnUiThread {
                    if (!this@IntroActivity.isFinishing()) {
                        val dialog = ConfirmDialog(
                            activity,
                            "서버 시스템 점검중 입니다.\n잠시후 다시 이용 부탁드립니다.",
                            "확인",
                            null
                        )
                        dialog.show()
                    }
                }
            }
        }, "requestFindSMS", false)
        httpReqHelper?.getRequest(url, null)
    }

    private fun startTimer(){
        timer = Timer()
        timer!!.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (Util.TOKEN.equals("")) {
                        moveLogin()
                    } else {
                        requestAutoLogin()
                    }
                }
            }
        }, 1000)
    }

    private fun moveLogin(){
        Util.changeServer()
        val intent = Intent(this@IntroActivity, LoginActivity::class.java)
        intent.putExtra("bck", true)
        startActivity(intent)
        finish()
    }

    private fun requestAutoLogin() { //LOGIN_URL
        val obj = JSONObject()

        obj.put("userDevice", Device.getInfo(this))
        obj.put("x_token", Util.TOKEN)

        var entity = StringEntity(obj.toString(), HTTP.UTF_8)
        val url = Constant.Serverlist[Util.ServerIndex] + "/v1/login/autoLogin"
        val httpReqHelper: HttpRequestHelper = HttpRequestHelper.instance!!
        httpReqHelper.init(this, this, object : HttpRequestHelper.OnParseResponseListener {
            override fun onSuccessResponse(apiName: String?, response: JSONObject?) {
                Log.d("requestAutoLogin Suc", apiName)
                val result: Int = JSONParser.getInt(response!!, "result")
                if (result == 0) {
                    val data: JSONObject = JSONParser.getJSONObject(response, "data")
                    val user = JSONParser.getJSONObject(data, "user")

                    Util.moveWebPage(this@IntroActivity, user, jumpUrl)

                } else {
                    moveLogin()
                }
            }

            override fun onError(code: Int, msg: String?) {
                Log.d("requestAutoLogin Err", "$code : $msg")
                activity?.runOnUiThread {
                    moveLogin()
                }
            }
        }, "requestAutoLogin", false)
        httpReqHelper.postJsonRequest(this, url, entity)
    }

    private fun compareVersion(verStr: String?): Boolean {
        if (verStr == null || verStr.length == 0) return false
        var result = false
        val curVersion: String = Util.version
        val curVers = curVersion.split("\\.".toRegex()).toTypedArray()
        val serVers = verStr.split("\\.".toRegex()).toTypedArray()
        val curIntVer = String.format(
            "%04d%04d%04d",
            curVers[0].toInt(),
            curVers[1].toInt(),
            curVers[2].toInt()
        ).toInt()
        val serIntVer = String.format(
            "%04d%04d%04d",
            serVers[0].toInt(),
            serVers[1].toInt(),
            serVers[2].toInt()
        ).toInt()
        if (curIntVer < serIntVer) result = true
        return result
    }
}
