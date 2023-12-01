package com.example.doctorbabu.patient.DoctorConsultationModule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityBookAppointmentBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.aviran.cookiebar2.CookieBar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class BookAppointment extends AppCompatActivity {
    ActivityBookAppointmentBinding binding;
    Firebase firebase;
    int morningStartHour = 8, morningStartMinute = 0 , morningEndHour = 12,afternoonStartHour = 13,
                afternoonEndHour=18, afternoonStartMinute = 0,nightStartHour = 19, nightEndHour = 23,nightStartMinute = 0;
    int year,month,day;
    String chipName,appointmentHour,appointmentMinute,timePeriod,doctorID;
//    ArrayList<AppointmentModel> appointmentModel;
    ArrayList<String>databaseAppointmentTime = new ArrayList<>();
    AlarmManager alarmManager;
    String appointmentID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookAppointmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        doctorID = getIntent().getStringExtra("doctorID");
        createNotificationChannel();
        firebase = Firebase.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getAppointmentTime();
        getAppointmentDate();
        binding.bookAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookAppointment();
            }
        });
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    public void bookAppointment(){
        FirebaseUser user = firebase.getUserID();
        appointmentID = getUniqueID();
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        HashMap<String, String> appointmentData =  new HashMap<>();
        appointmentData.put("appointmentID",appointmentID);
        appointmentData.put("patientID",user.getUid());
        appointmentData.put("doctorID",doctorID);
        if(timePeriod.equalsIgnoreCase("Am")){
            appointmentData.put("appointmentHour",appointmentHour.trim());
        }else{
            int hour =  Integer.parseInt(appointmentHour.trim());
            String appointmentHourInternational = String.valueOf(hour+12);
            appointmentData.put("appointmentHour",appointmentHourInternational);
        }
        appointmentData.put("appointmentMinute",appointmentMinute.trim());
        appointmentData.put("timePeriod",timePeriod);
        appointmentData.put("appointmentDate",year+"-"+month+"-"+day);
        setReminder(appointmentHour, appointmentMinute);
        reference.child(doctorID).child(appointmentID).setValue(appointmentData);
        reference.child(user.getUid()).child(appointmentID).setValue(appointmentData);
        clearChipViews();
        checkAppointmentDate();
        CookieBar.build(BookAppointment.this)
                .setTitle("Appointment Booked")
                .setMessage("Your appointment has been booked")
                .setSwipeToDismiss(true)
                .setDuration(3000)
                .setTitleColor(R.color.white)
                .setBackgroundColor(R.color.blue)
                .setCookiePosition(CookieBar.TOP)  // Cookie will be displayed at the Top
                .show();
    }

    public void setReminder(String hour, String minute){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH,month-1);
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.DAY_OF_MONTH,day);
        if(timePeriod.equalsIgnoreCase("am")){
            calendar.set(Calendar.HOUR_OF_DAY,Integer.parseInt(hour));
        }else{
            calendar.set(Calendar.HOUR_OF_DAY,Integer.parseInt(hour)+12);
        }
        calendar.set(Calendar.MINUTE,Integer.parseInt(minute));
        calendar.set(Calendar.SECOND,0);
        Intent intent = new Intent(this, AppointmentReceiver.class);
        intent.putExtra("doctorId",doctorID);
        intent.putExtra("appointmentID",appointmentID);
        int broadcastCode = getBroadcastCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, broadcastCode, intent, PendingIntent.FLAG_MUTABLE);
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
    }

    public String getUniqueID(){
        return UUID.randomUUID().toString();
    }

    public int getBroadcastCode(){
        AppointmentReceiver appointmentReceiver = new AppointmentReceiver();
        appointmentReceiver.setBroadcastCode();
        return appointmentReceiver.getBroadcastCode();

    }

    public void calculateMorningTime(){
        int timeLimit = morningEndHour - morningStartHour;
        String pattern = "00";
        DecimalFormat numberFormatter = new DecimalFormat(pattern);
        for(int i =0; i<= (timeLimit * 60)/20; i++){
            if(i == 0){
                String minute = numberFormatter.format(morningStartMinute);
                String hour = numberFormatter.format(morningStartHour);
                chipName = hour +":" +minute;
                if(databaseAppointmentTime.contains(chipName)){
                    createAppointmentChips("morning",false);
                } else{
                    createAppointmentChips("morning",true);
                }
                morningStartMinute +=20;
            }else if(morningStartMinute == 60){
                morningStartHour++;
                morningStartMinute = 0;
                String minute = numberFormatter.format(morningStartMinute);
                String hour = numberFormatter.format(morningStartHour);
                chipName = hour +":" +minute;
                if(databaseAppointmentTime.contains(chipName)){
                    createAppointmentChips("morning",false);
                } else{
                    createAppointmentChips("morning",true);
                }
                morningStartMinute +=20;
            }else{
                String minute = numberFormatter.format(morningStartMinute);
                String hour = numberFormatter.format(morningStartHour);
                chipName = hour +":" +minute;
                if(databaseAppointmentTime.contains(chipName)){
                    createAppointmentChips("morning",false);
                } else{
                    createAppointmentChips("morning",true);
                }
                morningStartMinute +=20;
            }

        }
    }

    public void calculaterAfternoonTime(){
        int timeLimit = afternoonEndHour - afternoonStartHour;
        String pattern = "00";
        DecimalFormat numberFormatter = new DecimalFormat(pattern);
        for(int i =0; i<= (timeLimit * 60)/20; i++){
            if(i == 0){
                String minute = numberFormatter.format(afternoonStartMinute);
                String hour = numberFormatter.format(afternoonStartHour);
                String varification = hour+":"+minute;
                chipName = Integer.parseInt(hour)-12 +":" +minute;
                if(databaseAppointmentTime.contains(varification)){
                    createAppointmentChips("afternoon",false);
                } else{
                    createAppointmentChips("afternoon",true);
                }
                afternoonStartMinute +=20;
            }else if(afternoonStartMinute == 60){
                afternoonStartHour++;
                afternoonStartMinute = 0;
                String minute = numberFormatter.format(afternoonStartMinute);
                String hour = numberFormatter.format(afternoonStartHour);
                String varification = hour+":"+minute;
                chipName = Integer.parseInt(hour)-12 +":" +minute;
                if(databaseAppointmentTime.contains(varification)){
                    createAppointmentChips("afternoon",false);
                } else{
                    createAppointmentChips("afternoon",true);
                }
                afternoonStartMinute +=20;
            }else{
                String minute = numberFormatter.format(afternoonStartMinute);
                String hour = numberFormatter.format(afternoonStartHour);
                String varification = hour+":"+minute;
                chipName = Integer.parseInt(hour)-12 +":" +minute;
                if(databaseAppointmentTime.contains(varification)){
                    createAppointmentChips("afternoon",false);
                } else{
                    createAppointmentChips("afternoon",true);
                }
                afternoonStartMinute +=20;
            }

        }
    }
    public void calculaterNightTime(){
        int timeLimit = nightEndHour - nightStartHour;
        String pattern = "00";
        DecimalFormat numberFormatter = new DecimalFormat(pattern);
        for(int i =0; i<= (timeLimit * 60)/20; i++){
            if(i == 0){
                String minute = numberFormatter.format(nightStartMinute);
                String hour = numberFormatter.format(nightStartHour);
                String varification = hour+":"+minute;
                chipName = Integer.parseInt(hour)-12 +":" +minute;
                if(databaseAppointmentTime.contains(varification)){
                    createAppointmentChips("night",false);
                } else{
                    createAppointmentChips("night",true);
                }
                nightStartMinute +=20;
            }else if(nightStartMinute == 60){
                nightStartHour++;
                nightStartMinute = 0;
                String minute = numberFormatter.format(nightStartMinute);
                String hour = numberFormatter.format(nightStartHour);
                String varification = hour+":"+minute;
                chipName = Integer.parseInt(hour)-12 +":" +minute;
                if(databaseAppointmentTime.contains(varification)){
                    createAppointmentChips("night",false);
                } else{
                    createAppointmentChips("night",true);
                }
                nightStartMinute +=20;
            }else{
                String minute = numberFormatter.format(nightStartMinute);
                String hour = numberFormatter.format(nightStartHour);
                String varification = hour+":"+minute;
                chipName = Integer.parseInt(hour)-12 +":" +minute;
                if(databaseAppointmentTime.contains(varification)){
                    createAppointmentChips("night",false);
                } else{
                    createAppointmentChips("night",true);
                }
                nightStartMinute +=20;
            }

        }
    }
    public void createAppointmentChips(String dayTime,boolean clickable){
        Random random = new Random();
        @SuppressLint("InflateParams")
        Chip chip = (Chip) LayoutInflater.from(BookAppointment.this).inflate(R.layout.appointment_chip, null);
        chip.setText(chipName);
        chip.setId(random.nextInt());
        chip.setHeight(80);
        chip.setClickable(clickable);
        if(clickable){
            chip.setEnabled(true);
        }else{
            chip.setEnabled(false);
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#D6DBDF")));
        }
        if(dayTime.equalsIgnoreCase("morning")){
            binding.morningChipGroup.addView(chip);
        } else if(dayTime.equalsIgnoreCase("afternoon")){
            binding.afternoonChipGroup.addView(chip);
        } else{
            binding.nightChipGroup.addView(chip);
        }
    }

    public void getAppointmentDate(){
        binding.calendarView.setCurrentDate(org.threeten.bp.LocalDate.now());
        binding.calendarView.setPagingEnabled(false);
        binding.calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                if(selected){
                  clearChipViews();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkAppointmentDate();
                        }
                    },1000);
                    binding.chipLayout.setVisibility(View.VISIBLE);
                    binding.infoLayout.setVisibility(View.GONE);
                    year = date.getYear();
                    month = date.getMonth();
                    day =  date.getDay();
                }else{
                    binding.chipLayout.setVisibility(View.GONE);
                    binding.infoLayout.setVisibility(View.VISIBLE);
                }

            }
        });
    }
    public void clearChipViews(){
        binding.afternoonChipGroup.removeAllViews();
        binding.morningChipGroup.removeAllViews();
        binding.nightChipGroup.removeAllViews();
        morningStartHour = 8; morningStartMinute = 0;morningEndHour = 12;afternoonStartHour = 13;
        afternoonEndHour=18;afternoonStartMinute =0;nightStartHour = 19;
        nightEndHour = 23;nightStartMinute = 0;
    }
    
    public void checkAppointmentDate(){
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        reference.child(doctorID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    databaseAppointmentTime.clear();
                    for(DataSnapshot snap : snapshot.getChildren()){
                        String today = year+"-"+month+"-"+day;
                        Log.w("Today's Date",today);
                        if(today.equals(snap.child("appointmentDate").getValue().toString())){
                            String hour = snap.child("appointmentHour").getValue().toString();
                            String minute = snap.child("appointmentMinute").getValue().toString();
                            String time = hour +":" +minute;
                            databaseAppointmentTime.add(time);
                        }
                    }
                    calculateMorningTime();
                    calculaterAfternoonTime();
                    calculaterNightTime();
                } else {
                    calculateMorningTime();
                    calculaterAfternoonTime();
                    calculaterNightTime();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    

    public void getAppointmentTime(){
        binding.morningChipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                recordTime(checkedIds,"AM");
                binding.afternoonChipGroup.clearCheck();
                binding.nightChipGroup.clearCheck();
            }
        });
        binding.afternoonChipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                recordTime(checkedIds,"PM");
                binding.nightChipGroup.clearCheck();
                binding.morningChipGroup.clearCheck();
            }
        });
        binding.nightChipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                recordTime(checkedIds,"PM");
                binding.afternoonChipGroup.clearCheck();
                binding.morningChipGroup.clearCheck();
            }
        });
    }

    public void recordTime(List<Integer> checkedIds,String period){
        if(!checkedIds.isEmpty()){
            for(int id :checkedIds){
                Chip chip = findViewById(id);
                String appointmentTimeTmp = chip.getText().toString();
                String [] array = appointmentTimeTmp.split(":");
                appointmentHour = array[0];
                appointmentMinute = array[1];
                timePeriod = period;
            }
        }
    }
    public void createNotificationChannel() {
        CharSequence name = "AppointmentReminderChannel";
        String description = "Channel for Reminding appointment";
        NotificationChannel channel = new NotificationChannel("appointmentReminder", name, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}