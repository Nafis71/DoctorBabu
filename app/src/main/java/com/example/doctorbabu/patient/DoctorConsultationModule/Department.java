package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.FragmentDepartmentBinding;


public class Department extends Fragment {
    FragmentDepartmentBinding binding;


    public Department() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.generalPhysician.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("General Physician");
            }
        });
        binding.gynecologist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Gynecologist");
            }
        });
        binding.paediatrician.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Paediatrician");
            }
        });
        binding.dermatologist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Dermatologist");
            }
        });
        binding.psychiatrist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Psychiatrist");
            }
        });
        binding.cardiologist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Cardiologist");
            }
        });
        binding.nutritionist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Nutritionist");
            }
        });
        binding.ophthalmologist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Ophthalmologist");
            }
        });
        binding.neurologist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Neurologist");
            }
        });

    }
    public void loadSpecialistDoctor(String specialist){
        Intent intent = new Intent(requireActivity(), ViewAllDoctor.class);
        intent.putExtra("specialist",specialist);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDepartmentBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
}