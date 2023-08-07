package com.example.doctorbabu.doctor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityDoctorPrescribeMedicineBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

public class DoctorPrescribeMedicine extends AppCompatActivity {
    String patientId;
    int id = 0, medicineHeaderCounter = 1;
    ArrayList<TextInputLayout> editTexts = new ArrayList<>();
    ActivityDoctorPrescribeMedicineBinding binding;
    TextInputLayout editTextLayout;
    TextView medicineAddHeader;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorPrescribeMedicineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        patientId = getIntent().getStringExtra("patientId");
        loadPatientData();
        binding.addBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBox();
            }
        });
        binding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void addBox() {
        final View addView = getLayoutInflater().inflate(R.layout.medicine_text_box, null, false);
        binding.medicineAddLayout.addView(addView);
        editTextLayout = addView.findViewById(R.id.medicineName);
        medicineAddHeader = addView.findViewById(R.id.medicineAddHeader);
        medicineAddHeader.setText("Enter Medicine " + (medicineHeaderCounter + 1) + " Details");
        editTextLayout.setId(id + 1);
        id++;
        medicineHeaderCounter++;
        editTexts.add(editTextLayout);
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
                binding.upload.setVisibility(View.VISIBLE);
            }
        }, 2500);
    }
}