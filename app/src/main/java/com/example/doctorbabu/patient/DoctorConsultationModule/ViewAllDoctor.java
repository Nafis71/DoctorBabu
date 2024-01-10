package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.doctorbabu.DatabaseModels.doctorInfoModel;
import com.example.doctorbabu.Adapters.viewAllDoctorAdapter;
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
    String specialist = null;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewAllDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.doctorRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new viewAllDoctorAdapter(this, doctors);
        binding.doctorRecyclerView.setAdapter(adapter);
        allDoctorLoadExecutor = Executors.newSingleThreadExecutor();
        allDoctorLoadExecutor.execute(this::loadData);
        binding.back.setOnClickListener(view -> {
            specialist = null;
            finish();
        });
        specialist = getIntent().getStringExtra("specialist");

    }

    protected void onStart() {
        super.onStart();
        binding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
           @Override
           public void onRefresh() {
               Handler handler = new Handler();
               handler.postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       loadData();
                       binding.swipe.setRefreshing(false);
                   }
               },1000);

           }
       });
    }

    public void loadData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.doctorRecyclerView.showShimmer();
        DatabaseReference reference = database.getReference("doctorInfo");
        if(specialist == null){
            loadAllDoctor(reference);
        } else {
            loadSpecificSpecialistDoctor(reference);
        }


    }
    protected void loadAllDoctor(DatabaseReference loadAllDataReference){
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
    protected void loadSpecificSpecialistDoctor(DatabaseReference specificSpecialistReference){
        specificSpecialistReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctors.clear();
                if(snapshot.exists()){
                    binding.doctorCount.setText(String.valueOf(snapshot.getChildrenCount()));
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        doctorInfoModel model = snap.getValue(doctorInfoModel.class);
                        assert model != null;
                        if(model.getSpecialty().contains(specialist))
                        {
                            doctors.add(model);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    binding.doctorRecyclerView.hideShimmer();
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
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
        allDoctorLoadExecutor.shutdown();
    }

    public void onBackPressed() {
        specialist = null;
        finish();
    }
}