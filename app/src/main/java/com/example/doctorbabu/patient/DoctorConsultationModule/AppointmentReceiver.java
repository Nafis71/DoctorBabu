package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.AlarmModules.MedicineAlarmDestination;

import java.util.Random;

public class AppointmentReceiver extends BroadcastReceiver {
    int broadcastCode = 0;
    String notificationText = "You have an appointment ";
    @Override
    public void onReceive(Context context, Intent appointmentIntent) {
        Intent intent = new Intent(context, MedicineAlarmDestination.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, broadcastCode, intent, PendingIntent.FLAG_MUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "appointmentReminder")
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle("Doctor Babu")
                .setContentText(notificationText)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManagerCompat.notify(124, builder.build());
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
