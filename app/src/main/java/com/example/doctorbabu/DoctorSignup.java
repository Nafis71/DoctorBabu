package com.example.doctorbabu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class DoctorSignup extends AppCompatActivity {

    AutoCompleteTextView medicalSpecialty;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_signup);
        viewBinding();
        medicalSpecialtySelection();
    }
    public void viewBinding()
    {
        medicalSpecialty = findViewById(R.id.medicalSpecialty);
    }
    public void medicalSpecialtySelection()
    {
            String  [] items = {"General Physician", "Gynecology","Pediatrics","Dermatology","Psychiatry","Psychology","Nutrition &amp; Dietetic","Ophthalmology","Neurology"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorSignup.this,R.layout.medical_specialty_menu, items);
            medicalSpecialty.setAdapter(adapter);
    }
}