package com.example.doctorbabu.patient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityIdentifyDiseaseBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.aviran.cookiebar2.CookieBar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdentifyDisease extends AppCompatActivity {
    Animation leftAnimation,fadein;
    ActivityIdentifyDiseaseBinding binding;
    ArrayList<String> symptomsList = new ArrayList<>();
    ArrayList<String> transformedSymptomsList = new ArrayList<>();
    ExecutorService backgroundExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIdentifyDiseaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.analyze.setVisibility(View.INVISIBLE);
        binding.infoCard.setVisibility(View.GONE);
        leftAnimation = AnimationUtils.loadAnimation(this,R.anim.left_animation);
        fadein = AnimationUtils.loadAnimation(this,R.anim.fadein);
        backgroundExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setSymptomsAdapter();
        analyzeButtonListener();
    }
    public void setSymptomsAdapter(){
        binding.symptoms.setThreshold(3);
        String namesOfSymptoms[] = new String[]{"Neck Pain","Knee Pain","Headache"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(IdentifyDisease.this, R.layout.drop_menu, namesOfSymptoms);
        binding.symptoms.setAdapter(adapter);
        binding.symptoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.symptoms.dismissDropDown();
            }
        });
        setSymptomsTextWatcher();
    }
    public void setSymptomsTextWatcher(){
       binding.symptoms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               String chipName = binding.symptoms.getText().toString().trim();
               if(!symptomsList.contains(chipName))
               {
                   symptomsList.add(chipName);
                   binding.symptoms.setText(null);
                   symptomsChipMaker(chipName);
               } else {
                   CookieBar.build(IdentifyDisease.this)
                           .setTitle("Duplicate Input")
                           .setMessage("Given symptom is already added!")
                           .setSwipeToDismiss(true)
                           .setDuration(3000)
                           .setTitleColor(R.color.white)
                           .setBackgroundColor(R.color.blue)
                           .setCookiePosition(CookieBar.TOP)  // Cookie will be displayed at the Top
                           .show();
               }
           }
       });
    }

    public void symptomsChipMaker(String chipName){
        Random random = new Random();
        Chip chip = (Chip) LayoutInflater.from(IdentifyDisease.this).inflate(R.layout.chip_layout,null);
        chip.setText(chipName);
        chip.setId(random.nextInt());
        chip.setHeight(80);
        binding.chipGroup.addView(chip);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    binding.chipGroup.removeView(chip);
                    symptomsList.remove(chip.getText().toString());
                    hideAndShowAnalyzeButton();
                }
            });
        hideAndShowAnalyzeButton();
    }
    public void hideAndShowAnalyzeButton(){
        if(symptomsList.size() >= 3){
            binding.analyze.setVisibility(View.VISIBLE);;
            binding.infoCard.setVisibility(View.GONE);
        } else if(symptomsList.size() == 0){
            binding.analyze.setVisibility(View.INVISIBLE);
            binding.infoCard.setVisibility(View.GONE);
        } else {
            binding.analyze.setVisibility(View.INVISIBLE);
            binding.infoCard.setVisibility(View.VISIBLE);
            binding.infoCard.setAnimation(leftAnimation);
        }
    }
    public void analyzeButtonListener(){
        binding.analyze.setOnClickListener(view -> {
            removeViews();
            backgroundExecutor.execute(this::transformSymptomList);

        });
    }

    public void removeViews(){
        binding.symptomsImage.setVisibility(View.GONE);
        binding.symptomsLayout.setVisibility(View.GONE);
        binding.chipGroup.setVisibility(View.GONE);
        binding.headerText3.setVisibility(View.GONE);
        binding.headerText.setVisibility(View.GONE);
        binding.headerText2.setVisibility(View.GONE);
        binding.analyze.setVisibility(View.GONE);
        showViews();
    }
    public void showViews(){
        binding.loadingAnimation.setVisibility(View.VISIBLE);
        binding.loadingTextCard.setVisibility(View.VISIBLE);
        binding.loadingAnimation.setAnimation(fadein);
        binding.loadingTextCard.setAnimation(leftAnimation);
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

    public void transformSymptomList(){
        for(String symptom: symptomsList){
            transformedSymptomsList.add(symptom.replaceAll(" ","_").toLowerCase());
        }

    }
    @Override
    public void onBackPressed(){
        finish();
    }
}