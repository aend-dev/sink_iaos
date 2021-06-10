package com.sinkleader.install.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.sinkleader.install.R
import com.sinkleader.install.ui.view.QRscanDialog
import org.json.JSONObject

class QRscenActivity : BaseActivity() {
    var activity : QRscenActivity? = null
    var vQRcode : DecoratedBarcodeView? = null

    var capture : CaptureManager? =null

    var title : TextView? = null
    var backBtn : Button? = null
    var openBtn : Button? = null

    var callback : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscan)
        activity = this

        callback = intent.getStringExtra("callback")

        initUI(savedInstanceState)

    }

    fun initUI(savedInstanceState: Bundle?){
        title = findViewById(R.id.title_scan)
        title?.setText(intent.getStringExtra("name"))

        // QR CODE
        vQRcode = findViewById(R.id.view_scan)
        capture = CaptureManager(this, vQRcode)
        capture?.initializeFromIntent(this.intent, savedInstanceState)
        capture?.decode()

        vQRcode?.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                Log.d("barcodeResult", result.toString())
                enterBarcodeData(result.toString(), true)
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
            }
        })

        // POPUP
        backBtn = findViewById(R.id.back_btn_scan)
        backBtn?.setOnClickListener {
            finish()
        }

        openBtn = findViewById(R.id.open_btn_scan)
        openBtn?.setOnClickListener {
            QRscanDialog(this, title?.text, {
                Log.d("QRscanDialog", it)
                enterBarcodeData(it, false)
            }).show()
        }
    }


    override fun onResume() {
        super.onResume()
        capture!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture!!.onDestroy()
    }

    fun enterBarcodeData(barcode : String, is_scan : Boolean){
        val intent = Intent()
        intent.putExtra("callback", callback)

        var obj = JSONObject()
        obj.put("barcode", barcode)
        obj.put("is_scan", is_scan)
        intent.putExtra("data", obj.toString())
        activity!!.setResult(RESULT_OK, intent)
        activity!!.finish()
    }
}
