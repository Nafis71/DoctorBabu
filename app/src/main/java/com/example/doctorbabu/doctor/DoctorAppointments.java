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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.doctorbabu.Adapters.DoctorAppointmentAdapter;
import com.example.doctorbabu.DatabaseModels.AppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.FragmentDoctorAppointmentsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class DoctorAppointments extends Fragment {
    FragmentDoctorAppointmentsBinding binding;
    ExecutorService loadDataExecutor;
    Firebase firebase;
    String doctorId;
    DoctorAppointmentAdapter adapter;
    ArrayList<AppointmentModel> appointmentModels;


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

    public void loadDoctorId() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("loginDetails", Context.MODE_PRIVATE);
        doctorId = preferences.getString("doctorId", "loginAs");
    }

    public void loadData() {
        appointmentModels = new ArrayList<>();
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.appointmentRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false), R.layout.shimmer_layout_appointment);
                adapter = new DoctorAppointmentAdapter(requireActivity(), appointmentModels,binding.recyclerLayout,binding.descriptionLayout,binding.noAppointmentLayout);
                binding.appointmentRecyclerView.showShimmer();
            }
        });
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        reference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                appointmentModels.clear();
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
                        String formattedDate = currentYear +"-"+currentMonth+"-"+currentDay;
                        assert model != null;
                        if(model.getAppointmentDate().equals(formattedDate)){
                            appointmentModels.add(model);
                        }
                    }
                    if(appointmentModels.isEmpty()){
                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.descriptionLayout.setVisibility(View.GONE);
                                binding.appointmentRecyclerView.setVisibility(View.GONE);
                                binding.noAppointmentLayout.setVisibility(View.VISIBLE);
                            }
                        });
                        return;
                    }
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.descriptionLayout.setVisibility(View.VISIBLE);
                            binding.noAppointmentLayout.setVisibility(View.GONE);
                            binding.appointmentRecyclerView.setVisibility(View.VISIBLE);
                            binding.appointmentRecyclerView.setAdapter(adapter);
                            binding.appointmentRecyclerView.hideShimmer();
                        }
                    });
                } else {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.descriptionLayout.setVisibility(View.GONE);
                            binding.appointmentRecyclerView.setVisibility(View.GONE);
                            binding.noAppointmentLayout.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDoctorAppointmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        loadDataExecutor.shutdown();
    }
}