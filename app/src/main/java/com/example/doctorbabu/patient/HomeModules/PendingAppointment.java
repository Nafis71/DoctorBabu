package com.example.doctorbabu.patient.HomeModules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.example.doctorbabu.Adapters.PendingAppointmentAdapter;
import com.example.doctorbabu.Adapters.alarmListAdapter;
import com.example.doctorbabu.DatabaseModels.AppointmentModel;
import com.example.doctorbabu.DatabaseModels.PendingAppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityPendingAppointmentBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PendingAppointment extends AppCompatActivity {
    ActivityPendingAppointmentBinding binding;
    ArrayList<PendingAppointmentModel> model;
    PendingAppointmentAdapter adapter;
    PendingAppointmentModel pendingAppointmentModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPendingAppointmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getAppointmentData();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void getAppointmentData(){
        model = new ArrayList<>();
        binding.appointmentRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false),R.layout.shimmer_layout_appointment);
        adapter = new PendingAppointmentAdapter(this,model);
        binding.appointmentRecyclerView.setAdapter(adapter);
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        reference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    binding.noAppointmentHeader.setVisibility(View.GONE);
                    model.clear();
                    for(DataSnapshot snap : snapshot.getChildren()){
                        pendingAppointmentModel = snap.getValue(PendingAppointmentModel.class);
                        model.add(pendingAppointmentModel);
                    }
                    adapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}