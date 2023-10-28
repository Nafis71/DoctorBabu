package com.example.doctorbabu.patient;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.doctorbabu.R;
import com.example.doctorbabu.SqliteDatabase.SqliteDatabase;

import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ALARM_TYPE = "once";
    public static int broadcastCode = 0;
    long[] pattern = {0, 100, 400, 600, 800, 1000};
    String medicineName, notificationText = "Medicine Reminder : It's time for you to take ", alarmType, id;

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent AlarmReceiver) {
        medicineName = AlarmReceiver.getStringExtra("medicineName");
        alarmType = AlarmReceiver.getStringExtra("alarmType");
        id = AlarmReceiver.getStringExtra("id");
        notificationText = notificationText + medicineName;
        Intent intent = new Intent(context, MedicineAlarmDestination.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, broadcastCode, intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "medicineAlarm")
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle("Doctor Babu")
                .setContentText(notificationText)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(pattern)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(123, builder.build());
        MediaPlayer mediaPlayer = MediaPlayer.create(context, Settings.System.DEFAULT_ALARM_ALERT_URI);
        mediaPlayer.start();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.stop();
                if (alarmType.equals(ALARM_TYPE)) {
                    try (SqliteDatabase database = new SqliteDatabase(context)) {
                        database.deactivateAlarm(id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 5000);
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
