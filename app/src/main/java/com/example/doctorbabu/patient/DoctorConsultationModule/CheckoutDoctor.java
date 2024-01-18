package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.AppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.databinding.ActivityCheckoutDoctorBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class CheckoutDoctor extends AppCompatActivity {
    String doctorId, doctorTitle, doctorName, doctorDegree, doctorSpecialty, doctorCurrentlyWorking, photoUrl, consultationFee, appointmentId;
    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    int requestCode = 1;
    ActivityCheckoutDoctorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        doctorId = getIntent().getStringExtra("doctorId");
        doctorTitle = getIntent().getStringExtra("doctorTitle");
        doctorName = getIntent().getStringExtra("doctorName");
        doctorDegree = getIntent().getStringExtra("doctorDegree");
        doctorSpecialty = getIntent().getStringExtra("doctorSpecialty");
        doctorCurrentlyWorking = getIntent().getStringExtra("doctorCurrentlyWorking");
        photoUrl = getIntent().getStringExtra("photoUrl");
        consultationFee = getIntent().getStringExtra("consultationFee");
        appointmentId = getIntent().getStringExtra("appointmentId");
        loadData();
        binding.back.setOnClickListener(view -> finish());
        binding.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkingAppointment();
            }
        });

    }

    public void checkingAppointment() {
        if (isPermissionGranted()) {
            if (!appointmentId.equalsIgnoreCase("null")) {
                removeBooking();
            } else {
                launchActivity();
            }

        } else {
            askPermission();
        }
    }

    public void removeBooking() {
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        reference.child(user.getUid()).child(appointmentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    AppointmentModel model = snapshot.getValue(AppointmentModel.class);
                    reference.child(user.getUid()).child(appointmentId).removeValue();
                    reference.child(doctorId).child(appointmentId).removeValue();
                    DatabaseReference counterReference = firebase.getDatabaseReference("appointmentPatient");
                    counterReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                int appointmentDone = Integer.parseInt(String.valueOf(snapshot.child("done").getValue()));
                                appointmentDone += 1;
                                counterReference.child("done").setValue(String.valueOf(appointmentDone));
                                DatabaseReference appointmentTakenReference = firebase.getDatabaseReference("takenAppointments");
                                String uniqueKey = getUniqueKey();
                                appointmentTakenReference.child(user.getUid()).child(uniqueKey).setValue(model);
                                appointmentTakenReference.child(doctorId).child(uniqueKey).setValue(model);
                                launchActivity();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            throw error.toException();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void launchActivity() {
        Intent intent = new Intent(CheckoutDoctor.this, DocumentUpload.class);
        intent.putExtra("doctorId", doctorId);
        intent.putExtra("doctorTitle", doctorTitle);
        intent.putExtra("doctorName", doctorName);
        intent.putExtra("photoUrl", photoUrl);
        startActivity(intent);
        finish();
    }

    public String getUniqueKey() {
        return UUID.randomUUID().toString();
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean isPermissionGranted() {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void loadData() {
        String name = doctorTitle + doctorName;
        binding.doctorName.setText(name);
        binding.doctorDegree.setText(doctorDegree);
        binding.doctorSpecialties.setText(doctorSpecialty);
        binding.currentlyWorking.setText(doctorCurrentlyWorking);

        binding.amount.setText(consultationFee);
        double vat = Double.parseDouble(consultationFee) * 0.05;
        binding.vatAmount.setText(String.valueOf(Math.round(vat)));
        double totalAmount = vat + Double.parseDouble(consultationFee);
        binding.totalAmount.setText(String.valueOf(Math.round(totalAmount)));
        Glide.with(this).load(photoUrl).into(binding.profilePicture);
    }
}