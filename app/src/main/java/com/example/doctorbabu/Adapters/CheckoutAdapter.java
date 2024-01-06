package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.CartModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.myViewHolder> {
    Context context;
    ArrayList<CartModel> model;
    ExecutorService medicineDataExecutor;

    public CheckoutAdapter(Context context, ArrayList<CartModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public CheckoutAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_checkout_medicine_layout, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutAdapter.myViewHolder holder, int position) {
        medicineDataExecutor = Executors.newSingleThreadExecutor();
        CartModel dbModel = model.get(position);
        holder.medicineQuantity.setText(dbModel.getQuantity());
        holder.medicinePrice.setText(dbModel.getTotalPrice());
        medicineDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fetchMedicineData(holder, dbModel);
            }
        });

    }

    public void fetchMedicineData(CheckoutAdapter.myViewHolder holder, CartModel dbModel) {
        Firebase firebase = Firebase.getInstance();
        if (dbModel.getMedicineType().equalsIgnoreCase("tablet")) {
            DatabaseReference reference = firebase.getDatabaseReference("medicineData");
            reference.child(dbModel.getMedicineId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Glide.with(context).load(String.valueOf(snapshot.child("medicinePicture").getValue())).into(holder.medicineImage);
                        holder.medicineName.setText(String.valueOf(snapshot.child("medicineName").getValue()));
                        holder.medicineDosageOrbottleSize.setText(String.valueOf(snapshot.child("medicineDosage").getValue()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        } else if (dbModel.getMedicineType().equalsIgnoreCase("syrup")) {
            DatabaseReference reference = firebase.getDatabaseReference("syrupData");
            reference.child(dbModel.getMedicineId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Glide.with(context).load(String.valueOf(snapshot.child("syrupPicture").getValue())).into(holder.medicineImage);
                        holder.medicineName.setText(String.valueOf(snapshot.child("syrupName").getValue()));
                        holder.medicineDosageOrbottleSize.setText(String.valueOf(snapshot.child("bottleSize").getValue()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        } else if (dbModel.getMedicineType().equalsIgnoreCase("herbalSyrup")) {
            DatabaseReference reference = firebase.getDatabaseReference("herbalSyrupData");
            reference.child(dbModel.getMedicineId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Glide.with(context).load(String.valueOf(snapshot.child("syrupPicture").getValue())).into(holder.medicineImage);
                        holder.medicineName.setText(String.valueOf(snapshot.child("syrupName").getValue()));
                        holder.medicineDosageOrbottleSize.setText(String.valueOf(snapshot.child("bottleSize").getValue()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        }
        medicineDataExecutor.shutdown();
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView medicineImage;
        TextView medicineName, medicineDosageOrbottleSize, medicinePrice, medicineQuantity;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            medicineImage = itemView.findViewById(R.id.medicineImage);
            medicineName = itemView.findViewById(R.id.medicineName);
            medicineDosageOrbottleSize = itemView.findViewById(R.id.medicineDosageOrbottleSize);
            medicinePrice = itemView.findViewById(R.id.medicinePrice);
            medicineQuantity = itemView.findViewById(R.id.medicineQuantity);
        }
    }
}
