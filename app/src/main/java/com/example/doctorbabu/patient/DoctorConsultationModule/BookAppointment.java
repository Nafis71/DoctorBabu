package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityBookAppointmentBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookAppointment extends AppCompatActivity {
    ActivityBookAppointmentBinding binding;
    Firebase firebase;
    int morningStartHour = 8, morningStartMinute = 0, morningEndHour = 12, afternoonStartHour = 13,
            afternoonEndHour = 18, afternoonStartMinute = 0, nightStartHour = 19, nightEndHour = 23, nightStartMinute = 0;
    int year, month, day;
    String chipName, appointmentHour, appointmentMinute, timePeriod, doctorID;
    ArrayList<String> databaseAppointmentTime = new ArrayList<>();
    AlarmManager alarmManager;
    String appointmentID,currentDate,selectedDate;
    ExecutorService appointmentTimeExecutor, appointmentDateExecutor, bookAppointmentExecutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookAppointmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        doctorID = getIntent().getStringExtra("doctorId");
        createNotificationChannel();
        firebase = Firebase.getInstance();
        appointmentTimeExecutor = Executors.newSingleThreadExecutor();
        appointmentDateExecutor = Executors.newSingleThreadExecutor();
        bookAppointmentExecutor = Executors.newSingleThreadExecutor();
        appointmentTimeExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getAppointmentTime();

            }
        });
        appointmentDateExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getAppointmentDate();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        bookAppointmentExecutor.execute(new Runnable() {
            @Override
            public void run() {
                binding.bookAppointment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bookAppointment();
                    }
                });
            }
        });
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        binding.okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void bookAppointment() {
        FirebaseUser user = firebase.getUserID();
        appointmentID = getUniqueID();
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        HashMap<String, String> appointmentData = new HashMap<>();
        appointmentData.put("appointmentID", appointmentID);
        appointmentData.put("patientID", user.getUid());
        appointmentData.put("doctorID", doctorID);
        if (timePeriod.equalsIgnoreCase("Am")) {
            appointmentData.put("appointmentHour", appointmentHour.trim());
        } else {
            int hour = Integer.parseInt(appointmentHour.trim());
            String appointmentHourInternational = String.valueOf(hour + 12);
            appointmentData.put("appointmentHour", appointmentHourInternational);
        }
        appointmentData.put("appointmentMinute", appointmentMinute.trim());
        appointmentData.put("timePeriod", timePeriod);
        appointmentData.put("appointmentDate", year + "-" + month + "-" + day);
        setReminder(appointmentHour, appointmentMinute);                        //setting alarm broadcast here
        reference.child(doctorID).child(appointmentID).setValue(appointmentData);
        reference.child(user.getUid()).child(appointmentID).setValue(appointmentData);
        clearChipViews();
        binding.mainBody.setVisibility(View.GONE);
        binding.bookAppointment.setVisibility(View.GONE);
        binding.bookAppointmentDoneLayout.setVisibility(View.VISIBLE);
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

    public void setReminder(String hour, String minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        if (timePeriod.equalsIgnoreCase("am")) {
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour) + 12);
        }
        calendar.set(Calendar.MINUTE, Integer.parseInt(minute));
        calendar.set(Calendar.SECOND, 0);
        Intent intent = new Intent(this, AppointmentReceiver.class);
        intent.putExtra("doctorId", doctorID);
        intent.putExtra("appointmentID", appointmentID);
        int broadcastCode = getBroadcastCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, broadcastCode, intent, PendingIntent.FLAG_MUTABLE);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    public String getUniqueID() {
        return UUID.randomUUID().toString();
    }

    public int getBroadcastCode() {
        AppointmentReceiver appointmentReceiver = new AppointmentReceiver();
        appointmentReceiver.setBroadcastCode();
        return appointmentReceiver.getBroadcastCode();

    }

    public void calculateMorningTime() {
        int timeLimit = morningEndHour - morningStartHour;
        String pattern = "00";
        DecimalFormat numberFormatter = new DecimalFormat(pattern);
        for (int i = 0; i <= (timeLimit * 60) / 20; i++) {
            if (i == 0) {
                String minute = numberFormatter.format(morningStartMinute);
                String hour = numberFormatter.format(morningStartHour);
                chipName = hour + ":" + minute;
                if (databaseAppointmentTime.contains(chipName)) {
                    createAppointmentChips("morning", false);
                } else {
                    createAppointmentChips("morning", true);
                }
                morningStartMinute += 20;
            } else if (morningStartMinute == 60) {
                morningStartHour++;
                morningStartMinute = 0;
                String minute = numberFormatter.format(morningStartMinute);
                String hour = numberFormatter.format(morningStartHour);
                chipName = hour + ":" + minute;
                if (databaseAppointmentTime.contains(chipName)) {
                    createAppointmentChips("morning", false);
                } else {
                    createAppointmentChips("morning", true);
                }
                morningStartMinute += 20;
            } else {
                String minute = numberFormatter.format(morningStartMinute);
                String hour = numberFormatter.format(morningStartHour);
                chipName = hour + ":" + minute;
                if (databaseAppointmentTime.contains(chipName)) {
                    createAppointmentChips("morning", false);
                } else {
                    createAppointmentChips("morning", true);
                }
                morningStartMinute += 20;
            }

        }
    }

    public void calculateAfternoonTime() {
        int timeLimit = afternoonEndHour - afternoonStartHour;
        String pattern = "00";
        DecimalFormat numberFormatter = new DecimalFormat(pattern);
        for (int i = 0; i <= (timeLimit * 60) / 20; i++) {
            if (i == 0) {
                String minute = numberFormatter.format(afternoonStartMinute);
                String hour = numberFormatter.format(afternoonStartHour);
                String varification = hour + ":" + minute;
                chipName = Integer.parseInt(hour) - 12 + ":" + minute;
                if (databaseAppointmentTime.contains(varification)) {
                    createAppointmentChips("afternoon", false);
                } else {
                    createAppointmentChips("afternoon", true);
                }
                afternoonStartMinute += 20;
            } else if (afternoonStartMinute == 60) {
                afternoonStartHour++;
                afternoonStartMinute = 0;
                String minute = numberFormatter.format(afternoonStartMinute);
                String hour = numberFormatter.format(afternoonStartHour);
                String varification = hour + ":" + minute;
                chipName = Integer.parseInt(hour) - 12 + ":" + minute;
                if (databaseAppointmentTime.contains(varification)) {
                    createAppointmentChips("afternoon", false);
                } else {
                    createAppointmentChips("afternoon", true);
                }
                afternoonStartMinute += 20;
            } else {
                String minute = numberFormatter.format(afternoonStartMinute);
                String hour = numberFormatter.format(afternoonStartHour);
                String varification = hour + ":" + minute;
                chipName = Integer.parseInt(hour) - 12 + ":" + minute;
                if (databaseAppointmentTime.contains(varification)) {
                    createAppointmentChips("afternoon", false);
                } else {
                    createAppointmentChips("afternoon", true);
                }
                afternoonStartMinute += 20;
            }

        }
    }

    public void calculateNightTime() {
        int timeLimit = nightEndHour - nightStartHour;
        String pattern = "00";
        DecimalFormat numberFormatter = new DecimalFormat(pattern);
        for (int i = 0; i <= (timeLimit * 60) / 20; i++) {
            if (i == 0) {
                String minute = numberFormatter.format(nightStartMinute);
                String hour = numberFormatter.format(nightStartHour);
                String varification = hour + ":" + minute;
                chipName = Integer.parseInt(hour) - 12 + ":" + minute;
                if (databaseAppointmentTime.contains(varification)) {
                    createAppointmentChips("night", false);
                } else {
                    createAppointmentChips("night", true);
                }
                nightStartMinute += 20;
            } else if (nightStartMinute == 60) {
                nightStartHour++;
                nightStartMinute = 0;
                String minute = numberFormatter.format(nightStartMinute);
                String hour = numberFormatter.format(nightStartHour);
                String varification = hour + ":" + minute;
                chipName = Integer.parseInt(hour) - 12 + ":" + minute;
                if (databaseAppointmentTime.contains(varification)) {
                    createAppointmentChips("night", false);
                } else {
                    createAppointmentChips("night", true);
                }
                nightStartMinute += 20;
            } else {
                String minute = numberFormatter.format(nightStartMinute);
                String hour = numberFormatter.format(nightStartHour);
                String varification = hour + ":" + minute;
                chipName = Integer.parseInt(hour) - 12 + ":" + minute;
                if (databaseAppointmentTime.contains(varification)) {
                    createAppointmentChips("night", false);
                } else {
                    createAppointmentChips("night", true);
                }
                nightStartMinute += 20;
            }

        }
    }

    public void createAppointmentChips(String dayTime, boolean clickable) {
        String currentTime = getCurrentTime();
        String[] currentTimeArray = currentTime.split(":");
        String[] generatedTimeArray = chipName.split(":");
        int currentHour = Integer.parseInt(currentTimeArray[0]);
        int generatedHour = Integer.parseInt(generatedTimeArray[0]);
        if(dayTime.equalsIgnoreCase("afternoon") | dayTime.equalsIgnoreCase("night")){
            generatedHour += 12;
        }
        Random random = new Random();
        @SuppressLint("InflateParams")
        Chip chip = (Chip) LayoutInflater.from(BookAppointment.this).inflate(R.layout.appointment_chip, null);
        chip.setText(chipName);
        chip.setId(random.nextInt());
        chip.setHeight(80);
        chip.setClickable(clickable);
        if(currentDate.equals(selectedDate)){
            if (clickable && generatedHour >= currentHour) {
                chip.setEnabled(true);
            } else {
                chip.setEnabled(false);
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#D6DBDF")));
            }
        } else {
            if (clickable) {
                chip.setEnabled(true);
            } else {
                chip.setEnabled(false);
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#D6DBDF")));
            }
        }

        if (dayTime.equalsIgnoreCase("morning")) {
            binding.morningChipGroup.addView(chip);
        } else if (dayTime.equalsIgnoreCase("afternoon")) {
            binding.afternoonChipGroup.addView(chip);
        } else {
            binding.nightChipGroup.addView(chip);
        }
    }

    public void getAppointmentDate() {
        binding.calendarView.setPagingEnabled(false);
        binding.calendarView.setCurrentDate(CalendarDay.today());
        binding.calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                if (selected) {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDateTime now = LocalDateTime.now();
                    currentDate = dtf.format(now);
                    String[] dateArray = currentDate.split("-");
                    String pattern = "0";
                    DecimalFormat numberFormatter = new DecimalFormat(pattern);
                    String currentYear = numberFormatter.format(Integer.parseInt(dateArray[0]));
                    String currentMonth = numberFormatter.format(Integer.parseInt(dateArray[1]));
                    String currentDay = numberFormatter.format(Integer.parseInt(dateArray[2]));
                    currentDate = currentYear+"-"+currentMonth+"-"+currentDay;
                    year = date.getYear();
                    month = date.getMonth();
                    day = date.getDay();
                    selectedDate = year+"-"+month+"-"+day;
                    if (month == Integer.parseInt(currentMonth) && year == Integer.parseInt(currentYear)) {
                        if (day >= Integer.parseInt(currentDay) && month >= Integer.parseInt(currentMonth) && year >= Integer.parseInt(currentYear)) {
                            clearChipViews();
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    checkAppointmentDate();
                                }
                            }, 800);
                            binding.chipLayout.setVisibility(View.VISIBLE);
                            binding.infoLayout.setVisibility(View.GONE);
                        } else {
                            clearChipViews();
                            binding.chipLayout.setVisibility(View.GONE);
                            binding.infoLayout.setVisibility(View.VISIBLE);
                            errorMessage("Restricted", "You can't choose previous dates for booking");
                        }

                    } else {
                        clearChipViews();
                        binding.chipLayout.setVisibility(View.GONE);
                        binding.infoLayout.setVisibility(View.VISIBLE);
                        errorMessage("Restricted", "You can't choose forwarding month for booking");
                    }
                } else {
                    binding.chipLayout.setVisibility(View.GONE);
                    binding.infoLayout.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    public void errorMessage(String title, String message) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(BookAppointment.this);
        dialog.setTitle(title).setIcon(R.drawable.cross)
                .setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setCancelable(false);
        dialog.create().show();
    }

    public void clearChipViews() {
        binding.afternoonChipGroup.removeAllViews();
        binding.morningChipGroup.removeAllViews();
        binding.nightChipGroup.removeAllViews();
        morningStartHour = 8;
        morningStartMinute = 0;
        morningEndHour = 12;
        afternoonStartHour = 13;
        afternoonEndHour = 18;
        afternoonStartMinute = 0;
        nightStartHour = 19;
        nightEndHour = 23;
        nightStartMinute = 0;
    }

    public void checkAppointmentDate() {
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        reference.child(doctorID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    databaseAppointmentTime.clear();
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        String appointedTime = year + "-" + month + "-" + day;
                        if (appointedTime.equals(snap.child("appointmentDate").getValue().toString())) {
                            String hour = snap.child("appointmentHour").getValue().toString();
                            String minute = snap.child("appointmentMinute").getValue().toString();
                            String time = hour + ":" + minute;
                            databaseAppointmentTime.add(time);
                        }
                    }
                }
                if(currentDate.equals(selectedDate)){
                    String currentTime = getCurrentTime();
                    String[] currentTimeArray = currentTime.split(":");
                    int hour = Integer.parseInt(currentTimeArray[0]);
                    if (hour > 12 && hour <= 18) {
                        binding.morningHeader.setVisibility(View.GONE);
                        binding.morningDivider.setVisibility(View.GONE);
                        binding.morningChipGroup.setVisibility(View.GONE);
                        calculateAfternoonTime();
                        calculateNightTime();
                        return;
                    } else if (hour > 18) {
                        binding.morningHeader.setVisibility(View.GONE);
                        binding.morningDivider.setVisibility(View.GONE);
                        binding.morningChipGroup.setVisibility(View.GONE);
                        binding.afternoonHeader.setVisibility(View.GONE);
                        binding.afternoonDivider.setVisibility(View.GONE);
                        binding.afternoonChipGroup.setVisibility(View.GONE);
                        calculateNightTime();
                        return;
                    }
                }
                calculateMorningTime();
                calculateAfternoonTime();
                calculateNightTime();
                binding.morningHeader.setVisibility(View.VISIBLE);
                binding.morningDivider.setVisibility(View.VISIBLE);
                binding.morningChipGroup.setVisibility(View.VISIBLE);
                binding.afternoonHeader.setVisibility(View.VISIBLE);
                binding.afternoonDivider.setVisibility(View.VISIBLE);
                binding.afternoonChipGroup.setVisibility(View.VISIBLE);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });

    }


    public void getAppointmentTime() {
        binding.morningChipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                toggleBookButton(checkedIds);
                recordTime(checkedIds, "AM");
                binding.afternoonChipGroup.clearCheck();
                binding.nightChipGroup.clearCheck();
            }
        });
        binding.afternoonChipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                toggleBookButton(checkedIds);
                recordTime(checkedIds, "PM");
                binding.nightChipGroup.clearCheck();
                binding.morningChipGroup.clearCheck();
            }
        });
        binding.nightChipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                toggleBookButton(checkedIds);
                recordTime(checkedIds, "PM");
                binding.afternoonChipGroup.clearCheck();
                binding.morningChipGroup.clearCheck();
            }
        });
    }

    public String getCurrentTime(){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public void toggleBookButton(List<Integer> checkedIds) {
        if (checkedIds.isEmpty()) {
            binding.bookAppointment.setVisibility(View.GONE);
        } else {
            binding.bookAppointment.setVisibility(View.VISIBLE);
        }
    }

    public void recordTime(List<Integer> checkedIds, String period) {
        if (!checkedIds.isEmpty()) {
            for (int id : checkedIds) {
                Chip chip = findViewById(id);
                String appointmentTimeTmp = chip.getText().toString();
                String[] array = appointmentTimeTmp.split(":");
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
        appointmentTimeExecutor.shutdown();
        appointmentDateExecutor.shutdown();
        bookAppointmentExecutor.shutdown();
    }
}