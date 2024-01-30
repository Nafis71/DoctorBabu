package com.example.doctorbabu.patient.MedicinePurchaseModules;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityMyPrescriptionBinding;

public class MyPrescription extends AppCompatActivity {

    ActivityMyPrescriptionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyPrescriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}