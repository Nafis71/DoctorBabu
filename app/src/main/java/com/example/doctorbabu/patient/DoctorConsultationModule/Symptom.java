package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.FragmentSymptomBinding;


public class Symptom extends Fragment {
    FragmentSymptomBinding binding;


    public Symptom() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.fever.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("General Physician");
            }
        });
        binding.weightLossAndGain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Nutritionist");
            }
        });
        binding.skinDisease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Dermatologist");
            }
        });
        binding.abdominalPain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Gastroenterologist");
            }
        });
        binding.acidity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Gastroenterologist");
            }
        });
        binding.headache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Neurologist");
            }
        });
        binding.childDisease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Paediatrician");
            }
        });
        binding.stress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Psychiatrist");
            }
        });
        binding.pregnancy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSpecialistDoctor("Gynecologist");
            }
        });

    }

    public void loadSpecialistDoctor(String specialist){
        Intent intent = new Intent(requireActivity(), ViewAllDoctor.class);
        intent.putExtra("specialist",specialist);
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSymptomBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
}