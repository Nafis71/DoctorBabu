package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.AppointmentModel;
import com.example.doctorbabu.DatabaseModels.doctorInfoModel;
import com.example.doctorbabu.DatabaseModels.joiningDates;
import com.example.doctorbabu.DatabaseModels.leavingDates;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivitySpecificDoctorInfoBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpecificDoctorInfo extends AppCompatActivity {
    private final String[] titles = new String[]{"Info", "Experience", "Reviews"};
    String doctorId;
    Firebase firebase;
    FirebaseUser user;
    doctorInfoModel model;
    ActivitySpecificDoctorInfoBinding binding;
    ExecutorService favouriteRecordExecutor, doctorDataExecutor, doctorExperienceExecutor, getFavouriteDoctorStatus, appointmentExecutor, videoCallAppointmentExecutor,
            removeBookingExecutor;
    boolean toggleButton;
    Dialog dialog;
    String onlineStatus, currentlyWorking, appointmentId = "";
    boolean hasBooked, hasBookedToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySpecificDoctorInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingScreen();
        doctorId = getIntent().getStringExtra("doctorId");
        firebase = Firebase.getInstance();
        user = firebase.getUserID();
        favouriteRecordExecutor = Executors.newSingleThreadExecutor();
        doctorDataExecutor = Executors.newSingleThreadExecutor();
        doctorExperienceExecutor = Executors.newSingleThreadExecutor();
        getFavouriteDoctorStatus = Executors.newSingleThreadExecutor();
        appointmentExecutor = Executors.newSingleThreadExecutor();
        videoCallAppointmentExecutor = Executors.newSingleThreadExecutor();
        removeBookingExecutor = Executors.newSingleThreadExecutor();
        getFavouriteDoctorStatus.execute(new Runnable() {
            @Override
            public void run() {
                getFavouriteDoctorStatus();
            }
        });
        removeBookingExecutor.execute(new Runnable() {
            @Override
            public void run() {
                removeBooking();
            }
        });
        doctorDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        });
        favouriteRecordExecutor.execute(new Runnable() {
            @Override
            public void run() {
                recordFavourite();
            }
        });
        doctorExperienceExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getDoctorExperience();
            }
        });
        videoCallAppointmentExecutor.execute(new Runnable() {
            @Override
            public void run() {
                binding.videoCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (Integer.parseInt(onlineStatus) == 1) {
                            checkAppointmentHistoryVideoCall();
                        } else {
                            errorMessage("Offline", "We can't establish a video call due to doctor's unavailability");
                        }
                    }
                });
            }
        });
        binding.goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        closeLoadingScreen();
        appointmentExecutor.execute(new Runnable() {
            @Override
            public void run() {
                binding.appointment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        checkAppointmentHistory();
                    }
                });
            }
        });
    }

    public void loadingScreen() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.loading_screen);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void closeLoadingScreen() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.mainBody.setVisibility(View.VISIBLE);
                binding.subBody.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        }, 1000);
    }

    public void checkAppointmentHistoryVideoCall() {
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        AppointmentModel model = snap.getValue(AppointmentModel.class);
                        assert model != null;
                        String currentTime = getCurrentTime();
                        String[] currentTimeArray = currentTime.split(":");
                        int hour = Integer.parseInt(currentTimeArray[0]);
                        int minute = Integer.parseInt(currentTimeArray[1]);
                        if (getCurrentDate().equalsIgnoreCase(model.getAppointmentDate()) && model.getDoctorID().equalsIgnoreCase(doctorId)) {
                            if (hour == Integer.parseInt(model.getAppointmentHour()) && minute == Integer.parseInt(model.getAppointmentMinute())) {
                                proceedToVideoCall();
                                return;
                            } else if (hour == Integer.parseInt(model.getAppointmentHour()) && minute> Integer.parseInt(model.getAppointmentMinute()) && minute <= (Integer.parseInt(model.getAppointmentMinute()) + 10)) {
                                proceedToVideoCall();
                                return;
                            }
                            hasBookedToday = true;
                        }
                    }
                    if (hasBookedToday) {
                        errorMessage("Found Pending Appointment", "You have already booked appointment for this doctor today. Check Pending Appointment Section for more info.");
                        return;
                    }
                }
                proceedToVideoCall();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });

    }

    public void removeBooking() {
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        AppointmentModel model = snap.getValue(AppointmentModel.class);
                        assert model != null;
                        String currentTime = getCurrentTime();
                        String[] currentTimeArray = currentTime.split(":");
                        int hour = Integer.parseInt(currentTimeArray[0]);
                        int minute = Integer.parseInt(currentTimeArray[1]);
                        String currentDate = getCurrentDate();
                        if (currentDate.equalsIgnoreCase(model.getAppointmentDate())) {
                            if (hour >= Integer.parseInt(model.getAppointmentHour()) && minute > Integer.parseInt(model.getAppointmentMinute())) {
                                reference.child(user.getUid()).child(model.getAppointmentID()).removeValue();
                                reference.child(doctorId).child(model.getAppointmentID()).removeValue();
                                return;
                            }
                        } else {
                            String[] currentDateArray = currentDate.split("-");
                            int year = Integer.parseInt(currentDateArray[0]);
                            int month = Integer.parseInt(currentDateArray[1]);
                            int day = Integer.parseInt(currentDateArray[2]);
                            String[] appointedDateArray = model.getAppointmentDate().split("-");
                            int appointedYear = Integer.parseInt(appointedDateArray[0]);
                            int appointedMonth = Integer.parseInt(appointedDateArray[1]);
                            int appointedDay = Integer.parseInt(appointedDateArray[2]);
                            if(year >= appointedYear && month == appointedMonth && day > appointedDay){
                                reference.child(user.getUid()).child(model.getAppointmentID()).removeValue();
                                reference.child(doctorId).child(model.getAppointmentID()).removeValue();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
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

    public void proceedToVideoCall() {
        Intent intent = new Intent(SpecificDoctorInfo.this, CheckoutDoctor.class);
        intent.putExtra("doctorId", doctorId);
        intent.putExtra("doctorTitle", model.getTitle());
        String doctorName = model.getTitle() + model.getFullName();
        intent.putExtra("doctorName", doctorName);
        intent.putExtra("doctorDegree", model.getDegrees());
        intent.putExtra("doctorSpecialty", model.getSpecialty());
        intent.putExtra("doctorCurrentlyWorking", currentlyWorking);
        intent.putExtra("photoUrl", model.getPhotoUrl());
        intent.putExtra("consultationFee", model.getConsultationFee());
        if (!appointmentId.isEmpty()) {
            intent.putExtra("appointmentId", appointmentId);
        } else {
            intent.putExtra("appointmentId", "null");
        }
        startActivity(intent);
    }


    public String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public void checkAppointmentHistory() {
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        AppointmentModel model = snap.getValue(AppointmentModel.class);
                        assert model != null;
                        if (model.getDoctorID().equals(doctorId)) {
                            hasBooked = true;
                        }
                    }
                    if (hasBooked) {
                        errorMessage("Already Booked", "You have already booked appointment with this doctor,you will be able to book again when the previous one is finished or cancelled");
                        return;
                    }
                }
                Intent intent = new Intent(SpecificDoctorInfo.this, BookAppointment.class);
                intent.putExtra("doctorId", doctorId);
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void errorMessage(String title, String message) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(SpecificDoctorInfo.this);
        dialog.setTitle(title).setIcon(R.drawable.cross)
                .setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setCancelable(false);
        dialog.create().show();
    }

    public void recordFavourite() {
        binding.outlinedLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (toggleButton) {
                    binding.outlinedLove.setImageResource(R.drawable.filledlove);
                    toggleButton = false;
                    addFavourite();
                } else {
                    binding.outlinedLove.setImageResource(R.drawable.blanklove);
                    toggleButton = true;
                    removeFavourite();
                }
            }
        });
    }

    public void loadData() {
        DatabaseReference DoctorReference = firebase.getDatabaseReference("doctorInfo");
        DoctorReference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    model = snapshot.getValue(doctorInfoModel.class);
                    assert model != null;
                    String doctorName = model.getTitle() + model.getFullName();
                    binding.doctorName.setText(doctorName);
                    binding.doctorDegree.setText(model.getDegrees());
                    binding.doctorSpecialties.setText(model.getSpecialty());
                    binding.rating.setText(String.valueOf(model.getRating()));
                    binding.bmdc.setText(model.getBmdc());
                    binding.consultationFee.setText(model.getConsultationFee());
                    Glide.with(SpecificDoctorInfo.this).load(model.getPhotoUrl()).into(binding.profilePicture);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
        reference.child(doctorId).child("onlineStatus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    onlineStatus = String.valueOf(snapshot.getValue());
                    if (Integer.parseInt(onlineStatus) != 0) {
                        binding.onlineStatusBanner.setVisibility(View.VISIBLE);
                        binding.onlineStatus.setVisibility(View.VISIBLE);
                        binding.offlineStatusBanner.setVisibility(View.GONE);
                        binding.offlineStatus.setVisibility(View.GONE);
                    } else {
                        binding.offlineStatusBanner.setVisibility(View.VISIBLE);
                        binding.offlineStatus.setVisibility(View.VISIBLE);
                        binding.onlineStatusBanner.setVisibility(View.GONE);
                        binding.onlineStatus.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doctorInfoPageAdapter adapter = new doctorInfoPageAdapter(SpecificDoctorInfo.this, doctorId);
                binding.vPager.setAdapter(adapter);
                new TabLayoutMediator(binding.tabs, binding.vPager, ((tab, position) -> tab.setText(titles[position]))).attach();
            }
        });
    }

    public void getDoctorExperience() {
        ArrayList<Integer> periods = new ArrayList<>();
        DatabaseReference experienceReference = firebase.getDatabaseReference("doctorPastExperience");
        experienceReference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    joiningDates model = snap.getValue(joiningDates.class);
                    assert model != null;
                    leavingDates model2 = snap.getValue(leavingDates.class);
                    assert model2 != null;
                    String joiningDate = model.getJoiningDate();
                    String leavingDate = model2.getLeavingDate();
                    String[] splitText = joiningDate.split("/");
                    int year = Integer.parseInt(splitText[0]);
                    int month = Integer.parseInt(splitText[1]);
                    int day = Integer.parseInt(splitText[2]);
                    LocalDate beginningDay = LocalDate.of(year, month, day);
                    splitText = leavingDate.split("/");
                    year = Integer.parseInt(splitText[0]);
                    month = Integer.parseInt(splitText[1]);
                    day = Integer.parseInt(splitText[2]);
                    LocalDate endDay = LocalDate.of(year, month, day);
                    Period period = Period.between(beginningDay, endDay);
                    String years = String.valueOf(period.getYears());
                    periods.add(Integer.parseInt(years));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        DatabaseReference currentlyWorkingReference = firebase.getDatabaseReference("doctorCurrentlyWorking");
        currentlyWorkingReference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (!String.valueOf(snapshot.child("joiningDate").getValue()).equals("null")) {
                        currentlyWorking = String.valueOf(snapshot.child("hospitalName").getValue());
                        binding.currentlyWorking.setText(String.valueOf(snapshot.child("hospitalName").getValue()));
                        String date = String.valueOf(snapshot.child("joiningDate").getValue());
                        String[] splitText = date.split("/");
                        int year = Integer.parseInt(splitText[0]);
                        int month = Integer.parseInt(splitText[1]);
                        int day = Integer.parseInt(splitText[2]);
                        LocalDate bday = LocalDate.of(year, month, day);
                        LocalDate today = LocalDate.now();
                        Period period = Period.between(bday, today);
                        String years = String.valueOf(period.getYears());
                        periods.add(Integer.parseInt(years));
                        calculateExperience(periods);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void calculateExperience(ArrayList<Integer> periods) {
        int totalExperience = 0;
        for (int i = 0; i < periods.size(); i++) {
            totalExperience = totalExperience + periods.get(i);
        }
        String totalExperienceString = String.valueOf(totalExperience) + "+ years";
        binding.totalExperience.setText(totalExperienceString);
    }

    public void getFavouriteDoctorStatus() {
        DatabaseReference reference = firebase.getDatabaseReference("favouriteDoctors");
        reference.child(user.getUid()).child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.outlinedLove.setImageResource(R.drawable.filledlove);
                    toggleButton = false;
                } else {
                    toggleButton = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void addFavourite() {
        HashMap<String, String> data = new HashMap<>();
        data.put("doctorId", doctorId);
        data.put("photoUrl", model.getPhotoUrl());
        data.put("fullName", model.getFullName());
        data.put("title", model.getTitle());
        data.put("specialty", model.getSpecialty());
        DatabaseReference reference = firebase.getDatabaseReference("favouriteDoctors");
        reference.child(user.getUid()).child(doctorId).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(binding.parentLayout, model.getTitle() + " " + model.getFullName() + " added to your favourite list", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    public void removeFavourite() {
        DatabaseReference reference = firebase.getDatabaseReference("favouriteDoctors");
        reference.child(user.getUid()).child(doctorId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(binding.parentLayout, model.getTitle() + " " + model.getFullName() + " removed from your favourite list", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        doctorDataExecutor.shutdown();
        favouriteRecordExecutor.shutdown();
        doctorExperienceExecutor.shutdown();
        getFavouriteDoctorStatus.shutdown();
        appointmentExecutor.shutdown();
        videoCallAppointmentExecutor.shutdown();
        removeBookingExecutor.shutdown();
    }
}