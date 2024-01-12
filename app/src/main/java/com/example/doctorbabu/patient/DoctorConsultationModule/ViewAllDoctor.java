package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.doctorbabu.Adapters.doctorSearchAdapter;
import com.example.doctorbabu.DatabaseModels.doctorInfoModel;
import com.example.doctorbabu.Adapters.viewAllDoctorAdapter;
import com.example.doctorbabu.DatabaseModels.doctorSearchResultModel;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityViewAllDoctorBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewAllDoctor extends AppCompatActivity {
    viewAllDoctorAdapter adapter;
    doctorSearchAdapter searchAdapter;
    ArrayList<doctorInfoModel> doctors = new ArrayList<>();
    ArrayList<doctorSearchResultModel> searchModel;
    ExecutorService allDoctorLoadExecutor,searchDoctor;
    ActivityViewAllDoctorBinding binding;
    String specialist = null;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewAllDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        allDoctorLoadExecutor = Executors.newSingleThreadExecutor();
        searchDoctor = Executors.newSingleThreadExecutor();
        allDoctorLoadExecutor.execute(this::loadData);
        binding.back.setOnClickListener(view -> {
            specialist = null;
            finish();
        });
        specialist = getIntent().getStringExtra("specialist");
        searchDoctor.execute(new Runnable() {
            @Override
            public void run() {
                binding.searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean isFocused) {
                        if(isFocused){
                            searchDoctor();
                        }
                    }
                });
            }
        });
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
        DatabaseReference reference = database.getReference("doctorInfo");
        if(specialist == null){
            loadAllDoctor(reference);
        } else {
            loadSpecificSpecialistDoctor(reference);
        }
    }
    public void searchDoctor() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty() && !query.equals(" ")) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    filterList(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private void filterList(String searchedDoctor) {
        int count = 0;
        ArrayList<doctorInfoModel> filteredList = new ArrayList<>();
        binding.doctorRecyclerView.setLayoutManager(new LinearLayoutManager(ViewAllDoctor.this,LinearLayoutManager.VERTICAL,false),R.layout.shimmer_layout_available_doctor);
        adapter = new viewAllDoctorAdapter(this, filteredList);
        binding.doctorRecyclerView.showShimmer();
        for (doctorInfoModel doctor : doctors) {
            if (doctor.getFullName().toLowerCase().contains(searchedDoctor.toLowerCase()) | doctor.getSpecialty().toLowerCase().contains(searchedDoctor.toLowerCase()) | doctor.getDoctorId().toLowerCase().contains(searchedDoctor.toLowerCase())) {
                filteredList.add(doctor);
                count++;
            }
        }
        Handler handler = new Handler(Looper.getMainLooper());
        if (filteredList.isEmpty()) {
            int finalCount = count;
            handler.postDelayed(() -> {
                binding.doctorCount.setText(finalCount);
                binding.progressBar.setVisibility(View.GONE);
                binding.doctorRecyclerView.hideShimmer();
                binding.doctorRecyclerView.setVisibility(View.GONE);
            }, 200);

        } else {
            int finalCount = count;
            handler.postDelayed(() -> {
                binding.doctorRecyclerView.setAdapter(adapter);
                binding.doctorRecyclerView.hideShimmer();
                binding.progressBar.setVisibility(View.GONE);
                binding.doctorCount.setText(String.valueOf(finalCount));
            }, 1000);
        }
    }
    protected void loadAllDoctor(DatabaseReference loadAllDataReference){
        binding.doctorRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false), R.layout.shimmer_layout_available_doctor);
        binding.doctorRecyclerView.showShimmer();
        adapter = new viewAllDoctorAdapter(this, doctors);
        binding.doctorRecyclerView.setAdapter(adapter);
        loadAllDataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                doctors.clear();
                if (snapshot.exists()) {
                    binding.doctorCount.setText(String.valueOf(snapshot.getChildrenCount()));
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        doctorInfoModel model = snap.getValue(doctorInfoModel.class);
                        doctors.add(model);
                        count++;
                    }
                    Collections.shuffle(doctors);
                    binding.doctorCount.setText(String.valueOf(count));
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
    protected void loadSpecificSpecialistDoctor(DatabaseReference specificSpecialistReference){
        binding.doctorRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        binding.doctorRecyclerView.showShimmer();
        adapter = new viewAllDoctorAdapter(this, doctors);
        binding.doctorRecyclerView.setAdapter(adapter);
        specificSpecialistReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctors.clear();
                int count = 0;
                if(snapshot.exists()){
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        doctorInfoModel model = snap.getValue(doctorInfoModel.class);
                        assert model != null;
                        if(model.getSpecialty().contains(specialist))
                        {
                            doctors.add(model);
                            count +=1;
                        }
                    }
                    binding.doctorCount.setText(String.valueOf(count));
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