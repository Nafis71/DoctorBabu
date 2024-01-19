package com.example.doctorbabu.patient.MedicinePurchaseModules;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityShopByPrescriptionBinding;

public class ShopByPrescription extends AppCompatActivity {
    ActivityShopByPrescriptionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopByPrescriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}