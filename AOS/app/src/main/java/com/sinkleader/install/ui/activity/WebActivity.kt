package com.sinkleader.install.ui.activity

import android.annotation.TargetApi
import android.app.DownloadManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.webkit.*
import android.widget.Toast
import com.sinkleader.install.R
import com.sinkleader.install.network.JSONParser
import com.sinkleader.install.ui.view.ConfirmDialog
import com.sinkleader.install.ui.view.ConfirmDialogTwo
import com.sinkleader.install.ui.view.PhotoDialog
import com.sinkleader.install.ui.view.WebViewCustom
import com.sinkleader.install.util.*
import droidninja.filepicker.FilePickerBuilder
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class WebActivity : BaseActivity() {
    val ON_CALLBACK = 230

    var webView: WebViewCustom? = null
    var activity: BaseActivity? = null
    var isBack = false
    var isLayer = false

    var uploadMessage: ValueCallback<Array<Uri?>>? = null //이미지 업로드시 사용

    var mediaManager : MediaManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        var url = intent.getStringExtra("WEB_URL")

        if (url == null) url = Util.HOME_URL //홈 주소
        initUI()
        initWebView()

//        url = "http://kkksssyyy.iptime.org:8080"

        loadURL(url)
    }

    override fun onBackPressed() {
        if (isBack) {
            return
        }

        if (isLayer) {
            isLayer = false

            val ret = JSONObject()
            ret.put("callbackMethod", "callbackIsLayer")
            ret.put("data", JSONObject())
            var function = "nativeCallback"
            function += "(\'$ret\')"

            callJavascript(function)
            return
        }

        if (webView!!.canGoBack()) {
            val history = webView!!.copyBackForwardList()
            val index = history.currentIndex
            val item = history.getItemAtIndex(index - 1)
            if (item.url == Util.SIGNUP_URL) {
            }
            webView!!.goBack()
            return
        }
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == WEB_LOGIN && resultCode == RESULT_OK) {
            val url: String = Constant.FrontUrls.get(Util.FrontIndex).toString() + data!!.getStringExtra(
                "url"
            )
            loadURL(url)
        } else if (requestCode == 324 && resultCode == RESULT_OK) {
            val callback = data!!.getStringExtra("callback")
            val data_str = data.getStringExtra("data")
            val url = data.getStringExtra("urls")
            if (callback.length < 1) {
                return
            }
            val obj = JSONObject()
            try {
                val datas = JSONObject(data_str)
                obj.put("callbackMethod", callback)
                obj.put("data", datas)
            } catch (e: JSONException) {2
                e.printStackTrace()
            }
            if (url != null && url.length > 0) {
                loadURL(url)
                return
            }
            var function = "nativeCallback"
            function += "(\'$obj\')"
            callJavascript(function)
            return
        } else if (requestCode == ON_CALLBACK) {
            if (resultCode == RESULT_OK) {
                val reObj = JSONObject()
                reObj.put("callbackMethod", data!!.getStringExtra("callback")!!)
                val jsonstr = data.getStringExtra("data")
                reObj.put("data", JSONObject(jsonstr))
                var function = "nativeCallback"
                function += "(\'$reObj\')"
                callJavascript(function)

            }else if (resultCode == 995){
                if (webView?.originalUrl!!.contains(Constant.FrontUrls.get(Util.FrontIndex) + "/home")) {
                    var function = "searchFavorites"
                    function += "()"
                    callJavascript(function)
                }
            }
        }

        Log.e("resultCode:: ", resultCode.toString())
        if (requestCode == 8080 && resultCode == RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                val datas = data.data
                Log.d("data8080", data?.data.toString())


                uploadMessage?.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        data
                    )
                )
            } else {
                uploadMessage?.onReceiveValue(arrayOf(data?.data))
            }
            uploadMessage = null
        }

        if (requestCode == 233) { //photo
            if (resultCode == RESULT_OK){
                val dataList: ArrayList<Uri> = data!!.getParcelableArrayListExtra("SELECTED_PHOTOS")
                Log.d("dataList", dataList.toString())
                val list : Array<Uri?> = dataList.toTypedArray()
                uploadMessage?.onReceiveValue(list)
            }else{
                uploadMessage?.onReceiveValue(null)
            }
            uploadMessage = null
        }else if (requestCode == MediaManager.SET_CAMERA){
            if (resultCode == RESULT_OK){
                val dataList: ArrayList<Uri> = ArrayList()
                dataList.add(mediaManager?.url!!)
                val list : Array<Uri?> = dataList.toTypedArray()

                uploadMessage?.onReceiveValue(list)
            }else{
                uploadMessage?.onReceiveValue(null)
            }
            uploadMessage = null
        }

    }

    override fun onDestroy() {
        webView!!.destroy()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults == null) {
            Toast.makeText(this, "$requestCode 권한 결과 값을 받아오지 못하였습니다.", Toast.LENGTH_LONG).show()
            return
        }
        if (requestCode == Constant.RC_PERMISSION_LOCATION) {
            if (grantResults.size == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                activity?.runOnUiThread {
                    val dialog = ConfirmDialog(
                        activity,
                        "위치 사용 권한이 없습니다.\n애플리케이션 설정에서 권한을 허가해주세요.",
                        "확인",
                        {
                            var intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(
                                    "package:" + application.packageName
                                )
                            )
                            intent.addCategory(Intent.CATEGORY_DEFAULT)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            activity?.startActivity(intent)
                        })
                    dialog.show()
                }

                val reObj = JSONObject()
                reObj.put("callbackMethod", "callbackGps")
                reObj.put("data", "")
                var function = "nativeCallback"
                function += "(\'$reObj\')"
                callJavascript(function)
            }else if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                settingGps("callbackGps")
            }
        }
    }

    private fun initUI() {
        activity = this
        val back = intent.getBooleanExtra("bck", false)

        setFinishAppWhenPressedBackKey(back)
    }

    private fun initWebView() {
        webView = findViewById(R.id.webview_web)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView?.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_BOUND, true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }
        webView?.addJavascriptInterface(JavaScriptInterface(), "nativeApp")
        webView?.settings?.cacheMode = WebSettings.LOAD_NO_CACHE
        webView?.settings?.setAppCacheEnabled(true)
        webView?.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                val aUrl = url.split("\\?").toTypedArray()
                if (aUrl[0].equals(Constant.FrontUrls.get(Util.FrontIndex) + "/home")) {
                    webView?.clearHistory()
                }
            }


            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError,
            ) {
                var code = 0
                var getUrl = ""
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        code = error.errorCode
                    }
                    getUrl = request.url.toString()
                }
                super.onReceivedError(view, request, error)
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest,
                errorResponse: WebResourceResponse,
            ) {
                var code = 0
                var getUrl = ""
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    code = errorResponse.statusCode
                    getUrl = request.url.toString()
                }
                if (getUrl.contains("favicon.ico")) {
                    return
                }
                super.onReceivedHttpError(view, request, errorResponse)
            }

            override fun onReceivedSslError(
                view: WebView,
                handler: SslErrorHandler,
                error: SslError,
            ) {
            }

            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String,
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
            }
        })
        webView?.setWebChromeClient(object : WebChromeClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri?>>,
                fileChooserParams: FileChooserParams?,
            ): Boolean {
                uploadMessage = filePathCallback

                PhotoDialog(activity, object : PhotoDialog.ConfirmDialogListener{
                    override fun onPhoto() {
                        var maxCount = 10
                        if (webView?.url!!.contains("mypage/profile")){
                            maxCount = 1
                        }
                        FilePickerBuilder.instance
                            .setMaxCount(maxCount) //optional
                            .setActivityTheme(R.style.LibAppTheme) //optional
                            .enableCameraSupport(false)
                            .enableVideoPicker(false)
                            .enableSelectAll(false)
                            .showGifs(true)
                            .pickPhoto(activity!!)
                    }
                    override fun onCamera() {
                        if (mediaManager == null){
                            mediaManager = MediaManager(activity)
                        }

                        mediaManager?.getImageFromCamera()
                    }
                    override fun onClose() {
                        uploadMessage?.onReceiveValue(null)
                    }
                }).show()
                return true
            }

            override fun onJsAlert(
                view: WebView, url: String,
                message: String, result: JsResult,
            ): Boolean {
                val dialog = ConfirmDialog(
                    this@WebActivity,
                    message,
                    "확인",
                    object : ConfirmDialog.ConfirmDialogListener {
                        override fun onConfirm1() {
                            result.confirm()
                        }
                    })
                dialog.show()
                return true
            }

            override fun onJsConfirm(
                view: WebView,
                url: String,
                message: String,
                result: JsResult,
            ): Boolean {
                val dialog = ConfirmDialogTwo(
                    this@WebActivity,
                    message,
                    "확인",
                    "취소",
                    object : ConfirmDialogTwo.ConfirmDialogListener {
                        override fun onConfirm1() {
                            result.confirm()
                        }

                        override fun onConfirm2() {
                            result.cancel()
                        }
                    })
                dialog.show()
                return true
            }
        })
        webView?.setDownloadListener(DownloadListener { url: String?, userAgent: String?, contentDisposition: String, mimetype: String?, contentLength: Long ->
            downloadURL(
                url,
                contentDisposition
            )
        })
    }

    private fun callJavascript(function: String) {
        Log.d("callJavascript", function)
        //        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
        val func = "$function;"
        webView?.evaluateJavascript(func, null)
        //        } else {
//            String func = "javascript:" + function + ";";
//            webView.loadUrl(func);
//        }
    }

    private fun moveWebBrowser(url: String) {
        var dataUrl = ""
        if (!url.startsWith("http")){
            dataUrl = "http://" + url
        }else{
            dataUrl = url
        }

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(dataUrl)
        startActivity(intent)
    }

    inner class JavaScriptInterface {
        @JavascriptInterface
        fun call(strJson: String) {
            Log.d("JavaScriptInterface", "call Start :\n$strJson")
            runOnUiThread {
                try {
                    val json = JSONObject(strJson)
                    val method: String = JSONParser.getString(json, "method")
                    val callback: String = JSONParser.getString(json, "callbackMethod")
                    val data: JSONObject = JSONParser.getJSONObject(json, "data")
                    ActionNativeCall(method, callback, data)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private inner class ActionNativeCall(
        method: String,
        callback: String,
        data: JSONObject,
    ) {
        init {
            if (method == "openWebView") {
                val openURL: String = JSONParser.getString(data, "url")
                val intent = Intent(this@WebActivity, WebActivity::class.java)
                intent.putExtra("WEB_URL", openURL)
                activity!!.startActivityForResult(intent, 324)
            } else if (method == "changeUrlWebView") { // native_call? callJavascript &func=myFunction &value0=text&type0=string
                val url: String = JSONParser.getString(data, "url")
                loadURL(url)
            } else if (method == "closeWebView") { // native_call? callJavascript &func=myFunction &value0=text&type0=string
                val dataSTR = data.toString()
                val intent = Intent()
                intent.putExtra("callback", callback)
                intent.putExtra("data", dataSTR)
                activity!!.setResult(RESULT_OK, intent)
                activity!!.finish()
            } else if (method == "getUserInfo") {
                val obj: JSONObject = Device.getInfo(activity)
                obj.put("token", Util.TOKEN)
                obj.put("refresh_token", Util.RETOKEN)
                obj.put("app_user_grade", Util.USER_GRADE)

                obj.put("user_type", Util.USER_TYPE)
                obj.put("center_seq", Util.USER_CENTER_SEQ)
                obj.put("profile_img_url", Util.USER_IMG)

                obj.put("user_sno", Util.USER_SEQ)
                obj.put("user_name", Util.USER_NAME)
                obj.put("last_access_date", Util.USER_LASTDATE)

                var loginid = PrefMgr.instance.getString("login_sno", "")

                if (Util.USER_SEQ == ""){
                    obj.put("is_first", false)
                }else if (loginid != null && loginid.contains(Util.USER_SEQ)){
                    obj.put("is_first", false)
                }else{
                    loginid = loginid + "," + Util.USER_SEQ
                    PrefMgr.instance.put("login_sno", loginid)
                    obj.put("is_first", true)
                }

                val ret = JSONObject()
                ret.put("callbackMethod", callback)
                ret.put("data", obj)
                var function = "nativeCallback"
                function += "(\'$ret\')"
                callJavascript(function)
            } else if (method == "signUp") {
                val obj = JSONObject()
                obj.put("type", Util.SIGN_TYPE)
                obj.put("id", Util.SIGN_ID)
                obj.put("profile_image", Util.SIGN_IMG)
                obj.put("email", Util.SIGN_EMAIL)
                obj.put("device_info", Device.getInfo(activity))

                val ret = JSONObject()
                ret.put("callbackMethod", callback)
                ret.put("data", obj)
                var function = "nativeCallback"
                function += "(\'$ret\')"
                callJavascript(function)

            } else if (method == "downLoad") {
                val fileName: String = JSONParser.getString(data, "title")
                val url: String = JSONParser.getString(data, "url")
                downloadURL(url, fileName)
            } else if (method == "getGPS") {
                if (!hasPermission(Constant.Permission_Location)) {
                    requestPermission(
                        activity,
                        Constant.Permission_Location,
                        Constant.RC_PERMISSION_LOCATION
                    )
                } else {
                    settingGps(callback)
                }

            } else if (method == "reLoadPage") { //
                webView!!.reload()
            } else if (method == "isBackKey") { //
                val `is`: String = JSONParser.getString(data, "value")
                if (`is` == "Y") {
                    isBack = true
                } else if (`is` == "N") {
                    isBack = false
                }
            }else if (method == "isLayer") { //
                isLayer = true

            } else if (method == "openBrowser") { //
                val openURL: String = JSONParser.getString(data, "url")
                moveWebBrowser(openURL)

            } else if (method == "openNaverMap") {
                val address: String = JSONParser.getString(data, "address")
                openNaverMap(address)

            } else if (method == "openScanner") { //
                val name: String = JSONParser.getString(data, "name")

                val intent = Intent(activity, QRscenActivity::class.java)
                intent.putExtra("name", name)
                intent.putExtra("callback", callback)
                startActivityForResult(intent, ON_CALLBACK)

            } else if (method == "openSignature") { //
                val intent = Intent(activity, SignActivity::class.java)
                intent.putExtra("callback", callback)
                startActivityForResult(intent, ON_CALLBACK)

            } else if (method == "Logout") { //
                Util.TOKEN = ""
                Util.RETOKEN = ""
                Util.USER_GRADE = ""
                Util.USER_TYPE = ""
                Util.USER_CENTER_SEQ = ""
                Util.USER_IMG = ""
                Util.USER_SEQ = ""
                Util.USER_NAME = ""
                Util.USER_LASTDATE = ""

                PrefMgr.instance.put("token", "")

                val intent = Intent(activity, LoginActivity::class.java)
                activity?.startActivity(intent)
                activity?.finish()

            } else if (method == "actionShare") {
                val title: String = JSONParser.getString(data, "title")
                val share_url: String = JSONParser.getString(data, "url")
                runOnUiThread {
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.type = "text/plain"
                    intent.putExtra(Intent.EXTRA_SUBJECT, title)
                    intent.putExtra(Intent.EXTRA_TEXT, share_url)
                    val chooser = Intent.createChooser(intent, "공유")
                    startActivity(chooser)
                }
            } else if (method == "openTel") { //
                val tel: String = JSONParser.getString(data, "tel")
                val tt = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
                startActivity(tt)

            } else if (method == "clearHistory") { //
                runOnUiThread {
                    webView?.clearHistory()
                }
            }
        }
    }

    private fun openNaverMap(str: String){
//        val str = URLEncoder.encode( "서울특별시 강남구 선릉로647 4층 에이엔",  "utf8")

        val url = "nmap://search?query="+ str +"&appname=com.sinkleader.install"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addCategory(Intent.CATEGORY_BROWSABLE)

        val list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (list == null || list.isEmpty()) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.nhn.android.nmap")
                )
            )
        } else {
            startActivity(intent)
        }
    }

    private fun loadURL(url: String) {
        val header: MutableMap<String, String> = HashMap()
        val token = Util.TOKEN
        if (token != "") {
            header["x-token"] = token
        }

        val seq = Util.USER_SEQ
        if (seq != "") {
            header["user_seq"] = seq
        }
        val refesh = Util.RETOKEN
        if (refesh != "") {
            header["x-refresh-token"] = refesh
        }
        webView!!.loadUrl(url, header)
    }

    @Throws(JSONException::class)
    private fun settingGps(callbankMethod: String?) {
        var callbankMethod = callbankMethod
        if (callbankMethod == null) {
            callbankMethod = "callbackGps"
        }
        val location: List<String> = getCurPosition().split(",")
        if (location[0] == "0" && location[1] == "0") {
            ConfirmDialogTwo(this@WebActivity,
                "GPS가 꺼져있습니다.\n사용 설정화면으로 이동 하시겠습니까?",
                "이동",
                "취소",
                object : ConfirmDialogTwo.ConfirmDialogListener {
                    override fun onConfirm1() {
                        // GPS설정 화면으로 이동
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        startActivity(intent)
                    }

                    override fun onConfirm2() {}
                }).show()

            val reObj = JSONObject()
            reObj.put("callbackMethod", callbankMethod)
            reObj.put("data", "")
            var function = "nativeCallback"
            function += "(\'$reObj\')"
            callJavascript(function)
            return
        }

        val reObj = JSONObject()
        val reData = JSONObject()
        reData.put("latitude", location[1])
        reData.put("longitude", location[0])
        reData.put("enable", location[0])
        reObj.put("callbackMethod", callbankMethod)
        reObj.put("data", reData)
        var function = "nativeCallback"
        function += "(\'$reObj\')"
        callJavascript(function)
    }

    fun downloadURL(url: String?, fileName: String) {
        var fileName = fileName
        fileName = fileName.replace("attachment; filename=", "")
        fileName = fileName.replace(";", "")
        //attachment; filename*=UTF-8''뒤에 파일명이있는데 파일명만 추출하기위해 앞에 attachment; filename*=UTF-8''제거
        val file = File(
            Environment.getExternalStorageDirectory()
                .toString() + "/" + Environment.DIRECTORY_DOWNLOADS, fileName
        )
        val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setDescription("$fileName Downloading Summit")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(file))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
        request.addRequestHeader("Access-Token", "Bearer " + Util.TOKEN)
        request.addRequestHeader("Access-Device", "AOS")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            request.setRequiresCharging(false)
        }
        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
    }

    companion object {
        const val WEB_LOGIN = 3000
    }
}
