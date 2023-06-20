package com.example.doctorbabu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    public static int SplashScreen = 3500;
    Animation fadein, bottomanim;
    ImageView image;
    TextView text1, text2;
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
        fadein = AnimationUtils.loadAnimation(this,R.anim.fadein);
        bottomanim =  AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        image = findViewById(R.id.splash_img);
        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        image.setAnimation(fadein);
        text1.setAnimation(bottomanim);
        text2.setAnimation(bottomanim);
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