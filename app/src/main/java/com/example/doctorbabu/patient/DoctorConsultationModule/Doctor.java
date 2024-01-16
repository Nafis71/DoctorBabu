package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.doctorbabu.Adapters.FavouriteDoctorAdapter;
import com.example.doctorbabu.Adapters.availableDoctorAdapter;
import com.example.doctorbabu.Adapters.recentlyViewedDoctorAdapter;
import com.example.doctorbabu.DatabaseModels.doctorInfoModel;
import com.example.doctorbabu.DatabaseModels.recentlyViewedDoctorModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
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


public class Doctor extends Fragment {
    availableDoctorAdapter adapter;
    FavouriteDoctorAdapter favouriteDoctorAdapter;
    ArrayList<doctorInfoModel> favouriteDoctorModels;
    recentlyViewedDoctorAdapter recentlyViewedAdapter;
    ArrayList<doctorInfoModel> list;
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
    doctorInfoModel model = doctorInfoModel.getInstance();
    ExecutorService loadDoctorExecutor, recentlyViewedExecutor, favouriteDoctorExecutor;
    boolean hasPressed;


    Dialog dialog;

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
        super.onStart();
        recentlyViewedExecutor = Executors.newSingleThreadExecutor();
        loadDoctorExecutor = Executors.newSingleThreadExecutor();
        favouriteDoctorExecutor = Executors.newSingleThreadExecutor();
        super.onStart();
        if (code == 0) {
            loadingScreen();
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
                favouriteDoctorExecutor.execute(this::loadFavouriteDoctor);
                binding.vPager.setVisibility(View.VISIBLE);
                handler.postDelayed(() -> {
                    loadDoctorExecutor.execute(Doctor.this::loadAvailableDoctor);
                    code = 1;
                    dialog.dismiss();
                }, 500);
            }, 1100);

        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            recentlyViewedExecutor.execute(Doctor.this::loadRecentlyViewed);
            favouriteDoctorExecutor.execute(this::loadFavouriteDoctor);
            loadDoctorExecutor.execute(Doctor.this::loadAvailableDoctor);
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
                Intent intent = new Intent(requireActivity(), DiagnosisTerms.class);
                startActivity(intent);
            }
        });
        try {
            binding.searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean isFocused) {
                    if (isFocused && !hasPressed) {
                        hasPressed = true;
                        binding.searchView.clearFocus();
                        Intent intent = new Intent(requireActivity(), DoctorSearch.class);
                        requireActivity().startActivity(intent);
                    }
                }
            });
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public void loadingScreen() {
        dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.loading_screen);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void setRecyclerView() {
        if (isAdded()) {
            binding.recentlyViewedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false), R.layout.shimmer_layout_recently_viewed);
            recentlyViewedAdapter = new recentlyViewedDoctorAdapter(requireContext(), recentlyViewedModel);
            binding.recentlyViewedRecyclerView.setAdapter(recentlyViewedAdapter);
            binding.availableDoctorRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()), R.layout.shimmer_layout_available_doctor);
            list = new ArrayList<>();
            adapter = new availableDoctorAdapter(requireContext(), list, userId);
            binding.availableDoctorRecyclerView.setAdapter(adapter);
            favouriteDoctorModels = new ArrayList<>();
            binding.favouriteDoctorRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false), R.layout.shimmer_layout_favourite_doctor);
            favouriteDoctorAdapter = new FavouriteDoctorAdapter(requireContext(), favouriteDoctorModels);
        }

    }

    public void setPageAdapter() {
        final String[] titles = new String[]{getResources().getString(R.string.departmentTab), getResources().getString(R.string.symptomsTab)};
        pageAdapter pageadapter = new pageAdapter(requireActivity());
        binding.vPager.setAdapter(pageadapter);
        new TabLayoutMediator(binding.tabs, binding.vPager, ((tab, position) -> tab.setText(titles[position]))).attach();
    }

    public void loadFavouriteDoctor() {
        if (isAdded()) {
            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    binding.favouriteDoctorRecyclerView.showShimmer();
                }
            });
            Firebase firebase = Firebase.getInstance();
            FirebaseUser user = firebase.getUserID();
            DatabaseReference reference = firebase.getDatabaseReference("favouriteDoctors");
            reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    favouriteDoctorModels.clear();
                    if (snapshot.exists()) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            doctorInfoModel model = snap.getValue(doctorInfoModel.class);
                            favouriteDoctorModels.add(model);
                        }
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.favouriteDoctorRecyclerView.setAdapter(favouriteDoctorAdapter);
                                binding.favouriteDoctorRecyclerView.hideShimmer();
                                binding.favouriteLayout.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });

        }
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
                        model = snap.getValue(doctorInfoModel.class);
                        assert model != null;
                        if (model.getRating() >= 4.8) {
                            if (count < 11) {
                                list.add(model);
                                count++;
                            }
                        }

                    }
                    Collections.shuffle(list);
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
        favouriteDoctorExecutor = Executors.newSingleThreadExecutor();
        hasPressed = false;
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
        recentlyViewedExecutor.shutdown();
        favouriteDoctorExecutor.shutdown();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}