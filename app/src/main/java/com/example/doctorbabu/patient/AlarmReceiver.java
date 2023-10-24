package com.example.doctorbabu.patient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.doctorbabu.R;

public class AlarmReceiver extends BroadcastReceiver {
    public static int broadcastCode=0;
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent AlarmReceiver) {
        Intent intent = new Intent(context,MedicineAlarmDestination.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,broadcastCode,intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "medicineAlarm")
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle("Doctor Babu")
                .setContentText("Medicine Reminder : It's time for you to take medicine")
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(123, builder.build());
    }
}
