package com.sinkleader.install.fcm;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sinkleader.install.R;
import com.sinkleader.install.network.JSONParser;
import com.sinkleader.install.ui.activity.IntroActivity;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static int seq = 0;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map notificationData = remoteMessage.getData();
        try {
            JSONObject jsonObject = new JSONObject(notificationData);
            String title = JSONParser.getString(jsonObject, "title");
            String msg = JSONParser.getString(jsonObject, "body");
            String url = JSONParser.getString(jsonObject, "url");

            int id = seq; // Integer.parseInt(JSONParser.getString(jsonObject, "noti_seq"));

            seq += 1;
            if (seq == 100){
                seq = 0;
            }


            sendNotification(title, msg, url, id);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendNotification(String title, String msg, final String url, final int id){//noti_seq, push_log_seq
        Intent intent = settingIntent(url);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, FcmLibrary.PUSH_CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent);

            manager.notify(id, builder.build());
        }else {
            Notification noti = new NotificationCompat.Builder(this, FcmLibrary.PUSH_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setVibrate(new long[]{300,200,300,200})
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentIntent(resultPendingIntent).build();
            noti.defaults |= Notification.DEFAULT_LIGHTS;
//        noti.flags |= Notification.FLAG_;

            int noti_id = id;
//            manager.cancel(noti_id);
            manager.notify(noti_id, noti);
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        FcmLibrary.getInstance().setToken(token);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        // FcmLibrary.getInstance().sendRegistrationToServer(token); 에서 처리
    }


    private boolean checkRunningApp(){
        boolean isbool = false;
        String strPackage = "";
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> proceses = am.getRunningAppProcesses();

        //프로세서 전체를 반복
        for(ActivityManager.RunningAppProcessInfo process : proceses)
        {
            if(process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
            {
                strPackage = process.processName; //package이름과 동일함.
                Log.d("TEST", strPackage);
                isbool = true;
            }
        }

        return isbool;
    }

    private Intent settingIntent(String url){
        Intent intent = new Intent(this, IntroActivity.class);

        if (url != null && URLUtil.isValidUrl(url)){
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        }else{
            intent.putExtra("WEB_URL", url);
        }

        return intent;
    }
}
