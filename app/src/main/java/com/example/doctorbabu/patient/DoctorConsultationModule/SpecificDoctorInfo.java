package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.doctorInfoModel;
import com.example.doctorbabu.DatabaseModels.joiningDates;
import com.example.doctorbabu.DatabaseModels.leavingDates;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivitySpecificDoctorInfoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpecificDoctorInfo extends AppCompatActivity {
    String doctorId;
    Firebase firebase;
    FirebaseUser user;
    doctorInfoModel model;
    private final String[] titles = new String[]{"Info", "Experience", "Reviews"};
    ActivitySpecificDoctorInfoBinding binding;
    ExecutorService favouriteRecordExecutor,doctorDataExecutor,doctorExperienceExecutor,getFavouriteDoctorStatus;
    boolean toggleButton;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpecificDoctorInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingScreen();
        doctorId = getIntent().getStringExtra("doctorId");
        firebase = Firebase.getInstance();
        user = firebase.getUserID();
        favouriteRecordExecutor = Executors.newSingleThreadExecutor();
        doctorDataExecutor = Executors.newSingleThreadExecutor();
        doctorExperienceExecutor = Executors.newSingleThreadExecutor();
        getFavouriteDoctorStatus = Executors.newSingleThreadExecutor();
        getFavouriteDoctorStatus.execute(new Runnable() {
            @Override
            public void run() {
                getFavouriteDoctorStatus();
            }
        });
        doctorDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        });
        favouriteRecordExecutor.execute(new Runnable() {
            @Override
            public void run() {
                recordFavourite();
            }
        });
        doctorExperienceExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getDoctorExperience();
            }
        });
        binding.videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SpecificDoctorInfo.this, CheckoutDoctor.class);
                intent.putExtra("doctorId", doctorId);
                intent.putExtra("doctorTitle", model.getTitle());
                String doctorName = model.getTitle() + model.getFullName();
                intent.putExtra("doctorName", doctorName);
                intent.putExtra("doctorDegree", model.getDegrees());
                intent.putExtra("doctorSpecialty", model.getSpecialty());
                intent.putExtra("doctorCurrentlyWorking", model.getCurrentlyWorking());
                intent.putExtra("photoUrl", model.getPhotoUrl());
                intent.putExtra("consultationFee", model.getConsultationFee());
                startActivity(intent);
            }
        });
        binding.goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        closeLoadingScreen();
        binding.appointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SpecificDoctorInfo.this, BookAppointment.class);
                intent.putExtra("doctorId",doctorId);
                startActivity(intent);
            }
        });
    }

    public void loadingScreen() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.loading_screen);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void closeLoadingScreen() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.mainBody.setVisibility(View.VISIBLE);
                binding.subBody.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        }, 1000);
    }



    public void recordFavourite(){
        binding.outlinedLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (toggleButton) {
                    binding.outlinedLove.setImageResource(R.drawable.filledlove);
                    toggleButton = false;
                    addFavourite();
                } else {
                    binding.outlinedLove.setImageResource(R.drawable.blanklove);
                    toggleButton = true;
                    removeFavourite();
                }
            }
        });
    }
    public void loadData() {
        DatabaseReference DoctorReference = firebase.getDatabaseReference("doctorInfo");
        DoctorReference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    model = snapshot.getValue(doctorInfoModel.class);
                    assert model != null;
                    String doctorName = model.getTitle() + model.getFullName();
                    binding.doctorName.setText(doctorName);
                    binding.doctorDegree.setText(model.getDegrees());
                    binding.doctorSpecialties.setText(model.getSpecialty());
                    binding.rating.setText(String.valueOf(model.getRating()));
                    binding.bmdc.setText(model.getBmdc());
                    binding.consultationFee.setText(model.getConsultationFee());
                    Glide.with(SpecificDoctorInfo.this).load(model.getPhotoUrl()).into(binding.profilePicture);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doctorInfoPageAdapter adapter = new doctorInfoPageAdapter(SpecificDoctorInfo.this, doctorId);
                binding.vPager.setAdapter(adapter);
                new TabLayoutMediator(binding.tabs, binding.vPager, ((tab, position) -> tab.setText(titles[position]))).attach();
            }
        });
    }
    public void getDoctorExperience(){
        ArrayList<Integer> periods = new ArrayList<>();
        DatabaseReference experienceReference = firebase.getDatabaseReference("doctorPastExperience");
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
        DatabaseReference currentlyWorkingReference = firebase.getDatabaseReference("doctorCurrentlyWorking");
        currentlyWorkingReference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
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
                        calculateExperience(periods);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void calculateExperience(ArrayList<Integer> periods){
        int totalExperience = 0;
        for (int i = 0; i < periods.size(); i++) {
            totalExperience = totalExperience + periods.get(i);
        }
        String totalExperienceString = String.valueOf(totalExperience) + "+ years";
        binding.totalExperience.setText(totalExperienceString);
    }
    public void getFavouriteDoctorStatus(){
        DatabaseReference reference = firebase.getDatabaseReference("favouriteDoctors");
        reference.child(user.getUid()).child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    binding.outlinedLove.setImageResource(R.drawable.filledlove);
                    toggleButton = false;
                }else{
                    toggleButton = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void addFavourite() {
        HashMap<String, String> data = new HashMap<>();
        data.put("doctorId", doctorId);
        data.put("photoUrl", model.getPhotoUrl());
        data.put("fullName", model.getFullName());
        data.put("title", model.getTitle());
        data.put("specialty", model.getSpecialty());
        DatabaseReference reference = firebase.getDatabaseReference("favouriteDoctors");
        reference.child(user.getUid()).child(doctorId).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(binding.parentLayout, model.getTitle() + " " + model.getFullName() + " added to your favourite list", Snackbar.LENGTH_LONG).show();
            }
        });
    }
    public void removeFavourite() {
        DatabaseReference reference = firebase.getDatabaseReference("favouriteDoctors");
        reference.child(user.getUid()).child(doctorId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(binding.parentLayout, model.getTitle() + " " + model.getFullName() + " removed from your favourite list", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        doctorDataExecutor.shutdown();
        favouriteRecordExecutor.shutdown();
        doctorExperienceExecutor.shutdown();
        getFavouriteDoctorStatus.shutdown();
    }
}