package com.example.doctorbabu.patient.DoctorConsultationModule;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.doctorbabu.DatabaseModels.AppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.HomeModules.Home;
import com.example.doctorbabu.patient.PatientProfileModule.PrescriptionHistory;
import com.example.doctorbabu.patient.PatientProfileModule.Profile;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PatientBottomBar extends AppCompatActivity {
    private static final String CHANNEL_ID = "Appointment Cancelled Channel";
    private static final int NOTIFICATION_ID = 300;
    ChipNavigationBar bottomNavigation;
    FragmentManager fm;
    boolean isOpenDoctorVideo = false, isOpenHome = false, isOpenHistory = false, isOpenProfile = false, isBackPressed = false, isAdded;
    String code = null;
    int count, fragmentId = -1;
    String fragmentName,doctorName = "";
    int prescriptionCounter = 0;
    Bitmap image;
    ExecutorService executorService,cancelledAppointmentNotifier,notificationExecutor,prescriptionExecutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        code = getIntent().getStringExtra("code");
        setContentView(R.layout.activity_patient_bottom_bar);
        bottomNavigation = findViewById(R.id.bottomBar);
        executorService = Executors.newSingleThreadExecutor();
        cancelledAppointmentNotifier = Executors.newSingleThreadExecutor();
        notificationExecutor = Executors.newSingleThreadExecutor();
        prescriptionExecutor = Executors.newSingleThreadExecutor();
        prescriptionExecutor.execute(new Runnable() {
            @Override
            public void run() {
                listenPrescription();
            }
        });
        cancelledAppointmentNotifier.execute(new Runnable() {
            @Override
            public void run() {
                listenCancelledAppointment();
            }
        });
        try{
            executorService.execute(() -> bottomNavigation.setOnItemSelectedListener(id -> {
                if (id == R.id.nav_doctor_video) {
                    if (!isOpenDoctorVideo && !isBackPressed) {
                        count = getSupportFragmentManager().getBackStackEntryCount();
                        for (int i = 0; i < count; i++) {
                            fragmentName = getSupportFragmentManager().getBackStackEntryAt(i).getName();
                            if (fragmentName.equals("doctor")) {
                                fragmentId = getSupportFragmentManager().getBackStackEntryAt(i).getId();
                                break;
                            }
                        }
                        if (fragmentId != -1) {
                            getSupportFragmentManager().popBackStack(fragmentId, 100);
                            navBarEnableDisable("doctor");
                        } else {
                            loadFragment(new Doctor(), "doctor");
                            navBarEnableDisable("doctor");
                        }

                    }
                } else if (id == R.id.nav_home) {
                    if (!isOpenHome && !isBackPressed) {
                        count = getSupportFragmentManager().getBackStackEntryCount();
                        for (int i = 0; i < count; i++) {
                            fragmentName = getSupportFragmentManager().getBackStackEntryAt(i).getName();
                            assert fragmentName != null;
                            if (fragmentName.equals("home")) {
                                fragmentId = getSupportFragmentManager().getBackStackEntryAt(i).getId();
                                break;
                            }
                        }
                        if (fragmentId != -1) {
                            getSupportFragmentManager().popBackStack(fragmentId, 200);
                            navBarEnableDisable("home");
                        } else {
                            loadFragment(new Home(), "home");
                            navBarEnableDisable("home");
                        }
                    }
                } else if (id == R.id.nav_history) {
                    if (!isOpenHistory && !isBackPressed) {
                        count = getSupportFragmentManager().getBackStackEntryCount();
                        for (int i = 0; i < count; i++) {
                            fragmentName = getSupportFragmentManager().getBackStackEntryAt(i).getName();
                            assert fragmentName != null;
                            if (fragmentName.equals("history")) {
                                fragmentId = getSupportFragmentManager().getBackStackEntryAt(i).getId();
                                break;
                            }
                        }
                        if (fragmentId != -1) {
                            getSupportFragmentManager().popBackStack(fragmentId, 300);
                            navBarEnableDisable("history");
                        } else {
                            loadFragment(new PrescriptionHistory(), "history");
                            navBarEnableDisable("history");
                        }
                    }

                } else if (id == R.id.nav_profile) {
                    if (!isOpenProfile && !isBackPressed) {
                        count = getSupportFragmentManager().getBackStackEntryCount();
                        for (int i = 0; i < count; i++) {
                            fragmentName = getSupportFragmentManager().getBackStackEntryAt(i).getName();
                            assert fragmentName != null;
                            if (fragmentName.equals("profile")) {
                                fragmentId = getSupportFragmentManager().getBackStackEntryAt(i).getId();
                                break;
                            }
                        }
                        if (fragmentId != -1) {
                            getSupportFragmentManager().popBackStack(fragmentId, 400);
                            navBarEnableDisable("profile");
                        } else {
                            loadFragment(new Profile(), "profile");
                            navBarEnableDisable("profile");
                        }
                    }
                }
                fragmentId = -1;
                fragmentName = null;
            }));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        loadFragment(new Home(),"home");
        isOpenHome = true;
    }

    public void loadFragment(Fragment fragment, String tag) {
        fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction().addToBackStack(tag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.container, fragment);
        ft.commit();
    }

    public void onBackPressed() {
        String fragmentName;
        isBackPressed = true;
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 1) {
            fragmentName = getSupportFragmentManager().getBackStackEntryAt(0).getName();
        } else {
            fragmentName = getSupportFragmentManager().getBackStackEntryAt(count - 2).getName();
        }
        if (count == 1 && Objects.equals(fragmentName, "home")) {
            finish();
        } else {
            getSupportFragmentManager().popBackStack();
            switch (Objects.requireNonNull(fragmentName)) {
                case "doctor":
                    bottomNavigation.setItemSelected(R.id.nav_doctor_video, true);
                    navBarEnableDisable("doctor");
                    break;
                case "history":
                    bottomNavigation.setItemSelected(R.id.nav_history, true);
                    navBarEnableDisable("history");
                    break;
                case "home":
                    bottomNavigation.setItemSelected(R.id.nav_home, true);
                    navBarEnableDisable("home");
                    break;
                case "profile":
                    bottomNavigation.setItemSelected(R.id.nav_profile, true);
                    navBarEnableDisable("profile");
                    break;
            }
        }
    }

    private void navBarEnableDisable(String tag) {
        switch (tag) {
            case "doctor":
                isOpenDoctorVideo = true;
                isOpenHome = false;
                isOpenProfile = false;
                isOpenHistory = false;
                isBackPressed = false;
                break;
            case "home":
                isOpenHome = true;
                isOpenHistory = false;
                isOpenProfile = false;
                isOpenDoctorVideo = false;
                isBackPressed = false;
                break;
            case "history":
                isOpenHistory = true;
                isOpenHome = false;
                isOpenProfile = false;
                isOpenDoctorVideo = false;
                isBackPressed = false;
                break;
            case "profile":
                isOpenProfile = true;
                isOpenHistory = false;
                isOpenHome = false;
                isOpenDoctorVideo = false;
                isBackPressed = false;
                break;
        }

    }

    public void listenPrescription(){
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        DatabaseReference reference = firebase.getDatabaseReference("prescriptionNotification");
        reference.child(user.getUid()).limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    AppointmentModel model = snapshot.getValue(AppointmentModel.class);
                    reference.child(user.getUid()).removeValue();
                    assert model != null;
                    loadDoctorInfo(model,true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        SharedPreferences preferences = getSharedPreferences("prescriptionCounter", MODE_PRIVATE);
        int counter = preferences.getInt("counter",0);
        bottomNavigation.showBadge(R.id.nav_history,counter);
    }
    public void listenCancelledAppointment(){
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user =  firebase.getUserID();
        DatabaseReference reference = firebase.getDatabaseReference("cancelledAppointmentNotification");
        reference.child(user.getUid()).limitToFirst(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    AppointmentModel model = snapshot.getValue(AppointmentModel.class);
                    reference.child(user.getUid()).removeValue();
                    assert model != null;
                    loadDoctorInfo(model,false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void loadDoctorInfo(AppointmentModel model,boolean isPrescription){
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
        reference.child(model.getDoctorID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorName = String.valueOf(snapshot.child("title").getValue()) + String.valueOf(snapshot.child("fullName").getValue());
                notificationExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isPrescription){
                            prescriptionNotificationManager();
                        }else{
                            cancelledAppointmentNotificationManager(String.valueOf(snapshot.child("photoUrl").getValue()));
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void prescriptionNotificationManager(){
        String notificationText = "You have just received an e-prescription from "+ doctorName;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(this).setSmallIcon(R.drawable.applogo).setContentText(notificationText).setSubText("Appointment Cancellation").setChannelId(CHANNEL_ID).build();
        notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "Appointment Cancelled Channel", NotificationManager.IMPORTANCE_HIGH));
        notificationManager.notify(NOTIFICATION_ID, notification);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
        prescriptionCounter +=1;
        SharedPreferences preferences = getSharedPreferences("prescriptionCounter", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("counter",prescriptionCounter);
        editor.apply();
        bottomNavigation.showBadge(R.id.nav_history,prescriptionCounter);
    }

    public void cancelledAppointmentNotificationManager(String doctorPicture) {
        try {
            URL url = new URL(doctorPicture);
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

        } catch (IOException e) {
            System.out.println(e);
        }
        String notificationText = doctorName + " cancelled an appointment with you";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(this).setLargeIcon(image).setSmallIcon(R.drawable.applogo).setContentText(notificationText).setSubText("Appointment Cancellation").setChannelId(CHANNEL_ID).build();
        notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "Appointment Cancelled Channel", NotificationManager.IMPORTANCE_HIGH));
        notificationManager.notify(NOTIFICATION_ID, notification);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
        notificationExecutor.shutdown();
        cancelledAppointmentNotifier.shutdown();
        prescriptionExecutor.shutdown();
    }
}