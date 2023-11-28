package com.example.doctorbabu.patient.AlarmModules;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

import com.ebanx.swipebtn.OnStateChangeListener;
import com.example.doctorbabu.DatabaseModels.AlarmListModel;
import com.example.doctorbabu.SqliteDatabase.SqliteDatabase;
import com.example.doctorbabu.databinding.ActivityMedicineAlarmDestinationBinding;

public class MedicineAlarmDestination extends AppCompatActivity {
    ActivityMedicineAlarmDestinationBinding binding;
    MediaPlayer mediaPlayer;
    Vibrator vibrator;
    String alarmID;
    boolean isRinging,isVibrating,fromNotification = true;
    AlarmListModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineAlarmDestinationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
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
            mediaPlayer.start();
            isRinging = true;
        }

    }
    public void stopRingtone(){
        mediaPlayer.stop();
    }
    public void vibrateDevice(){
        if(!isVibrating && !fromNotification){
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