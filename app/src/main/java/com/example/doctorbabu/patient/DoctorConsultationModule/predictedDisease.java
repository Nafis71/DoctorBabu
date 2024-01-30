package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class predictedDisease extends AppCompatActivity {
    ActivityPredictedDiseaseBinding binding;
    boolean hasAnimated, isTextGenerationFinished;
    int count;
    String predictedDisease;
    ExecutorService firebaseExecutor;
    FirebaseDatabase database;
    String Url, specialist;
    Animation bottomAnimation, leftAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPredictedDiseaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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

    @Override
    protected void onStart() {
        super.onStart();
        firebaseExecutor = Executors.newSingleThreadExecutor();
        loadPreviousIntentData();

    }

    public void loadPreviousIntentData() {
        predictedDisease = getIntent().getStringExtra("predictedDisease");
        binding.identifiedDisease.setText(predictedDisease);
        binding.diseaseInfoText.setText(predictedDisease);
        firebaseExecutor.execute(this::loadDiseaseData);
    }

    public void loadDiseaseData() {
        DatabaseReference loadSymptomInfo = database.getReference("symptoms");
        loadSymptomInfo.orderByChild("symptomName").equalTo(predictedDisease).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        binding.diseaseInfoTextBody.setText(String.valueOf(snap.child("symptomInfo").getValue()));
                        binding.doctorRecommendationCardDiseaseNameHeader.setText(predictedDisease);
                        Url = String.valueOf(snap.child("link").getValue());
                        specialist = String.valueOf(snap.child("specialist").getValue());
                        String initialCharacter = specialist.substring(0, 1).toUpperCase();
                        String restOfTheCharacters = specialist.substring(1);
                        specialist = initialCharacter + restOfTheCharacters;
                        binding.recommendedDoctor.setText(specialist);
                        animateText();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void animateText() {
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