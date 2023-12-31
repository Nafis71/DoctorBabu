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
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.HomeModules.MedicineDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.myViewHolder> {
    Context context;
    ArrayList<MedicineModel> model;
    ExecutorService tracker;

    public MedicineAdapter(Context context, ArrayList<MedicineModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public MedicineAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_medicine_layout, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineAdapter.myViewHolder holder, int position) {
        MedicineModel dbModel = model.get(position);
        holder.medicineName.setText(dbModel.getMedicineName());
        holder.medicineDosage.setText(dbModel.getMedicineDosage());
        Glide.with(context).load(dbModel.getMedicinePicture()).into(holder.medicineImage);
        double perPiecePrice = Double.parseDouble(dbModel.getMedicinePerPiecePrice());
        double pataSize = Double.parseDouble(dbModel.getMedicinePataSize());
        String price = String.valueOf(perPiecePrice * pataSize);
        holder.medicinePrice.setText(price);
        tracker = Executors.newSingleThreadExecutor();
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tracker.execute(new Runnable() {
                    @Override
                    public void run() {
                        trackMedicine(dbModel);
                    }
                });
            }
        });
    }

    public void trackMedicine(MedicineModel dbModel) {
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        DatabaseReference trackReference = firebase.getDatabaseReference("trackMedicine");
        trackReference.child(dbModel.getMedicineId()).child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    saveRecord(dbModel, trackReference, user);
                } else {
                    launchMedicineDetails(dbModel);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void saveRecord(MedicineModel dbModel, DatabaseReference trackReference, FirebaseUser user) {
        HashMap<String, String> data = new HashMap<>();
        data.put("count", String.valueOf(1));
        trackReference.child(dbModel.getMedicineId()).child(user.getUid()).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                launchMedicineDetails(dbModel);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //do nothing
            }
        });
    }

    public void launchMedicineDetails(MedicineModel dbModel) {
        AppCompatActivity activity = (AppCompatActivity) context;
        Intent intent = new Intent(context, MedicineDetails.class);
        intent.putExtra("medicineId", dbModel.getMedicineId());
        if (context instanceof MedicineDetails) {
            activity.startActivity(intent);
            activity.finish();
            tracker.shutdown();
        } else {
            activity.startActivity(intent);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView medicineImage;
        TextView medicineName, medicineDosage, medicinePrice;
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
}
