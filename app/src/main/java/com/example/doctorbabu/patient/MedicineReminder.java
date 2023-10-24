package com.example.doctorbabu.patient;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityMedicineReminderBinding;

public class MedicineReminder extends AppCompatActivity {
    ActivityMedicineReminderBinding binding;
    AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineReminderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.addAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MedicineReminder.this,AddMedicineAlarm.class);
                startActivity(intent);
            }
        });
        binding.nodataImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MedicineReminder.this,AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(MedicineReminder.this,0,intent, PendingIntent.FLAG_IMMUTABLE);
                if(alarmManager == null){
                    alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                }
                alarmManager.cancel(pendingIntent);
                Toast.makeText(MedicineReminder.this, "Alarm Cancelled", Toast.LENGTH_LONG).show();
            }
        });
    }
}