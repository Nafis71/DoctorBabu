package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.UUID;

public class PatientCall extends AppCompatActivity{
    String uniqueId, userId, doctorId, photoUrl, name, email,callerIdFromDoctor;

    URL url;

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_call);
        callerIdFromDoctor = getIntent().getStringExtra("callerIdFromDoctor");
        userId = getIntent().getStringExtra("userId");
        doctorId = getIntent().getStringExtra("doctorId");
        initializePeer();

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
            long appID = 633349627;
            String appSign = "2f63d4608b7ba6ed887a37562e679b0e842ac4bef26d56ee88eb1474d8112f6b";
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
                    Intent intent = new Intent(PatientCall.this, PrescribeMedicinePrompt.class);
                    intent.putExtra("patientId", callerIdFromDoctor); //callerIdFromDoctor is basically patient id which is coming from callRoom Doctor module.
                    startActivity(intent);
                    finish();
                }
            });
        } else {  //patient side
            long appID = 633349627;
            String appSign = "2f63d4608b7ba6ed887a37562e679b0e842ac4bef26d56ee88eb1474d8112f6b";
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

    public String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    public void onBackPressed() {
       //
    }
}
