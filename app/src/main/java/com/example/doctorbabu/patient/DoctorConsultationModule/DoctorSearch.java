package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.doctorbabu.Adapters.doctorSearchAdapter;
import com.example.doctorbabu.DatabaseModels.doctorSearchResultModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.databinding.ActivityDoctorSearchBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoctorSearch extends AppCompatActivity {
    ActivityDoctorSearchBinding binding;
    doctorSearchAdapter searchAdapter;
    ArrayList<doctorSearchResultModel> doctorList;
    Firebase firebase;
    FirebaseUser user;
    ExecutorService searchExecutor, loadDataExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebase = Firebase.getInstance();
        user = firebase.getUserID();
        searchExecutor = Executors.newSingleThreadExecutor();
        loadDataExecutor = Executors.newSingleThreadExecutor();
        loadDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadAllDoctor();
            }
        });
        searchExecutor.execute(new Runnable() {
            @Override
            public void run() {
                setSearch();
            }
        });

    }

    public void setSearch() {
        binding.searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if (isFocused) {
                    showInputMethod(view.findFocus());
                    searchDoctor();
                }
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.searchView.requestFocus();
            }
        });
    }

    private void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    public void loadAllDoctor() {
        doctorList = new ArrayList<>();
        DatabaseReference allDoctorReference = firebase.getDatabaseReference("doctorInfo");
        allDoctorReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        doctorSearchResultModel searchModel = snap.getValue(doctorSearchResultModel.class);
                        doctorList.add(searchModel);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void searchDoctor() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty() && !query.equals(" ")) {
                    binding.searchRecyclerView.setVisibility(View.VISIBLE);
                    binding.searchRecyclerView.showShimmer();
                    binding.searchDataLayout.setVisibility(View.GONE);
                    binding.noDataLayout.setVisibility(View.GONE);

                    filterList(query);
                } else {
                    binding.searchRecyclerView.setVisibility(View.GONE);
                    binding.searchDataLayout.setVisibility(View.VISIBLE);
                    binding.noDataLayout.setVisibility(View.GONE);
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
        ArrayList<doctorSearchResultModel> filteredList = new ArrayList<>();
        binding.searchRecyclerView.setLayoutManager(new LinearLayoutManager(DoctorSearch.this));
        searchAdapter = new doctorSearchAdapter(this, filteredList, user.getUid(), binding.searchView);
        for (doctorSearchResultModel doctor : doctorList) {
            if (doctor.getFullName().toLowerCase().contains(searchedDoctor.toLowerCase()) | doctor.getSpecialty().toLowerCase().contains(searchedDoctor.toLowerCase()) | doctor.getDoctorId().toLowerCase().contains(searchedDoctor.toLowerCase())) {
                filteredList.add(doctor);
            }
        }
        Handler handler = new Handler(Looper.getMainLooper());
        if (filteredList.isEmpty()) {
            handler.postDelayed(() -> {
                binding.searchRecyclerView.hideShimmer();
                binding.searchRecyclerView.setVisibility(View.GONE);
                binding.noDataLayout.setVisibility(View.VISIBLE);
                binding.searchDataLayout.setVisibility(View.GONE);
            }, 200);

        } else {
            handler.postDelayed(() -> {
                binding.searchRecyclerView.setAdapter(searchAdapter);
                binding.searchRecyclerView.hideShimmer();
            }, 2000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        loadDataExecutor.shutdown();
        searchExecutor.shutdown();
    }
}