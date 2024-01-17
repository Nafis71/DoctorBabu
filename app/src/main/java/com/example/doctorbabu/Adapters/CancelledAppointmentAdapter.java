package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.DatabaseModels.AppointmentModel;
import com.example.doctorbabu.R;

import java.util.ArrayList;

public class CancelledAppointmentAdapter extends RecyclerView.Adapter<CancelledAppointmentAdapter.myViewHolder> {
    Context context;
    ArrayList<AppointmentModel> model;

    public CancelledAppointmentAdapter(Context context, ArrayList<AppointmentModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public CancelledAppointmentAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_cancelled_appointment,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CancelledAppointmentAdapter.myViewHolder holder, int position) {

    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView profilePicture;
        TextView doctorName,appointmentDate,appointmentTime,cancelledBy;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            doctorName = itemView.findViewById(R.id.doctorName);
            appointmentDate = itemView.findViewById(R.id.appointmentDate);
            appointmentTime = itemView.findViewById(R.id.appointmentTime);
            cancelledBy = itemView.findViewById(R.id.cancelledBy);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
