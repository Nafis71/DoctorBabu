package com.example.doctorbabu.Databases;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.DoctorInfo;
import com.example.doctorbabu.patient.DoctorInfoActivity;

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
            Intent intent = new Intent(activity, DoctorInfoActivity.class);
            intent.putExtra("doctorId",dbmodel.getDoctorId());
            intent.putExtra("doctorName",dbmodel.getFullName());
            intent.putExtra("doctorTitle",dbmodel.getTitle());
            intent.putExtra("doctorDegree",dbmodel.getDegrees());
            intent.putExtra("doctorSpecialties",dbmodel.getSpecialty());
            intent.putExtra("photoUrl",dbmodel.getPhotoUrl());
            intent.putExtra("doctorRating",String.valueOf(dbmodel.getRating()));
            intent.putExtra("bmdc",dbmodel.getBmdc());
            activity.startActivity(intent);
            activity.finish();
        });

    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView doctorName, doctorSpecialties, doctorDegree, doctorRating;
        CardView takeConsultation;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            doctorName = itemView.findViewById(R.id.doctorName);
            doctorSpecialties = itemView.findViewById(R.id.doctorSpecialties);
            doctorRating = itemView.findViewById(R.id.doctorRating);
            doctorDegree = itemView.findViewById(R.id.doctorDegree);
            takeConsultation = itemView.findViewById(R.id.takeConsultation);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}