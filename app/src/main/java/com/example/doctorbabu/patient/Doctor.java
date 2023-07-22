package com.example.doctorbabu.patient;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.doctorbabu.Databases.availableDoctorAdapter;
import com.example.doctorbabu.Databases.availableDoctorModel;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class Doctor extends Fragment {
    availableDoctorAdapter adapter;
    doctorSearchAdapter searchAdapter;
    recentlyViewedDoctorAdapter recentlyViewedAdapter;
    ArrayList<availableDoctorModel> list;
    ArrayList<doctorSearchResultModel> doctorList = new ArrayList<>();
    ArrayList<recentlyViewedDoctorModel> recentlyViewedModel = new ArrayList<>();
    StringBuilder doctorNameID = new StringBuilder();
    int count = 0;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
    Thread searchDoctorThread;
    FragmentDoctorBinding binding;
    public Doctor() {
        // Required empty public constructor
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPageAdapter();
        Thread loadDoctorThread = new Thread(this::loadAvailableDoctor);
        loadDoctorThread.start();
        searchDoctor();
        loadRecentlyViewed();
        binding.consultationAnim2.setOnClickListener(view1 -> {
            binding.availableDoctorRecyclerView.requestFocus();
            binding.availableDoctorRecyclerView.clearFocus();
        });
    }

    public void setPageAdapter() {
        final String[] titles = new String[]{getResources().getString(R.string.departmentTab), getResources().getString(R.string.symptomsTab)};
        pageAdapter pageadapter = new pageAdapter(requireActivity());
        binding.vPager.setAdapter(pageadapter);
        new TabLayoutMediator(binding.tabs,binding.vPager,((tab, position) -> tab.setText(titles[position]))).attach();
    }

    public void loadAvailableDoctor() {
            DatabaseReference reference = database.getReference("doctorInfo");
            binding.availableDoctorRecyclerView.showShimmer();
            doctorNameID = new StringBuilder();
            reference.addValueEventListener(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    count = 0;
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        availableDoctorModel model = snap.getValue(availableDoctorModel.class);
                        assert model != null;
                        if (model.getRating() >= 4.8 && model.getOnlineStatus() != 0) {
                            if (count < 11) {
                                list.add(model);
                                count++;
                            }
                        }
                        doctorNameID.append(model.getFullName()).append(" ").append("(").append(model.getDoctorId()).append(")");
                        doctorSearchResultModel searchModel = new doctorSearchResultModel();
                        searchModel.setDoctorNameAndId(doctorNameID.toString());
                        searchModel.setDepartment(model.getSpecialty());
                        searchModel.setProfilePicture(model.getPhotoUrl());
                        doctorList.add(searchModel);
                        doctorNameID.setLength(0);
                    }
                    adapter.notifyDataSetChanged();
                    binding.availableDoctorRecyclerView.hideShimmer();
                    binding.progressBar.setVisibility(View.GONE);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
    }
    public void searchDoctor(){
        searchDoctorThread = new Thread(() -> binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.isEmpty() && !newText.equals(" "))
                {
                    requireActivity().runOnUiThread(() -> {
                        binding.searchRecyclerView.setVisibility(View.VISIBLE);
                        binding.searchRecyclerView.showShimmer();

                    });
                    filterList(newText);
                }else{
                    requireActivity().runOnUiThread(() -> binding.searchRecyclerView.setVisibility(View.GONE));

                }
                return true;
            }
        }));
        searchDoctorThread.start();

    }
    private void filterList(String searchedDoctor) {
        Thread newSearchThread =  new Thread(() -> {
            ArrayList<doctorSearchResultModel> filteredList =  new ArrayList<>();
            searchAdapter = new doctorSearchAdapter(requireContext(),filteredList);
            for(doctorSearchResultModel doctor : doctorList){
                if(doctor.getDoctorNameAndId().toLowerCase().contains(searchedDoctor.toLowerCase())
                        | doctor.getDepartment().toLowerCase().contains(searchedDoctor.toLowerCase())){
                    filteredList.add(doctor);
                }
            }
            if(filteredList.isEmpty()){
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    binding.searchRecyclerView.hideShimmer();
                    binding.searchRecyclerView.setVisibility(View.GONE);
                },2000);

            }else{
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {
                    binding.searchRecyclerView.setAdapter(searchAdapter);
                    binding.searchRecyclerView.hideShimmer();
                },2000);
            }
        });
        newSearchThread.start();

    }

    public void loadRecentlyViewed(){
        ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
        backgroundExecutor.execute(() -> {
            binding.recentlyViewedRecyclerView.showShimmer();
            DatabaseReference reference = database.getReference("recentlyViewed");
            reference.child(user.getUid()).orderByChild("time").limitToFirst(15).addValueEventListener(new ValueEventListener() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    recentlyViewedModel.clear();
                    if(snapshot.exists()){
                        binding.recentlyViewed.setVisibility(View.VISIBLE);
                        for(DataSnapshot snap : snapshot.getChildren())
                        {
                            recentlyViewedDoctorModel model = snap.getValue(recentlyViewedDoctorModel.class);
                            recentlyViewedModel.add(model);
                        }
                        Collections.reverse(recentlyViewedModel);
                        recentlyViewedAdapter.notifyDataSetChanged();
                        binding.recentlyViewedRecyclerView.hideShimmer();
                    }else{
                        binding.recentlyViewed.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });

        });



    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorBinding.inflate(inflater,container,false);
        binding.recentlyViewedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false),R.layout.shimmer_layout_recently_viewed);
        recentlyViewedAdapter = new recentlyViewedDoctorAdapter(requireContext(),recentlyViewedModel);
        binding.recentlyViewedRecyclerView.setAdapter(recentlyViewedAdapter);
        binding.availableDoctorRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()), R.layout.shimmer_layout_available_doctor);
        binding.availableDoctorRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()),R.layout.shimmer_layout_doctor_search);
        list = new ArrayList<>();
        adapter = new availableDoctorAdapter(requireContext(), list,user.getUid());
        binding.availableDoctorRecyclerView.setAdapter(adapter);
        return binding.getRoot();
    }
    public void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}