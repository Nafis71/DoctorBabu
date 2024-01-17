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

import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;


public class AppointmentDeleter extends BroadcastReceiver {
    String appointmentID,doctorID,appointmentHour,appointmentDate,appointmentMinute,timePeriod,doctorName;
    Firebase firebase;
    FirebaseUser user;
    String notificationText = "You have missed an appointment with ";

    int broadcastCode = 123;

    @Override
    public void onReceive(Context context, Intent appointmentDeleter) {
        appointmentID = appointmentDeleter.getStringExtra("appointmentID");
        doctorID = appointmentDeleter.getStringExtra("doctorID");
        doctorName = appointmentDeleter.getStringExtra("doctorName");
        getAppointmentData();
        notificationBuilder(context);
    }


    public void notificationBuilder(Context context){
        notificationText = notificationText + doctorName +" \npress here to book again!";
        Intent intent = new Intent(context, BookAppointment.class);
        intent.putExtra("doctorID",doctorID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, broadcastCode, intent, PendingIntent.FLAG_MUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "appointmentReminder")
                .setSmallIcon(R.drawable.applogo)
                .setContentTitle("Missed Appointment")
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

    public void getAppointmentData(){
        firebase = Firebase.getInstance();
        user = firebase.getUserID();
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        reference.child(user.getUid()).child(appointmentID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    appointmentHour = String.valueOf(snapshot.child("appointmentHour").getValue());
                    appointmentDate = String.valueOf(snapshot.child("appointmentDate").getValue());
                    appointmentMinute = String.valueOf(snapshot.child("appointmentMinute").getValue());
                    timePeriod = String.valueOf(snapshot.child("timePeriod").getValue());
                    insertMissedAppointment();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void insertMissedAppointment(){
        DatabaseReference reference = firebase.getDatabaseReference("missedAppointments");
        HashMap<String, String> appointmentData =  new HashMap<>();
        appointmentData.put("appointmentID",appointmentID);
        appointmentData.put("patientID",user.getUid());
        appointmentData.put("doctorID",doctorID);
        appointmentData.put("appointmentHour",appointmentHour);
        appointmentData.put("appointmentMinute",appointmentMinute);
        appointmentData.put("timePeriod",timePeriod);
        appointmentData.put("appointmentDate",appointmentDate);
        reference.child(doctorID).child(appointmentID).setValue(appointmentData);
        reference.child(user.getUid()).child(appointmentID).setValue(appointmentData);
        deleteAppointment();
    }

    public void deleteAppointment(){
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        reference.child(user.getUid()).child(appointmentID).removeValue();
        reference.child(doctorID).child(appointmentID).removeValue();
    }

}
