package com.sinkleader.install.util

import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.*
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import java.io.IOException
import java.util.*

/**
 * Created by Snow on 2017-02-23.
 */
class GpsInfo(private val mContext: Context) : Service(), LocationListener {
    // 현재 GPS 사용유무
    var isGPSEnabled = false
    // 네트워크 사용유무
    var isNetworkEnabled = false
    /**
     * GPS 나 wife 정보가 켜져있는지 확인합니다.
     */
    // GPS 상태값
    var isGetLocation = false
    var lat = 0.0 // 위도 = 0.0
    var lng = 0.0 // 경도 = 0.0
    protected var locationManager: LocationManager? = null
    var location: Location? = null

    fun getLoc(): Location? {
        location = null
        try {
            locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            // GPS 정보 가져오기
            isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // 현재 네트워크 상태 값 알아오기
            isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!isGPSEnabled && !isNetworkEnabled) { // GPS 와 네트워크사용이 가능하지 않을때 소스 구현
            } else {
                isGetLocation = true
                // 네트워크 정보로 부터 위치값 가져오기
                if (isNetworkEnabled) {
                    locationManager!!.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                    if (locationManager != null) {
                        location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (location != null) { // 위도 경도 저장
                            lat = location!!.latitude
                            lng = location!!.longitude
                        }
                    }
                }
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager!!.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                        if (locationManager != null) {
                            location = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (location != null) {
                                lat = location!!.latitude
                                lng = location!!.longitude
                            }
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        return location
    }

    /**
     * GPS 종료
     */
    fun stopUsingGPS() {
        if (locationManager != null) {
            try {
                locationManager!!.removeUpdates(this@GpsInfo)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 위도값을 가져옵니다.
     */
    val latitude: Double
        get() {
            if (location != null) {
                lat = location!!.latitude
            }
            return lat
        }

    /**
     * 경도값을 가져옵니다.
     */
    val longitude: Double
        get() {
            if (location != null) {
                lng = location!!.longitude
            }
            return lng
        }

    /**
     * GPS 정보를 가져오지 못했을때
     * 설정값으로 갈지 물어보는 alert 창
     */
    fun showSettingsAlert() {
        val alertDialog = AlertDialog.Builder(mContext)
        alertDialog.setTitle("GPS 사용유무셋팅")
        alertDialog.setMessage("GPS 셋팅이 되지 않았을수도 있습니다. \n 설정창으로 가시겠습니까 ? ")
        // OK 를 누르게 되면 설정창으로 이동합니다.
        alertDialog.setPositiveButton("Settings"
        ) { dialog, which ->
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            mContext.startActivity(intent)
        }
        // Cancle 하면 종료 합니다.
        alertDialog.setNegativeButton("Cancel"
        ) { dialog, which -> dialog.cancel() }
        alertDialog.show()
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onLocationChanged(location: Location) { // TODO Auto-generated method stub
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) { // TODO Auto-generated method stub
    }

    override fun onProviderEnabled(provider: String) { // TODO Auto-generated method stub
    }

    override fun onProviderDisabled(provider: String) { // TODO Auto-generated method stub
    }

    companion object {
        // 최소 GPS 정보 업데이트 거리 10미터
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10
        // 최소 GPS 정보 업데이트 시간 밀리세컨이므로 1분
        private const val MIN_TIME_BW_UPDATES = 1000 * 60 * 1.toLong()

        /**
         * 위도,경도로 주소구하기
         *
         * @param lat
         * @param lng
         * @return 주소
         */
        fun getLocation(mContext: Context?, lat: Double, lng: Double): String {
            var nowAddress = "현재 위치를 확인 할 수 없습니다."
            val geocoder = Geocoder(mContext, Locale.KOREA)
            val address: List<Address>?
            try {
                if (geocoder != null) { //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
//한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
                    address = geocoder.getFromLocation(lat, lng, 1)
                    if (address != null && address.size > 0) { // 주소 받아오기
                        val currentLocationAddress = address[0].getAddressLine(0)
                        nowAddress = currentLocationAddress
                    }
                }
            } catch (e: IOException) {
                Toast.makeText(mContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
            return nowAddress
        }

        /**
         * 위도,경도로 주소구하기
         *
         * @param lat
         * @param lng
         * @return 주소
         */
        fun getCoordinate(mContext: Context?, lat: Double, lng: Double): String {
            var nowAddress = "현재 위치를 확인 할 수 없습니다."
            val geocoder = Geocoder(mContext, Locale.KOREA)
            val address: List<Address>?
            try {
                if (geocoder != null) { //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
//한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
                    address = geocoder.getFromLocation(lat, lng, 1)
                    if (address != null && address.size > 0) { // 주소 받아오기
                        val currentLocationAddress = address[0].getAddressLine(0)
                        nowAddress = currentLocationAddress
                    }
                }
            } catch (e: IOException) {
                Toast.makeText(mContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
            return nowAddress
        }
    }

    init {
        getLoc()
    }
}