package com.example.doctorbabu.patient;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.doctorbabu.Databases.alarmListAdapter;
import com.example.doctorbabu.Databases.alarmListModel;
import com.example.doctorbabu.R;
import com.example.doctorbabu.SqliteDatabase.SqliteDatabase;
import com.example.doctorbabu.databinding.ActivityMedicineReminderBinding;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedicineReminder extends AppCompatActivity {
    ActivityMedicineReminderBinding binding;
    AlarmManager alarmManager;
    alarmListModel model;
    alarmListAdapter adapter;
    ArrayList<alarmListModel> modelsList;
    ExecutorService databaseExecutor;
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        recreate();
                    } else {
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineReminderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        databaseExecutor = Executors.newSingleThreadExecutor();
        databaseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                readDatabase();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        binding.addAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MedicineReminder.this, MedicineAlarm.class);
                activityResultLauncher.launch(intent);
            }
        });
    }

    public void readDatabase(){
        try(SqliteDatabase database = new SqliteDatabase(this)){
            modelsList = new ArrayList<>();
            modelsList = database.readAllData();
            if(modelsList.size() != 0){
                binding.nodataImage.setVisibility(View.GONE);
                binding.nodataText.setVisibility(View.GONE);
                binding.alarmListRecyclerView.setVisibility(View.VISIBLE);
                displayData();
            } else {
                binding.nodataImage.setVisibility(View.VISIBLE);
                binding.nodataText.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void displayData(){
        binding.alarmListRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false),R.layout.shimmer_layout_doctor_search);
        adapter = new alarmListAdapter(this,modelsList,activityResultLauncher);
        binding.alarmListRecyclerView.setAdapter(adapter);
    }
}