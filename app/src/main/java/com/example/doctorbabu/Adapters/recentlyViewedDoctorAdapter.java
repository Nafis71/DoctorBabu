package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.recentlyViewedDoctorModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.DoctorConsultationModule.SpecificDoctorInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class recentlyViewedDoctorAdapter extends RecyclerView.Adapter<recentlyViewedDoctorAdapter.myViewHolder> {
    Context context;
    ArrayList<recentlyViewedDoctorModel> model;
    ExecutorService onlineStausExecutor;

    public recentlyViewedDoctorAdapter(Context context, ArrayList<recentlyViewedDoctorModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public recentlyViewedDoctorAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_recently_viewed, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull recentlyViewedDoctorAdapter.myViewHolder holder, int position) {
        recentlyViewedDoctorModel dbmodel = model.get(position);
        onlineStausExecutor = Executors.newSingleThreadExecutor();
        Glide.with(context).load(dbmodel.getPhotoUrl()).into(holder.profilePicture);
        holder.profilePicture.setBackgroundColor(Color.parseColor("#DDDDDD"));
        onlineStausExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Firebase firebase = Firebase.getInstance();
                DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
                reference.child(dbmodel.getDoctorId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            int onlineStatus = Integer.parseInt(String.valueOf(snapshot.child("onlineStatus").getValue()));
                            if (onlineStatus == 0){
                                holder.greenDot.setImageResource(R.drawable.greydot);
                                return;
                            }
                            holder.greenDot.setImageResource(R.drawable.greendot);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        holder.profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) context;
                Intent intent = new Intent(context, SpecificDoctorInfo.class);
                intent.putExtra("doctorId",dbmodel.getDoctorId());
                activity.startActivity(intent);
            }
        });

    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture, greenDot;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            greenDot = itemView.findViewById(R.id.greenDot);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
