package com.example.doctorbabu.patient;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.doctorbabu.Databases.availableDoctorAdapter;
import com.example.doctorbabu.Databases.availableDoctorModel;
import com.example.doctorbabu.Databases.doctorSearchAdapter;
import com.example.doctorbabu.Databases.doctorSearchResultModel;
import com.example.doctorbabu.Databases.recentlyViewedDoctorAdapter;
import com.example.doctorbabu.Databases.recentlyViewedDoctorModel;
import com.example.doctorbabu.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.todkars.shimmer.ShimmerRecyclerView;
import java.util.ArrayList;
import java.util.Collections;

import timber.log.Timber;

public class Doctor extends Fragment {
    ViewPager2 vPager;
    TabLayout tabs;
    ShimmerRecyclerView recyclerView,searchRecyclerView,recentlyViewedRecyclerView;
    androidx.appcompat.widget.SearchView searchView;
    LinearProgressIndicator progressBar;
    private final String[] titles = new String[]{"Departments", "Symptoms"};

    availableDoctorAdapter adapter;
    doctorSearchAdapter searchAdapter;
    recentlyViewedDoctorAdapter recentlyViewedAdapter;
    ArrayList<availableDoctorModel> list;
    ArrayList<doctorSearchResultModel> doctorList = new ArrayList<>();
    ArrayList<recentlyViewedDoctorModel> recentlyViewedModel = new ArrayList<>();
    RelativeLayout recentlyViewed;
    StringBuilder doctorNameID = new StringBuilder();
    int count = 0;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");

    public Doctor() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewBinding();
        loadAvailableDoctor();
        searchDoctor();
        loadRecentlyViewed();
    }

    public void viewBinding() {
        vPager = requireView().findViewById(R.id.vPager);
        tabs = requireView().findViewById(R.id.tabs);
        searchView = requireView().findViewById(R.id.searchView);
        progressBar = requireView().findViewById(R.id.progressBar);
        recentlyViewed = requireView().findViewById(R.id.recentlyViewed);
        pageAdapter adapter = new pageAdapter(requireActivity());
        vPager.setAdapter(adapter);
        new TabLayoutMediator(tabs, vPager, ((tab, position) -> tab.setText(titles[position]))).attach();

    }

    public void loadAvailableDoctor() {
        progressBar.setVisibility(View.VISIBLE);
        DatabaseReference reference = database.getReference("doctorInfo");
        recyclerView.setHasFixedSize(true);
        searchRecyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()), R.layout.shimmer_layout_available_doctor);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()),R.layout.shimmer_layout_doctor_search);
        list = new ArrayList<>();
        adapter = new availableDoctorAdapter(requireContext(), list,user.getUid());
        recyclerView.setAdapter(adapter);
        recyclerView.showShimmer();
        doctorNameID = new StringBuilder();
        Handler handler = new Handler();
        reference.addValueEventListener(new ValueEventListener() {
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
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.hideShimmer();
                        progressBar.setVisibility(View.GONE);
                        searchView.setVisibility(View.VISIBLE);
                    }
                }, 2000);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    public void searchDoctor(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.isEmpty() && !newText.equals(" "))
                {
                    searchRecyclerView.setVisibility(View.VISIBLE);
                    searchRecyclerView.showShimmer();
                    filterList(newText);
                }else{
                    searchRecyclerView.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }
    private void filterList(String searchedDoctor) {
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
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    searchRecyclerView.hideShimmer();
                    searchRecyclerView.setVisibility(View.GONE);
                }
            },2000);

        }else{
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    searchRecyclerView.setAdapter(searchAdapter);
                    searchRecyclerView.hideShimmer();
                }
            },2000);
        }
    }

    public void loadRecentlyViewed(){
        recentlyViewedRecyclerView.setHasFixedSize(true);
        recentlyViewedRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false),R.layout.shimmer_layout_recently_viewed);
        recentlyViewedAdapter = new recentlyViewedDoctorAdapter(requireContext(),recentlyViewedModel);
        recentlyViewedRecyclerView.setAdapter(recentlyViewedAdapter);
        recentlyViewedRecyclerView.showShimmer();
        DatabaseReference reference = database.getReference("recentlyViewed");
        reference.child(user.getUid()).orderByChild("time").limitToLast(15).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recentlyViewedModel.clear();
                if(snapshot.exists()){
                    recentlyViewed.setVisibility(View.VISIBLE);
                    for(DataSnapshot snap : snapshot.getChildren())
                    {
                        recentlyViewedDoctorModel model = snap.getValue(recentlyViewedDoctorModel.class);
                        recentlyViewedModel.add(model);
                    }
                    Collections.reverse(recentlyViewedModel);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recentlyViewedRecyclerView.hideShimmer();
                            recentlyViewedAdapter.notifyDataSetChanged();
                        }
                    },1500);
                }else{
                    recentlyViewed.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_doctor, container, false);
        recyclerView = view.findViewById(R.id.shimmer_recycler_view);
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView);
        recentlyViewedRecyclerView = view.findViewById(R.id.recentlyViewedRecyclerView);
        return view;
    }
}