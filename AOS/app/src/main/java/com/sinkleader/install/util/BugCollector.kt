package com.sinkleader.install.util

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Environment
import java.io.*
import java.util.*

class BugCollector {
    private val currentFileName: String
        private get() {
            val d = Date()
            var date = ""
            var hour = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d)
                val format = SimpleDateFormat("HH-mm-ss", Locale.getDefault())
                hour = format.format(d)
            }
            return date + "_" + hour + ".txt"
        }

    companion object {
        const val EXTRA_CRASH = "BugCollector.CRASH"
        const val EXTRA_WASPLAYING = "BugCollector.WASPLAYING"
    }

    init { //make debug folder
        val dir = File(Environment.getExternalStorageDirectory().toString() + "/bug")
        if (dir.exists() == false) dir.mkdirs()
        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            e.printStackTrace()
            val file = File(dir, currentFileName)
            var fs: FileOutputStream? = null
            try {
                fs = FileOutputStream(file)
                val sb = StringBuilder()
                val sw = StringWriter()
                e.printStackTrace(PrintWriter(sw))
                sb.append(e.message)
                sb.append(System.getProperty("line.separator"))
                sb.append(sw.toString())
                sb.append(System.getProperty("line.separator"))
                fs.write(sb.toString().toByteArray())
            } catch (e1: FileNotFoundException) {
                e1.printStackTrace()
            } catch (e1: IOException) {
                e1.printStackTrace()
            } finally {
                if (fs != null) {
                    try {
                        fs.close()
                    } catch (e1: IOException) {
                        e1.printStackTrace()
                    }
                }
            }
            //Toast.makeText(MainActivity.getContext() , "APP CRASH!", Toast.LENGTH_LONG).show();
//https://medium.com/@ssaurel/how-to-auto-restart-an-android-application-after-a-crash-or-a-force-close-error-1a361677c0ce
//                Intent intent = new Intent(MainActivity.getActivity(), MainActivity.class);
//                intent.putExtra(EXTRA_CRASH , e.getMessage());
//                intent.putExtra(EXTRA_WASPLAYING , SCContentViewManager.getInst().isPlaying());
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
//                                        | Intent.FLAG_ACTIVITY_NEW_TASK);
//                PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.getContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
//                AlarmManager mgr = (AlarmManager) MainActivity.getContext().getSystemService(MainActivity.getContext().ALARM_SERVICE);
//                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
//                MainActivity.getActivity().finish();
            System.exit(2)
        }
    }
}