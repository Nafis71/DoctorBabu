package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;

public class CheckoutDoctor extends AppCompatActivity {
    TextView doctorNameView, doctorDegreeView, doctorSpecialtiesView, currentlyWorkingView;
    ImageView profilePicture, goBack;
    CardView bkash, nagad;
    String doctorId, doctorTitle, doctorName, doctorDegree, doctorSpecialty, doctorCurrentlyWorking, photoUrl;
    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    int requestCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout_doctor);
        doctorId = getIntent().getStringExtra("doctorId");
        doctorTitle = getIntent().getStringExtra("doctorTitle");
        doctorName = getIntent().getStringExtra("doctorName");
        doctorDegree = getIntent().getStringExtra("doctorDegree");
        doctorSpecialty = getIntent().getStringExtra("doctorSpecialty");
        doctorCurrentlyWorking = getIntent().getStringExtra("doctorCurrentlyWorking");
        photoUrl = getIntent().getStringExtra("photoUrl");
        viewBinding();
        loadData();
        goBack.setOnClickListener(view -> finish());
        bkash.setOnClickListener(view -> {
            if (isPermissionGranted()) {
                Intent intent = new Intent(CheckoutDoctor.this, CallDoctor.class);
                intent.putExtra("doctorId", doctorId);
                intent.putExtra("doctorTitle", doctorTitle);
                intent.putExtra("doctorName", doctorName);
                intent.putExtra("photoUrl", photoUrl);
                startActivity(intent);
                finish();
            } else {
                askPermission();
            }

        });
        nagad.setOnClickListener(view -> {
            if (isPermissionGranted()) {
                Intent intent = new Intent(CheckoutDoctor.this, CallDoctor.class);
                intent.putExtra("doctorId", doctorId);
                intent.putExtra("doctorTitle", doctorTitle);
                intent.putExtra("doctorName", doctorName);
                intent.putExtra("photoUrl", photoUrl);
                startActivity(intent);
                finish();
            } else {
                askPermission();
            }
        });


    }

    private void viewBinding() {
        doctorNameView = findViewById(R.id.doctorName);
        doctorDegreeView = findViewById(R.id.doctorDegree);
        doctorSpecialtiesView = findViewById(R.id.doctorSpecialties);
        currentlyWorkingView = findViewById(R.id.currentlyWorking);
        profilePicture = findViewById(R.id.profilePicture);
        goBack = findViewById(R.id.back);
        bkash = findViewById(R.id.bkash);
        nagad = findViewById(R.id.nagad);
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean isPermissionGranted() {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void loadData() {
        String name = doctorTitle + doctorName;
        doctorNameView.setText(name);
        doctorDegreeView.setText(doctorDegree);
        doctorSpecialtiesView.setText(doctorSpecialty);
        currentlyWorkingView.setText(doctorCurrentlyWorking);
        Glide.with(this).load(photoUrl).into(profilePicture);
    }
}