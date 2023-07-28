package com.example.doctorbabu;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;


@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class MainActivity extends AppCompatActivity {
    public static int SplashScreen = 3500;
    Animation fadein, bottomanim;
    ImageView image;
    TextView text1, text2;
    String language;
    int requestCode = 1;
    String[] permissions = new String[]{android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS};

    public void onStart() {
        super.onStart();
        SharedPreferences preferences = getSharedPreferences("language", Context.MODE_PRIVATE);
        language = preferences.getString("lang", "");
        if (!language.isEmpty()) {
            setLanguage(language);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        bottomanim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation);
        image = findViewById(R.id.splash_img);
        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        image.setAnimation(fadein);
        text1.setAnimation(bottomanim);
        text2.setAnimation(bottomanim);
        loadNext();
    }

    public void loadNext() {
        new Handler().postDelayed(() -> {
            if (isPermissionGranted()) {
                Intent intent = new Intent(MainActivity.this, LoginOptions.class);
                startActivity(intent);
                finish();
            } else {
                askPermission();
                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(MainActivity.this);
                dialog.setTitle("Permission").setIcon(R.drawable.done)
                        .setMessage("Please accept all permissions to continue")
                        .setPositiveButton("Okay", (dialogInterface, i) -> loadNext()).setCancelable(false);
                dialog.create().show();
            }
        }, SplashScreen);
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

    public void setLanguage(String language) {
        Resources resources = this.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}