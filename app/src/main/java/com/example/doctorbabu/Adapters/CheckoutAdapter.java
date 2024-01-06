package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.DatabaseModels.CartModel;
import com.example.doctorbabu.R;

import java.util.ArrayList;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.myViewHolder> {
    Context context;
    ArrayList<CartModel> model;

    public CheckoutAdapter(Context context, ArrayList<CartModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public CheckoutAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_checkout_medicine_layout,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutAdapter.myViewHolder holder, int position) {
        CartModel dbModel = model.get(position);
        holder.medicineQuantity.setText(dbModel.getQuantity());

    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView medicineImage;
        TextView medicineName,medicineDosageOrbottleSize,medicinePrice,medicineQuantity;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            medicineImage = itemView.findViewById(R.id.medicineImage);
            medicineName = itemView.findViewById(R.id.medicineName);
            medicineDosageOrbottleSize = itemView.findViewById(R.id.medicineDosageOrbottleSize);
            medicinePrice = itemView.findViewById(R.id.medicinePrice);
            medicineQuantity = itemView.findViewById(R.id.medicineQuantity);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
