package com.example.doctorbabu.doctor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.PatientProfileModule.PrescriptionHistory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoctorDashboard extends AppCompatActivity {
    private static final String CHANNEL_ID = "Call Channel";
    private static final int NOTIFICATION_ID = 100;
    String doctorId, callerId, callerName, callerPicture;
    ExecutorService callerNotification;
    ChipNavigationBar bottomNavigation;
    FragmentManager fm;
    Bitmap image;
    int check = 0;
    boolean showOnline;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);
        callerNotification = Executors.newSingleThreadExecutor();
        loadDoctorid();
        callerNotification.execute(new Runnable() {
            @Override
            public void run() {
                loadCallerId();
            }
        });
        bottomNavigation = findViewById(R.id.bottomBar);
        bottomNavigation.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int id) {
                if (id == R.id.nav_home) {
                    loadFragment(new DoctorHome(), false);

                } else if (id == R.id.nav_call) {
                    loadFragment(new DoctorCallRoom(), false);
                } else if (id == R.id.nav_appointment) {
                    loadFragment(new DoctorAppointments(), false);
                } else {
                    loadFragment(new DoctorProfile(), false);
                }
            }
        });
        bottomNavigation.setItemSelected(R.id.nav_profile,true);
    }

    public void loadFragment(Fragment fragment, boolean flag) {
        fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction().setReorderingAllowed(true).addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (flag) {
            if (check == 0) {
                ft.add(R.id.container, fragment);
                check++;
            } else {
                ft.replace(R.id.container, fragment);
            }
        } else {
            ft.replace(R.id.container, fragment);
        }
        ft.commit();

    }

    public void loadDoctorid() {
        SharedPreferences preferences = getSharedPreferences("loginDetails", Context.MODE_PRIVATE);
        doctorId = preferences.getString("doctorId", "loginAs");
    }

    public void loadCallerId() {
        DatabaseReference reference = database.getReference("callRoom");
        reference.child(doctorId).child("incoming").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (!String.valueOf(snapshot.getValue()).equals("null")) {
                        callerId = String.valueOf(snapshot.getValue());
                        loadCallerDetails();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void loadCallerDetails() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = database.getReference("users");
        reference.child(callerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    callerName = String.valueOf(snapshot.child("fullName").getValue());
                    callerPicture = String.valueOf(snapshot.child("photoUrl").getValue());
                    Thread newThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            notificationManager();
                        }
                    });
                    newThread.start();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void notificationManager() {
        try {
            URL url = new URL(callerPicture);
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

        } catch (IOException e) {
            System.out.println(e);
        }
        String notificationText = callerName + " is trying to call you";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(this).setLargeIcon(image).setSmallIcon(R.drawable.applogo).setContentText(notificationText).setSubText("New Call Room Created").setChannelId(CHANNEL_ID).build();
        notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, "Calling Channel", NotificationManager.IMPORTANCE_HIGH));
        notificationManager.notify(NOTIFICATION_ID, notification);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Quitting App").setMessage("Are you sure you want to quit?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.create().show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences preference = getSharedPreferences("onlinePreference",Context.MODE_PRIVATE);
        showOnline = preference.getBoolean("showOnline",true);
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
        if(showOnline){
            reference.child(doctorId).child("onlineStatus").setValue(1);
        }else{
            reference.child(doctorId).child("onlineStatus").setValue(0);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
        reference.child(doctorId).child("onlineStatus").setValue(0);
    }
}