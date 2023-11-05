package com.example.doctorbabu.patient.AlarmModules;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.view.Display;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.doctorbabu.R;
import com.example.doctorbabu.SqliteDatabase.SqliteDatabase;

import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ALARM_TYPE = "once";
    public int broadcastCode = 0;
    DisplayManager displayManager;
    String medicineName, notificationText = "Medicine Reminder : It's time for you to take ", alarmType, id;

    @Override
    public void onReceive(Context context, Intent AlarmReceiver) {
        medicineName = AlarmReceiver.getStringExtra("medicineName");
        alarmType = AlarmReceiver.getStringExtra("alarmType");
        id = AlarmReceiver.getStringExtra("id");
        notificationText = notificationText + medicineName;
        Intent intent = new Intent(context, MedicineAlarmDestination.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("alarmID", id);
        intent.putExtra("fromNotification",true);
        deactivateAlarm(context);
        displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        for(Display display : displayManager.getDisplays()){
            if(display.getState() ==  Display.STATE_OFF){
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        launchIntent(intent, context);
                    }},2000);
            }else{
                createNotification(context, intent);
            }
        }
    }

    public void createNotification(Context context, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, broadcastCode, intent, PendingIntent.FLAG_MUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "medicineAlarm")
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle("Doctor Babu")
                .setContentText(notificationText)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManagerCompat.notify(123, builder.build());
    }

    public void deactivateAlarm(Context context) {
        if (alarmType.equals(ALARM_TYPE)) {
            try (SqliteDatabase database = new SqliteDatabase(context)) {
                database.deactivateAlarm(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void launchIntent(Intent intent, Context context) {
        intent.putExtra("fromNotification",false);
        context.startActivity(intent);
    }

    public int getUniqueID() {
        Random random = new Random();
        int randomNumber = random.nextInt();
        if (randomNumber != broadcastCode) {
            return randomNumber;
        } else {
            getUniqueID();
        }
        return randomNumber;
    }

    public void setBroadcastCode() {
        broadcastCode = getUniqueID();
    }

    public int getBroadcastCode() {
        return broadcastCode;
    }
}
