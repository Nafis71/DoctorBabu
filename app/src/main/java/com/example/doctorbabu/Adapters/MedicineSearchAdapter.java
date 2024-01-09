package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.MedicineSearchModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.MedicinePurchaseModules.SyrupDetails;
import com.example.doctorbabu.patient.MedicinePurchaseModules.TabletDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedicineSearchAdapter extends RecyclerView.Adapter<MedicineSearchAdapter.myViewHolder> {
    Context context;
    ArrayList<MedicineSearchModel> model;
    ExecutorService tracker;

    public MedicineSearchAdapter(Context context, ArrayList<MedicineSearchModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public MedicineSearchAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_medicine_search_layout,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineSearchAdapter.myViewHolder holder, int position) {
        tracker = Executors.newSingleThreadExecutor();
        MedicineSearchModel dbModel = model.get(position);
        Glide.with(context).load(dbModel.getMedicinePicture()).into(holder.medicineImage);
        holder.medicineName.setText(dbModel.getMedicineName());
        holder.medicineBrandName.setText(dbModel.getBrandName());
        if(dbModel.getMedicineType().equalsIgnoreCase("tablet")){
            holder.medicineDosage.setText(dbModel.getMedicineDosage());
        } else {
            holder.medicineDosage.setText(dbModel.getBottleSize());
        }
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

    public void trackMedicine(MedicineSearchModel dbModel) {
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
    public void saveRecord(MedicineSearchModel dbModel, DatabaseReference trackReference, FirebaseUser user) {
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

    public void launchMedicineDetails(MedicineSearchModel dbModel) {
        AppCompatActivity activity = (AppCompatActivity) context;
        Intent intent;
        if(dbModel.getMedicineType().equalsIgnoreCase("tablet")){
             intent = new Intent(context, TabletDetails.class);
        }else {
             intent = new Intent(context, SyrupDetails.class);
        }
        intent.putExtra("medicineId", dbModel.getMedicineId());
        if (context instanceof TabletDetails) {
            activity.startActivity(intent);
            activity.finish();
            tracker.shutdown();
        } else {
            activity.startActivity(intent);
        }
    }

    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView medicineImage;
        TextView medicineName,medicineDosage,medicineBrandName;
        RelativeLayout card;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            medicineImage = itemView.findViewById(R.id.medicinePicture);
            medicineName = itemView.findViewById(R.id.medicineName);
            medicineDosage = itemView.findViewById(R.id.medicineDosage);
            medicineBrandName = itemView.findViewById(R.id.medicineBrandName);
            card = itemView.findViewById(R.id.card);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
