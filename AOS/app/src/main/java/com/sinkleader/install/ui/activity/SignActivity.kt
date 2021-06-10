package com.sinkleader.install.ui.activity

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RelativeLayout
import com.sinkleader.install.R
import com.sinkleader.install.ui.view.sign.PocketSignatureView
import com.sinkleader.install.util.Util
import org.json.JSONObject

class SignActivity : BaseActivity() {
    var activity : SignActivity? = null
    var display : Point? = null

    var callback : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

        callback = intent.getStringExtra("callback")
        display = Util.getDisplaySize(this)

        Log.d("display", "display : ${display.toString()}")

        activity = this
        val view = findViewById<RelativeLayout>(R.id.test_sign)

        var sign = PocketSignatureView(view.context)
        view.addView(sign)

        val prebutton = findViewById<Button>(R.id.pre_btn_sign)
        prebutton.setOnClickListener {
            finish()
        }

        val nextbutton = findViewById<Button>(R.id.ok_btn_sign)
        nextbutton.setOnClickListener {
//            sign?.saveVectorImage(Environment.getExternalStorageDirectory().toString() + File.separator + Constant.SDCARD_FOLDER, "TestSign")
            val strSVG = sign?.getVectorImageToString()
            var baseSVG =  Util.getBase64String(strSVG)
            baseSVG = baseSVG.replace("\\n".toRegex(), "")
            val intent = Intent()
            intent.putExtra("callback", callback)
            var obj = JSONObject()
            obj.put("svg", "data:image/svg+xml;base64," + baseSVG)
            intent.putExtra("data", obj.toString())
            activity!!.setResult(RESULT_OK, intent)
            activity!!.finish()
        }

        if (display!!.x > display!!.y){
            var layouts = nextbutton.layoutParams
            layouts.width = Util.getDipToPx(this, 300)
            nextbutton.layoutParams = layouts
        }
    }
}
