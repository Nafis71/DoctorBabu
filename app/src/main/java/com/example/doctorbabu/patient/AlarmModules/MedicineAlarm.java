package com.example.doctorbabu.patient.AlarmModules;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorbabu.DatabaseModels.alarmListModel;
import com.example.doctorbabu.R;
import com.example.doctorbabu.SqliteDatabase.SqliteDatabase;
import com.example.doctorbabu.databinding.ActivityMedicineAlarmBinding;
import com.example.doctorbabu.permissionClass.AppPermission;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Random;

public class MedicineAlarm extends AppCompatActivity {     //This activity does the both edit and add alarm function (Two in one)
    ActivityMedicineAlarmBinding binding;
    MaterialTimePicker timePicker;
    Calendar calendar;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    AlarmReceiver alarmReceiver = new AlarmReceiver();
    alarmListModel model;
    String id;
    int hour, minute;
    boolean isEditMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineAlarmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        createNotificationChannel();
        medicineNameTextWatcher();
        if (getIntent().hasExtra("id")) {
            id = getIntent().getStringExtra("id");
            isEditMode = true;
        } else {
            isEditMode = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isEditMode) {
            editAlarmMode();
        } else {
            addAlarmMode();
        }

    }    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    checkOverlayPermission(isEditMode);
                }
            });

    public void addAlarmMode() {
        binding.timePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });
        binding.setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkOverlayPermission(isEditMode);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void editAlarmMode() {
        binding.setAlarmButton.setText("Update Alarm");
        model = new alarmListModel();
        getSpecificAlarmData();
        binding.timePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker(model);
            }
        });
        binding.setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkOverlayPermission(isEditMode);
            }
        });

    }

    public void getSpecificAlarmData() {
        try (SqliteDatabase database = new SqliteDatabase(this)) {
            model = database.readSpecificData(id);
            hour = model.getHour();
            minute = model.getMinute();
            uiElementUpdater(model);
            setTimeInCalender();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showTimePicker() {                           //for add the alarm
        timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Select Alarm Time")
                .build();
        timePicker.show(getSupportFragmentManager(), "alarm");
        timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                hour = timePicker.getHour();
                minute = timePicker.getMinute();
                if (isPM()) {
                    binding.AmPm.setText("PM");
                    binding.hour.setText(String.valueOf(hour - 12));

                } else if (hour == 0) {
                    binding.AmPm.setText("AM");
                    binding.hour.setText(String.valueOf(12));
                } else if (hour == 12) {
                    binding.AmPm.setText("PM");
                    binding.hour.setText(String.valueOf(hour));
                } else {
                    binding.AmPm.setText("AM");
                    binding.hour.setText(String.valueOf(hour));
                }
                binding.minute.setText(String.valueOf(timePicker.getMinute()));
                setTimeInCalender();
            }
        });
    }

    public void uiElementUpdater(alarmListModel model) {
        if (hour > 12) {
            binding.AmPm.setText("PM");
            binding.hour.setText(String.valueOf(model.getHour() - 12));
        } else if (hour == 0) {
            binding.AmPm.setText("AM");
            binding.hour.setText("12");
        } else {
            binding.AmPm.setText("AM");
            binding.hour.setText(String.valueOf(hour));
        }
        binding.minute.setText(String.valueOf(minute));
        binding.medicineName.setText(model.getMedicineName());
        binding.headerText.setText("Update");
        setRadioButton(model);
    }

    public void setRadioButton(alarmListModel model) {
        if (model.getAlarmType().equalsIgnoreCase("Once")) {
            binding.once.setChecked(true);
        } else {
            binding.daily.setChecked(true);
        }
    }

    public void showTimePicker(alarmListModel model) {               //for update the alarm
        timePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(model.getHour())
                .setMinute(model.getMinute())
                .setTitleText("Select Alarm Time")
                .build();
        timePicker.show(getSupportFragmentManager(), "alarm");
        timePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                hour = timePicker.getHour();
                minute = timePicker.getMinute();
                if (isPM()) {
                    binding.AmPm.setText("PM");
                    binding.hour.setText(String.valueOf(hour - 12));
                } else if (hour == 0) {
                    binding.AmPm.setText("AM");
                    binding.hour.setText(String.valueOf(12));
                } else if (hour == 12) {
                    binding.AmPm.setText("PM");
                    binding.hour.setText(String.valueOf(hour));
                } else {
                    binding.AmPm.setText("AM");
                    binding.hour.setText(String.valueOf(hour));
                }
                binding.minute.setText(String.valueOf(minute));
                setTimeInCalender();
            }
        });
    }

    public boolean isPM() {
        return timePicker.getHour() > 12;
    }

    public void setTimeInCalender() {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public void setAlarm() {
        if (!validateMedicineName()) {
            return;
        }
        binding.setAlarmButton.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        int broadcastCode = getBroadcastCode();
        String medicineName = binding.medicineName.getText().toString().trim();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, alarmReceiver.getClass());
        intent.putExtra("medicineName", medicineName);
        if (binding.daily.isChecked()) {
            setRepeatedAlarm(medicineName, broadcastCode, intent);
        } else {
            setNonRepeatedAlarm(medicineName, broadcastCode, intent);
        }
    }

    public void checkOverlayPermission(boolean isEditMode) {
        AppPermission appPermission = AppPermission.createInstance(this);
        if (!appPermission.canDrawOverlays()) {
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
            dialog.setTitle("Warning").setIcon(R.drawable.warning)
                    .setMessage("Doctor Babu needs permission to draw over other apps to set an alarm")
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                            activityResultLauncher.launch(intent);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).setCancelable(false);
            dialog.create().show();
        } else {
            if (isEditMode) {
                updateAlarm(model);
            } else {
                setAlarm();
            }

        }
    }

    public void updateAlarm(alarmListModel model) {
        if (!validateMedicineName()) {
            return;
        }
        binding.setAlarmButton.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
        int broadcastCode = model.getBroadcastCode();
        String medicineName = binding.medicineName.getText().toString().trim();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, alarmReceiver.getClass());
        intent.putExtra("medicineName", medicineName);
        if (binding.daily.isChecked()) {
            setRepeatedAlarm(medicineName, broadcastCode, intent);
        } else {
            setNonRepeatedAlarm(medicineName, broadcastCode, intent);
        }
    }

    public int getBroadcastCode() {
        alarmReceiver.setBroadcastCode();
        return alarmReceiver.getBroadcastCode();
    }

    public void setRepeatedAlarm(String medicineName, int broadcastCode, Intent intent) {
        setPendingIntentRepeatedAlarm(intent, broadcastCode, isEditMode, medicineName);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    public void setNonRepeatedAlarm(String medicineName, int broadcastCode, Intent intent) {
        setPendingIntentNonRepeatedAlarm(intent, broadcastCode, isEditMode, medicineName);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

    }

    public void setPendingIntentNonRepeatedAlarm(Intent intent, int broadcastCode, boolean isEditMode, String medicineName) {
        intent.putExtra("alarmType", "once");
        if (isEditMode) {
            intent.putExtra("id", model.getId());
            updateData(model.getId(), medicineName, hour, minute, broadcastCode, "once", 1);
        } else {
            String id = getUniqueID();
            intent.putExtra("id", id);
            saveData(id, medicineName, hour, minute, broadcastCode, "once", 1);
        }
        pendingIntent = PendingIntent.getBroadcast(this, broadcastCode, intent, PendingIntent.FLAG_MUTABLE);
    }

    public void setPendingIntentRepeatedAlarm(Intent intent, int broadcastCode, boolean isEditMode, String medicineName) {
        intent.putExtra("alarmType", "repeated");
        if (isEditMode) {
            intent.putExtra("id", model.getId());
            updateData(model.getId(), medicineName, hour, minute, broadcastCode, "repeat", 1);
        } else {
            String id = getUniqueID();
            intent.putExtra("id", id);
            saveData(id, medicineName, hour, minute, broadcastCode, "repeat", 1);
        }
        pendingIntent = PendingIntent.getBroadcast(this, broadcastCode, intent, PendingIntent.FLAG_MUTABLE);
    }

    public void cancelAlarm(int broadcastCode) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, broadcastCode, intent, PendingIntent.FLAG_MUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    public void saveData(String id, String medicineName, int hour, int minute, int broadcastCode, String alarmType, int alarmStatus) {
        try (SqliteDatabase database = new SqliteDatabase(this)) {
            long result = database.addAlarmInfo(id, medicineName, hour, minute, broadcastCode, alarmType, alarmStatus);
            if (result != -1) {
                postOperation();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }, 3300);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateData(String id, String medicineName, int hour, int minute, int broadcastCode, String alarmType, int alarmStatus) {
        try (SqliteDatabase database = new SqliteDatabase(this)) {
            database.updateAlarm(id, medicineName, hour, minute, broadcastCode, alarmType, alarmStatus);
            postOperation();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }, 3300);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void postOperation() {
        binding.medicineName.setText(null);
        binding.hour.setText(null);
        binding.minute.setText(null);
        binding.AmPm.setText(null);
        String pattern = "00";
        DecimalFormat numberFormatter = new DecimalFormat(pattern);
        String formattedMinute = numberFormatter.format(minute);
        String formattedHour = numberFormatter.format(hour);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.progressBar.setVisibility(View.INVISIBLE);
                Snackbar.make(binding.parentLayout, "Alarm set for "+formattedHour+" :"+formattedMinute+" "+binding.AmPm.getText().toString(), 3000).show();

                Handler secondHandler = new Handler();
                secondHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.setAlarmButton.setVisibility(View.VISIBLE);
                    }
                }, 2000);
            }
        }, 1100);
    }

    public String getUniqueID() {
        Random random = new Random();
        return String.valueOf(random.nextInt((25000 - 330) + 1));
    }

    public boolean validateMedicineName() {
        String medicineName = binding.medicineName.getText().toString().trim();
        if (medicineName.isEmpty()) {
            binding.medicineNameLayout.setError("Please add your medicine name first!");
            return false;
        } else {
            return true;
        }
    }

    public void medicineNameTextWatcher() {
        binding.medicineName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.medicineNameLayout.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void createNotificationChannel() {
        CharSequence name = "MedicineReminderChannel";
        String description = "Channel for Alarm Manager";
        NotificationChannel channel = new NotificationChannel("medicineAlarm", name, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}