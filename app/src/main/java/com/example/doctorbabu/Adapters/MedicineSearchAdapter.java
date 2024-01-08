package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.DatabaseModels.MedicineSearchModel;
import com.example.doctorbabu.R;

import java.util.ArrayList;

public class MedicineSearchAdapter extends RecyclerView.Adapter<MedicineSearchAdapter.myViewHolder> {
    Context context;
    ArrayList<MedicineSearchModel> model;
    @NonNull
    @Override
    public MedicineSearchAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_medicine_search_layout,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineSearchAdapter.myViewHolder holder, int position) {
        MedicineSearchModel dbModel = model.get(position);

    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView medicineImage;
        TextView medicineName,medicineDosage,medicineBrandName;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            medicineImage = itemView.findViewById(R.id.medicineImage);
            medicineName = itemView.findViewById(R.id.medicineName);
            medicineDosage = itemView.findViewById(R.id.medicineDosage);
            medicineBrandName = itemView.findViewById(R.id.medicineBrandName);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
