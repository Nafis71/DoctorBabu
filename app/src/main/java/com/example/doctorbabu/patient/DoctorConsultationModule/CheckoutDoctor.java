package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityCheckoutBinding;
import com.example.doctorbabu.databinding.ActivityCheckoutDoctorBinding;

public class CheckoutDoctor extends AppCompatActivity {
    String doctorId, doctorTitle, doctorName, doctorDegree, doctorSpecialty, doctorCurrentlyWorking, photoUrl,consultationFee;
    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    int requestCode = 1;
    ActivityCheckoutDoctorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        doctorId = getIntent().getStringExtra("doctorId");
        doctorTitle = getIntent().getStringExtra("doctorTitle");
        doctorName = getIntent().getStringExtra("doctorName");
        doctorDegree = getIntent().getStringExtra("doctorDegree");
        doctorSpecialty = getIntent().getStringExtra("doctorSpecialty");
        doctorCurrentlyWorking = getIntent().getStringExtra("doctorCurrentlyWorking");
        photoUrl = getIntent().getStringExtra("photoUrl");
        consultationFee = getIntent().getStringExtra("consultationFee");
        loadData();
        binding.back.setOnClickListener(view -> finish());
        binding.bkash.setOnClickListener(view -> {
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
        binding.nagad.setOnClickListener(view -> {
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
        binding.doctorName.setText(name);
        binding.doctorDegree.setText(doctorDegree);
        binding.doctorSpecialties.setText(doctorSpecialty);
        binding.currentlyWorking.setText(doctorCurrentlyWorking);

        binding.amount.setText(consultationFee);
        double vat = Double.parseDouble(consultationFee) * 0.05;
        binding.vatAmount.setText(String.valueOf(Math.round(vat)));
        double totalAmount = vat + Double.parseDouble(consultationFee);
        binding.totalAmount.setText(String.valueOf(Math.round(totalAmount)));
        Glide.with(this).load(photoUrl).into(binding.profilePicture);
    }
}