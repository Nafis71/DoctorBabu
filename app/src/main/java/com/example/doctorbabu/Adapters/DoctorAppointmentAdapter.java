package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.DatabaseModels.PendingAppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.DoctorConsultationModule.BookAppointment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.aviran.cookiebar2.CookieBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DoctorAppointmentAdapter extends RecyclerView.Adapter<DoctorAppointmentAdapter.myViewHolder> {
    Context context;
    ArrayList<PendingAppointmentModel> model;
    ExecutorService cancelAppointmentExecutor;

    public DoctorAppointmentAdapter(Context context, ArrayList<PendingAppointmentModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public DoctorAppointmentAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_doctor_pending_appointment_layout,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorAppointmentAdapter.myViewHolder holder, int position) {
        cancelAppointmentExecutor = Executors.newSingleThreadExecutor();
        PendingAppointmentModel dbModel = model.get(position);
        getAppointmentData(holder,dbModel);
        getPatientData(holder, dbModel);
        holder.cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelAppointmentExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        cancelAppointment(dbModel);
                    }
                });
            }
        });
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
        data.put("cancelledBy","doctor");
        DatabaseReference reference = firebase.getDatabaseReference("cancelledAppointments");
        reference.child(dbModel.getDoctorID()).child(dbModel.getAppointmentID()).setValue(data);
        reference.child(dbModel.getPatientID()).child(dbModel.getAppointmentID()).setValue(data);
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

    public void getPatientData(myViewHolder holder, PendingAppointmentModel dbModel){
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("users");
        reference.child(dbModel.getPatientID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    holder.patientName.setText(String.valueOf(snapshot.child("fullName").getValue()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
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
    public static class myViewHolder extends RecyclerView.ViewHolder{
        TextView patientName,appointmentTime,appointmentDate;
        MaterialButton cancelButton;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            patientName = itemView.findViewById(R.id.patientName);
            appointmentTime = itemView.findViewById(R.id.appointmentTime);
            appointmentDate = itemView.findViewById(R.id.appointmentDate);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
