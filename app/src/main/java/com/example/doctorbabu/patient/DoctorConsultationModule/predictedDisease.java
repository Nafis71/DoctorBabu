package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityPredictedDiseaseBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class predictedDisease extends AppCompatActivity {
    ActivityPredictedDiseaseBinding binding;
    boolean hasAnimated, isTextGenerationFinished;
    int count;
    ArrayList<String> predictedDisease ;
    ArrayList<Integer> predictedDiseasePercentage;
    ExecutorService firebaseExecutor;
    FirebaseDatabase database;
    String Url, specialist;
    Animation bottomAnimation, leftAnimation;
    ArrayList<TextView> diseaseHeader;
    ArrayList<TextView> diseasePrecentage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPredictedDiseaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recordTextViews();
        binding.moreInfo.setVisibility(View.GONE);
        database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
        bottomAnimation = AnimationUtils.loadAnimation(this, R.anim.predicted_disease_bottom_animation);
        leftAnimation = AnimationUtils.loadAnimation(this, R.anim.left_animation);
        binding.doctorRecommendationCard.setVisibility(View.GONE);
        binding.diseaseResultCard.setAnimation(bottomAnimation);
        binding.diseaseinfoCard.setAnimation(bottomAnimation);
        PushDownAnim.setPushDownAnimTo(binding.doctorListButton, binding.moreInfo)
                .setScale(PushDownAnim.MODE_SCALE, 0.95f);

    }
    public void recordTextViews(){
        diseaseHeader = new ArrayList<>();
        diseasePrecentage = new ArrayList<>();
        diseaseHeader.add(binding.identifiedDisease1);
        diseaseHeader.add(binding.identifiedDisease2);
        diseaseHeader.add(binding.identifiedDisease3);
        diseaseHeader.add(binding.identifiedDisease1);
        diseasePrecentage.add(binding.identifiedDiseasePercentage1);
        diseasePrecentage.add(binding.identifiedDiseasePercentage2);
        diseasePrecentage.add(binding.identifiedDiseasePercentage3);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseExecutor = Executors.newSingleThreadExecutor();
        loadPreviousIntentData();
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(predictedDisease.this, IdentifyDisease.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @SuppressLint("SetTextI18n")
    public void loadPreviousIntentData() {
        predictedDisease = getIntent().getStringArrayListExtra("predictedDisease");
        predictedDiseasePercentage = getIntent().getIntegerArrayListExtra("predictedDiseasePercentage");
        assert predictedDisease != null;
        assert predictedDiseasePercentage != null;
        int maxValue = Collections.max(predictedDiseasePercentage);
        int minValue = Collections.min(predictedDiseasePercentage);
        if(predictedDisease.size()!=0){
            binding.resultLayout.setVisibility(View.VISIBLE);
            for(int i= 0; i<predictedDisease.size();i++){
                diseaseHeader.get(i).setText(predictedDisease.get(i) + " :");
                diseaseHeader.get(i).setVisibility(View.VISIBLE);
                if(predictedDiseasePercentage.get(i) > minValue && predictedDiseasePercentage.get(i) <= maxValue || predictedDiseasePercentage.get(i) == 100 ){
                    diseasePrecentage.get(i).setText(String.valueOf(predictedDiseasePercentage.get(i)) + "% (High Chances)");
                } else {
                    diseasePrecentage.get(i).setText(String.valueOf(predictedDiseasePercentage.get(i)) + "% (Low Chances)");
                }
                diseasePrecentage.get(i).setVisibility(View.VISIBLE);
            }
            firebaseExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    int index = predictedDiseasePercentage.indexOf(maxValue);
                    loadDiseaseData(index);
                }
            });
//
        } else {
            binding.noResultLayout.setVisibility(View.VISIBLE);
        }

    }

    public void loadDiseaseData(int index) {
        DatabaseReference loadSymptomInfo = database.getReference("symptoms");
        loadSymptomInfo.orderByChild("symptomName").equalTo(predictedDisease.get(index)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        binding.diseaseInfoText.setText(predictedDisease.get(index));
                        binding.diseaseInfoTextBody.setText(String.valueOf(snap.child("symptomInfo").getValue()));
                        binding.doctorRecommendationCardDiseaseNameHeader.setText(predictedDisease.get(index));
                        Url = String.valueOf(snap.child("link").getValue());
                        specialist = String.valueOf(snap.child("specialist").getValue());
                        String initialCharacter = specialist.substring(0, 1).toUpperCase();
                        String restOfTheCharacters = specialist.substring(1);
                        specialist = initialCharacter + restOfTheCharacters;
                        binding.recommendedDoctor.setText(specialist);
                        animateText(index);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void animateText(int index) {
        if (!hasAnimated) {
            final String animateText = binding.diseaseInfoTextBody.getText().toString();
            binding.diseaseInfoTextBody.setText(null);
            count = 0;
            hasAnimated = true;
            new CountDownTimer(animateText.length() * 100L, 20) {
                @SuppressLint("SetTextI18n")
                @Override
                public void onTick(long l) {
                    if (count == animateText.length() - 1) {
                        if (!isTextGenerationFinished) {
                            binding.moreInfo.setVisibility(View.VISIBLE);
                            binding.doctorRecommendationCard.setVisibility(View.VISIBLE);
                            binding.doctorRecommendationCard.setAnimation(leftAnimation);
                            moreInfoIntializer();
                            doctorListButtonListerner();
                        }
                        isTextGenerationFinished = true;

                    } else {
                        binding.diseaseInfoTextBody.setText(binding.diseaseInfoTextBody.getText().toString() + animateText.charAt(count));
                        count++;
                    }

                }

                @Override
                public void onFinish() {

                }
            }.start();
        }
    }

    public void moreInfoIntializer() {
        binding.moreInfo.setOnClickListener(view -> gotoUrl(Url));
    }

    public void doctorListButtonListerner() {
        binding.doctorListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(predictedDisease.this, ViewAllDoctor.class);
                intent.putExtra("specialist", specialist);
                startActivity(intent);

            }
        });
    }

    public void gotoUrl(String url) {
        Uri uri = Uri.parse(url);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseExecutor.shutdown();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(predictedDisease.this, IdentifyDisease.class);
        startActivity(intent);
        finish();
    }
}