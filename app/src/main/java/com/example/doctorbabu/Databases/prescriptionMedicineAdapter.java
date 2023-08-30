package com.example.doctorbabu.Databases;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.R;

import java.util.ArrayList;

public class prescriptionMedicineAdapter extends RecyclerView.Adapter<prescriptionMedicineAdapter.myViewHolder> {
    Context context;
    ArrayList<prescriptionMedicineModel> model;

    public prescriptionMedicineAdapter(Context context, ArrayList<prescriptionMedicineModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public prescriptionMedicineAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_prescription_medicine_list,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull prescriptionMedicineAdapter.myViewHolder holder, int position) {
            prescriptionMedicineModel dbmodel = model.get(position);
            holder.medicineName.setText(dbmodel.getMedicineName());
            holder.medicineDetails.setText(dbmodel.getMedicineDetails());
    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        TextView medicineName,medicineDetails;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            medicineName = itemView.findViewById(R.id.medicineName);
            medicineDetails = itemView.findViewById(R.id.medicineDetails);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
