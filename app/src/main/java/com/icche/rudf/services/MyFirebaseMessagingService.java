package com.icche.rudf.services;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.icche.rudf.WebviewActivityFile;
import com.icche.rudf.utils.NotificationUtils;
import com.icche.rudf.vo.NotificationVO;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgingService";
    private static final String TITLE = "title";
    private static final String EMPTY = "";
    private static final String MESSAGE = "message";
    private static final String IMAGE = "image";
    private static final String ACTION = "action";
    private static final String DATA = "data";
    private static final String ACTION_DESTINATION = "action_destination";

    SQLiteDatabase sqLiteDatabaseObj;
    String SQLiteDataBaseQueryHolder;

    @Override
    public void onCreate() {
        SQLiteDataBaseBuild();
        SQLiteTableBuild();
    }
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> data = remoteMessage.getData();
            handleData(data);

        } else if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification());
        }// Check if message contains a notification payload.

    }

    private void handleNotification(RemoteMessage.Notification RemoteMsgNotification) {
        String message = RemoteMsgNotification.getBody();
        String title = RemoteMsgNotification.getTitle();
        NotificationVO notificationVO = new NotificationVO();
        notificationVO.setTitle(title);
        notificationVO.setMessage(message);

        Intent resultIntent = new Intent(getApplicationContext(), WebviewActivityFile.class);
        resultIntent.putExtra("viewNotificationMsg", message);
        resultIntent.putExtra("viewNotificationTitle", title);


        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.displayNotification(notificationVO, resultIntent);
        notificationUtils.playNotificationSound();

        String date = DateFormat.getDateInstance(DateFormat.LONG).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        SQLiteDataBaseQueryHolder = "INSERT INTO aisTable (title,message, time) VALUES('" + title +
                "', '" + message + "', '" +date+" "+time + "');";
        sqLiteDatabaseObj.execSQL(SQLiteDataBaseQueryHolder);
    }

    private void handleData(Map<String, String> data) {
        String title = data.get(TITLE);
        String message = data.get(MESSAGE);
        String iconUrl = data.get(IMAGE);
        String action = data.get(ACTION);
        String actionDestination = data.get(ACTION_DESTINATION);
        NotificationVO notificationVO = new NotificationVO();
        notificationVO.setTitle(title);
        notificationVO.setMessage(message);
        notificationVO.setIconUrl(iconUrl);
        notificationVO.setAction(action);
        notificationVO.setActionDestination(actionDestination);

        Intent resultIntent = new Intent(getApplicationContext(), WebviewActivityFile.class);
        resultIntent.putExtra("viewNotificationMsg", message);
        resultIntent.putExtra("viewNotificationTitle", title);
        resultIntent.putExtra("viewNotificationUrl", iconUrl);

        NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
        notificationUtils.displayNotification(notificationVO, resultIntent);
        notificationUtils.playNotificationSound();

        String date = DateFormat.getDateInstance(DateFormat.FULL).format(new Date());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        SQLiteDataBaseQueryHolder = "INSERT INTO aisTable (title,message, time) VALUES('" + title +
                "', '" + message + "', '" + date+" "+time + "');";
        sqLiteDatabaseObj.execSQL(SQLiteDataBaseQueryHolder);
    }

    public void SQLiteDataBaseBuild() {
        sqLiteDatabaseObj = openOrCreateDatabase("ais", Context.MODE_PRIVATE, null);
    }

    public void SQLiteTableBuild() {
        sqLiteDatabaseObj.execSQL("CREATE TABLE IF NOT EXISTS aisTable (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title VARCHAR, message VARCHAR, time VARCHAR);");
    }

}
