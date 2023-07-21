package com.example.doctorbabu.Databases;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;

import java.util.ArrayList;

public class recentlyViewedDoctorAdapter extends RecyclerView.Adapter<recentlyViewedDoctorAdapter.myViewHolder> {
    Context context;
    ArrayList<recentlyViewedDoctorModel> model;

    public recentlyViewedDoctorAdapter(Context context, ArrayList<recentlyViewedDoctorModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public recentlyViewedDoctorAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_recently_viewed,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull recentlyViewedDoctorAdapter.myViewHolder holder, int position) {
        recentlyViewedDoctorModel dbmodel = model.get(position);
        Glide.with(context).load(dbmodel.getPhotoUrl()).into(holder.profilePicture);

    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView profilePicture,greenDot;
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
