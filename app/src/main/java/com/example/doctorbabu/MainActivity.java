package com.example.doctorbabu;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {
    public static int SplashScreen = 4000;
    Animation topAnim;
    ImageView image;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        image = findViewById(R.id.splash_img);
        image.setAnimation(topAnim);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this,Login.class);
            startActivity(intent);
            finish();
        }, SplashScreen);
    }
}