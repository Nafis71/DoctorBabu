package com.example.doctorbabu.patient.HomeModules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.doctorbabu.Adapters.MissedAppointmentAdapter;
import com.example.doctorbabu.Adapters.PendingAppointmentAdapter;
import com.example.doctorbabu.Adapters.alarmListAdapter;
import com.example.doctorbabu.DatabaseModels.AppointmentModel;
import com.example.doctorbabu.DatabaseModels.PendingAppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityPendingAppointmentBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PendingAppointment extends AppCompatActivity {
    ActivityPendingAppointmentBinding binding;
    ArrayList<PendingAppointmentModel> model;
    PendingAppointmentAdapter adapter;
    MissedAppointmentAdapter missedAdapter;
    PendingAppointmentModel pendingAppointmentModel;
    ExecutorService pendingAppointmentExecutor, missedAppointmentExecutor;

    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPendingAppointmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        toggle = new ActionBarDrawerToggle(this,binding.drawerLayout,R.string.openDrawer,R.string.closeDrawer);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        binding.drawerLayout.setStatusBarBackgroundColor(Color.parseColor("#FDFEFE"));
        pendingAppointmentExecutor = Executors.newSingleThreadExecutor();
        missedAppointmentExecutor = Executors.newSingleThreadExecutor();
        pendingAppointmentExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getAppointmentData();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        binding.navBar.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.navMissed){
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    getMissedAppointmentData();
                } else if(item.getItemId() == R.id.navPending){
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    getAppointmentData();
                }
                return false;
            }
        });
    }

    public void getAppointmentData(){
        binding.headerText.setText("Pending");
        binding.appointmentRecyclerView.removeAllViews();
        binding.appointmentRecyclerView.showShimmer();
        model = new ArrayList<>();
        binding.appointmentRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false),R.layout.shimmer_layout_appointment);
        adapter = new PendingAppointmentAdapter(this,model);
        binding.appointmentRecyclerView.setAdapter(adapter);
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    binding.noAppointmentHeader.setVisibility(View.GONE);
                    binding.descriptionHeader.setVisibility(View.VISIBLE);
                    model.clear();
                    for(DataSnapshot snap : snapshot.getChildren()){
                        pendingAppointmentModel = snap.getValue(PendingAppointmentModel.class);
                        model.add(pendingAppointmentModel);
                    }
                    adapter.notifyDataSetChanged();
                    binding.appointmentRecyclerView.hideShimmer();
                }else{
                    binding.appointmentRecyclerView.hideShimmer();
                    binding.nodataText.setText(R.string.noPendingAppointments);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void getMissedAppointmentData(){
        binding.headerText.setText("Missed");
        binding.appointmentRecyclerView.removeAllViews();
        binding.appointmentRecyclerView.showShimmer();
        model = new ArrayList<>();
        binding.appointmentRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false),R.layout.shimmer_layout_appointment);
        missedAdapter = new MissedAppointmentAdapter(this,model);
        binding.appointmentRecyclerView.setAdapter(missedAdapter);
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        DatabaseReference reference = firebase.getDatabaseReference("missedAppointments");
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    binding.noAppointmentHeader.setVisibility(View.GONE);
                    binding.descriptionHeader.setVisibility(View.VISIBLE);
                    model.clear();
                    for(DataSnapshot snap : snapshot.getChildren()){
                        pendingAppointmentModel = snap.getValue(PendingAppointmentModel.class);
                        model.add(pendingAppointmentModel);
                    }
                    missedAdapter.notifyDataSetChanged();
                    binding.appointmentRecyclerView.hideShimmer();
                }else{
                    binding.appointmentRecyclerView.hideShimmer();
                    binding.nodataText.setText(R.string.noMissingAppointments);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
            pendingAppointmentExecutor.shutdown();
            missedAppointmentExecutor.shutdown();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        pendingAppointmentExecutor.shutdown();
        missedAppointmentExecutor.shutdown();
    }
}