package com.sinkleader.install.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.sinkleader.install.R
import com.sinkleader.install.ui.view.QRscanDialog
import kotlin.math.log


class QRscenActivity : BaseActivity() {
    var activity : QRscenActivity? = null
    var vQRcode : DecoratedBarcodeView? = null

    var capture : CaptureManager? =null

    var backBtn : Button? = null
    var openBtn : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrscan)
        activity = this
        initUI(savedInstanceState)

    }

    fun initUI(savedInstanceState: Bundle?){
        // QR CODE
        vQRcode = findViewById(R.id.view_scan)
        capture = CaptureManager(this, vQRcode)
        capture?.initializeFromIntent(this.intent, savedInstanceState)
        capture?.decode()

        vQRcode?.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                Log.d("barcodeResult", result.toString())
                enterBarcodeData(result.toString())
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
            QRscanDialog(this, "", {
                Log.d("QRscanDialog", it)
                enterBarcodeData(it)
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

    fun enterBarcodeData(barcode : String){
        val intent = Intent()
        intent.putExtra("barcode", barcode)
        activity!!.setResult(RESULT_OK, intent)
        activity!!.finish()
    }
}
