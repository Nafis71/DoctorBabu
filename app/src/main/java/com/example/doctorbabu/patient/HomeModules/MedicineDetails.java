package com.example.doctorbabu.patient.HomeModules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.Adapters.MedicineAdapter;
import com.example.doctorbabu.DatabaseModels.MedicineModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityMedicineDetailsBinding;
import com.example.doctorbabu.patient.PatientProfileModule.EditProfile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.co.senab.photoview.PhotoViewAttacher;

public class MedicineDetails extends AppCompatActivity {
    ActivityMedicineDetailsBinding binding;
    String medicineId;
    ExecutorService medicineDataExecutor,relativeMedicineListExecutor;
    Firebase firebase;
    ArrayList<MedicineModel> model;
    MedicineAdapter adapter;
    String medicineGenericName;
    Dialog dialog;
    ArrayList<String> sheetList;
    int sheet = 1,sheetSize;
    double medicinePrice,perPiecePrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingScreen();
        binding = ActivityMedicineDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        medicineId = getIntent().getStringExtra("medicineId");
        firebase = Firebase.getInstance();
        medicineDataExecutor = Executors.newSingleThreadExecutor();
        relativeMedicineListExecutor = Executors.newSingleThreadExecutor();
        medicineDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadMedicineData();
            }
        });
        relativeMedicineListExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadRelativeMedicines();
            }
        });
        closeLoadingScreen();
    }

    public void loadingScreen(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.loading_screen);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void generateSheets(){
        sheetList = new ArrayList<>();
        for(int i=0;i<200;i++){
            if(i == 0){
                String sheetString = (i+1) +" "+"Sheet";
                sheetList.add(sheetString);
            }else{
                String sheetString = (i+1) +" "+"Sheets";
                sheetList.add(sheetString);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.drop_menu, sheetList);
        binding.sheet.setAdapter(adapter);
        setSheetListener();
    }

    public void setSheetListener(){
        binding.sheet.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String sheetText = binding.sheet.getText().toString();
                String[] sheetTextArray = sheetText.split(" ");
                sheet = Integer.parseInt(sheetTextArray[0]);
                medicinePrice = (perPiecePrice * sheetSize * sheet);
                Formatter formatter = new Formatter();
                formatter.format("%.2f",medicinePrice);
                String price = formatter.toString();
                binding.medicinePrice.setText(price);
            }
        });
    }


    public void closeLoadingScreen(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                generateSheets();
                binding.medicineImage.setVisibility(View.VISIBLE);
                binding.purchaseLayout.setVisibility(View.VISIBLE);
                binding.relativeBrandLayout.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        },2000);
    }

    public void loadMedicineData(){
        DatabaseReference reference = firebase.getDatabaseReference("medicineData");
        reference.child(medicineId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    setViews(snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }
    public void loadRelativeMedicines(){
        model =  new ArrayList<>();
        binding.relativeMedicineRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false), R.layout.shimmer_layout_medicine);
        adapter = new MedicineAdapter(this, model);
        binding.relativeMedicineRecyclerView.setAdapter(adapter);
        DatabaseReference reference = firebase.getDatabaseReference("medicineData");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snap : snapshot.getChildren()){
                        if(String.valueOf(snap.child("genericName").getValue()).equalsIgnoreCase(medicineGenericName) && !String.valueOf(snap.child("medicineId").getValue()).equalsIgnoreCase(medicineId)){
                            MedicineModel medicineModel = snap.getValue(MedicineModel.class);
                            model.add(medicineModel);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setViews(DataSnapshot snapshot){
        Glide.with(this).load(String.valueOf(snapshot.child("medicinePicture").getValue())).into(binding.medicineImage);
        binding.medicineName.setText(String.valueOf(snapshot.child("medicineName").getValue()));
        binding.medicineDosage.setText(String.valueOf(snapshot.child("medicineDosage").getValue()));
        medicineGenericName = String.valueOf(snapshot.child("genericName").getValue());
        binding.medicineGeneric.setText(medicineGenericName);
        binding.medicineBrandName.setText(String.valueOf(snapshot.child("brandName").getValue()));
        calculatePrice(snapshot);
    }
    public void calculatePrice(DataSnapshot snapshot){
        perPiecePrice = Double.parseDouble(String.valueOf(snapshot.child("medicinePerPiecePrice").getValue()));
        sheetSize  = Integer.parseInt(String.valueOf(snapshot.child("medicinePataSize").getValue()));
        medicinePrice = (perPiecePrice * sheetSize * sheet);
        String price = String.valueOf(medicinePrice);
        binding.medicinePrice.setText(price);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        medicineDataExecutor.shutdown();
        relativeMedicineListExecutor.shutdown();
    }
}