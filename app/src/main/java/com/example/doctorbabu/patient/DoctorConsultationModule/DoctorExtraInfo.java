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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class DoctorExtraInfo extends Fragment {
    TextView doctorCode, about;
    String doctorId;

    public DoctorExtraInfo() {
        // Required empty public constructor
    }

    public DoctorExtraInfo(String doctorId) {
        this.doctorId = doctorId;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewBinding();
        doctorCode.setText(doctorId);
        loadAbout();
    }

    public void viewBinding() {

        doctorCode = requireView().findViewById(R.id.doctorCode);
        about = requireView().findViewById(R.id.about);
    }

    public void loadAbout() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("doctorInfo");
        reference.child(doctorId).child("about").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (!String.valueOf(snapshot.getValue()).equals("null")) {
                        about.setText(String.valueOf(snapshot.getValue()));
                    } else {
                        about.setText("Didn't find any relevent data");
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doctor_extra_info, container, false);
    }
}