package com.example.doctorbabu.patient.MedicinePurchaseModules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Bundle;

import com.example.doctorbabu.Adapters.CatagorialMedicineAdapter;
import com.example.doctorbabu.DatabaseModels.MedicineModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivitySeeAllBinding;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SeeAll extends AppCompatActivity {
    ActivitySeeAllBinding binding;
    ExecutorService medicineDataExecutor;
    CatagorialMedicineAdapter adapter;
    ArrayList<MedicineModel> medicineModels;
    Firebase firebase;
    String medicineType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeeAllBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        medicineType = getIntent().getStringExtra("medicineType");
        firebase = Firebase.getInstance();
        medicineDataExecutor = Executors.newSingleThreadExecutor();
        medicineDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadAllMedicineData();
            }
        });
    }
    public void loadAllMedicineData(){
        medicineModels = new ArrayList<>();
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 3,GridLayoutManager.VERTICAL,false));
        adapter = new CatagorialMedicineAdapter(this,medicineModels,medicineType);
        if(medicineType.equalsIgnoreCase("tablet")){
            DatabaseReference reference = firebase.getDatabaseReference("tabletData");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot snap : snapshot.getChildren()){
                            MedicineModel model = snap.getValue(MedicineModel.class);
                            medicineModels.add(model);
                        }
                        Collections.shuffle(medicineModels);
                        binding.recyclerView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else if(medicineType.equalsIgnoreCase("syrup")){
            DatabaseReference reference = firebase.getDatabaseReference("syrupData");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot snap : snapshot.getChildren()){
                            MedicineModel model = snap.getValue(MedicineModel.class);
                            medicineModels.add(model);
                        }
                        Collections.shuffle(medicineModels);
                        binding.recyclerView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            DatabaseReference reference = firebase.getDatabaseReference("herbalSyrupData");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot snap : snapshot.getChildren()){
                            MedicineModel model = snap.getValue(MedicineModel.class);
                            medicineModels.add(model);
                        }
                        Collections.shuffle(medicineModels);
                        binding.recyclerView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        medicineDataExecutor.shutdown();
    }
}