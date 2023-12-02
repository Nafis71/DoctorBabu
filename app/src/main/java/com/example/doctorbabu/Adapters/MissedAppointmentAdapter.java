package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.DatabaseModels.AppointmentModel;
import com.example.doctorbabu.DatabaseModels.PendingAppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MissedAppointmentAdapter extends RecyclerView.Adapter<MissedAppointmentAdapter.myViewHolder> {

    Context context;
    ArrayList<PendingAppointmentModel> model;

    public MissedAppointmentAdapter(Context context, ArrayList<PendingAppointmentModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public MissedAppointmentAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_missed_appointment,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MissedAppointmentAdapter.myViewHolder holder, int position) {
        PendingAppointmentModel dbModel = model.get(position);
        getDoctorData(holder,dbModel);

    }
    public void getDoctorData(myViewHolder holder, PendingAppointmentModel dbModel){
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
        reference.child(dbModel.getDoctorID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String title = String.valueOf(snapshot.child("title").getValue());
                    String fullName = String.valueOf(snapshot.child("fullName").getValue());
                    String doctorName = title+fullName;
                    holder.doctorName.setText(doctorName);
                    getAppointmentData(holder,dbModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getAppointmentData(myViewHolder holder, PendingAppointmentModel dbModel){
        String hour = dbModel.getAppointmentHour();
        String timePeriod = dbModel.getTimePeriod();
        if(timePeriod.equalsIgnoreCase("pm")){
            hour = String.valueOf(Integer.parseInt(hour) - 12);
        }
        String minute = dbModel.getAppointmentMinute();
        String time = hour+":"+minute+" "+timePeriod;
        holder.appointmentTime.setText(time);
        holder.appointmentDate.setText(dbModel.getAppointmentDate());
    }



    @Override
    public int getItemCount() {
        return model.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        TextView doctorName,appointmentDate,appointmentTime;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.doctorName);
            appointmentDate = itemView.findViewById(R.id.appointmentDate);
            appointmentTime = itemView.findViewById(R.id.appointmentTime);
        }
    }
}
