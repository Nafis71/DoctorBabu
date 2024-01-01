package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.DatabaseModels.MedicineModel;
import com.example.doctorbabu.R;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.myViewHolder> {
    Context context;
    ArrayList<MedicineModel> model;

    public CartAdapter(Context context, ArrayList<MedicineModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public CartAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_cart_list,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.myViewHolder holder, int position) {
        MedicineModel dbModel = model.get(position);

    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView medicineImage;
        TextView medicineName, medicineDosage, medicineBrand,medicinePrice,medicineSheet;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            medicineImage = itemView.findViewById(R.id.medicineImage);
            medicineName = itemView.findViewById(R.id.medicineName);
            medicineDosage = itemView.findViewById(R.id.medicineDosage);
            medicineBrand = itemView.findViewById(R.id.medicineBrandName);
            medicinePrice = itemView.findViewById(R.id.medicinePrice);
            medicineSheet = itemView.findViewById(R.id.medicineSheet);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
