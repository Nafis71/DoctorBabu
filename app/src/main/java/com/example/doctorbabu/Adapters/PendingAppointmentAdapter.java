package com.example.doctorbabu.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.PendingAppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PendingAppointmentAdapter extends RecyclerView.Adapter<PendingAppointmentAdapter.myViewHolder> {

    Context context;
    ArrayList<PendingAppointmentModel> model;

    public PendingAppointmentAdapter(Context context, ArrayList<PendingAppointmentModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public PendingAppointmentAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_appointment_list, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingAppointmentAdapter.myViewHolder holder, int position) {
        PendingAppointmentModel dbModel = model.get(position);
        getDoctorData(holder, dbModel);
    }

    public void getDoctorData(myViewHolder holder, PendingAppointmentModel dbModel) {
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
        reference.child(dbModel.getDoctorID()).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Glide.with(context).load(String.valueOf(snapshot.child("photoUrl").getValue())).into(holder.profilePicture);
                    String doctorTitle = String.valueOf(snapshot.child("title").getValue());
                    String doctorFullName = String.valueOf(snapshot.child("fullName").getValue());
                    holder.doctorName.setText(doctorTitle + doctorFullName);
                    getAppointmentData(holder, dbModel);
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
        Button takeAppointment;
        MaterialCardView card;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.doctorName);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            appointmentDate = itemView.findViewById(R.id.appointmentDate);
            appointmentTime = itemView.findViewById(R.id.appointmentTime);
            takeAppointment = itemView.findViewById(R.id.takeAppointment);
            card = itemView.findViewById(R.id.card);
        }
    }
}
