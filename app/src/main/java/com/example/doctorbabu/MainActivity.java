package com.example.doctorbabu;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    public static int SplashScreen = 3000;
    Animation topAnim;
    ImageView image;
    String language;
    public void onStart()
    {
        super.onStart();
        SharedPreferences preferences = getSharedPreferences("language", Context.MODE_PRIVATE);
         language = preferences.getString("lang","");
         if(!language.isEmpty())
         {
             setLanguage(language);
         }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        image = findViewById(R.id.splash_img);
        image.setAnimation(topAnim);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this,LoginOptions.class);
            startActivity(intent);
            finish();
        }, SplashScreen);
    }
    public void setLanguage(String language)
    {
        Resources resources = this.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration,resources.getDisplayMetrics());
    }
}