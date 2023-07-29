package com.example.doctorbabu.patient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import com.bumptech.glide.Glide;
import com.example.doctorbabu.Databases.joiningDates;
import com.example.doctorbabu.Databases.leavingDates;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityDoctorInfoBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoctorInfoActivity extends AppCompatActivity {
    String doctorId,doctorName,doctorTitle,doctorDegree,doctorSpecialties,doctorCurrentlyWorking,doctorRating,bmdc,photoUrl;
    ActivityDoctorInfoBinding binding;
    ExecutorService loadDataExecutor;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private final String[] titles = new String[]{"Info", "Experience", "Reviews"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadDataExecutor = Executors.newSingleThreadExecutor();
        doctorId = getIntent().getStringExtra("doctorId");
        doctorName = getIntent().getStringExtra("doctorName");
        doctorTitle = getIntent().getStringExtra("doctorTitle");
        doctorDegree = getIntent().getStringExtra("doctorDegree");      //receiving all data that has been passed down to this view
        doctorSpecialties = getIntent().getStringExtra("doctorSpecialties");
        doctorCurrentlyWorking = getIntent().getStringExtra("doctorCurrentlyWorking");
        doctorRating = getIntent().getStringExtra("doctorRating");
        bmdc = getIntent().getStringExtra("bmdc");
        photoUrl = getIntent().getStringExtra("photoUrl");
        loadDataExecutor.execute(this::loadData);
        binding.outlinedLove.setOnClickListener(new View.OnClickListener() {
            boolean toggleButton = true;
            @Override
            public void onClick(View view) {

                if (toggleButton) {
                    binding.outlinedLove.setImageResource(R.drawable.filledlove);
                    toggleButton = false;
//                    addFavourite();
                } else {
                    binding.outlinedLove.setImageResource(R.drawable.blanklove);
                    toggleButton = true;
                }
            }
        });
        binding.videoCall.setOnClickListener(view -> {
            Intent intent = new Intent(DoctorInfoActivity.this, CheckoutDoctor.class);
            intent.putExtra("doctorId", doctorId);
            intent.putExtra("doctorTitle", doctorTitle);
            intent.putExtra("doctorName", doctorName);
            intent.putExtra("doctorDegree", doctorDegree);
            intent.putExtra("doctorSpecialty", doctorSpecialties);
            intent.putExtra("doctorCurrentlyWorking", doctorCurrentlyWorking);
            intent.putExtra("photoUrl", photoUrl);
            startActivity(intent);
        });
    }
    @SuppressLint("SetTextI18n")
    public void loadData() {
        runOnUiThread(() -> Glide.with(DoctorInfoActivity.this).load(photoUrl).into(binding.profilePicture));
        binding.doctorName.setText(doctorTitle +" "+ doctorName);
        binding.doctorDegree.setText(doctorDegree);
        binding.doctorSpecialties.setText(doctorSpecialties);
        binding.currentlyWorking.setText(doctorCurrentlyWorking);
        binding.rating.setText(doctorRating);
        binding.bmdc.setText(bmdc);
        DatabaseReference reference = database.getReference("doctorInfo");
        reference.child(doctorId).child("onlineStatus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = String.valueOf(snapshot.getValue());
                    if (Integer.parseInt(status) != 0) {
                        binding.onlineStatusBanner.setVisibility(View.VISIBLE);
                        binding.onlineStatus.setVisibility(View.VISIBLE);
                        binding.offlineStatusBanner.setVisibility(View.GONE);
                        binding.offlineStatus.setVisibility(View.GONE);
                    } else {
                        binding.offlineStatusBanner.setVisibility(View.VISIBLE);
                        binding.offlineStatus.setVisibility(View.VISIBLE);
                        binding.onlineStatusBanner.setVisibility(View.GONE);
                        binding.onlineStatus.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        ArrayList<Integer> periods = new ArrayList<>();
        DatabaseReference experienceReference = database.getReference("doctorPastExperience");
        experienceReference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    joiningDates model = snap.getValue(joiningDates.class);
                    assert model != null;
                    leavingDates model2 = snap.getValue(leavingDates.class);
                    assert model2 != null;
                    String joiningDate = model.getJoiningDate();
                    String leavingDate = model2.getLeavingDate();
                    String[] splitText = joiningDate.split("/");
                    int year = Integer.parseInt(splitText[0]);
                    int month = Integer.parseInt(splitText[1]);
                    int day = Integer.parseInt(splitText[2]);
                    LocalDate beginningDay = LocalDate.of(year, month, day);
                    splitText = leavingDate.split("/");
                    year = Integer.parseInt(splitText[0]);
                    month = Integer.parseInt(splitText[1]);
                    day = Integer.parseInt(splitText[2]);
                    LocalDate endDay = LocalDate.of(year, month, day);
                    Period period = Period.between(beginningDay, endDay);
                    String years = String.valueOf(period.getYears());
                    periods.add(Integer.parseInt(years));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        experienceReference = database.getReference("doctorCurrentlyWorking");
        experienceReference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!String.valueOf(snapshot.child("joiningDate").getValue()).equals("null")) {
                    binding.currentlyWorking.setText(String.valueOf(snapshot.child("hospitalName").getValue()));
                    String date = String.valueOf(snapshot.child("joiningDate").getValue());
                    String[] splitText = date.split("/");
                    int year = Integer.parseInt(splitText[0]);
                    int month = Integer.parseInt(splitText[1]);
                    int day = Integer.parseInt(splitText[2]);
                    LocalDate bday = LocalDate.of(year, month, day);
                    LocalDate today = LocalDate.now();
                    Period period = Period.between(bday, today);
                    String years = String.valueOf(period.getYears());
                    periods.add(Integer.parseInt(years));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            int totalExperience = 0;
            for (int i = 0; i < periods.size(); i++) {
                totalExperience = totalExperience + periods.get(i);
            }
            String totalExperienceString = totalExperience + "+ years";
            binding.totalExperience.setText(totalExperienceString);
        }, 1000);
        runOnUiThread(() -> {
            doctorInfoPageAdapter adapter = new doctorInfoPageAdapter(DoctorInfoActivity.this, doctorId);
            binding.vPager.setAdapter(adapter);
            new TabLayoutMediator(binding.tabs, binding.vPager, ((tab, position) -> tab.setText(titles[position]))).attach();
        });
    }
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, ViewAllDoctor.class);
        startActivity(intent);
        finish();
    }
}