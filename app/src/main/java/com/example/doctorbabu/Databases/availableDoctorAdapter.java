package com.example.doctorbabu.Databases;

import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class availableDoctorAdapter extends RecyclerView.Adapter<availableDoctorAdapter.myViewHolder>{

    Context context;
    ArrayList<availableDoctorModel> model;

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_available_doctor,parent,false);
        return new myViewHolder(view);
    }

    public availableDoctorAdapter(Context context, ArrayList<availableDoctorModel> model) {
        this.context = context;
        this.model = model;
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        availableDoctorModel dbModel = model.get(position);
        String name = dbModel.getTitle() + " " + dbModel.getFullName();
        holder.doctorName.setText(name);
        holder.doctordegree.setText(dbModel.getDegrees());
        holder.rating.setText(String.valueOf(dbModel.getRating()));
        holder.doctorSpecialties.setText(dbModel.getSpecialty());
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference currentlyWorkingReference = database.getReference("doctorCurrentlyWorking");
        currentlyWorkingReference.child(dbModel.getDoctorId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        DataSnapshot snapshot = task.getResult();
                        holder.currentlyWorking.setText(String.valueOf(snapshot.child("hospitalName").getValue()));
                    }
                }
            }
        });
        if(!dbModel.photoUrl.equals("null"))
        {
            Glide.with(context).load(dbModel.getPhotoUrl()).into(holder.profilePicture);
        }
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity)context;
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new DoctorInfo(dbModel.getDoctorId(),dbModel.getFullName(),dbModel.getTitle(),dbModel.getDegrees(),dbModel.getSpecialty(),holder.currentlyWorking.getText().toString(),dbModel.getPhotoUrl(),dbModel.getRating(),dbModel.getBmdc())).commit();
            }
        });

    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder{
        CardView card;
        ImageView profilePicture;
        TextView doctorName, doctordegree,rating,doctorSpecialties,currentlyWorking;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            doctorName = itemView.findViewById(R.id.doctorName);
            doctordegree = itemView.findViewById(R.id.doctorDegree);
            rating = itemView.findViewById(R.id.rating);
            doctorSpecialties = itemView.findViewById(R.id.doctorSpecialties);
            currentlyWorking =itemView.findViewById(R.id.currentlyWorking);
            card = itemView.findViewById(R.id.card);
        }
    }


}
