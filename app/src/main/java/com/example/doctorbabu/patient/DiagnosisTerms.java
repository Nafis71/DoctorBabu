package com.example.doctorbabu.patient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityDiagnosisTermsBinding;

public class DiagnosisTerms extends AppCompatActivity {
    ActivityDiagnosisTermsBinding binding;
    Animation fadein;
    boolean firstCheckBoxChecked, secondCheckBoxChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiagnosisTermsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadTermData();
        fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        binding.nextButton.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        callAnimation();
        checkBoxStateListener();
        nextButtonListener();
    }

    public void callAnimation() {
        binding.consentImage.setAnimation(fadein);
    }

    public void checkBoxState() {
        if (firstCheckBoxChecked && secondCheckBoxChecked) {
            binding.nextButton.setVisibility(View.VISIBLE);
        } else {
            binding.nextButton.setVisibility(View.GONE);
        }
    }

    public void checkBoxStateListener() {
        binding.firstCheckBox.addOnCheckedStateChangedListener((checkBox, state) -> {
            if (state == 1) {
                firstCheckBoxChecked = true;
                checkBoxState();
            } else {
                firstCheckBoxChecked = false;
                checkBoxState();
            }
        });
        binding.secondCheckBox.addOnCheckedStateChangedListener((checkBox, state) -> {
            if (state == 1) {
                secondCheckBoxChecked = true;
                checkBoxState();
            } else {
                secondCheckBoxChecked = false;
                checkBoxState();
            }
        });
    }

    public void nextButtonListener() {
        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveTermData();
            }
        });
    }

    public void saveTermData() {
        SharedPreferences preferences = getSharedPreferences("terms", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("hasAccepted", true);
        editor.apply();
        launchNewInstance();
    }

    public void loadTermData() {
        SharedPreferences preferences = getSharedPreferences("terms", MODE_PRIVATE);
        boolean hasAccepted = preferences.getBoolean("hasAccepted", false);
        if (hasAccepted) {
            launchNewInstance();
        }
    }

    public void launchNewInstance() {
        Intent intent = new Intent(DiagnosisTerms.this, IdentifyDisease.class);
        startActivity(intent);
        finish();
    }

}