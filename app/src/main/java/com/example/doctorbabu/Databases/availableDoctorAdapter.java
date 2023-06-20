package com.example.doctorbabu.Databases;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;
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
        holder.doctorType.setText(dbModel.doctorType);
        holder.rating.setText(String.valueOf(dbModel.getRating()));
        if(!dbModel.photoUrl.equals("null"))
        {
            Glide.with(context).load(dbModel.getPhotoUrl()).into(holder.profilePicture);
        }

    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView profilePicture;
        TextView doctorName,doctorType,rating;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            doctorName = itemView.findViewById(R.id.doctorName);
            doctorType = itemView.findViewById(R.id.doctorType);
            rating = itemView.findViewById(R.id.rating);

        }
    }


}
