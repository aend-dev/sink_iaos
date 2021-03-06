package com.sinkleader.install.network

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.net.*
import android.os.Build
import android.util.Log
import com.loopj.android.http.*
import com.sinkleader.install.R
import com.sinkleader.install.ui.view.ConfirmDialog
import com.sinkleader.install.util.PrefMgr
import com.sinkleader.install.util.Util
import cz.msebera.android.httpclient.Header
import cz.msebera.android.httpclient.client.params.ClientPNames
import cz.msebera.android.httpclient.entity.StringEntity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class HttpRequestHelper{
    interface OnParseResponseListener {
        @Throws(JSONException::class)
        fun onSuccessResponse(apiName: String?, response: JSONObject?)
        fun onError(code: Int, msg: String?)
    }

    private val m_syncHttpClient = SyncHttpClient(true, 80, 443)
    private val m_asyncHttpClient = AsyncHttpClient(true, 80, 443)
    private var m_requestHandle: RequestHandle? = null
    private var m_enApi: String? = null
    private var m_context: Context? = null
    private var m_Activity: Activity? = null
    private var m_parseResponseListener: OnParseResponseListener? = null
    private val m_bLoading = false
    private var m_dlgProgress: ProgressDialog? = null
    private var m_isShowProgres = false
    var m_isNetwork = false
    fun init(
        context: Context?,
        activity: Activity? = null,
        listener: OnParseResponseListener?,
        apiName: String?,
        isShowProgress: Boolean
    ) { //        init((Activity) context, listener, apiName, isShowProgress);
        m_context = context
        m_Activity = activity
        m_enApi = apiName
        m_parseResponseListener = listener
        m_isShowProgres = isShowProgress
        //m_app = (MyApplication) activity.getApplication();
        setHttpClientProperty()
        setNeedFileTransfer(true)

        try {
            if (isShowProgress) {
                showProgress(context, "", "????????? ????????? ?????????.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    //    public void init(Activity activity, OnParseResponseListener listener, String apiName, boolean isShowProgress) {
//
//    }
    /**
     * Set the file transfer flag
     *
     * @param isFileTransfer
     */
    fun setNeedFileTransfer(isFileTransfer: Boolean) {
        if (isFileTransfer) {
            m_asyncHttpClient.setTimeout(MAX_TIMEOUT)
        } else {
            m_asyncHttpClient.setTimeout(AsyncHttpClient.DEFAULT_SOCKET_TIMEOUT)
        }
    }

    /**
     * Show the progress dialog
     *
     * @param ctx
     * @param title
     * @param msg
     */
    private fun showProgress(ctx: Context?, title: String, msg: String) {
        m_dlgProgress = ProgressDialog(ctx)
        m_dlgProgress!!.setTitle(title)
        m_dlgProgress!!.setMessage(msg)
        m_dlgProgress!!.setCancelable(true)
        m_dlgProgress!!.setCanceledOnTouchOutside(false)
        m_dlgProgress!!.isIndeterminate = false
        m_dlgProgress!!.show()
    }

    /**
     * Hide the progress dialog
     */
    fun hideProgress(dlgProgress: ProgressDialog?) {
        if (dlgProgress != null && dlgProgress.isShowing) (m_context as Activity?)!!.runOnUiThread { dlgProgress.dismiss() }
    }

    val isProgressShowing: Boolean
        get() = m_dlgProgress != null && m_dlgProgress!!.isShowing

    /**
     * Cancel the http request
     */
    fun cancelHttpRequest(requestHandle: RequestHandle?) {
        requestHandle?.cancel(true)
    }

    private fun setHttpClientProperty() {
        m_asyncHttpClient.maxConnections = AsyncHttpClient.DEFAULT_MAX_CONNECTIONS
        m_asyncHttpClient.setMaxRetriesAndTimeout(1, MAX_TIMEOUT)
        m_asyncHttpClient.httpClient.params.setParameter(
            ClientPNames.ALLOW_CIRCULAR_REDIRECTS,
            true
        )
    }

    /**
     * Check the available of network
     *
     * @return
     */
    open fun checkNetworkAvailable(context: Context?, isInit :Boolean) {
        if (Build.VERSION.SDK_INT < 28){
            val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            m_isNetwork = activeNetwork?.isConnectedOrConnecting == true

        }else{
            if (isInit) {
                val connectivityManager = context?.getSystemService(ConnectivityManager::class.java)
                val networkRequest = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .build()
                connectivityManager?.registerNetworkCallback(networkRequest, networkCallBack)
            }
        }
    }

    // ????????? ???????????? ??????
    open fun terminateNetworkCallback(context: Context?) {
        val connectivityManager = context?.getSystemService(ConnectivityManager::class.java)
        connectivityManager?.unregisterNetworkCallback(networkCallBack)
    }

    private val networkCallBack = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // ??????????????? ????????? ??? ???????????????.
            Log.d("onAvailable", "onAvailable")
            m_isNetwork = true
        }

        override fun onLost(network: Network) {
            // ??????????????? ?????? ??? ???????????????.
            Log.d("onAvailable", "onLost")
            m_isNetwork = false
        }
    }

    private fun setCommonHeader() {
        m_asyncHttpClient.removeAllHeaders()
        // Header??? ?????? ??????
        val token = Util.TOKEN
        val retoken = Util.RETOKEN
        val sno = Util.USER_SEQ
        m_asyncHttpClient.addHeader("x-token", token)
        m_asyncHttpClient.addHeader("x-refresh-token", retoken)
        m_asyncHttpClient.addHeader("user-sno", sno)
    }

    fun postRequest(url: String?, params: RequestParams?) {
        checkNetworkAvailable(m_context, false)

        setCommonHeader()
        m_requestHandle = m_asyncHttpClient.post(url, params, responseHandler)
    }

    fun getJsonRequest(context: Context?, url: String?, entity: StringEntity?) {
        checkNetworkAvailable(m_context, false)

        setCommonHeader()
        m_requestHandle = m_asyncHttpClient.get(
            context,
            url,
            entity,
            "application/json;charset=UTF-8",
            responseHandler
        )
    }

    fun postJsonRequest(context: Context?, url: String?, entity: StringEntity?) {
        checkNetworkAvailable(m_context, false)

        setCommonHeader()
        m_requestHandle = m_asyncHttpClient.post(
            context,
            url,
            entity,
            "application/json;charset=UTF-8",
            responseHandler
        )
    }

    fun putJsonRequest(context: Context?, url: String?, entity: StringEntity?) {
        checkNetworkAvailable(m_context, false)

        setCommonHeader()
        m_requestHandle = m_asyncHttpClient.put(
            context,
            url,
            entity,
            "application/json;charset=UTF-8",
            responseHandler
        )
    }

    fun putRequest(url: String?, params: RequestParams?) {
        checkNetworkAvailable(m_context, false)

        setCommonHeader()
        m_requestHandle = m_asyncHttpClient.put(url, params, responseHandler)
    }

    fun getRequest(url: String?, params: RequestParams?) {
        checkNetworkAvailable(m_context, false)

        setCommonHeader()
        m_requestHandle = m_asyncHttpClient[url, params, responseHandler]
    }

    fun deleteRequest(context: Context?, url: String, params: RequestParams) {
        checkNetworkAvailable(m_context, false)

        setCommonHeader()
        m_requestHandle = m_asyncHttpClient.delete(context, url, null, params, responseHandler)
    }

    fun getSyncRequest(url: String?, params: RequestParams?) {
        checkNetworkAvailable(m_context, false)
        setCommonHeader()
        m_requestHandle = m_syncHttpClient[url, params, responseHandler]
    }
    /////////////////////////////////////////////////////////////////////////////////////
//  ?????? ?????????
/////////////////////////////////////////////////////////////////////////////////////
    private val responseHandler: ResponseHandlerInterface
        private get() {
            val context = m_context
            val parseResponseListener = m_parseResponseListener
            val apiName = m_enApi
            val isShowProgress = m_isShowProgres
            val dlgProgress = m_dlgProgress
            val requestHandle = m_requestHandle
            try {
                if (isShowProgress) {
                    dlgProgress!!.setOnCancelListener { cancelHttpRequest(requestHandle) }
                }
            } catch (e: Exception) {
            }
            return object : JsonHttpResponseHandler() {
                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<Header>?,
                    response: JSONObject?
                ) {
                    super.onSuccess(statusCode, headers, response)
                    if (isShowProgress) {
                        hideProgress(dlgProgress)
                    }
                    try {
                        parseResponseListener!!.onSuccessResponse(apiName, response)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                        parseResponseListener!!.onError(-9, "???????????? ???????????? ????????? ????????? ?????????????????????.")
                    }
                }

                override fun onSuccess(statusCode: Int, headers: Array<Header>?, response: JSONArray?) {
                    super.onSuccess(statusCode, headers, response)
                    //onFailResponse(null);
                    onFailResponse(statusCode, response.toString())
                }

                override fun onSuccess(
                    statusCode: Int,
                    headers: Array<Header>?,
                    responseString: String?
                ) {
                    super.onSuccess(statusCode, headers, responseString)
                    //onFailResponse(null);
                    onFailResponse(statusCode, responseString)
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<Header>?,
                    throwable: Throwable?,
                    errorResponse: JSONObject?
                ) {
                    super.onFailure(statusCode, headers, throwable, errorResponse)
                    if (errorResponse == null){
                        onFailResponse(-999, null)
                        return
                    }
                    val code = JSONParser.getInt(errorResponse, "error_code")
                    val message = JSONParser.getString(errorResponse, "message")

                    onFailResponse(code, message)
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<Header>?,
                    responseString: String?,
                    throwable: Throwable?
                ) {
                    super.onFailure(statusCode, headers, responseString, throwable)

                    onFailResponse(statusCode, responseString)
                }

                override fun onFailure(
                    statusCode: Int,
                    headers: Array<Header>?,
                    throwable: Throwable?,
                    errorResponse: JSONArray?
                ) {
                    super.onFailure(statusCode, headers, throwable, errorResponse)
                    onFailResponse(statusCode, null)
                }

                fun onFailResponse(code: Int, msg: String?) {
                    hideProgress(dlgProgress)
                    //Toast.makeText(context.getApplicationContext(), R.string.data_fetch_error, Toast.LENGTH_SHORT).show();
                    var message = msg
                    if (msg == null || msg.length > 100){
                        message = m_context!!.getString(R.string.server_error)
                    }

                    parseResponseListener!!.onError(code, message)
                }
            }
        }

    companion object {
        private val TAG = HttpRequestHelper::class.java.simpleName
        private const val MAX_TIMEOUT = 5 * 1000 // 5???
        val instance: HttpRequestHelper = HttpRequestHelper()
    }
}