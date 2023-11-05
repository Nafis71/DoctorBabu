package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.DatabaseModels.doctorPastExperienceModel;
import com.example.doctorbabu.R;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

public class doctorPastExperienceAdapter extends RecyclerView.Adapter<doctorPastExperienceAdapter.myViewHolder> {

    Context context;
    ArrayList<doctorPastExperienceModel> model;

    public doctorPastExperienceAdapter(Context context, ArrayList<doctorPastExperienceModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public doctorPastExperienceAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_doctor_past_experience, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull doctorPastExperienceAdapter.myViewHolder holder, int position) {
        doctorPastExperienceModel dbmodel = model.get(position);
        holder.hospitalName.setText(dbmodel.getHospitalName());
        holder.designationName.setText(dbmodel.getDesignation());
        holder.departmentName.setText(dbmodel.getDepartment());
        holder.joinDateText.setText(dbmodel.getJoiningDate());
        holder.leavedateText.setText(dbmodel.getLeavingDate());
        String date = dbmodel.getJoiningDate();
        String[] splitText = date.split("/");
        int year = Integer.parseInt(splitText[0]);
        int month = Integer.parseInt(splitText[1]);
        int day = Integer.parseInt(splitText[2]);
        LocalDate beginningDate = LocalDate.of(year, month, day);
        date = dbmodel.getLeavingDate();
        splitText = date.split("/");
        year = Integer.parseInt(splitText[0]);
        month = Integer.parseInt(splitText[1]);
        day = Integer.parseInt(splitText[2]);
        LocalDate endingDate = LocalDate.of(year, month, day);
        Period age = Period.between(beginningDate, endingDate);
        String years = String.valueOf(age.getYears());
        String months = String.valueOf(age.getMonths());
        String yearText, monthText;
        if (age.getYears() > 1 && age.getMonths() > 1) {
            yearText = " years ";
            monthText = " months";
        } else if (age.getYears() < 1 && age.getMonths() > 1) {
            yearText = " year ";
            monthText = " months";
        } else if (age.getYears() > 1 && age.getMonths() < 1) {
            yearText = " years ";
            monthText = " month";
        } else {
            yearText = " year ";
            monthText = " month";
        }
        String result = years + yearText + months + monthText;
        holder.period.setText(result);
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        TextView hospitalName, designationName, departmentName, joinDateText, leavedateText, period;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            hospitalName = itemView.findViewById(R.id.hospitalName);
            designationName = itemView.findViewById(R.id.designationName);
            departmentName = itemView.findViewById(R.id.departmentName);
            joinDateText = itemView.findViewById(R.id.joinDateText);
            leavedateText = itemView.findViewById(R.id.leavedateText);
            period = itemView.findViewById(R.id.period);
        }
    }
}
