package com.example.doctorbabu.doctor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.databinding.ActivityPrescribeMedicinePromptBinding;
import com.google.firebase.database.DatabaseReference;

public class PrescribeMedicinePrompt extends AppCompatActivity {
    ActivityPrescribeMedicinePromptBinding binding;
    String doctorId,patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPrescribeMedicinePromptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        patientId = getIntent().getStringExtra("patientId");
        SharedPreferences preferences = getSharedPreferences("loginDetails", Context.MODE_PRIVATE);
        doctorId = preferences.getString("doctorId", "loginAs");
        binding.skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCallRoom();
                finish();
            }
        });
        binding.yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCallRoom();
                Intent intent = new Intent(PrescribeMedicinePrompt.this, DoctorPrescribeMedicine.class);
                intent.putExtra("patientId", patientId);
                startActivity(intent);
                finish();
            }
        });
    }

    public void clearCallRoom(){
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("callRoom");
        reference.child(doctorId).child("connId").removeValue();
        reference.child(doctorId).child("incoming").setValue("null");
        reference.child(doctorId).child("isAvailable").setValue(false);
        reference.child(doctorId).child("status").setValue(0);
    }

    @Override
    public void onBackPressed() {
        //blocking user action
    }
}