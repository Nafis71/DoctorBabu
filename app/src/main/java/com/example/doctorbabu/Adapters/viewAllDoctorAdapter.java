package com.example.doctorbabu.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.doctorInfoModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.DoctorConsultationModule.SpecificDoctorInfo;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class viewAllDoctorAdapter extends RecyclerView.Adapter<viewAllDoctorAdapter.myViewHolder> {
    Context context;
    ArrayList<doctorInfoModel> model;

    public viewAllDoctorAdapter(Context context, ArrayList<doctorInfoModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public viewAllDoctorAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_all_doctor_list, parent, false);
        return new myViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull viewAllDoctorAdapter.myViewHolder holder, int position) {
        doctorInfoModel dbmodel = model.get(position);
        Glide.with(context).load(dbmodel.getPhotoUrl()).into(holder.profilePicture);
        holder.profilePicture.setBackgroundColor(Color.parseColor("#DDDDDD"));
        holder.doctorName.setText(dbmodel.getTitle() + " " + dbmodel.getFullName());
        holder.doctorDegree.setText(dbmodel.getDegrees());
        holder.doctorSpecialties.setText(dbmodel.getSpecialty());
        holder.doctorRating.setText(String.valueOf(dbmodel.getRating()));
        holder.takeConsultation.setOnClickListener(view -> {
            AppCompatActivity activity = (AppCompatActivity) context;
            Intent intent = new Intent(activity, SpecificDoctorInfo.class);
            intent.putExtra("doctorId",dbmodel.getDoctorId());
            activity.startActivity(intent);
        });
        getWorkingData(dbmodel,holder);
        if(dbmodel.getOnlineStatus() == 1){
            holder.onlineStatus.setText("Online");
        } else {
            holder.onlineStatus.setText("Offline");
            holder.onlineCard.setCardBackgroundColor(Color.parseColor("#CD6155"));
        }

    }

    public void getWorkingData(doctorInfoModel dbmodel,myViewHolder holder){
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorCurrentlyWorking");
        reference.child(dbmodel.getDoctorId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    holder.currentlyWorking.setText(String.valueOf(snapshot.child("hospitalName").getValue()));
                    holder.workingPost.setText(String.valueOf(snapshot.child("designation").getValue()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView doctorName, doctorSpecialties, doctorDegree, doctorRating,currentlyWorking,onlineStatus,workingPost;
        LinearLayout takeConsultation;
        MaterialCardView onlineCard;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            doctorName = itemView.findViewById(R.id.doctorName);
            doctorSpecialties = itemView.findViewById(R.id.doctorSpecialties);
            doctorRating = itemView.findViewById(R.id.rating);
            doctorDegree = itemView.findViewById(R.id.doctorDegree);
            takeConsultation = itemView.findViewById(R.id.card);
            currentlyWorking  = itemView.findViewById(R.id.currentlyWorking);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);
            onlineCard = itemView.findViewById(R.id.onlineCard);
            workingPost = itemView.findViewById(R.id.workingPost);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
