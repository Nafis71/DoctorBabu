package com.example.doctorbabu.patient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityMainBinding;
import com.example.doctorbabu.databinding.ActivityViewAllDoctorBinding;

public class ViewAllDoctor extends AppCompatActivity {
    ActivityViewAllDoctorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewAllDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}