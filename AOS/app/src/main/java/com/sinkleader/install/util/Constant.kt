package com.sinkleader.install.util

import android.Manifest

interface Constant {
    companion object {
        val Serverlist = arrayOf(
            "http://api.superbin.co.kr/app",
            "http://api.app.superbin.link",
            "http://office.aend.co.kr:20002", // 정대리님
            "http://192.168.29.194:20002"  // 신대리님
//            "http://192.168.29.248:30000"
        )

        val FrontUrls = arrayOf(
                "http://app.superbin.co.kr",
                "http://app.superbin.link",
                "http://office.aend.co.kr:20022",
                "http://192.168.29.194:20022"
        )

        const val SDCARD_FOLDER = "SinkLeader_Install"
        const val TMP_FOLDER = "$SDCARD_FOLDER/TMP"

        val Permission_Location = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
        )

        val Permission_Login = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION)

        const val RC_PERMISSION_CAMERA = 1000
        const val RC_PERMISSION_GALLERY = 1001
        const val RC_PERMISSION_LOCATION = 1002
        const val RC_PERMISSION_LOGIN = 1003
    }
}