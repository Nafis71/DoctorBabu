package com.example.doctorbabu.patient.HomeModules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.doctorbabu.Adapters.CancelledAppointmentAdapter;
import com.example.doctorbabu.Adapters.MissedAppointmentAdapter;
import com.example.doctorbabu.Adapters.PendingAppointmentAdapter;
import com.example.doctorbabu.DatabaseModels.AppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityPendingAppointmentBinding;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
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

public class PendingAppointment extends AppCompatActivity {
    ActivityPendingAppointmentBinding binding;
    ArrayList<AppointmentModel> model;
    PendingAppointmentAdapter adapter;
    MissedAppointmentAdapter missedAdapter;
    CancelledAppointmentAdapter cancelledAdapter;
    AppointmentModel appointmentModel;
    ExecutorService pendingAppointmentExecutor;

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
                } else if(item.getItemId() == R.id.navCancelled){
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                    getCancelledAppointment();
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
        adapter = new PendingAppointmentAdapter(this,model,binding.descriptionHeader,binding.noAppointmentHeader);
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
                        appointmentModel = snap.getValue(AppointmentModel.class);
                        assert appointmentModel != null;
                        failSafeAutoCancelAppointment(appointmentModel);
                    }
                    adapter.notifyDataSetChanged();
                    binding.appointmentRecyclerView.hideShimmer();
                }else{
                    binding.appointmentRecyclerView.hideShimmer();
                    binding.nodataText.setText(R.string.noPendingAppointments);
                    binding.noAppointmentHeader.setVisibility(View.VISIBLE);
                    binding.descriptionHeader.setVisibility(View.GONE);
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
        model.clear();
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
                        appointmentModel = snap.getValue(AppointmentModel.class);
                        model.add(appointmentModel);
                    }
                    missedAdapter.notifyDataSetChanged();
                    binding.appointmentRecyclerView.hideShimmer();
                }else{
                    binding.appointmentRecyclerView.hideShimmer();
                    binding.nodataText.setText(R.string.noMissingAppointments);
                    binding.noAppointmentHeader.setVisibility(View.VISIBLE);
                    binding.descriptionHeader.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void getCancelledAppointment(){
        binding.headerText.setText("Cancelled");
        binding.appointmentRecyclerView.removeAllViews();
        binding.appointmentRecyclerView.showShimmer();
        model.clear();
        binding.appointmentRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false),R.layout.shimmer_layout_appointment);
        cancelledAdapter = new CancelledAppointmentAdapter(this,model);
        binding.appointmentRecyclerView.setAdapter(cancelledAdapter);
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        DatabaseReference reference = firebase.getDatabaseReference("cancelledAppointments");
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    binding.noAppointmentHeader.setVisibility(View.GONE);
                    binding.descriptionHeader.setVisibility(View.VISIBLE);
                    model.clear();
                    for(DataSnapshot snap : snapshot.getChildren()){
                        appointmentModel = snap.getValue(AppointmentModel.class);
                        model.add(appointmentModel);
                    }
                    cancelledAdapter.notifyDataSetChanged();
                    binding.appointmentRecyclerView.hideShimmer();
                }else{
                    binding.appointmentRecyclerView.hideShimmer();
                    binding.nodataText.setText(R.string.noAppointmentCancelled);
                    binding.noAppointmentHeader.setVisibility(View.VISIBLE);
                    binding.descriptionHeader.setVisibility(View.GONE);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void failSafeAutoCancelAppointment(AppointmentModel dbModel){
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        String currentTime = getCurrentTime();
        String[] currentTimeArray = currentTime.split(":");
        int hour = Integer.parseInt(currentTimeArray[0]);
        int minute = Integer.parseInt(currentTimeArray[1]);
        String currentDate = getCurrentDate();
        if (currentDate.equalsIgnoreCase(dbModel.getAppointmentDate())) {
            if (hour >= Integer.parseInt(dbModel.getAppointmentHour()) && minute > Integer.parseInt(dbModel.getAppointmentMinute())) {
                saveMissedAppointment(reference,dbModel);
                return;
            }
        } else {
            String[] currentDateArray = currentDate.split("-");
            int year = Integer.parseInt(currentDateArray[0]);
            int month = Integer.parseInt(currentDateArray[1]);
            int day = Integer.parseInt(currentDateArray[2]);
            String[] appointedDateArray = dbModel.getAppointmentDate().split("-");
            int appointedYear = Integer.parseInt(appointedDateArray[0]);
            int appointedMonth = Integer.parseInt(appointedDateArray[1]);
            int appointedDay = Integer.parseInt(appointedDateArray[2]);
            if(year >= appointedYear && month == appointedMonth && day > appointedDay){
                saveMissedAppointment(reference,dbModel);
                return;
            }
        }
        model.add(appointmentModel);
    }
    @SuppressLint("NotifyDataSetChanged")
    public void saveMissedAppointment(DatabaseReference reference, AppointmentModel dbModel){
        Firebase firebase = Firebase.getInstance();
        reference.child(dbModel.getPatientID()).child(dbModel.getAppointmentID()).removeValue();
        reference.child(dbModel.getDoctorID()).child(dbModel.getAppointmentID()).removeValue();
        reference = firebase.getDatabaseReference("missedAppointments");
        reference.child(dbModel.getPatientID()).child(dbModel.getAppointmentID()).setValue(dbModel);
    }
    public String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public String getCurrentDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        String currentDate = dtf.format(now);
        String[] dateArray = currentDate.split("-");
        String pattern = "0";
        DecimalFormat numberFormatter = new DecimalFormat(pattern);
        String currentYear = numberFormatter.format(Integer.parseInt(dateArray[0]));
        String currentMonth = numberFormatter.format(Integer.parseInt(dateArray[1]));
        String currentDay = numberFormatter.format(Integer.parseInt(dateArray[2]));
        return currentYear + "-" + currentMonth + "-" + currentDay;
    }

    @Override
    public void onBackPressed() {
        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
            pendingAppointmentExecutor.shutdown();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        pendingAppointmentExecutor.shutdown();
    }
}