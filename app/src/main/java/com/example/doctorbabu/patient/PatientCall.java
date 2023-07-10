package com.example.doctorbabu.patient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.webkit.WebView;

import android.widget.ImageView;
import com.example.doctorbabu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetActivityDelegate;
import org.jitsi.meet.sdk.JitsiMeetActivityInterface;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetOngoingConferenceService;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import org.jitsi.meet.sdk.JitsiMeetView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import timber.log.Timber;

public class PatientCall extends AppCompatActivity {
    ImageView closeCall, audioButton, videoButton;
    WebView webView;
    String uniqueId, userId, doctorId, photoUrl, name, email;
    FirebaseAuth auth;
    URL url;
    boolean isPeerConnected = false, isAudio = true, isVideo = true, pageExit = false;

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_call);
        userId = getIntent().getStringExtra("userId");
        doctorId = getIntent().getStringExtra("doctorId");
        URL serverUrl;
        try {
            serverUrl = new URL("https://meet.jit.si");
            JitsiMeetConferenceOptions defaultOptions = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverUrl).setFeatureFlag("prejoinpage.enabled", false)
                    .build();
            JitsiMeet.setDefaultConferenceOptions(defaultOptions);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
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
                    Timber.tag("user name").i(name);
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
        JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
        userInfo.setAvatar(url);
        userInfo.setDisplayName(name);
        userInfo.setEmail(email);
        JitsiMeetConferenceOptions room = new JitsiMeetConferenceOptions.Builder()
                .setRoom(uniqueId).setFeatureFlag("prejoinpage.enabled", false).setUserInfo(userInfo).build();
        JitsiMeetActivity.launch(this, room);
        finish();
//        String time = String.valueOf(System.currentTimeMillis());
//        SharedPreferences preferences = getSharedPreferences("callStatus",MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putString("finishedOn",time);
//        editor.apply();
    }

    public String getUniqueId() {
        return UUID.randomUUID().toString();
    }
    public void onBackPressed()
    {
        finish();
    }
}
