package com.example.doctorbabu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

public class DoctorLogin extends AppCompatActivity {
    TextInputLayout email,password;
    TextView forgetPass, signUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_login);
        viewBinding();
    }
    public void viewBinding()
    {
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        forgetPass = findViewById(R.id.forgetPass);
        signUp = findViewById(R.id.newUser);
    }
}