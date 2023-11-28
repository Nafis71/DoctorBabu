package com.example.doctorbabu.patient.DoctorConsultationModule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityBookAppointmentBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.aviran.cookiebar2.CookieBar;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookAppointment extends AppCompatActivity {
    ActivityBookAppointmentBinding binding;
    Firebase firebase;
    int morningStartHour = 8, morningStartMinute = 0 , morningEndHour = 12,afternoonStartHour = 13,
                afternoonEndHour=18, afternoonStartMinute = 0,nightStartHour = 19, nightEndHour = 23,nightStartMinute = 0;
    int year,month,day;
    String chipName,appointmentHour,appointmentMinute,timePeriod,doctorID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookAppointmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        doctorID = getIntent().getStringExtra("doctorID");
        calculateMorningTime();
        calculaterAfternoonTime();
        calculaterNightTime();
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

    }
    public void bookAppointment(){
        FirebaseUser user = firebase.getUserID();
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        HashMap<String, String> appointmentData =  new HashMap<>();
        appointmentData.put("patientID",user.getUid());
        if(timePeriod.equalsIgnoreCase("Am")){
            appointmentData.put("appointmentHour",appointmentHour.trim());
        }else{
            int hour =  Integer.parseInt(appointmentHour.trim());
            String appointmentHourInternational = String.valueOf(hour+12);
            appointmentData.put("appointmentHour",appointmentHourInternational);
        }
        appointmentData.put("appointmentMinute",appointmentMinute.trim());
        appointmentData.put("timePeriod",timePeriod);
        appointmentData.put("appointmentDate",day+"/"+month+"/"+year);
        reference.child(doctorID).push().setValue(appointmentData);
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

    public void calculateMorningTime(){
        int timeLimit = morningEndHour - morningStartHour;
        String pattern = "00";
        DecimalFormat numberFormatter = new DecimalFormat(pattern);
        for(int i =0; i<= (timeLimit * 60)/20; i++){
            if(i == 0){
                String minute = numberFormatter.format(morningStartMinute);
                String hour = numberFormatter.format(morningStartHour);
                chipName = hour +":" +minute;
                createAppointmentChips("morning");
                morningStartMinute +=20;
            }else if(morningStartMinute == 60){
                morningStartHour++;
                morningStartMinute = 0;
                String minute = numberFormatter.format(morningStartMinute);
                String hour = numberFormatter.format(morningStartHour);
                chipName = hour +":" +minute;
                createAppointmentChips("morning");
                morningStartMinute +=20;
            }else{
                String minute = numberFormatter.format(morningStartMinute);
                String hour = numberFormatter.format(morningStartHour);
                chipName = hour +":" +minute;
                createAppointmentChips("morning");
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
                chipName = Integer.parseInt(hour)-12 +":" +minute;
                createAppointmentChips("afternoon");
                afternoonStartMinute +=20;
            }else if(afternoonStartMinute == 60){
                afternoonStartHour++;
                afternoonStartMinute = 0;
                String minute = numberFormatter.format(afternoonStartMinute);
                String hour = numberFormatter.format(afternoonStartHour);
                chipName = Integer.parseInt(hour)-12 +":" +minute;
                createAppointmentChips("afternoon");
                afternoonStartMinute +=20;
            }else{
                String minute = numberFormatter.format(afternoonStartMinute);
                String hour = numberFormatter.format(afternoonStartHour);
                chipName = Integer.parseInt(hour)-12 +":" +minute;
                createAppointmentChips("afternoon");
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
                chipName = Integer.parseInt(hour)-12 +":" +minute;
                createAppointmentChips("night");
                nightStartMinute +=20;
            }else if(nightStartMinute == 60){
                nightStartHour++;
                nightStartMinute = 0;
                String minute = numberFormatter.format(nightStartMinute);
                String hour = numberFormatter.format(nightStartHour);
                chipName = Integer.parseInt(hour)-12 +":" +minute;
                createAppointmentChips("night");
                nightStartMinute +=20;
            }else{
                String minute = numberFormatter.format(nightStartMinute);
                String hour = numberFormatter.format(nightStartHour);
                chipName = Integer.parseInt(hour)-12 +":" +minute;
                createAppointmentChips("night");
                nightStartMinute +=20;
            }

        }
    }
    public void createAppointmentChips(String dayTime){
        Random random = new Random();
        @SuppressLint("InflateParams")
        Chip chip = (Chip) LayoutInflater.from(BookAppointment.this).inflate(R.layout.appointment_chip, null);
        chip.setText(chipName);
        chip.setId(random.nextInt());
        chip.setHeight(80);
        chip.setClickable(true);
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
    
    public void checkAppointDate(){
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        // TODO: 11/29/2023  
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}