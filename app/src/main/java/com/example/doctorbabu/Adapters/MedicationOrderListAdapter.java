package com.example.doctorbabu.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.MedicationOrderListModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
public class MedicationOrderListAdapter extends RecyclerView.Adapter<MedicationOrderListAdapter.myViewHolder> {
    Context context;
    ArrayList<MedicationOrderListModel> model;

    public MedicationOrderListAdapter(Context context, ArrayList<MedicationOrderListModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public MedicationOrderListAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_medicine_order_list, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationOrderListAdapter.myViewHolder holder, int position) {
        MedicationOrderListModel dbModel = model.get(position);
        loadMedicineInfo(dbModel,holder);
        loadOrderInfo(dbModel,holder);
    }
    public void loadMedicineInfo(MedicationOrderListModel dbModel,MedicationOrderListAdapter.myViewHolder holder){
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference;
        if(dbModel.getMedicineType().equalsIgnoreCase("tablet")){
            reference = firebase.getDatabaseReference("tabletData");
        } else if (dbModel.getMedicineType().equalsIgnoreCase("syrup")) {
            reference = firebase.getDatabaseReference("syrupData");
        }else{
            reference = firebase.getDatabaseReference("herbalSyrupData");
        }
        reference.child(dbModel.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Glide.with(context).load(String.valueOf(snapshot.child("medicinePicture").getValue())).into(holder.medicinePicture);
                    holder.medicineName.setText(String.valueOf(snapshot.child("medicineName").getValue()));
                    holder.medicineBrandName.setText(String.valueOf(snapshot.child("brandName").getValue()));
                    if(dbModel.getMedicineType().equalsIgnoreCase("tablet")){
                        holder.medicineDosage.setText(String.valueOf(snapshot.child("medicineDosage").getValue()));
                    } else {
                        holder.medicineDosage.setText(String.valueOf(snapshot.child("bottleSize").getValue()));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });

    }
    @SuppressLint("SetTextI18n")
    public void loadOrderInfo(MedicationOrderListModel dbModel, MedicationOrderListAdapter.myViewHolder holder){
        String quantity = "QTY- "+dbModel.getProductQuantity();
        holder.quantity.setText(quantity);
        if(dbModel.getTrackOrder() == 0){
            holder.trackOrder.setText("Processing");
        } else if (dbModel.getTrackOrder() == 1) {
            holder.trackOrder.setText("Out to deliver");
            holder.trackOrder.setTextColor(Color.parseColor("#229954"));
        } else{
            holder.trackOrder.setText("Delivered");
            holder.trackOrder.setTextColor(Color.parseColor("#229954"));
        }
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy ");
        // we create instance of the Date and pass milliseconds to the constructor
        Date date = new Date(dbModel.getOrderTime());
        String orderDate = "Order Date- "+dateFormat.format(date);
        holder.orderDate.setText(orderDate);
        holder.medicinePrice.setText(dbModel.getTotalPrice());
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView medicinePicture;
        TextView medicineName, medicineDosage, medicineBrandName, orderDate, medicinePrice, quantity, trackOrder;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            medicinePicture = itemView.findViewById(R.id.medicinePicture);
            medicineName = itemView.findViewById(R.id.medicineName);
            medicineDosage = itemView.findViewById(R.id.medicineDosage);
            medicineBrandName = itemView.findViewById(R.id.medicineBrandName);
            orderDate = itemView.findViewById(R.id.orderDate);
            medicinePrice = itemView.findViewById(R.id.medicinePrice);
            quantity = itemView.findViewById(R.id.quantity);
            trackOrder = itemView.findViewById(R.id.trackOrder);
        }
    }
}
