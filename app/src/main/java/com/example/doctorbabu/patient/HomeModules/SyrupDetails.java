package com.example.doctorbabu.patient.HomeModules;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivitySyrupDetailsBinding;

public class SyrupDetails extends AppCompatActivity {
    ActivitySyrupDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySyrupDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}