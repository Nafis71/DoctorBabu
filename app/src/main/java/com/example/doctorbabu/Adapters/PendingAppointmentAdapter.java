package com.example.doctorbabu.Adapters;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.PendingAppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.SqliteDatabase.SqliteDatabase;
import com.example.doctorbabu.patient.AlarmModules.AlarmReceiver;
import com.example.doctorbabu.patient.DoctorConsultationModule.AppointmentReceiver;
import com.example.doctorbabu.patient.DoctorConsultationModule.CheckoutDoctor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.aviran.cookiebar2.CookieBar;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class PendingAppointmentAdapter extends RecyclerView.Adapter<PendingAppointmentAdapter.myViewHolder> {

    Context context;
    ArrayList<PendingAppointmentModel> model;
    String doctorTitle, doctorFullName, doctorDegree, doctorSpecialty, doctorCurrentlyWorking, photoUrl, consultationFee;
    Firebase firebase;
    RelativeLayout descriptionHeader,noAppointmentHeader;

    public PendingAppointmentAdapter(Context context, ArrayList<PendingAppointmentModel> model,RelativeLayout descriptionHeader,RelativeLayout noAppointmentHeader) {
        this.context = context;
        this.model = model;
        this.descriptionHeader = descriptionHeader;
        this.noAppointmentHeader = noAppointmentHeader;
    }

    @NonNull
    @Override
    public PendingAppointmentAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_appointment_list, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingAppointmentAdapter.myViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PendingAppointmentModel dbModel = model.get(position);
        getDoctorData(holder, dbModel);
        String currentTime = getCurrentTime();
        String currentDate = getCurrentDate();
        String[] currentTimeArray = currentTime.split(":");
        int hour = Integer.parseInt(currentTimeArray[0]);
        int minute = Integer.parseInt(currentTimeArray[1]);
        if (currentDate.equalsIgnoreCase(dbModel.getAppointmentDate())) {
            if (hour == Integer.parseInt(dbModel.getAppointmentHour()) && minute == Integer.parseInt(dbModel.getAppointmentMinute()) || hour == Integer.parseInt(dbModel.getAppointmentHour()) && (Integer.parseInt(dbModel.getAppointmentMinute()) + 10) >= minute) {
                holder.cancel.setVisibility(View.GONE);
                holder.videoCall.setVisibility(View.VISIBLE);
            }
        }
        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                model.remove(position);
                if(model.size() == 0){
                    descriptionHeader.setVisibility(View.GONE);
                    noAppointmentHeader.setVisibility(View.VISIBLE);
                }
                cancelAppointment(dbModel);
                notifyDataSetChanged();
            }
        });
        failSafeAutoCancelAppointment(dbModel);
    }

    public void failSafeAutoCancelAppointment(PendingAppointmentModel dbModel){
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        String currentTime = getCurrentTime();
        String[] currentTimeArray = currentTime.split(":");
        int hour = Integer.parseInt(currentTimeArray[0]);
        int minute = Integer.parseInt(currentTimeArray[1]);
        String currentDate = getCurrentDate();
        if (currentDate.equalsIgnoreCase(dbModel.getAppointmentDate())) {
            if (hour >= Integer.parseInt(dbModel.getAppointmentHour()) && minute > Integer.parseInt(dbModel.getAppointmentMinute())) {
                saveMissedAppointment(reference,dbModel);
            }
        } else {
            String[] currentDateArray = currentDate.split("-");
            int year = Integer.parseInt(currentDateArray[0]);
            int month = Integer.parseInt(currentDateArray[1]);
            int day = Integer.parseInt(currentDateArray[2]);
            String[] appointedDateArray = dbModel.getAppointmentDate().split("-");
            int appointedYear = Integer.parseInt(appointedDateArray[0]);
            int appointedMonth = Integer.parseInt(appointedDateArray[1]);
            int appointedDay = Integer.parseInt(appointedDateArray[2]);
            if(year >= appointedYear && month == appointedMonth && day > appointedDay){
                saveMissedAppointment(reference,dbModel);
            }
        }
    }

    public void saveMissedAppointment(DatabaseReference reference, PendingAppointmentModel dbModel){
        reference.child(dbModel.getPatientID()).child(dbModel.getAppointmentID()).removeValue();
        reference.child(dbModel.getDoctorID()).child(dbModel.getAppointmentID()).removeValue();
        reference = firebase.getDatabaseReference("missedAppointments");
        reference.child(dbModel.getPatientID()).child(dbModel.getAppointmentID()).setValue(dbModel);
    }
    public void cancelAppointment(PendingAppointmentModel dbModel){
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorAppointments");
        reference.child(dbModel.getPatientID()).child(dbModel.getAppointmentID()).removeValue();
        reference.child(dbModel.getDoctorID()).child(dbModel.getAppointmentID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                saveCancelledAppointment(dbModel);
            }
        });
    }
    public void saveCancelledAppointment(PendingAppointmentModel dbModel){
        Firebase firebase = Firebase.getInstance();
        HashMap<String,String> data = new HashMap<>();
        data.put("appointmentDate",dbModel.getAppointmentDate());
        data.put("appointmentHour",dbModel.getAppointmentHour());
        data.put("appointmentMinute", dbModel.getAppointmentMinute());
        data.put("appointmentID",dbModel.getAppointmentID());
        data.put("doctorID", dbModel.getDoctorID());
        data.put("patientID",dbModel.getPatientID());
        data.put("timePeriod",dbModel.getTimePeriod());
        data.put("cancelledBy","patient");
        DatabaseReference reference = firebase.getDatabaseReference("cancelledAppointments");
        reference.child(dbModel.getDoctorID()).child(dbModel.getAppointmentID()).setValue(data);
        reference.child(dbModel.getPatientID()).child(dbModel.getAppointmentID()).setValue(data);
        cancelAlarm(dbModel);
        AppCompatActivity activity = (AppCompatActivity)context;
        CookieBar.build(activity)
                .setTitle("Appointment Cancelled")
                .setMessage("Appointment has been cancelled successfully")
                .setSwipeToDismiss(true)
                .setDuration(3000)
                .setTitleColor(R.color.white)
                .setBackgroundColor(R.color.blue)
                .setCookiePosition(CookieBar.TOP)  // Cookie will be displayed at the Top
                .show();
    }

    public void cancelAlarm(PendingAppointmentModel dbModel) {
        Intent intent = new Intent(context, AppointmentReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(dbModel.getBroadcastCode()), intent, PendingIntent.FLAG_MUTABLE);
        AppCompatActivity activity = (AppCompatActivity)context;
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public String getCurrentDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        String currentDate = dtf.format(now);
        String[] dateArray = currentDate.split("-");
        String pattern = "0";
        DecimalFormat numberFormatter = new DecimalFormat(pattern);
        String currentYear = numberFormatter.format(Integer.parseInt(dateArray[0]));
        String currentMonth = numberFormatter.format(Integer.parseInt(dateArray[1]));
        String currentDay = numberFormatter.format(Integer.parseInt(dateArray[2]));
        return currentYear + "-" + currentMonth + "-" + currentDay;
    }

    public void getDoctorData(myViewHolder holder, PendingAppointmentModel dbModel) {
        firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
        reference.child(dbModel.getDoctorID()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    photoUrl = String.valueOf(snapshot.child("photoUrl").getValue());
                    Glide.with(context).load(photoUrl).into(holder.profilePicture);
                    doctorTitle = String.valueOf(snapshot.child("title").getValue());
                    doctorFullName = String.valueOf(snapshot.child("fullName").getValue());
                    doctorDegree = String.valueOf(snapshot.child("degrees").getValue());
                    doctorSpecialty = String.valueOf(snapshot.child("specialty").getValue());
                    consultationFee = String.valueOf(snapshot.child("consultationFee").getValue());
                    getCurrentlyWorkingData(dbModel, holder);
                    holder.doctorName.setText(doctorTitle + doctorFullName);
                    getAppointmentData(holder, dbModel);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getCurrentlyWorkingData(PendingAppointmentModel dbModel, myViewHolder holder) {
        DatabaseReference reference = firebase.getDatabaseReference("doctorCurrentlyWorking");
        reference.child(dbModel.getDoctorID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    doctorCurrentlyWorking = String.valueOf(snapshot.child("hospitalName").getValue());
                    try (SqliteDatabase database = new SqliteDatabase(context)) {
                        boolean isFound = database.searchAppointmentID(dbModel.getAppointmentID());
                        if (isFound) {
                            holder.videoCall.setVisibility(View.VISIBLE);
                            holder.videoCall.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    AppCompatActivity activity = (AppCompatActivity) context;
                                    Intent intent = new Intent(context, CheckoutDoctor.class);
                                    intent.putExtra("doctorId", dbModel.getDoctorID());
                                    intent.putExtra("doctorTitle", doctorTitle);
                                    intent.putExtra("doctorName", doctorFullName);
                                    intent.putExtra("doctorDegree", doctorDegree);
                                    intent.putExtra("doctorSpecialty", doctorSpecialty);
                                    intent.putExtra("doctorCurrentlyWorking", doctorCurrentlyWorking);
                                    intent.putExtra("photoUrl", photoUrl);
                                    intent.putExtra("consultationFee", consultationFee);
                                    activity.startActivity(intent);
                                    activity.finish();
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getAppointmentData(myViewHolder holder, PendingAppointmentModel dbModel) {
        String hour = dbModel.getAppointmentHour();
        String minute = dbModel.getAppointmentMinute();
        String timePeriod = dbModel.getTimePeriod();
        if (timePeriod.equalsIgnoreCase("pm")) {
            hour = String.valueOf(Integer.parseInt(hour) - 12);
        }
        String time = hour + ":" + minute + " " + timePeriod;
        holder.appointmentTime.setText(time);
        holder.appointmentDate.setText(dbModel.getAppointmentDate());
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView doctorName, appointmentDate, appointmentTime;
        MaterialCardView videoCall, cancel;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.doctorName);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            appointmentDate = itemView.findViewById(R.id.appointmentDate);
            appointmentTime = itemView.findViewById(R.id.appointmentTime);
            videoCall = itemView.findViewById(R.id.videoCall);
            cancel = itemView.findViewById(R.id.cancel);
        }
    }
}
