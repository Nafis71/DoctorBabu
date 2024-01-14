package com.example.doctorbabu.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.PendingAppointmentModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.SqliteDatabase.SqliteDatabase;
import com.example.doctorbabu.patient.DoctorConsultationModule.CheckoutDoctor;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PendingAppointmentAdapter extends RecyclerView.Adapter<PendingAppointmentAdapter.myViewHolder> {

    Context context;
    ArrayList<PendingAppointmentModel> model;
    String doctorTitle, doctorFullName, doctorDegree, doctorSpecialty, doctorCurrentlyWorking, photoUrl,consultationFee;
    Firebase firebase;

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
                                    intent.putExtra("consultationFee",consultationFee);
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
        MaterialCardView card, videoCall;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            doctorName = itemView.findViewById(R.id.doctorName);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            appointmentDate = itemView.findViewById(R.id.appointmentDate);
            appointmentTime = itemView.findViewById(R.id.appointmentTime);
            videoCall = itemView.findViewById(R.id.videoCall);
            card = itemView.findViewById(R.id.card);
        }
    }
}
