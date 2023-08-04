package com.example.doctorbabu.patient;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.doctorbabu.Databases.doctorInfoModel;
import com.example.doctorbabu.Databases.viewAllDoctorAdapter;
import com.example.doctorbabu.databinding.ActivityViewAllDoctorBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewAllDoctor extends AppCompatActivity {
    viewAllDoctorAdapter adapter;
    ArrayList<doctorInfoModel> doctors = new ArrayList<>();
    ExecutorService allDoctorLoadExecutor;
    ActivityViewAllDoctorBinding binding;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewAllDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        allDoctorLoadExecutor = Executors.newSingleThreadExecutor();
    }

    protected void onStart() {
        super.onStart();
        allDoctorLoadExecutor.execute(this::loadAll);
        binding.back.setOnClickListener(view -> {
            finish();
        });
    }

    public void loadAll() {
        binding.doctorRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new viewAllDoctorAdapter(this, doctors);
        binding.doctorRecyclerView.setAdapter(adapter);
        binding.doctorRecyclerView.showShimmer();
        DatabaseReference loadAllDataReference = database.getReference("doctorInfo");
        loadAllDataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctors.clear();
                if (snapshot.exists()) {
                    binding.doctorCount.setText(String.valueOf(snapshot.getChildrenCount()));
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        doctorInfoModel model = snap.getValue(doctorInfoModel.class);
                        doctors.add(model);
                    }
                    adapter.notifyDataSetChanged();
                    binding.doctorRecyclerView.hideShimmer();
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }
    protected void onResume(){
        super.onResume();
    }
    protected void onPause(){
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    public void onBackPressed() {
        finish();
    }
}