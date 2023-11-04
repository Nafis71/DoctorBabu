package com.example.doctorbabu.patient;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.WindowManager;

import com.ebanx.swipebtn.OnStateChangeListener;
import com.example.doctorbabu.Databases.alarmListModel;
import com.example.doctorbabu.R;
import com.example.doctorbabu.SqliteDatabase.SqliteDatabase;
import com.example.doctorbabu.databinding.ActivityMedicineAlarmDestinationBinding;

public class MedicineAlarmDestination extends AppCompatActivity {
    ActivityMedicineAlarmDestinationBinding binding;
    MediaPlayer mediaPlayer;
    Vibrator vibrator;
    String alarmID;
    boolean isRinging,isVibrating,fromNotification = true;
    alarmListModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineAlarmDestinationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getIntentData();

        binding.cancelAlarm.setOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(boolean active) {
                stopRingtone();
                stopVibration();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                },800);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        getMedicineName();
        playRingtone();
        vibrateDevice();
    }


    public void getIntentData(){
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            alarmID = bundle.getString("alarmID");
            fromNotification = bundle.getBoolean("fromNotification");
        }

    }

    public void getMedicineName(){
        try(SqliteDatabase database = new SqliteDatabase(this)) {
            model = database.readSpecificData(alarmID);
            binding.medicineName.setText(model.getMedicineName());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void playRingtone(){
        if(!isRinging && !fromNotification){
            mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
            mediaPlayer.start();
            isRinging = true;
        }

    }
    public void stopRingtone(){
        mediaPlayer.stop();
    }
    public void vibrateDevice(){
        if(!isVibrating && !fromNotification){
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VibrationEffect.createOneShot(60000, VibrationEffect.DEFAULT_AMPLITUDE));
            isVibrating = true;
        }

    }
    public void stopVibration(){
        vibrator.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}