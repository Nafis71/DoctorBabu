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
import com.example.doctorbabu.patient.MedicinePurchaseModules.SyrupDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyrupAdapter extends RecyclerView.Adapter<SyrupAdapter.myViewHolder> {
    Context context;
    ArrayList<MedicineModel> model;
    ExecutorService tracker;

    public SyrupAdapter(Context context, ArrayList<MedicineModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public SyrupAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_syrup_layout,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SyrupAdapter.myViewHolder holder, int position) {
        PushDownAnim.setPushDownAnimTo(holder.card)
                .setScale(PushDownAnim.MODE_SCALE, 0.95f);
        MedicineModel dbModel = model.get(position);
        Glide.with(context).load(dbModel.getMedicinePicture()).into(holder.syrupImage);
        holder.syrupName.setText(dbModel.getMedicineName());
        holder.syrupBottleSize.setText(dbModel.getBottleSize());
        holder.syrupPrice.setText(dbModel.getUnitPrice());
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
        Intent intent = new Intent(context, SyrupDetails.class);
        intent.putExtra("medicineId", dbModel.getMedicineId());
        if (context instanceof SyrupDetails) {
            activity.startActivity(intent);
            activity.finish();
            tracker.shutdown();
        } else {
            activity.startActivity(intent);
        }
    }

    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView syrupImage;
        TextView syrupName,syrupPrice,syrupBottleSize;
        MaterialCardView card;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            syrupImage = itemView.findViewById(R.id.syrupImage);
            syrupName =  itemView.findViewById(R.id.syrupName);
            syrupBottleSize = itemView.findViewById(R.id.syrupBottleSize);
            syrupPrice = itemView.findViewById(R.id.syrupPrice);
            card = itemView.findViewById(R.id.card);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
