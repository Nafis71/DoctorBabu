package com.example.doctorbabu;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

public class DoctorLogin extends AppCompatActivity {
    TextInputLayout email,password;
    TextView greetingText,secondGreetingText;
    ImageView image;
    Button signIn,signUp,forgetPass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_login);
        viewBinding();
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callSignUp(view);
            }
        });
    }
    public void viewBinding()
    {
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        forgetPass = findViewById(R.id.forgetPass);
        signUp = findViewById(R.id.newUser);
        image = findViewById(R.id.logo);
        greetingText = findViewById(R.id.text1);
        secondGreetingText = findViewById(R.id.text2);
        signIn = findViewById(R.id.signIn);

    }
    public void callSignUp(View view)
    {
        Intent intent = new Intent(DoctorLogin.this,DoctorSignup.class);
        Pair[] pairs = new Pair[7];
        pairs[0] = new Pair<View,String>(image, "imageTransition");
        pairs[1] = new Pair<View,String>(greetingText, "textTransition1");
        pairs[2] = new Pair<View,String>(secondGreetingText, "textTransition2");
        pairs[3] = new Pair<View,String>(email, "usernameTransition");
        pairs[4] = new Pair<View,String>(password, "passwordTransition");
        pairs[5] = new Pair<View,String>(signIn, "signinTransition");
        pairs[6] = new Pair<View,String>(signUp, "signupTransition");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(DoctorLogin.this,pairs);
        startActivity(intent, options.toBundle());
    }
    public void onBackPressed()
    {
        Intent intent = new Intent(DoctorLogin.this,LoginOptions.class);
        startActivity(intent);
        finish();
    }
}