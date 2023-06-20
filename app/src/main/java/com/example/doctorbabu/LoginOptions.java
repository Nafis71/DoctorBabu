package com.example.doctorbabu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.doctorbabu.doctor.DoctorDashboard;
import com.example.doctorbabu.doctor.DoctorLogin;
import com.example.doctorbabu.patient.Dashboard;
import com.example.doctorbabu.patient.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pl.droidsonroids.gif.GifImageView;

public class LoginOptions extends AppCompatActivity {
    Animation topAnim,bottomAnim;
    ImageView doctorIMG;
    TextView helloText;
    CardView patientCard,doctorCard;
    GifImageView helloGif;
    public void onStart()
    {
        super.onStart();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if(user!=null)
        {
            SharedPreferences preferences = getSharedPreferences("loginDetails",MODE_PRIVATE);
            String login = preferences.getString("loginAs","");
            if(login.equals("patient"))
            {
                Intent intent = new Intent(LoginOptions.this, Dashboard.class);
                startActivity(intent);
                finish();
            }
            if(login.equals("doctor"))
            {
                Intent intent = new Intent(LoginOptions.this, DoctorDashboard.class);
                startActivity(intent);
                finish();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_options);
        viewBinding();
        patientCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginOptions.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
        doctorCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginOptions.this, DoctorLogin.class);
                startActivity(intent);
                finish();
            }
        });

    }
    public void viewBinding()
    {
        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        doctorIMG = findViewById(R.id.doctorIMG);
        helloText = findViewById(R.id.text1);
        patientCard = findViewById(R.id.patientCard);
        doctorCard= findViewById(R.id.doctorCard);
        helloGif = findViewById(R.id.helloGif);
        doctorIMG.setAnimation(topAnim);
        helloText.setAnimation(bottomAnim);
        patientCard.setAnimation(bottomAnim);
        doctorCard.setAnimation(bottomAnim);
        helloGif.setAnimation(topAnim);
    }
}