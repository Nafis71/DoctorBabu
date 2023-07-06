package com.example.doctorbabu.patient;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.doctorbabu.Databases.availableDoctorAdapter;
import com.example.doctorbabu.Databases.availableDoctorModel;
import com.example.doctorbabu.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.Period;
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
                list.clear();
                for(DataSnapshot snap : snapshot.getChildren())
                {
                    availableDoctorModel model = snap.getValue(availableDoctorModel.class);
                    assert model != null;
                    if(model.getRating() >= 4.8)
                    {
                        DatabaseReference currentlyWorkingReference = database.getReference("doctorCurrentlyWorking");
                        currentlyWorkingReference.child(model.getDoctorId()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                model.setCurrentlyWorking(String.valueOf(snapshot.child("hospitalName").getValue()));
//                                String workingExperience = calculateExperience(String.valueOf(snapshot.child("joiningDate").getValue()));
//                                model.setWorkingExperience(workingExperience);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                throw error.toException();
                            }
                        });
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

    private String calculateExperience(String date)
    {
        String [] splitText = date.split("/");
        int year = Integer.parseInt(splitText[0]);
        int month = Integer.parseInt(splitText[1]);
        int day = Integer.parseInt(splitText[2]);
        LocalDate bday = LocalDate.of(year,month,day);
        LocalDate today = LocalDate.now();
        Period age = Period.between(bday, today);
        String years = String.valueOf(age.getYears());
        String months = String.valueOf(age.getMonths());
        String yearText, monthText;
        if(age.getYears() > 1 && age.getMonths() > 1)
        {
            yearText = " years "; monthText =" months";
        }
        else if(age.getYears() < 1 && age.getMonths() > 1)
        {
            yearText = " year "; monthText = " months";
        }
        else if(age.getYears() > 1 && age.getMonths() < 1)
        {
            yearText = " years "; monthText = " month";
        }
        else
        {
            yearText = " year "; monthText = " month";
        }
        return years+yearText+months+monthText;
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