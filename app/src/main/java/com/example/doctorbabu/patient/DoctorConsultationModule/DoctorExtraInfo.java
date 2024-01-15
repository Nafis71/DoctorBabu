package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.FragmentDoctorExtraInfoBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DoctorExtraInfo extends Fragment {
    FragmentDoctorExtraInfoBinding binding;
    String doctorId;
    ExecutorService loadDataExecutor;

    public DoctorExtraInfo() {
        // Required empty public constructor
    }

    public DoctorExtraInfo(String doctorId) {
        this.doctorId = doctorId;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.doctorCode.setText(doctorId);
        loadDataExecutor = Executors.newSingleThreadExecutor();
        loadDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadAbout();
            }
        });

    }

    public void loadAbout() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("doctorInfo");
        reference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (!String.valueOf(snapshot.child("about").getValue()).equals("null")) {
                        binding.about.setText(String.valueOf(snapshot.child("about").getValue()));
                    } else {
                        binding.about.setText("Didn't find any relevent data");
                    }
                    if(!String.valueOf(snapshot.child("consultationFee").getValue()).equals("null")){
                        binding.amount.setText(String.valueOf(snapshot.child("consultationFee").getValue()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        loadDataExecutor.shutdown();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDoctorExtraInfoBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
}