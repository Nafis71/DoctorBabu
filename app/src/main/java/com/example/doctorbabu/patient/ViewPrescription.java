package com.example.doctorbabu.patient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.doctorbabu.databinding.FragmentViewPrescriptionBinding;


public class ViewPrescription extends Fragment {
    FragmentViewPrescriptionBinding binding; //viewBinding
    String prescriptionId, doctorId, doctorName, doctorDegree, doctorSpecialty, bmdc, prescriptionDate, diagnosis, patientAge, patientName, patientGender, patientWeight;

    public ViewPrescription() {
    }

    public ViewPrescription(String prescriptionId, String doctorId, String doctorName, String doctorDegree, String doctorSpecialty, String bmdc, String prescriptionDate, String diagnosis, String patientAge, String patientName, String patientGender, String patientWeight) {
        this.prescriptionId = prescriptionId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorDegree = doctorDegree;
        this.doctorSpecialty = doctorSpecialty;
        this.bmdc = bmdc;
        this.prescriptionDate = prescriptionDate;
        this.diagnosis = diagnosis;
        this.patientAge = patientAge;
        this.patientName = patientName;
        this.patientGender = patientGender;
        this.patientWeight = patientWeight;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadPrescriptionData();
    }

    public void loadPrescriptionData() {
        binding.doctorName.setText(doctorName);
        binding.doctorDegree.setText(doctorDegree);
        binding.doctorSpecialties.setText(doctorSpecialty);
        binding.diagnosis.setText(diagnosis);
        binding.date.setText(prescriptionDate);
        binding.patientName.setText(patientName);
        binding.age.setText(patientAge);
        binding.weight.setText(patientWeight);
        binding.gender.setText(patientGender);
        binding.bmdc.setText(bmdc);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViewPrescriptionBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }
}