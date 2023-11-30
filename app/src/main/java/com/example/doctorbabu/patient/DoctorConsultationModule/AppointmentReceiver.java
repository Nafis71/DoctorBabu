package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.doctorbabu.DatabaseModels.PendingAppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class AppointmentReceiver extends BroadcastReceiver {
    int broadcastCode = 0;
    String doctorID, doctorName;
    String notificationText = "You have an appointment with ";
    Context context;

    @Override
    public void onReceive(Context context, Intent appointmentIntent) {
        this.context = context;
        doctorID = appointmentIntent.getStringExtra("doctorID");
        getDoctorName();

    }

    public void getDoctorName() {
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
        reference.child(doctorID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String doctorTitle = String.valueOf(snapshot.child("title").getValue());
                    String fullName = String.valueOf(snapshot.child("fullName").getValue());
                    doctorName = doctorTitle + fullName;
                    buildNotification();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void buildNotification() {
        notificationText = notificationText + doctorName;
        Intent intent = new Intent(context, PendingAppointmentModel.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, broadcastCode, intent, PendingIntent.FLAG_MUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "appointmentReminder")
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle("Doctor Appointment")
                .setContentText(notificationText)
                .setAutoCancel(false)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE).setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManagerCompat.notify(224, builder.build());
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
