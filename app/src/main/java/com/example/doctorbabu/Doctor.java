package com.example.doctorbabu;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.doctorbabu.Databases.availableDoctorAdapter;
import com.example.doctorbabu.Databases.availableDoctorModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Doctor extends Fragment {
    ViewPager2 vPager;
    TabLayout tabs;
    RecyclerView recyclerView;
    private String [] titles = new String[]{"Departments","Symptoms"};

    availableDoctorAdapter adapter;
    ArrayList<availableDoctorModel> list;


    public Doctor() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        viewBinding();
        loadAvailableDoctor();


    }
    public void viewBinding(){
        vPager = (ViewPager2) requireView().findViewById(R.id.vPager);
        tabs = (TabLayout) requireView().findViewById(R.id.tabs);
        pageAdapter adapter = new pageAdapter(requireActivity());
        vPager.setAdapter(adapter);
        new TabLayoutMediator(tabs,vPager,((tab, position) ->tab.setText(titles[position]))).attach();

    }
    public void loadAvailableDoctor()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("doctorInfo");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        list =  new ArrayList<>();
        adapter = new availableDoctorAdapter(requireContext(),list);
        recyclerView.setAdapter(adapter);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren())
                {
                    availableDoctorModel model = snap.getValue(availableDoctorModel.class);
                    assert model != null;
                    if(model.getRating() >= 4.8)
                    {
                        list.add(model);
                    }
                }
                adapter.notifyDataSetChanged();
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
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        return view;
    }
}