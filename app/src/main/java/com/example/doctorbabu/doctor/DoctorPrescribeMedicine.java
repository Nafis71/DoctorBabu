package com.example.doctorbabu.doctor;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityDoctorPrescribeMedicineBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.aviran.cookiebar2.CookieBar;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class DoctorPrescribeMedicine extends AppCompatActivity {
    String patientId, doctorId;
    int medicineHeaderCounter = 1;

    StringBuilder medicineNameBuilder = new StringBuilder();
    StringBuilder morningDozeBuilder = new StringBuilder();
    StringBuilder noonDozeBuilder = new StringBuilder();
    StringBuilder nightDozeBuilder = new StringBuilder();
    StringBuilder totalDaysBuilder = new StringBuilder();
    ArrayList<String> medicineName = new ArrayList<>();
    ArrayList<String> medicineGeneric = new ArrayList<>();
    ArrayList<String> morningDoze = new ArrayList<>();
    ArrayList<String> noonDoze = new ArrayList<>();
    ArrayList<String> nightDoze = new ArrayList<>();
    ArrayList<String> totalDays = new ArrayList<>();
    ActivityDoctorPrescribeMedicineBinding binding;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    int i =1;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorPrescribeMedicineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SharedPreferences preferences = getSharedPreferences("loginDetails", MODE_PRIVATE);
        doctorId = preferences.getString("doctorId", "");
        patientId = getIntent().getStringExtra("patientId");
        loadPatientData();
        binding.addBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBox();
            }
        });
        binding.proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.upload.setVisibility(View.VISIBLE);
                binding.proceed.setVisibility(View.GONE);
                binding.advice.setVisibility(View.VISIBLE);
                binding.medicineAddLayout.setVisibility(View.GONE);
            }
        });
        binding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadData();
            }
        });
    }
    public void onStart(){
        super.onStart();
    }

    @SuppressLint("SetTextI18n")
    public void addBox() {
        medicineName.add(binding.medicineName.getEditText().getText().toString());
        medicineGeneric.add(binding.medicineGeneric.getEditText().getText().toString());
        morningDoze.add(binding.morning.getEditText().getText().toString());
        noonDoze.add(binding.noon.getEditText().getText().toString());
        nightDoze.add(binding.night.getEditText().getText().toString());
        totalDays.add(binding.totalDays.getEditText().getText().toString());
        binding.medicineAddHeader.setText("Enter Medicine "+(medicineHeaderCounter++)+"Details");
        binding.medicineNameText.setText(null);
        binding.medicineGenericText.setText(null);
        binding.morningText.setText(null);
        binding.noonText.setText(null);
        binding.nightText.setText(null);
        binding.totalDaysText.setText(null);
        CookieBar.build(this)
                .setTitle(binding.medicineName.getEditText().getText().toString() + "Added")
                .setMessage("Please add another medicine's details")
                .setTitleColor(R.color.white)
                .setBackgroundColor(R.color.blue)
                .setCookiePosition(CookieBar.TOP)  // Cookie will be displayed at the bottom
                .show();

    }

    public void uploadData() {
        String uniqueID = getUniqueId();
        LocalDate date = LocalDate.now();
        HashMap<String, Object> prescription = new HashMap<>();
        DatabaseReference uploadReference = database.getReference();
        prescription.put("PrescribedBy",doctorId);
        prescription.put("PrescribedTo",patientId);
        prescription.put("date",date.toString());
        prescription.put("diagnosis",binding.diagnosis.getEditText().getText().toString().trim());
        prescription.put("advice",binding.advice.getEditText().getText().toString().trim());
        if(medicineName.size() == 0)
        {
            prescription.put("medicineName",binding.medicineName.getEditText().getText().toString());
            if(!binding.morning.getEditText().getText().toString().isEmpty())
            {
                prescription.put("morningDoze",binding.morning.getEditText().getText().toString());
            }else{
                prescription.put("morningDoze","null");
            }
            if(!binding.noon.getEditText().getText().toString().isEmpty())
            {
                prescription.put("noonDoze",binding.noon.getEditText().getText().toString());
            }else{
                prescription.put("noonDoze","null");
            }
            if(!binding.night.getEditText().getText().toString().isEmpty())
            {
                prescription.put("nightDoze",binding.night.getEditText().getText().toString());
            }else{
                prescription.put("nightDoze","null");
            }
            prescription.put("totalDays",binding.totalDays);
            uploadReference.child("prescription").child(patientId).child(uniqueID).setValue(prescription).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Snackbar.make(binding.rootLayout,"Prescription Uploaded",Snackbar.LENGTH_LONG).show();
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    },2000);
                }
            });
        }else{
            medicineName.add(binding.medicineName.getEditText().getText().toString());
            medicineGeneric.add(binding.medicineGeneric.getEditText().getText().toString());
            morningDoze.add(binding.morning.getEditText().getText().toString());
            noonDoze.add(binding.noon.getEditText().getText().toString());
            nightDoze.add(binding.night.getEditText().getText().toString());
            totalDays.add(binding.totalDays.getEditText().getText().toString());
            for(int i=0;i < medicineName.size();i++){
                if(medicineName.size() - 1 == 0)
                {
                    medicineNameBuilder.append(medicineName.get(i));
                    morningDozeBuilder.append(morningDoze.get(i));
                    noonDozeBuilder.append(noonDoze.get(i));
                    nightDozeBuilder.append(nightDoze.get(i));
                    totalDaysBuilder.append(totalDays.get(i));

                }else{
                    medicineNameBuilder.append(medicineName.get(i)).append(",");
                    morningDozeBuilder.append(morningDoze.get(i)).append(",");
                    noonDozeBuilder.append(noonDoze.get(i)).append(",");
                    nightDozeBuilder.append(nightDoze.get(i)).append(",");
                    totalDaysBuilder.append(totalDays.get(i)).append(",");
                }
            }
            prescription.put("medicineName",medicineNameBuilder.toString());
            prescription.put("morningDoze",morningDozeBuilder.toString());
            prescription.put("noonDoze",noonDozeBuilder.toString());
            prescription.put("nightDoze",nightDozeBuilder.toString());
            prescription.put("totalDays",totalDaysBuilder.toString());
        }
        uploadReference.child("prescription").child(patientId).child(uniqueID).setValue(prescription).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(binding.rootLayout,"Prescription Uploaded",Snackbar.LENGTH_LONG).show();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                },2000);
            }
        });
    }


    public void loadPatientData() {
        DatabaseReference userInfoReference = database.getReference("users");
        userInfoReference.child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    binding.patientName.setText(String.valueOf(snapshot.child("fullName").getValue()));
                    String dbheight = snapshot.child("height").getValue() + " CM";
                    binding.height.setText(dbheight);
                    String dbweight = snapshot.child("weight").getValue() + " KGS";
                    binding.weight.setText(dbweight);
                    String birthDate = String.valueOf(snapshot.child("birthDate").getValue());
                    String[] splitText = birthDate.split("/");
                    int year = Integer.parseInt(splitText[0]);
                    int month = Integer.parseInt(splitText[1]);
                    int day = Integer.parseInt(splitText[2]);
                    LocalDate from = LocalDate.of(year, month, day);
                    LocalDate to = LocalDate.now();
                    Period calculateAge = Period.between(from, to);
                    String calculatedYears = String.valueOf(calculateAge.getYears());
                    String years = calculatedYears + " years old";
                    binding.age.setText(years);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        DatabaseReference medicalHistoryReference = database.getReference("medicalhistory");
        ArrayList<String> medicalHistoryList = new ArrayList<>();
        medicalHistoryReference.child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        if (!String.valueOf(snap.getValue()).equals("null")) {
                            medicalHistoryList.add(String.valueOf(snap.getValue()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (medicalHistoryList.size() != 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < medicalHistoryList.size(); i++) {
                        if (medicalHistoryList.size() - 1 == i) {
                            stringBuilder.append(medicalHistoryList.get(i));
                        } else {
                            stringBuilder.append(medicalHistoryList.get(i)).append(",").append(" ");
                        }
                    }
                    binding.pastMedicalHistory.setText(stringBuilder.toString());
                } else {
                    binding.pastMedicalHistory.setText("N/A");
                }
            }
        }, 2000);
        DatabaseReference allergyReference = database.getReference("allergy");
        ArrayList<String> allergyList = new ArrayList<>();
        allergyReference.child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        if (!String.valueOf(snap.getValue()).equals("null")) {
                            allergyList.add(String.valueOf(snap.getValue()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (allergyList.size() != 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < allergyList.size(); i++) {
                        if (allergyList.size() - 1 == i) {
                            stringBuilder.append(allergyList.get(i));
                        } else {
                            stringBuilder.append(allergyList.get(i)).append(",").append(" ");
                        }
                        binding.allergy.setText(stringBuilder.toString());
                    }
                } else {
                    binding.allergy.setText("N/A");
                }
            }
        }, 2000);

        DatabaseReference bloodGroupReference = database.getReference("bloodgroup");
        bloodGroupReference.child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (!String.valueOf(snapshot.child("group")).equals("null")) {
                        binding.bloodGroup.setText(String.valueOf(snapshot.child("group").getValue()));
                    } else {
                        binding.bloodGroup.setText("N/A");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.patientInfoCard.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
                binding.lottieAnimation.setVisibility(View.GONE);
                binding.medicineAddLayout.setVisibility(View.VISIBLE);
                binding.addBox.setVisibility(View.VISIBLE);
                binding.proceed.setVisibility(View.VISIBLE);
            }
        }, 2500);
    }

    public String getUniqueId() {
        return UUID.randomUUID().toString();
    }
}