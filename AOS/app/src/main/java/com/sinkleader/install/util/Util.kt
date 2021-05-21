package com.sinkleader.install.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.sinkleader.install.BuildConfig
import com.sinkleader.install.MyApplication
import com.sinkleader.install.network.JSONParser
import com.sinkleader.install.ui.activity.WebActivity
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class Util : Constant {
    private val mFace_FONT_NANUMSQUARE_LIGHT: Typeface? = null
    private val mFace_FONT_NANUMSQUARE_REGULAR: Typeface? = null
    private val mFace_FONT_NANUMSQUARE_BOLD: Typeface? = null
    private val mFace_FONT_NANUMSQUARE_EXTRA: Typeface? = null

    companion object {
        var TOKEN = ""
        var RETOKEN = ""
        var USER_SEQ = ""
        var USER_PHONE = ""
        var USER_NAME = ""
        var USER_IMG = ""
        var USER_LASTDATE = ""

        var SIGN_TYPE = ""
        var SIGN_ID = ""
        var SIGN_IMG = ""
        var SIGN_EMAIL = ""

        var ServerIndex = 0
        var FrontIndex = 0

        var SERVER_URL: String = Constant.Serverlist.get(ServerIndex)
        var HOME_URL: String = Constant.FrontUrls.get(FrontIndex) + "/#/main"
        var PUSH_RECEIVE_URL: String = Constant.FrontUrls.get(FrontIndex) + "/common/push/receive"
        var SIGNUP_URL: String = Constant.FrontUrls.get(FrontIndex) + "/login"
        const val TOAST_LELNGTH = 1500
        var IsLockScreen = false

        fun getRealPath(contentUri: Uri): String? {
            var res: String? = null
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor = MyApplication.globalApplicationContext?.contentResolver?.query(contentUri, proj, null, null, null)!!
            if (cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                res = cursor.getString(column_index)
            }
            cursor.close()
            return res
        }

        fun changeServer() {
            PrefMgr.instance.put("ServerIndex", ServerIndex)
            SERVER_URL = Constant.Serverlist.get(ServerIndex)
            HOME_URL = Constant.FrontUrls.get(FrontIndex)
//            PUSH_RECEIVE_URL = Constant.FrontUrls.get(FrontIndex) + "/common/push/receive"
            SIGNUP_URL = Constant.FrontUrls.get(FrontIndex) + "/login/tems"
        }

        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
        val iPAddress: // for now eat exceptions
                String
            get() {
                try {
                    val interfaces: List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
                    for (intf in interfaces) {
                        val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                        for (addr in addrs) {
                            if (!addr.isLoopbackAddress) {
                                val sAddr = addr.hostAddress
                                //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                                val isIPv4 = sAddr.indexOf(':') < 0
                                if (isIPv4) return sAddr
                            }
                        }
                    }
                } catch (ignored: Exception) {
                } // for now eat exceptions
                return ""
            }

        fun setAlpha(view: View?, alpha: Float) {
            if (view == null) {
                return
            }
            //if (isHigherThanHoneyComb()) {
            view.alpha = alpha
            /*} else {
            Drawable w_drawable = view.getBackground();
			if (w_drawable != null) {
				w_drawable.setAlpha((int) (255 * alpha));
			}
		}*/
        }

        // Convert dip to pixel
        fun getDipToPx(context: Context, dip: Int): Int {
            return TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, dip.toFloat(), context.resources.displayMetrics).toInt()
        }

        // Convert sp to pixel
        fun getSpToPx(context: Context, dip: Int): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    dip.toFloat(), context.resources.displayMetrics).toInt()
        }

        fun getDisplaySize(activity: Activity): Point {
            val display = activity.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            return size
        }

        // 텍스트에 각이한 스타일 지정하기
        fun setTextStyle(szTxt: String?, nStyle: Int,
                         nColor: Int, nSize: Int): SpannableStringBuilder {
            val ssb = SpannableStringBuilder()
            ssb.clear()
            if (szTxt == null || szTxt == "") {
                return ssb
            }
            ssb.append(szTxt) // 주의 : 본문에 "."이 들어가면 행바꾸기됩니다.
            try {
                ssb.setSpan(StyleSpan(nStyle), 0, szTxt.length,
                        Spannable.SPAN_COMPOSING) // nStyle : Typeface.BOLD_ITALIC
                ssb.setSpan(ForegroundColorSpan(nColor), 0, szTxt.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                ssb.setSpan(AbsoluteSizeSpan(nSize), 0, szTxt.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } catch (e: Exception) {
                Log.d("Util", "setTextStyle() -->  " + e.message)
            }
            return ssb
            // textView.append(setTextStyle("테스트.스타일", Typeface.BOLD_ITALIC,
// Color.RED, 22));
        }

        /**
         * 금액(double)을 금액표시타입(소숫점2자리)으로 변환한다.
         *
         * @param moneyString 금액(double 형)
         * @return 변경된 금액 문자렬
         */
        fun makeMoneyType(moneyString: String): String {
            val format = "#,###.##" /* "#.##0.00" */
            val df = DecimalFormat(format)
            val dfs = DecimalFormatSymbols()
            dfs.groupingSeparator = ','
            df.groupingSize = 3
            df.decimalFormatSymbols = dfs
            return try {
                df.format(moneyString.toFloat().toDouble())
            } catch (e: Exception) {
                moneyString
            }
        }

        fun makeMoneyType(`val`: Int): String {
            return makeMoneyType(`val`.toString())
        }

        // Shows toast with specify delay that is shorter than Toast.LENGTH_SHORT
        @JvmOverloads
        fun showToast(context: Context, msg: String?, isLong: Boolean = false) {
            val toast = Toast.makeText(context.applicationContext,
                    msg, if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
            toast.show()
            val handler = Handler()
            handler.postDelayed({ toast.cancel() }, TOAST_LELNGTH.toLong())
        }

        @JvmOverloads
        fun showToast(context: Context, resId: Int, isLong: Boolean = false) {
            val toast = Toast.makeText(context.applicationContext,
                    context.getString(resId), if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
            toast.show()
            val handler = Handler()
            handler.postDelayed({ toast.cancel() }, TOAST_LELNGTH.toLong())
        }

        var typefaceCache: Map<String, Typeface> = HashMap()
        //check email
        fun isValidEmail(email: String): Boolean {
            var isValid = false
            val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
            val inputStr: CharSequence = email
            val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(inputStr)
            if (matcher.matches()) {
                isValid = true
            }
            return isValid
        }

        fun deleteDir(path: String?) {
            val dir = File(path)
            if (dir.exists()) {
                val childFileList = dir.listFiles()
                for (childFile in childFileList) {
                    if (childFile.isDirectory) {
                        deleteDir(childFile.absolutePath) // 하위 디렉토리 루프
                    } else {
                        childFile.delete() // 하위 파일삭제
                    }
                }
                dir.delete() // root 삭제
            }
        }

        /**
         * Delete all photo in sd card
         */
        fun deletePhotoOfSDCard(context: Context) {
            val rootDir = File(Environment.getExternalStorageDirectory().toString() + File.separator + Constant.Companion.SDCARD_FOLDER)
            if (rootDir.exists()) {
                try {
                    val list = rootDir.list()
                    deleteDir(Environment.getExternalStorageDirectory().toString() + File.separator + Constant.Companion.SDCARD_FOLDER)
                    galleryAddPic(context, list)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            //rootDir.mkdir();
        }

        fun galleryAddPic(context: Context, files: Array<String>?) {
            println("================================ galleryAddPic ================================ : " +
                    Environment.getExternalStorageDirectory() + File.separator + Constant.Companion.SDCARD_FOLDER)
            val rootDir: File
            val dir: Array<String>
            if (files == null) {
                rootDir = File(Environment.getExternalStorageDirectory().toString() + File.separator + Constant.Companion.SDCARD_FOLDER)
                if (rootDir.exists() == false) {
                    rootDir.mkdir()
                }

                dir = rootDir.list()
            } else {
                dir = files
            }
            try {
                for (i in dir.indices) {
                    val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    val f = File(Environment.getExternalStorageDirectory().toString() + File.separator + Constant.Companion.SDCARD_FOLDER + "/" + dir[i])
                    val contentUri = Uri.fromFile(f)
                    mediaScanIntent.data = contentUri
                    context.sendBroadcast(mediaScanIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            //        MediaScanner.newInstance(context).mediaScanning(Environment.getExternalStorageDirectory() + File.separator + Constant.SDCARD_FOLDER + "/");
        }

        fun deleteFiles(path: String?) {
            val dir = File(path)
            if (dir.exists()) {
                if (dir == null) return
                val childFileList = dir.listFiles() ?: return
                for (childFile in childFileList) {
                    if (childFile.isDirectory) {
                        continue
                    } else {
                        childFile.delete() // 하위 파일삭제
                    }
                }
            }
        }

        fun createRootDir() {
            var folder = File(Environment.getExternalStorageDirectory()
                    .toString() + "/" + Constant.Companion.SDCARD_FOLDER)
            folder.mkdirs()
            folder = File(Environment.getExternalStorageDirectory()
                    .toString() + "/" + Constant.Companion.TMP_FOLDER)
            folder.mkdirs()
        }

        fun cropBitmap(bitmap: Bitmap?, rect: Point, location: Point): Bitmap {
            return Bitmap.createBitmap(bitmap!!, location.x //X 시작위치 (원본의 4/1지점)
                    , location.y //Y 시작위치 (원본의 4/1지점)
                    , rect.x // 넓이 (원본의 절반 크기)
                    , rect.y)
        }

        /**
         * Show keyboard
         */
        fun showKeyboard(edit: EditText) {
            val imgr = edit.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imgr.showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT)
        }

        /**
         * Close keyboard
         */
        fun hideKeyboard(edit: EditText) {
            val imm = edit.context
                    .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(edit.windowToken, 0)
        }

        @SuppressLint("MissingPermission")
        fun getDeviceId(context: Context): String {
            var deviceId = ""
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            val tmDevice: String
            val tmSerial: String
            val androidId: String
            tmDevice = "" + tm.deviceId
            //tmSerial = "" + tm.getSimSerialNumber();
            //androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            val deviceUuid = UUID(tmDevice.hashCode().toLong(), tmDevice.hashCode().toLong() shl 32 or tmDevice.hashCode().toLong())
            deviceId = deviceUuid.toString()
            return deviceId
        }

        fun getDeviceModel(context: Context?): String {
            return Build.MODEL
        }

        fun getOS(context: Context): String {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return tm.simOperatorName
        }

        @SuppressLint("MissingPermission")
        fun getDeviceNumber(context: Context): String {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            var PhoneNum = ""
            try {
                PhoneNum = tm.line1Number
                if (PhoneNum.startsWith("+82")) {
                    PhoneNum = PhoneNum.replace("+82", "0")
                }
            } catch (e: Exception) {
                Log.d("getDeviceNumber", e.message)
            }
            return PhoneNum
        }

        val androidVersion: String
            get() {
                val release = Build.VERSION.RELEASE
                val sdkVersion = Build.VERSION.SDK_INT
                return "Android SDK: $sdkVersion ($release)"
            }

        val version: String
            get() = BuildConfig.VERSION_NAME

        fun goToAppSettings(activity: Activity) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", activity.packageName, null))
            activity.startActivity(intent)
        }

        fun gotoSetting(activity: Activity, request: Int) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivityForResult(intent, request)
        }

        val isEmulator: Boolean
            get() = (Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.FINGERPRINT.startsWith("Android-x86")
                    || Build.FINGERPRINT.startsWith("Android-x64")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                    || "google_sdk" == Build.PRODUCT)

        /**
         * bitmap 를 jpeg 파일로 저장; 출처 : http://snowbora.com/418
         */
        fun saveBitmapToFileCache(bitmap: Bitmap?,
                                  strFilePath: String?, bPNG: Boolean): Boolean {
            if (bitmap == null) {
                return false
            }
            var bRet: Boolean
            bRet = false
            val fileItem = File(strFilePath)
            var out: OutputStream? = null
            try {
                fileItem.parentFile.mkdirs()
                fileItem.createNewFile()
                out = FileOutputStream(fileItem)
                if (bPNG) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                } else {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                out.flush()
                bRet = true
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    out?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return bRet
        }

        private const val PHOTO_DATE_FORMAT = "'IMG'_yyyyMMdd_HHmmss"
        private var NEW_PHOTO_DIR_PATH = ""
        fun pathForNewCameraPhoto(fileName: String?): String {
            if (NEW_PHOTO_DIR_PATH.isEmpty()) {
                NEW_PHOTO_DIR_PATH = Environment.getExternalStorageDirectory().toString() + "/DCIM/Camera"
            }
            val dir = File(NEW_PHOTO_DIR_PATH)
            dir.mkdirs()
            val f = File(dir, fileName)
            return f.absolutePath
        }

        fun generateTempPhotoFileName(): String {
            val date = Date(System.currentTimeMillis())
            val dateFormat = SimpleDateFormat(PHOTO_DATE_FORMAT, Locale.KOREA)
            return "ContactPhoto-" + dateFormat.format(date) + ".jpg"
        }

        fun pathForCroppedPhoto(context: Context, fileName: String?): String {
            val dir = File(context.externalCacheDir.toString() + "/tmp")
            dir.mkdirs()
            val f = File(dir, fileName)
            return f.absolutePath
        }

        /**
         * @param intent
         * @param croppedPhotoUri
         * @param x               : ratio
         * @param y               : ratio
         * @param pix             : 1ratio = ? pix
         */
        fun addGalleryIntentExtras(intent: Intent, croppedPhotoUri: Uri?, x: Int, y: Int, pix: Int) {
            intent.putExtra("crop", "true")
            intent.putExtra("scale", true)
            intent.putExtra("scaleUpIfNeeded", true)
            intent.putExtra("aspectX", x)
            intent.putExtra("aspectY", y)
            intent.putExtra("outputX", pix * x)
            intent.putExtra("outputY", pix * y)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, croppedPhotoUri)
        }

        fun urlDecode(text: String?): String {
            return if (text == null || text.isEmpty()) "" else try {
                URLDecoder.decode(text, "UTF-8")
            } catch (e: Exception) {
                ""
            }
        }

        fun getExtensionOfFile(filePath: String?): String {
            var fileExtension = ""
            val m = Pattern.compile(".*/.*?(\\..*)").matcher(filePath)
            if (m.matches()) {
                fileExtension = m.group(1)
            }
            return fileExtension.replace(".", "")
        }

        const val DATE_FORMAT1 = "yyyy-MM-dd hh:mm:ss"
        const val DATE_FORMAT2 = "yyyy-MM-dd"
        fun getDateFromString(date: String?, format: String?): Date {
            val simpleDateFormat = SimpleDateFormat(format, Locale.KOREA)
            return try {
                simpleDateFormat.parse(date)
            } catch (e: Exception) {
                e.printStackTrace()
                Date()
            }
        }

        fun getStringFromDate(date: Date?, format: String?): String {
            val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
            return simpleDateFormat.format(date)
        }

        fun showLocalFileImageByThumbnail(context: Context?, cr: ContentResolver?, view: ImageView?, url: String?, image_id: Long) {
            if (view == null) return
            if (url == null || url.isEmpty()) {
                view.setImageResource(0)
            } else {
                val bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                        cr, image_id,
                        MediaStore.Images.Thumbnails.MINI_KIND,
                        null)
                view.setImageBitmap(bitmap)
            }
        }

        fun setNumOver1000(view: TextView, curNum: Int) {
            if (curNum >= 1000) {
                view.setText((curNum / 1000).toString() + "K")
            } else {
                view.text = curNum.toString() + ""
            }
        }

        /**
         * 길이에 따라 행바꾸어 진 텍스트 얻기
         *
         * @param content
         * @param Len
         * @return
         */
        fun getStringLengthUnit(content: String, Len: Int): String {
            if (content.isEmpty()) return ""
            if (content.length <= Len) return content
            val strFirst = content.substring(0, Len)
            val strSecond = content.substring(Len)
            return strFirst + "\n" + strSecond
        }

        fun getStringFromBase64(content: String): String {
            return try {
                val data = Base64.decode(content, Base64.DEFAULT)
                String(data, StandardCharsets.UTF_8)
            } catch (e: Exception) {
                e.printStackTrace()
                content
            }
        }

        fun getBase64String(content: String): String {
            return try {
                val data = content.toByteArray(StandardCharsets.UTF_8)
                Base64.encodeToString(data, Base64.DEFAULT)
            } catch (e: Exception) {
                content
            }
        }

        private var mProgress: ProgressDialog? = null
        fun showLoading(context: Context?, title: String?, msg: String?) {
            if (mProgress != null) return
            mProgress = ProgressDialog(context)
            mProgress!!.setTitle(title)
            mProgress!!.setMessage(msg)
            mProgress!!.setCancelable(true)
            mProgress!!.setCanceledOnTouchOutside(false)
            mProgress!!.isIndeterminate = false
            mProgress!!.setOnDismissListener { mProgress = null }
            mProgress!!.show()
        }

        fun hideLoading() {
            if (mProgress != null && mProgress!!.isShowing) {
                mProgress!!.dismiss()
                mProgress = null
            }
        }

        fun moveWebPage(activity:Activity, user:JSONObject, url:String) {
            TOKEN = JSONParser.getString(user, "token")
            RETOKEN = JSONParser.getString(user, "refresh_token")
            USER_PHONE = JSONParser.getString(user, "cell_phone")
            USER_SEQ = JSONParser.getInt(user, "user_sno").toString()
            USER_NAME = JSONParser.getString(user, "user_name")
            USER_LASTDATE = JSONParser.getString(user, "last_access_date")
            PrefMgr.instance.put("token", TOKEN)

            var web_url = Constant.FrontUrls[FrontIndex] + "/home"
            if (!url?.equals("")){
                web_url = url
            }

            val intent = Intent(activity, WebActivity::class.java)
            intent.putExtra("WEB_URL", web_url)
            intent.putExtra("bck", true)
            activity.startActivity(intent)
            activity.finish()
        }
    }
}

internal class MediaScanner private constructor(private val mContext: Context) {
    private var mPath: String? = null
    private var mMediaScanner: MediaScannerConnection? = null
    private var mMediaScannerClient: MediaScannerConnectionClient? = null
    fun mediaScanning(path: String?) {
        if (mMediaScanner == null) {
            mMediaScannerClient = object : MediaScannerConnectionClient {
                override fun onMediaScannerConnected() {
                    mMediaScanner!!.scanFile(mPath, null) // 디렉토리
                    // 가져옴
                }

                override fun onScanCompleted(path: String, uri: Uri) {}
            }
            mMediaScanner = MediaScannerConnection(mContext, mMediaScannerClient)
        }
        mPath = path
        mMediaScanner!!.connect()
    }

    companion object {
        fun newInstance(context: Context): MediaScanner {
            return MediaScanner(context)
        }
    }

}