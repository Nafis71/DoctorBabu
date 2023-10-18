package com.example.doctorbabu.patient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityIdentifyDiseaseBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class IdentifyDisease extends AppCompatActivity {
    ActivityIdentifyDiseaseBinding binding;
    ArrayList<String> symptomsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIdentifyDiseaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.analyze.setVisibility(View.GONE);
        binding.chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                if(checkedIds.isEmpty()){
                    symptomsList.clear();
                    binding.analyze.setVisibility(View.GONE);
                } else {
                    binding.analyze.setVisibility(View.VISIBLE);
                    recordSymptoms(checkedIds);
                    Log.w("Symptoms",symptomsList.toString());
                }
            }
        });
        binding.analyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.chipGroup.setVisibility(View.GONE);
                generateResult();
            }
        });
    }
    public void generateResult(){
        startPython();
        Python python = Python.getInstance();
        PyObject module = python.getModule("predictor");
        PyObject prediction = module.callAttr("main",symptomsList);
        Log.w("Python Reply: ",prediction.toString());

    }
    public void startPython(){
        if(!Python.isStarted())
        {
            Python.start(new AndroidPlatform(IdentifyDisease.this));
        }
    }

    public void recordSymptoms(List<Integer> checkedIds){
        symptomsList.clear();
        for(int i: checkedIds){
            Chip chip = findViewById(i);
            String chipText = chip.getText().toString();
            chipText= chipText.replaceAll(" ","_").toLowerCase();
            symptomsList.add(chipText);
        }
    }
    @Override
    public void onBackPressed(){
        finish();
    }
}