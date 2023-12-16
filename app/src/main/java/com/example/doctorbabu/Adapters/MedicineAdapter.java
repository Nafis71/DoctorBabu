package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.MedicineModel;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.HomeModules.MedicineDetails;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.myViewHolder> {
    Context context;
    ArrayList<MedicineModel> model;
    String medicineId;

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
        MedicineModel dbModel = model.get(position);
        medicineId = dbModel.getMedicineId();
        holder.medicineName.setText(dbModel.getMedicineName());
        holder.medicineDosage.setText(dbModel.getMedicineDosage());
        Glide.with(context).load(dbModel.getMedicinePicture()).into(holder.medicineImage);
        double perPiecePrice = Double.parseDouble(dbModel.getMedicinePerPiecePrice());
        double pataSize = Double.parseDouble(dbModel.getMedicinePataSize());
        String price = String.valueOf(perPiecePrice * pataSize);
        holder.medicinePrice.setText(price);
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) context;
                Intent intent = new Intent(context, MedicineDetails.class);
                intent.putExtra("medicineId",medicineId);
                if(context instanceof MedicineDetails){
                    activity.startActivity(intent);
                    activity.finish();
                }else{
                    activity.startActivity(intent);
                }
            }
        });
    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView medicineImage;
        TextView medicineName,medicineDosage,medicinePrice;
        MaterialCardView card;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            medicineImage = itemView.findViewById(R.id.medicineImage);
            medicineName = itemView.findViewById(R.id.medicineName);
            medicineDosage = itemView.findViewById(R.id.medicineDosage);
            medicinePrice = itemView.findViewById(R.id.medicinePrice);
            card = itemView.findViewById(R.id.card);
        }
    }
    @Override
    public int getItemCount() {
        return model.size();
    }
}
