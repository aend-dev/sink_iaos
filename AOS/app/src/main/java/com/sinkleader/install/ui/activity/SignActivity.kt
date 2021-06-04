package com.sinkleader.install.ui.activity

import android.graphics.Point
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.RelativeLayout
import com.sinkleader.install.R
import com.sinkleader.install.ui.view.sign.PocketSignatureView
import com.sinkleader.install.util.Constant
import com.sinkleader.install.util.Util
import java.io.File

class SignActivity : BaseActivity() {
    var activity : SignActivity? = null
    var display : Point? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

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
            sign?.saveVectorImage(Environment.getExternalStorageDirectory().toString() + File.separator + Constant.SDCARD_FOLDER, "TestSign")
        }

        if (display!!.x > display!!.y){
            var layouts = nextbutton.layoutParams
            layouts.width = Util.getDipToPx(this, 300)
            nextbutton.layoutParams = layouts
        }
    }
}
