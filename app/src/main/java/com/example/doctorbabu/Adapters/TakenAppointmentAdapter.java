package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.AppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TakenAppointmentAdapter extends RecyclerView.Adapter<TakenAppointmentAdapter.myViewHolder> {
    Context context;
    ArrayList<AppointmentModel> model;

    public TakenAppointmentAdapter(Context context, ArrayList<AppointmentModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public TakenAppointmentAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_taken_appointment,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TakenAppointmentAdapter.myViewHolder holder, int position) {
        AppointmentModel dbModel = model.get(position);
        getDoctorData(holder,dbModel);
    }

    public void getDoctorData(myViewHolder holder, AppointmentModel dbModel){
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
        reference.child(dbModel.getDoctorID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String title = String.valueOf(snapshot.child("title").getValue());
                    String fullName = String.valueOf(snapshot.child("fullName").getValue());
                    String doctorName = title+fullName;
                    Glide.with(context).load(String.valueOf(snapshot.child("photoUrl").getValue())).into(holder.profilePicture);
                    holder.doctorName.setText(doctorName);
                    getAppointmentData(holder,dbModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }
    public void getAppointmentData(myViewHolder holder, AppointmentModel dbModel){
        String hour = dbModel.getAppointmentHour();
        String timePeriod = dbModel.getTimePeriod();
        if(timePeriod.equalsIgnoreCase("pm")){
            hour = String.valueOf(Integer.parseInt(hour) - 12);
        }
        String minute = dbModel.getAppointmentMinute();
        String time = hour+":"+minute+" "+timePeriod;
        holder.appointmentTime.setText(time);
        holder.appointmentDate.setText(dbModel.getAppointmentDate());
        holder.cancelledBy.setText("Appointment finished");
        holder.cancelledBy.setTextColor(Color.parseColor("#27AE60"));
    }

    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView profilePicture;
        TextView doctorName,appointmentDate,appointmentTime,cancelledBy;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            doctorName = itemView.findViewById(R.id.doctorName);
            appointmentDate = itemView.findViewById(R.id.appointmentDate);
            appointmentTime = itemView.findViewById(R.id.appointmentTime);
            cancelledBy = itemView.findViewById(R.id.cancelledBy);
        }
    }
    @Override
    public int getItemCount() {
        return model.size();
    }
}
