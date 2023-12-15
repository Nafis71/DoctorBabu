package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.DatabaseModels.MedicineModel;
import com.example.doctorbabu.R;

import java.util.ArrayList;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.myViewHolder> {
    Context context;
    ArrayList<MedicineModel> model;

    public MedicineAdapter(Context context, ArrayList<MedicineModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public MedicineAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_medicine_layout,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineAdapter.myViewHolder holder, int position) {

    }
    public static class myViewHolder extends RecyclerView.ViewHolder{

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
    @Override
    public int getItemCount() {
        return model.size();
    }
}
