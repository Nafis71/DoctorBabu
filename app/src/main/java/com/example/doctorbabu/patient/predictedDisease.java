package com.example.doctorbabu.patient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;

import com.example.doctorbabu.databinding.ActivityPredictedDiseaseBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class predictedDisease extends AppCompatActivity {
    ActivityPredictedDiseaseBinding binding;
    boolean hasAnimated; int count;
    String predictedDisease;
    ExecutorService firebaseExecutor;
    FirebaseDatabase database;
    String Url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPredictedDiseaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.moreInfo.setVisibility(View.GONE);
        firebaseExecutor = Executors.newSingleThreadExecutor();
        database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadPreviousIntentData();
        animateText();

    }
    public void loadPreviousIntentData(){
        predictedDisease = getIntent().getStringExtra("predictedDisease");
        binding.identifiedDisease.setText(predictedDisease);
        binding.diseaseInfoText.setText(predictedDisease);
        firebaseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadDiseaseData();
            }
        });
    }
    public void loadDiseaseData(){
        DatabaseReference loadSymptomInfo =  database.getReference("symptoms");
        loadSymptomInfo.orderByChild("symptomName").equalTo(predictedDisease).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snap: snapshot.getChildren()){
                        binding.diseaseInfoTextBody.setText(String.valueOf(snap.child("symptomInfo").getValue()));
                        Url = String.valueOf(snap.child("link").getValue());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        firebaseExecutor.shutdown();
    }
    public void animateText(){
        if(!hasAnimated){
            final String animateText = binding.diseaseInfoTextBody.getText().toString();
            binding.diseaseInfoTextBody.setText(null);
            count = 0;
            hasAnimated = true;
            new CountDownTimer(animateText.length() * 100L, 20){
                @SuppressLint("SetTextI18n")
                @Override
                public void onTick(long l) {
                    if(count == animateText.length() - 1){
                        binding.moreInfo.setVisibility(View.VISIBLE);
                        moreInfoIntializer();
                    } else {
                        binding.diseaseInfoTextBody.setText(binding.diseaseInfoTextBody.getText().toString() + animateText.charAt(count));
                        count ++;
                    }

                }
                @Override
                public void onFinish() {

                }
            }.start();
        }
    }

    public void moreInfoIntializer(){
        binding.moreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoUrl(Url);
            }
        });
    }
    public void gotoUrl(String url){
        Uri uri = Uri.parse(url);
        startActivity(new Intent(Intent.ACTION_VIEW,uri));
    }
}