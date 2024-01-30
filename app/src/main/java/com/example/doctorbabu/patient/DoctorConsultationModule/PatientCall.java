package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorbabu.DatabaseModels.AppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.doctor.PrescribeMedicinePrompt;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceConfig;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceFragment;


import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PatientCall extends AppCompatActivity{
    String uniqueId, userId, doctorId, photoUrl, name, email,callerIdFromDoctor;

    URL url;
    ExecutorService doctorStatisticsExecutor,appointmentExecutor;
    int patientTreated,consultancyTaken,appointmentTaken = 0;
    boolean hasAppointment;

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_call);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        doctorStatisticsExecutor = Executors.newSingleThreadExecutor();
        appointmentExecutor = Executors.newSingleThreadExecutor();
        callerIdFromDoctor = getIntent().getStringExtra("callerIdFromDoctor");
        userId = getIntent().getStringExtra("userId");
        doctorId = getIntent().getStringExtra("doctorId");
        initializePeer();
        if(userId.equals("null")){
            doctorStatisticsExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    loadDoctorStatistics();
                }
            });
            appointmentExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    checkForAppointment();
                }
            });
        }
    }

    public void loadDoctorStatistics(){
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorStatistics");
        reference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    patientTreated = Integer.parseInt(String.valueOf(snapshot.child("patientTreated").getValue()));
                    consultancyTaken = Integer.parseInt(String.valueOf(snapshot.child("consultancyDone").getValue()));
                    appointmentTaken = Integer.parseInt(String.valueOf(snapshot.child("appointmentDone").getValue()));
                }else{
                    patientTreated = 0;
                    consultancyTaken = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void checkForAppointment(){
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        reference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        AppointmentModel model = snap.getValue(AppointmentModel.class);
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime now = LocalDateTime.now();
                        String currentDate = dtf.format(now);
                        String[] dateArray = currentDate.split("-");
                        String pattern = "0";
                        DecimalFormat numberFormatter = new DecimalFormat(pattern);
                        String currentYear = numberFormatter.format(Integer.parseInt(dateArray[0]));
                        String currentMonth = numberFormatter.format(Integer.parseInt(dateArray[1]));
                        String currentDay = numberFormatter.format(Integer.parseInt(dateArray[2]));
                        String formattedDate = currentYear + "-" + currentMonth + "-" + currentDay;
                        assert model != null;
                        if (model.getAppointmentDate().equals(formattedDate)) {
                            hasAppointment = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void initializePeer() {
        if (!userId.equals("null")) {
            uniqueId = getUniqueId();
            DatabaseReference reference = database.getReference("callRoom");
            reference.child(doctorId).child("connId").setValue(uniqueId);
            clientInfo(userId, false);
        } else {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    DatabaseReference reference = database.getReference("callRoom");
                    reference.child(doctorId).child("connId").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                uniqueId = String.valueOf(snapshot.getValue());
                                clientInfo(doctorId, true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }, 3000);
        }
    }

    public void clientInfo(String id, boolean statusCode) {
        if (statusCode) {
            DatabaseReference reference = database.getReference("doctorInfo");
            reference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    photoUrl = String.valueOf(snapshot.child("photoUrl").getValue());
                    String fullName = String.valueOf(snapshot.child("fullName").getValue());
                    String title = String.valueOf(snapshot.child("title").getValue());
                    name = title + fullName;
                    email = String.valueOf(snapshot.child("email").getValue());
                    try {
                        url = new URL(photoUrl);
                        callVideoScreen();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        } else {
            DatabaseReference reference = database.getReference("users");
            reference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    photoUrl = String.valueOf(snapshot.child("photoUrl").getValue());
                    name = String.valueOf(snapshot.child("fullName").getValue());
                    email = String.valueOf(snapshot.child("email").getValue());
                    try {
                        url = new URL(photoUrl);
                        callVideoScreen();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        }
    }

    public void callVideoScreen() {
        if (userId.equals("null")) {  //doctor side
            long appID = 1144567169;
            String appSign = "1b93ab06c902dfc5fe377b0013ddd0cfd11be1e9427298c65fc51b70ea65d527";
            String conferenceID = uniqueId;
            String userID = doctorId;
            String userName = name;

            ZegoUIKitPrebuiltVideoConferenceConfig config = new ZegoUIKitPrebuiltVideoConferenceConfig();
            ZegoUIKitPrebuiltVideoConferenceFragment fragment = ZegoUIKitPrebuiltVideoConferenceFragment.newInstance(appID, appSign, userID, userName,conferenceID,config);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitNow();
            fragment.setLeaveVideoConferenceListener(new ZegoUIKitPrebuiltVideoConferenceFragment.LeaveVideoConferenceListener() {
                @Override
                public void onLeaveConference() {
                    patientTreated+=1;
                    consultancyTaken+=1;
                    Firebase firebase = Firebase.getInstance();
                    DatabaseReference reference = firebase.getDatabaseReference("doctorStatistics");
                    reference.child(doctorId).child("patientTreated").setValue(patientTreated);
                    reference.child(doctorId).child("consultancyDone").setValue(consultancyTaken);
                    if(!hasAppointment && appointmentTaken == 0){
                        reference.child(doctorId).child("appointmentDone").setValue(appointmentTaken);
                    }
                    if(hasAppointment){
                        appointmentTaken+=1;
                        reference.child(doctorId).child("appointmentDone").setValue(appointmentTaken);
                    }
                    Intent intent = new Intent(PatientCall.this, PrescribeMedicinePrompt.class);
                    intent.putExtra("patientId", callerIdFromDoctor); //callerIdFromDoctor is basically patient id which is coming from callRoom Doctor module.
                    startActivity(intent);
                    finish();
                }
            });
        } else {  //patient side
            long appID = 1144567169;
            String appSign = "1b93ab06c902dfc5fe377b0013ddd0cfd11be1e9427298c65fc51b70ea65d527";
            String conferenceID = uniqueId;
            String userID = userId;
            String userName = name;
            ZegoUIKitPrebuiltVideoConferenceConfig config = new ZegoUIKitPrebuiltVideoConferenceConfig();
            ZegoUIKitPrebuiltVideoConferenceFragment fragment = ZegoUIKitPrebuiltVideoConferenceFragment.newInstance(appID, appSign, userID, userName,conferenceID,config);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commitNow();
            fragment.setLeaveVideoConferenceListener(new ZegoUIKitPrebuiltVideoConferenceFragment.LeaveVideoConferenceListener() {
                @Override
                public void onLeaveConference() {
                    Intent intent = new Intent(PatientCall.this, PatientReview.class);
                    intent.putExtra("doctorId", doctorId);
                    startActivity(intent);
                    finish();
                }
            });
        }}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appointmentExecutor.shutdown();
        doctorStatisticsExecutor.shutdown();
    }

    public String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    public void onBackPressed() {
       //
    }
}
