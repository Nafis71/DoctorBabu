package com.example.doctorbabu.patient;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityAddMedicineAlarmBinding;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;

public class AddMedicineAlarm extends AppCompatActivity {
    ActivityAddMedicineAlarmBinding binding;
    MaterialTimePicker timePicker;
    Calendar calendar;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMedicineAlarmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        createNotificationChannel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.timePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });
        binding.setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAlarm();
            }
        });
    }
    public void showTimePicker(){
        timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Alarm Time")
                .build();
        timePicker.show(getSupportFragmentManager(),"alarm");
        timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if(timePicker.getHour() > 12){
                   binding.AmPm.setText("PM");
                    binding.hour.setText(String.valueOf(timePicker.getHour()-12));
                }else{
                    binding.AmPm.setText("AM");
                    binding.hour.setText(String.valueOf(timePicker.getHour()));
                }
                binding.minute.setText(String.valueOf(timePicker.getMinute()));
                calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY,timePicker.getHour());
                calendar.set(Calendar.MINUTE,timePicker.getMinute());
                calendar.set(Calendar.SECOND,0);
                calendar.set(Calendar.MILLISECOND,0);

            }
        });
    }

    public void setAlarm(){
        int broadcastCode = AlarmReceiver.broadcastCode;
        AlarmReceiver.broadcastCode++;
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this,broadcastCode,intent, PendingIntent.FLAG_IMMUTABLE);
        if(binding.daily.isChecked()){
            Log.w("Alarm Set","set");
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);
        } else {
            Log.w("Alarm Set","Yes set");
            alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
        }
        Toast.makeText(this, "Alarm Set!", Toast.LENGTH_SHORT).show();


    }


    public void createNotificationChannel(){
        CharSequence name = "MedicineReminderChannel";
        String description = "Channel for Alarm Manager";
        NotificationChannel channel = new NotificationChannel("medicineAlarm",name, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

}