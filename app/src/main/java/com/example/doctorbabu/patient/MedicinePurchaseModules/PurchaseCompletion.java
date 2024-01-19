package com.example.doctorbabu.patient.MedicinePurchaseModules;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityPurchaseCompletionBinding;

public class PurchaseCompletion extends AppCompatActivity {
    ActivityPurchaseCompletionBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPurchaseCompletionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //preventing user from going back
    }
}