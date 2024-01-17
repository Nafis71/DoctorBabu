package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.doctorbabu.DatabaseModels.AppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.SqliteDatabase.SqliteDatabase;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class AppointmentReceiver extends BroadcastReceiver {
    int broadcastCode = 0;
    String doctorID, doctorName, appointmentID;
    String notificationText = "You have an appointment with ";
    Context context;
    Firebase firebase;

    @Override
    public void onReceive(Context context, Intent appointmentIntent) {
        this.context = context;
        firebase = Firebase.getInstance();
        doctorID = appointmentIntent.getStringExtra("doctorId");
        appointmentID = appointmentIntent.getStringExtra("appointmentID");
        getDoctorName();
        insertAppointmentInfo();
    }

    public void insertAppointmentInfo(){
        try(SqliteDatabase database = new SqliteDatabase(context)){
            FirebaseUser user = firebase.getUserID();
            database.insertAppointmentInfo(user.getUid(),appointmentID);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void getDoctorName() {
        DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
        reference.child(doctorID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String doctorTitle = String.valueOf(snapshot.child("title").getValue());
                    String fullName = String.valueOf(snapshot.child("fullName").getValue());
                    doctorName = doctorTitle + fullName;
                    buildNotification();
                    setTimer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void buildNotification() {
        notificationText = notificationText + doctorName;
        Intent intent = new Intent(context, AppointmentModel.class);
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
        notificationManagerCompat.notify(225, builder.build());
    }

    public void setTimer() {
        Intent intent = new Intent(context, AppointmentDeleter.class);
        intent.putExtra("appointmentID", appointmentID);
        intent.putExtra("doctorID", doctorID);
        intent.putExtra("doctorName", doctorName);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 123, intent, PendingIntent.FLAG_MUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (21 * 60000), pendingIntent);
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
