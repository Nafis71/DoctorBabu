package com.example.doctorbabu.doctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.example.doctorbabu.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

public class DoctorPrescribeMedicine extends AppCompatActivity {
    LinearProgressIndicator progressBar;
    TextView patientName, pastMedicalHistory, allergy, bloodGroup, height, weight, age;
    CardView patientInfoCard;
    String patientId;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_prescribe_medicine);
        patientId = getIntent().getStringExtra("patientId");
        viewBinding();
        progressBar.setVisibility(View.VISIBLE);
        loadPatientData();
    }

    public void viewBinding() {
        patientInfoCard = findViewById(R.id.patientInfoCard);
        patientName = findViewById(R.id.patientName);
        pastMedicalHistory = findViewById(R.id.pastMedicalHistory);
        allergy = findViewById(R.id.allergy);
        bloodGroup = findViewById(R.id.bloodGroup);
        height = findViewById(R.id.height);
        weight = findViewById(R.id.weight);
        age = findViewById(R.id.age);
        progressBar = findViewById(R.id.progressBar);
    }

    public void loadPatientData() {
        DatabaseReference userInfoReference = database.getReference("users");
        userInfoReference.child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    patientName.setText(String.valueOf(snapshot.child("fullName").getValue()));
                    String dbheight = snapshot.child("height").getValue() + " CM";
                    height.setText(dbheight);
                    String dbweight = snapshot.child("weight").getValue() + " KGS";
                    weight.setText(dbweight);
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
                    age.setText(years);
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
                    pastMedicalHistory.setText(stringBuilder.toString());
                } else {
                    pastMedicalHistory.setText("N/A");
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
                        allergy.setText(stringBuilder.toString());
                    }
                } else {
                    allergy.setText("N/A");
                }
            }
        }, 2000);

        DatabaseReference bloodGroupReference = database.getReference("bloodgroup");
        bloodGroupReference.child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (!String.valueOf(snapshot.child("group")).equals("null")) {
                        bloodGroup.setText(String.valueOf(snapshot.child("group").getValue()));
                    } else {
                        bloodGroup.setText("N/A");
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
                patientInfoCard.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }, 2500);
    }
}