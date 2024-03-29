package com.example.doctorbabu.doctor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.databinding.FragmentDoctorCallRoomBinding;
import com.example.doctorbabu.patient.DoctorConsultationModule.PatientCall;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DoctorCallRoom extends Fragment {


    FirebaseDatabase database;
    String doctorId, callerId;
    FragmentDoctorCallRoomBinding binding;
    ExecutorService callDetector;

    public DoctorCallRoom() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = requireActivity().getSharedPreferences("loginDetails", Context.MODE_PRIVATE);
        doctorId = preferences.getString("doctorId", "loginAs");
        database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        callDetector = Executors.newSingleThreadExecutor();
        callDetector.execute(this::loadData);
        binding.joinRoom.setOnClickListener(view13 -> joinRoom());
    }


    public void loadData() {
        DatabaseReference reference = database.getReference("callRoom");
        reference.child(doctorId).child("incoming").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (isAdded()) {
                        if (!String.valueOf(snapshot.getValue()).equals("null")) {
                            callerId = String.valueOf(snapshot.getValue());
                            loadCaller();
                            binding.noCallBackground.setVisibility(View.GONE);
                            binding.noCallInfo.setVisibility(View.GONE);
                            binding.profilePicture.setVisibility(View.VISIBLE);
                            binding.joinRoom.setVisibility(View.VISIBLE);
                            binding.callBackground.setVisibility(View.VISIBLE);

                        } else {
                            binding.profilePicture.setVisibility(View.GONE);
                            binding.joinRoom.setVisibility(View.GONE);
                            binding.callBackground.setVisibility(View.GONE);
                            binding.noCallBackground.setVisibility(View.VISIBLE);
                            binding.noCallInfo.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void loadCaller() {
        DatabaseReference reference = database.getReference("users");
        reference.child(callerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String photoUrl = String.valueOf(snapshot.child("photoUrl").getValue());
                binding.callerId.setText(String.valueOf(snapshot.child("fullName").getValue()));
                Glide.with(requireContext()).load(photoUrl).into(binding.profilePicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void joinRoom() {
        DatabaseReference reference = database.getReference("callRoom");
        reference.child(doctorId).child("isAvailable").setValue(true);
        reference.child(doctorId).child("status").setValue(1);
        Intent intent = new Intent(requireActivity(), PatientCall.class);
        intent.putExtra("userId", "null");
        intent.putExtra("callerIdFromDoctor",callerId);
        intent.putExtra("doctorId", doctorId);
        startActivity(intent);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDoctorCallRoomBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        callDetector.shutdown();
    }
}