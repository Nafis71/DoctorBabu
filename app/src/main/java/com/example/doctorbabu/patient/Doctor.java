package com.example.doctorbabu.patient;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.doctorbabu.Databases.availableDoctorAdapter;
import com.example.doctorbabu.Databases.doctorInfoModel;
import com.example.doctorbabu.Databases.doctorSearchAdapter;
import com.example.doctorbabu.Databases.doctorSearchResultModel;
import com.example.doctorbabu.Databases.recentlyViewedDoctorAdapter;
import com.example.doctorbabu.Databases.recentlyViewedDoctorModel;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.FragmentDoctorBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class Doctor extends Fragment {
    availableDoctorAdapter adapter;
    doctorSearchAdapter searchAdapter;
    recentlyViewedDoctorAdapter recentlyViewedAdapter;
    ArrayList<doctorInfoModel> list;
    ArrayList<doctorSearchResultModel> doctorList = new ArrayList<>();
    ArrayList<recentlyViewedDoctorModel> recentlyViewedModel = new ArrayList<>();
    StringBuilder doctorNameID = new StringBuilder();
    int count = 0, code = 0;
    String userId;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
    ValueEventListener listener;
    DatabaseReference availableDoctorReference;
    FragmentDoctorBinding binding;
    ExecutorService loadDoctorExecutor, recentlyViewedExecutor, searchExecutor, loadAllDoctorExecutor;
    ScheduledExecutorService scheduledThread;

    public Doctor() {
        // Required empty public constructor
    }

    public Doctor(int code) {
        this.code = code;
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = user.getUid();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPageAdapter();
        setRecyclerView();
        binding.vPager.setVisibility(View.GONE);
        binding.consultationAnim2.setVisibility(View.VISIBLE);
        binding.consultationAnim.setVisibility(View.VISIBLE);

    }

    public void onStart() {
        recentlyViewedExecutor = Executors.newSingleThreadExecutor();
        loadDoctorExecutor = Executors.newSingleThreadExecutor();
        searchExecutor = Executors.newSingleThreadExecutor();
        loadAllDoctorExecutor = Executors.newSingleThreadExecutor();
        scheduledThread = Executors.newSingleThreadScheduledExecutor();
        super.onStart();
        if (code == 0) {
            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.loading_screen);
            dialog.setCancelable(false);
            dialog.show();
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.availableDoctorRecyclerView.showShimmer();
            binding.consultationAnim2.setOnClickListener(view1 -> {
                binding.availableDoctorRecyclerView.requestFocus();
                binding.availableDoctorRecyclerView.clearFocus();
            });
            binding.viewAll.setOnClickListener(view -> {
                Intent intent = new Intent(requireContext(), ViewAllDoctor.class);
                startActivity(intent);
            });
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                binding.searchCard.setVisibility(View.VISIBLE);
                recentlyViewedExecutor.execute(Doctor.this::loadRecentlyViewed);
                searchExecutor.execute(Doctor.this::searchDoctor);
                binding.vPager.setVisibility(View.VISIBLE);
                handler.postDelayed(() -> {
                    loadDoctorExecutor.execute(Doctor.this::loadAvailableDoctor);
                    loadAllDoctorExecutor.execute(Doctor.this::loadAllDoctor);
                    code = 1;
                    dialog.dismiss();
                },500);
            }, 1000);

        } else {
            binding.vPager.setVisibility(View.VISIBLE);
            binding.searchCard.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
            recentlyViewedExecutor.execute(Doctor.this::loadRecentlyViewed);
            loadDoctorExecutor.execute(Doctor.this::loadAvailableDoctor);
            searchExecutor.execute(Doctor.this::searchDoctor);
            loadAllDoctorExecutor.execute(Doctor.this::loadAllDoctor);
            binding.consultationAnim2.setOnClickListener(view1 -> {
                binding.availableDoctorRecyclerView.requestFocus();
                binding.availableDoctorRecyclerView.clearFocus();
            });
            binding.viewAll.setOnClickListener(view -> {
                Intent intent = new Intent(requireContext(), ViewAllDoctor.class);
                startActivity(intent);
            });
        }
        binding.identifyDisease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireActivity(),DiagnosisTerms.class);
                startActivity(intent);
            }
        });

    }

    public void setRecyclerView() {
        if (isAdded()) {
            binding.recentlyViewedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false), R.layout.shimmer_layout_recently_viewed);
            recentlyViewedAdapter = new recentlyViewedDoctorAdapter(requireContext(), recentlyViewedModel);
            binding.recentlyViewedRecyclerView.setAdapter(recentlyViewedAdapter);
            binding.availableDoctorRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()), R.layout.shimmer_layout_available_doctor);
            binding.searchRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()), R.layout.shimmer_layout_doctor_search);
            list = new ArrayList<>();
            adapter = new availableDoctorAdapter(requireContext(), list, userId);
            binding.availableDoctorRecyclerView.setAdapter(adapter);
        }

    }

    public void setPageAdapter() {
        final String[] titles = new String[]{getResources().getString(R.string.departmentTab), getResources().getString(R.string.symptomsTab)};
        pageAdapter pageadapter = new pageAdapter(requireActivity());
        binding.vPager.setAdapter(pageadapter);
        new TabLayoutMediator(binding.tabs, binding.vPager, ((tab, position) -> tab.setText(titles[position]))).attach();
    }

    public void loadAvailableDoctor() {
        if (isAdded()) {
            availableDoctorReference = database.getReference("doctorInfo");
            doctorNameID = new StringBuilder();
            requireActivity().runOnUiThread(() -> binding.availableDoctorRecyclerView.showShimmer());

            listener = new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    count = 0;
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        doctorInfoModel model = snap.getValue(doctorInfoModel.class);
                        assert model != null;
                        if (model.getRating() >= 4.8) {
                            if (count < 11) {
                                list.add(model);
                                count++;
                            }
                        }

                    }
                    adapter.notifyDataSetChanged();
                    requireActivity().runOnUiThread(() -> {
                        binding.availableDoctorRecyclerView.hideShimmer();
                        binding.progressBar.setVisibility(View.GONE);
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            availableDoctorReference.orderByChild("onlineStatus").equalTo(1).addValueEventListener(listener);
        }

    }

    public void loadAllDoctor() {
        DatabaseReference allDoctorReference = database.getReference("doctorInfo");
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

            }
        });
    }

    public void searchDoctor() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty() && !newText.equals(" ")) {
                    requireActivity().runOnUiThread(() -> {
                        binding.searchRecyclerView.setVisibility(View.VISIBLE);
                        binding.searchRecyclerView.showShimmer();
                    });
                    filterList(newText);
                } else {
                    requireActivity().runOnUiThread(() -> binding.searchRecyclerView.setVisibility(View.GONE));
                }
                return true;
            }
        });

    }

    private void filterList(String searchedDoctor) {
        ArrayList<doctorSearchResultModel> filteredList = new ArrayList<>();
        searchAdapter = new doctorSearchAdapter(requireContext(), filteredList);
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
            }, 2000);

        } else {
            handler.postDelayed(() -> {
                binding.searchRecyclerView.setAdapter(searchAdapter);
                binding.searchRecyclerView.hideShimmer();
            }, 2000);
        }
    }

    public void loadRecentlyViewed() {
        if (isAdded() && isVisible()) {
            DatabaseReference reference = database.getReference("recentlyViewed");
            reference.child(userId).orderByChild("time").limitToFirst(15).addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    recentlyViewedModel.clear();
                    if (snapshot.exists()) {
                        requireActivity().runOnUiThread(() -> binding.recentlyViewed.setVisibility(View.VISIBLE));
                        binding.recentlyViewedRecyclerView.showShimmer();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            recentlyViewedDoctorModel model = snap.getValue(recentlyViewedDoctorModel.class);
                            recentlyViewedModel.add(model);
                        }
                        Collections.reverse(recentlyViewedModel);
                        recentlyViewedAdapter.notifyDataSetChanged();
                        binding.recentlyViewedRecyclerView.hideShimmer();
                    } else {
                        requireActivity().runOnUiThread(() -> binding.recentlyViewed.setVisibility(View.GONE));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDoctorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        recentlyViewedExecutor = Executors.newSingleThreadExecutor();
        loadDoctorExecutor = Executors.newSingleThreadExecutor();
        searchExecutor = Executors.newSingleThreadExecutor();
        loadAllDoctorExecutor = Executors.newSingleThreadExecutor();
        scheduledThread = Executors.newSingleThreadScheduledExecutor();
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        loadDoctorExecutor.shutdown();
        searchExecutor.shutdown();
        recentlyViewedExecutor.shutdown();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}