package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class CallDoctor extends AppCompatActivity {
    ImageView profilePicture;
    TextView callingWhomText;
    String doctorId, photoUrl, doctorTitle, doctorName;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
    FirebaseAuth auth;
    boolean isOkay = false;
    boolean isIncomingSet = false,hasDestroyed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_doctor);
        viewBinding();
        loadData();
        roomFind();

    }

    public void viewBinding() {

        profilePicture = findViewById(R.id.profilePicture);
        callingWhomText = findViewById(R.id.callingWhomText);
    }

    public void loadData() {
        photoUrl = getIntent().getStringExtra("photoUrl");
        doctorId = getIntent().getStringExtra("doctorId");
        doctorTitle = getIntent().getStringExtra("doctorTitle");
        doctorName = getIntent().getStringExtra("doctorName");
        String callingWhom = "Calling " + doctorTitle + doctorName;
        callingWhomText.setText(callingWhom);
        Glide.with(this).load(photoUrl).into(profilePicture);
    }

    public void roomFind() {
        DatabaseReference reference = database.getReference("callRoom");
        reference.child(doctorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = String.valueOf(snapshot.child("status").getValue());
                    if (Integer.parseInt(status) == 0) {
                        if (isIncomingSet) {
                            return;
                        }
                        auth = FirebaseAuth.getInstance();
                        FirebaseUser user = auth.getCurrentUser();
                        assert user != null;
                        reference.child(doctorId).child("incoming").setValue(user.getUid());
                        isIncomingSet = true;
                        ring();
                        callingWhomText.setText("Ringing");
                    }
                } else{
                    HashMap<String, Object> roomSetup = new HashMap<>();
                    roomSetup.put("incoming", "null");
                    roomSetup.put("isAvailable", false);
                    roomSetup.put("status", 0);
                    reference.child(doctorId).setValue(roomSetup);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void ring() {
        DatabaseReference callReference = database.getReference("callRoom");
        callReference.child(doctorId).child("isAvailable").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !hasDestroyed) {
                    if (String.valueOf(snapshot.getValue()).equals("true")) {
                        if (isOkay) {
                            return;
                        }
                        isOkay = true;
                        Intent intent = new Intent(CallDoctor.this, PatientCall.class);
                        auth = FirebaseAuth.getInstance();
                        FirebaseUser user = auth.getCurrentUser();
                        assert user != null;
                        intent.putExtra("userId", user.getUid());
                        intent.putExtra("doctorId", doctorId);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void onBackPressed() {
        //blocking user from pressing back button
    }

    @Override
    protected void onDestroy() {
        hasDestroyed = true;
        super.onDestroy();
    }
}