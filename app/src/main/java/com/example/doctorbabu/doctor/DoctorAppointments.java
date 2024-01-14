package com.example.doctorbabu.doctor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.FragmentDoctorAppointmentsBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DoctorAppointments extends Fragment {
    FragmentDoctorAppointmentsBinding binding;
    ExecutorService loadDataExecutor;
    Firebase firebase;
    String doctorId;


    public DoctorAppointments() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebase = Firebase.getInstance();
        loadDoctorId();
        loadDataExecutor = Executors.newSingleThreadExecutor();
        loadDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        });
    }

    public void loadDoctorId(){
        SharedPreferences preferences = requireActivity().getSharedPreferences("loginDetails", Context.MODE_PRIVATE);
        doctorId = preferences.getString("doctorId", "loginAs");
    }
    public void loadData(){

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDoctorAppointmentsBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        loadDataExecutor.shutdown();
    }
}