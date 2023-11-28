package com.example.doctorbabu.Adapters;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.DatabaseModels.AlarmListModel;
import com.example.doctorbabu.R;
import com.example.doctorbabu.SqliteDatabase.SqliteDatabase;
import com.example.doctorbabu.patient.AlarmModules.AlarmReceiver;
import com.example.doctorbabu.patient.AlarmModules.MedicineAlarm;
import com.google.android.material.card.MaterialCardView;

import org.aviran.cookiebar2.CookieBar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class alarmListAdapter extends RecyclerView.Adapter<alarmListAdapter.myViewHolder> {

    Context context;
    ArrayList<AlarmListModel> alarmListModel;
    Calendar calendar;
    AlarmManager alarmManager;
    Animation bottomAnimation;
    ActivityResultLauncher<Intent> activityResultLauncher;
    SelectedCard selectedCard;
    boolean isChecked;
    ImageView delete;


    public alarmListAdapter(Context context, ArrayList<AlarmListModel> alarmListModel, ActivityResultLauncher<Intent> activityResultLauncher, SelectedCard selectedCard, ImageView delete) {
        this.context = context;
        this.alarmListModel = alarmListModel;
        this.activityResultLauncher = activityResultLauncher;
        this.selectedCard = selectedCard;
        this.delete = delete;
    }

    @NonNull
    @Override
    public alarmListAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_alarm_list, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull alarmListAdapter.myViewHolder holder, int position) {
        AlarmListModel model = alarmListModel.get(position);
        String time = getTime(model);
        holder.time.setText(time);
        holder.medicineName.setText(model.getMedicineName());
        holder.alarmType.setText(getAlarmType(model));
        holder.activateSwitch.setChecked(model.getAlarmStatus() == 1);
        holder.activateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    restartAlarm(model);
                } else {
                    cancelAlarm(model);
                }
            }
        });

        holder.card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                longClickAction(model, holder);
                return true;
            }
        });


        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                singleClickAction(model, holder);
            }
        });

    }

    @Override
    public int getItemCount() {
        return alarmListModel.size();
    }

    public String getTime(AlarmListModel model) {
        String pattern = "00";
        DecimalFormat numberFormatter = new DecimalFormat(pattern);
        String minute = numberFormatter.format(model.getMinute());
        if (model.getHour() > 12) {
            return (model.getHour() - 12) + ":" + minute + " PM";
        } else if (model.getHour() == 0) {
            return 12 + ":" + minute + " AM";
        } else if (model.getHour() == 12) {
            return (model.getHour()) + ":" + minute + " PM";
        } else {
            return model.getHour() + ":" + minute + " AM";
        }
    }

    public String getAlarmType(AlarmListModel model) {
        if (model.getAlarmType().equalsIgnoreCase("once")) {
            return "Once";
        } else {
            return "Every Day";
        }
    }

    public void editAlarm(AlarmListModel model) {
        Intent intent = new Intent(context, MedicineAlarm.class);
        intent.putExtra("id", model.getId());
        activityResultLauncher.launch(intent);
    }

    public void cancelAlarm(AlarmListModel model) {
        AppCompatActivity activity = (AppCompatActivity) context;
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, model.getBroadcastCode(), intent, PendingIntent.FLAG_MUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        try (SqliteDatabase database = new SqliteDatabase(context)) {
            database.deactivateAlarm(model.getId());
            database.updateAlarmStatus(model.getId(), 0);
            setCookieBar(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCookieBar(AppCompatActivity activity) {
        CookieBar.build(activity)
                .setDuration(2500)
                .setSwipeToDismiss(true)
                .setTitle("Alarm")
                .setMessage("Alarm disabled successfully")
                .setTitleColor(R.color.white)
                .setBackgroundColor(R.color.blue)
                .setCookiePosition(CookieBar.TOP)  // Cookie will be displayed at the Top
                .show();
    }

    public void restartAlarm(AlarmListModel model) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("medicineName", model.getMedicineName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, model.getBroadcastCode(), intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        setCalender(model);
        if (getAlarmType(model).equalsIgnoreCase("once")) {
            setNonRepeatedAlarm(pendingIntent, intent, model);
        } else {
            setRepeatedAlarm(pendingIntent, intent, model);
        }
    }

    public void setCalender(AlarmListModel model) {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, model.getHour());
        calendar.set(Calendar.MINUTE, model.getMinute());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public void setNonRepeatedAlarm(PendingIntent pendingIntent, Intent intent, AlarmListModel model) {
        intent.putExtra("id", model.getId());
        intent.putExtra("alarmType", "once");
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        try (SqliteDatabase database = new SqliteDatabase(context)) {
            database.updateAlarmStatus(model.getId(), 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRepeatedAlarm(PendingIntent pendingIntent, Intent intent, AlarmListModel model) {
        intent.putExtra("alarmType", "repeated");
        intent.putExtra("id", model.getId());
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        try (SqliteDatabase database = new SqliteDatabase(context)) {
            database.updateAlarmStatus(model.getId(), 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void longClickAction(AlarmListModel model, myViewHolder holder) {
        if (!isChecked) {
            if(holder.activateSwitch.isChecked()){
                Toast.makeText(context, "Please disable the alarm first", Toast.LENGTH_LONG).show();
                return;
            }
            selectedCard.cards.add(model.getId());
            holder.checkImage.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            isChecked = true;
        }
    }

    public void singleClickAction(AlarmListModel model, myViewHolder holder) {
        if (isChecked) {
            if(holder.activateSwitch.isChecked()){
                Toast.makeText(context, "Please disable the alarm first", Toast.LENGTH_LONG).show();
                return;
            }
            if (selectedCard.cards.contains(model.getId())) {
                selectedCard.cards.remove(model.getId());
                holder.checkImage.setVisibility(View.GONE);
            } else {
                selectedCard.cards.add(model.getId());
                holder.checkImage.setVisibility(View.VISIBLE);
            }
            int size = selectedCard.cards.size();
            if (size == 0) {
                isChecked = false;
                delete.setVisibility(View.GONE);
            }

        } else {
            editAlarm(model);
        }
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView time, medicineName, alarmType;
        com.google.android.material.switchmaterial.SwitchMaterial activateSwitch;
        ImageView checkImage;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            time = itemView.findViewById(R.id.time);
            medicineName = itemView.findViewById(R.id.medicineName);
            alarmType = itemView.findViewById(R.id.alarmType);
            activateSwitch = itemView.findViewById(R.id.activateSwitch);
            checkImage = itemView.findViewById(R.id.checkImage);
            bottomAnimation = AnimationUtils.loadAnimation(context, R.anim.alarm_list_top_animation);
            card.setAnimation(bottomAnimation);
        }
    }


}
