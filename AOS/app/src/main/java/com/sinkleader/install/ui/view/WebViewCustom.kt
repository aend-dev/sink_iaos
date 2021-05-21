package com.sinkleader.install.ui.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import com.sinkleader.install.BuildConfig


class WebViewCustom : WebView {
    var mContext: Context? = null

    constructor(context: Context?) : super(context) {
        mContext = context
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.setAppCacheEnabled(false)
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        if (BuildConfig.BUILD_TYPE.toLowerCase() != "release") {
            setWebContentsDebuggingEnabled(true)
        }
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.allowFileAccess = true
        settings.javaScriptCanOpenWindowsAutomatically = true
        settings.setSupportMultipleWindows(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else if (Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT < 19) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
        // cookie save true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // HTTPS 페이지내 이미지가 안나오는 현상
            getSettings().mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) { //기기에 따라서 동작할수도있는걸 확인
            settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
            //최신 SDK 에서는 Deprecated 이나 아직 성능상에서는 유용하다
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            //부드러운 전환 또한 아직 동작
            settings.setEnableSmoothTransition(true)
        }
        // add User Agent
        val userAgent = getSettings().userAgentString
        getSettings().userAgentString = userAgent + " " + mUserAgent

        settings.textZoom = 100

    }

    override fun destroy() { //현재 웹뷰 메모리누수를 막으려면 수동으로 삭제할수밖에...
        val parent = this.parent
        if (parent is ViewGroup) {
            parent.removeView(this)
        }
        try {
            removeAllViews()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.destroy()
    }

    companion object {
        const val mUserAgent = "Superbin-Mobile"
    }
}