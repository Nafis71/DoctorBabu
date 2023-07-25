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

public class doctorSearchAdapter extends RecyclerView.Adapter<doctorSearchAdapter.myViewHolder> {
    Context context;
    ArrayList<doctorSearchResultModel> doctorSearchModel;

    public doctorSearchAdapter(Context context, ArrayList<doctorSearchResultModel> doctorSearchModel) {
        this.context = context;
        this.doctorSearchModel = doctorSearchModel;
    }

    @NonNull
    @Override
    public doctorSearchAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_doctor_search, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull doctorSearchAdapter.myViewHolder holder, int position) {
        doctorSearchResultModel model = doctorSearchModel.get(position);
        Glide.with(context).load(model.getProfilePicture()).into(holder.profilePicture);
        holder.doctorName.setText(model.getDoctorNameAndId());
        holder.doctorDepartment.setText(model.getDepartment());

    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView doctorName, doctorDepartment;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            doctorName = itemView.findViewById(R.id.doctorName);
            doctorDepartment = itemView.findViewById(R.id.doctorDepartment);
        }
    }

    @Override
    public int getItemCount() {
        return doctorSearchModel.size();
    }
}
