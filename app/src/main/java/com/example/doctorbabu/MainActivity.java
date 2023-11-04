package com.example.doctorbabu;

import android.Manifest;
import android.app.Activity;
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
import android.provider.Settings;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.doctorbabu.permissionClass.AppPermission;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;


@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class MainActivity extends AppCompatActivity {
    public static int SplashScreen = 3500;
    private final int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1;
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

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        checkForBatteryOptimizations();
                    } else {
                        loadNext();
                    }
                }
            });

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
        checkForBatteryOptimizations();

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

    private void checkForBatteryOptimizations() {
        AppPermission appPermission = AppPermission.createInstance(this);
        if(appPermission.isBatteryOptimizationsEnabled()){
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
            dialog.setTitle("Warning").setIcon(R.drawable.warning)
                    .setMessage("Battery optimization is enabled. It can interrupt running background services.")
                    .setPositiveButton("Disable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                            activityResultLauncher.launch(intent);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            loadNext();
                        }
                    }).setCancelable(false);
            dialog.create().show();
        } else {
            loadNext();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS) {
            checkForBatteryOptimizations();
        }
    }


}