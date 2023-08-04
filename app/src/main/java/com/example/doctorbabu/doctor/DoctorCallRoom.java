package com.example.doctorbabu.doctor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.PatientCall;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class DoctorCallRoom extends Fragment {
    ImageView profilePicture;
    TextView callerIdView, noCallInfo;
    Button disbandRoom, joinRoom, prescribeMedicine;
    FirebaseDatabase database;
    String doctorId, callerId;
    LottieAnimationView callBackground, noCallBackground;

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
        viewBinding();
        Thread backgroundThread = new Thread(this::loadData);
        backgroundThread.start();

        joinRoom.setOnClickListener(view13 -> joinRoom());
        disbandRoom.setOnClickListener(view12 -> disBandRoom());
        prescribeMedicine.setOnClickListener(view1 -> {
            Intent intent = new Intent(requireActivity(), DoctorPrescribeMedicine.class);
            intent.putExtra("patientId", callerId);
            startActivity(intent);
        });
    }

    public void viewBinding() {
        profilePicture = requireView().findViewById(R.id.profilePicture);
        callerIdView = requireView().findViewById(R.id.callerId);
        joinRoom = requireView().findViewById(R.id.joinRoom);
        disbandRoom = requireView().findViewById(R.id.disbandRoom);
        callBackground = requireView().findViewById(R.id.callBackground);
        noCallInfo = requireView().findViewById(R.id.noCallInfo);
        noCallBackground = requireView().findViewById(R.id.noCallBackground);
        prescribeMedicine = requireView().findViewById(R.id.prescribeMedicine);
    }

    public void loadData() {
        DatabaseReference reference = database.getReference("callRoom");
        reference.child(doctorId).child("incoming").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    if (!String.valueOf(snapshot.getValue()).equals("null")) {
                        callerId = String.valueOf(snapshot.getValue());
                        loadCaller();
                        noCallBackground.setVisibility(View.GONE);
                        noCallInfo.setVisibility(View.GONE);
                        profilePicture.setVisibility(View.VISIBLE);
                        joinRoom.setVisibility(View.VISIBLE);
                        disbandRoom.setVisibility(View.VISIBLE);
                        callBackground.setVisibility(View.VISIBLE);
                        prescribeMedicine.setVisibility(View.VISIBLE);

                    } else {
                        profilePicture.setVisibility(View.GONE);
                        joinRoom.setVisibility(View.GONE);
                        disbandRoom.setVisibility(View.GONE);
                        callBackground.setVisibility(View.GONE);
                        noCallBackground.setVisibility(View.VISIBLE);
                        noCallInfo.setVisibility(View.VISIBLE);
                        prescribeMedicine.setVisibility(View.GONE);
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
                callerIdView.setText(String.valueOf(snapshot.child("fullName").getValue()));
                Glide.with(requireContext()).load(photoUrl).into(profilePicture);
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
        intent.putExtra("doctorId", doctorId);
        startActivity(intent);
    }

    public void disBandRoom() {
        DatabaseReference reference = database.getReference("callRoom");
        reference.child(doctorId).child("connId").removeValue();
        reference.child(doctorId).child("incoming").setValue("null");
        reference.child(doctorId).child("isAvailable").setValue(false);
        reference.child(doctorId).child("status").setValue(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doctor_call_room, container, false);
    }
}