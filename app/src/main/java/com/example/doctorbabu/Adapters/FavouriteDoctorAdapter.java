package com.example.doctorbabu.Adapters;

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
import com.example.doctorbabu.DatabaseModels.doctorInfoModel;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.DoctorConsultationModule.SpecificDoctorInfo;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class FavouriteDoctorAdapter extends RecyclerView.Adapter<FavouriteDoctorAdapter.myViewHolder> {
    Context context;
    ArrayList<doctorInfoModel> model;

    public FavouriteDoctorAdapter(Context context, ArrayList<doctorInfoModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public FavouriteDoctorAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_favourite_doctor,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavouriteDoctorAdapter.myViewHolder holder, int position) {
        doctorInfoModel dbModel = model.get(position);
        Glide.with(context).load(dbModel.getPhotoUrl()).into(holder.profilePicture);
        String doctorName = dbModel.getTitle() + dbModel.getFullName();
        holder.doctorName.setText(doctorName);
        holder.doctorSpecialty.setText(dbModel.getSpecialty());
        holder.checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) context;
                Intent intent = new Intent(context, SpecificDoctorInfo.class);
                intent.putExtra("doctorId",dbModel.getDoctorId());
                activity.startActivity(intent);
            }
        });

    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView profilePicture;
        TextView doctorName,doctorSpecialty;
        MaterialButton checkout;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            doctorName = itemView.findViewById(R.id.doctorName);
            checkout = itemView.findViewById(R.id.checkOut);
            doctorSpecialty = itemView.findViewById(R.id.specialty);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
