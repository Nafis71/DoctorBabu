package com.example.doctorbabu.Databases;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.R;
import com.example.doctorbabu.SqliteDatabase.SqliteDatabase;
import com.example.doctorbabu.patient.AlarmReceiver;
import com.example.doctorbabu.patient.MedicineAlarm;
import com.google.android.material.card.MaterialCardView;

import org.aviran.cookiebar2.CookieBar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class alarmListAdapter extends RecyclerView.Adapter<alarmListAdapter.myViewHolder> {

    Context context;
    ArrayList<alarmListModel> alarmListModel;
    Calendar calendar;
    AlarmManager alarmManager;
    public alarmListAdapter(Context context, ArrayList<alarmListModel> alarmListModel){
        this.context = context;
        this.alarmListModel = alarmListModel;
    }
    @NonNull
    @Override
    public alarmListAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_alarm_list,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull alarmListAdapter.myViewHolder holder, int position) {
        alarmListModel model = alarmListModel.get(position);
        String time = getTime(model);
        holder.time.setText(time);
        holder.medicineName.setText(model.getMedicineName());
        holder.alarmType.setText(getAlarmType(model));
        holder.activateSwitch.setChecked(model.getAlarmStatus() == 1);
        holder.activateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    restartAlarm(model);
                } else{
                    cancelAlarm(model);
                }
            }
        });
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editAlarm(model);
            }
        });

    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        MaterialCardView card;
        TextView time,medicineName,alarmType;
        com.google.android.material.switchmaterial.SwitchMaterial activateSwitch;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card);
            time = itemView.findViewById(R.id.time);
            medicineName = itemView.findViewById(R.id.medicineName);
            alarmType = itemView.findViewById(R.id.alarmType);
            activateSwitch = itemView.findViewById(R.id.activateSwitch);
        }
    }

    @Override
    public int getItemCount() {
        return alarmListModel.size();
    }
    public String getTime(alarmListModel model){
        String pattern = "00";
        DecimalFormat numberFormatter = new DecimalFormat(pattern);
        String minute = numberFormatter.format(model.getMinute());
        if(model.getHour() > 12){
            return (model.getHour() - 12) +":"+ minute +" PM";
        } else if (model.getHour() == 0){
            return 12 +":"+ minute +" AM";
        } else {
            return model.getHour()+":"+ minute +" AM";
        }
    }
    public String getAlarmType(alarmListModel model){
        if(model.getAlarmType().equalsIgnoreCase("once")){
            return "Once";
        } else {
            return "Every Day";
        }
    }
    public void editAlarm(alarmListModel model){
        Intent intent = new Intent(context, MedicineAlarm.class);
        intent.putExtra("id",model.getId());
        context.startActivity(intent);
    }
    public void cancelAlarm(alarmListModel model){
        AppCompatActivity activity = (AppCompatActivity) context;
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, model.getBroadcastCode(), intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        try(SqliteDatabase database = new SqliteDatabase(context)){
            database.deactivateAlarm(model.getId());
            database.updateAlarmStatus(model.getId(),0);
            CookieBar.build(activity)
                    .setDuration(2500)
                    .setSwipeToDismiss(true)
                    .setTitle("Alarm")
                    .setMessage("Alarm disabled successfully")
                    .setTitleColor(R.color.white)
                    .setBackgroundColor(R.color.blue)
                    .setCookiePosition(CookieBar.TOP)  // Cookie will be displayed at the Top
                    .show();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void restartAlarm(alarmListModel model){
        Intent intent = new Intent(context,AlarmReceiver.class);
        intent.putExtra("medicineName", model.getMedicineName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,model.getBroadcastCode(),intent,PendingIntent.FLAG_IMMUTABLE);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        setCalender(model);
        if(getAlarmType(model).equalsIgnoreCase("once")){
            setNonRepeatedAlarm(pendingIntent,intent,model);
        } else {
            setRepeatedAlarm(pendingIntent,intent,model);
        }
    }
    public void setCalender(alarmListModel model){
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,model.getHour());
        calendar.set(Calendar.MINUTE,model.getMinute());
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
    }
    public void setNonRepeatedAlarm(PendingIntent pendingIntent,Intent intent,alarmListModel model){
        intent.putExtra("id",model.getId());
        intent.putExtra("alarmType", "once");
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
        try(SqliteDatabase database = new SqliteDatabase(context)){
            database.updateAlarmStatus(model.getId(),1);
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    public void setRepeatedAlarm(PendingIntent pendingIntent,Intent intent,alarmListModel model){
        intent.putExtra("alarmType", "repeated");
        intent.putExtra("id", model.getId());
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);
        try(SqliteDatabase database = new SqliteDatabase(context)){
            database.updateAlarmStatus(model.getId(),1);
        } catch(Exception e){
            e.printStackTrace();
        }
    }




}
