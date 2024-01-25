package com.example.doctorbabu.doctor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.AppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.databinding.FragmentDoctorHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DoctorHome extends Fragment {
    FragmentDoctorHomeBinding binding;
    Firebase firebase;
    String doctorId;
    ExecutorService doctorDataExecutor, appointmentExecutor, doctorStatisticsExecutor;
    boolean hasAppointment;

    public DoctorHome() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = requireActivity().getSharedPreferences("loginDetails", Context.MODE_PRIVATE);
        doctorId = preferences.getString("doctorId", "loginAs");
        firebase = Firebase.getInstance();
        doctorDataExecutor = Executors.newSingleThreadExecutor();
        appointmentExecutor = Executors.newSingleThreadExecutor();
        doctorStatisticsExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        doctorDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadDoctorData();
            }
        });
        appointmentExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadAppointment();
            }
        });
        doctorStatisticsExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadDoctorStatistics();
            }
        });

    }

    public void loadDoctorData() {
        DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
        reference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    Glide.with(requireActivity()).load(String.valueOf(snapshot.child("photoUrl").getValue())).into(binding.profilePicture);
                    String doctorName = String.valueOf(snapshot.child("title").getValue()) + String.valueOf(snapshot.child("fullName").getValue());
                    binding.userName.setText(doctorName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void loadAppointment() {
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        reference.child(doctorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        AppointmentModel model = snap.getValue(AppointmentModel.class);
                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDateTime now = LocalDateTime.now();
                        String currentDate = dtf.format(now);
                        String[] dateArray = currentDate.split("-");
                        String pattern = "0";
                        DecimalFormat numberFormatter = new DecimalFormat(pattern);
                        String currentYear = numberFormatter.format(Integer.parseInt(dateArray[0]));
                        String currentMonth = numberFormatter.format(Integer.parseInt(dateArray[1]));
                        String currentDay = numberFormatter.format(Integer.parseInt(dateArray[2]));
                        String formattedDate = currentYear + "-" + currentMonth + "-" + currentDay;
                        assert model != null;
                        if (model.getAppointmentDate().equals(formattedDate)) {
                            hasAppointment = true;
                        }
                    }
                    if (hasAppointment) {
                        binding.appointmentReminder.setText("You have appointments booked for today.");
                    } else {
                        binding.appointmentReminder.setText("So far, no appointments have been scheduled for today.");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void loadDoctorStatistics() {
        DatabaseReference reference = firebase.getDatabaseReference("doctorStatistics");
        reference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    binding.patientAmount.setText(String.valueOf(snapshot.child("patientTreated").getValue()));
                    binding.consultancyAmount.setText(String.valueOf(snapshot.child("consultancyDone").getValue()));
                    binding.appointmentAmount.setText(String.valueOf(snapshot.child("appointmentDone").getValue()));
                } else {
                    binding.patientAmount.setText("0");
                    binding.consultancyAmount.setText("0");
                    binding.appointmentAmount.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        doctorDataExecutor.shutdown();
        appointmentExecutor.shutdown();
        doctorStatisticsExecutor.shutdown();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDoctorHomeBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }
}