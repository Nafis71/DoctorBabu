package com.example.doctorbabu.patient.HomeModules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivitySyrupDetailsBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyrupDetails extends AppCompatActivity {
    ActivitySyrupDetailsBinding binding;
    Dialog dialog;
    ArrayList<String> bottleList;
    ExecutorService syrupDataExecutor;
    Firebase firebase;
    FirebaseUser user;
    int bottle = 1,unitPrice,syrupQuantity;
    double syrupPrice;
    String medicineId,syrupGenericName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySyrupDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingScreen();
        medicineId = getIntent().getStringExtra("medicineId");
        firebase = Firebase.getInstance();
        user = firebase.getUserID();
        syrupDataExecutor = Executors.newSingleThreadExecutor();
        syrupDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadSyrupData();
            }
        });
        closeLoadingScreen();
    }

    public void loadingScreen() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.loading_screen);
        dialog.setCancelable(false);
        dialog.show();
    }
    public void closeLoadingScreen() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                generateBottles();
                binding.syrupImage.setVisibility(View.VISIBLE);
                binding.purchaseLayout.setVisibility(View.VISIBLE);
                binding.relativeBrandLayout.setVisibility(View.VISIBLE);
                binding.syrupDescriptionLayout.setVisibility(View.VISIBLE);
                binding.medicalOverViewLayout.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        }, 2000);
    }

    public void generateBottles() {
        bottleList = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            if (i == 0) {
                String sheetString = (i + 1) + " " + "Bottle";
                bottleList.add(sheetString);
            } else {
                String sheetString = (i + 1) + " " + "Bottles";
                bottleList.add(sheetString);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.drop_menu, bottleList);
        binding.bottle.setAdapter(adapter);
        setBottleListener();
    }
    public void setBottleListener() {
        binding.bottle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String sheetText = binding.bottle.getText().toString();
                String[] sheetTextArray = sheetText.split(" ");
                bottle = Integer.parseInt(sheetTextArray[0]);
                syrupPrice = (bottle * unitPrice);
                binding.medicinePrice.setText(String.valueOf(syrupPrice));
            }
        });
    }

    public void loadSyrupData() {
        DatabaseReference reference = firebase.getDatabaseReference("syrupData");
        reference.child(medicineId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    setViews(snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }
    public void setViews(@NonNull DataSnapshot snapshot) {
        Glide.with(this).load(String.valueOf(snapshot.child("syrupPicture").getValue())).into(binding.syrupImage);
        binding.syrupName.setText(String.valueOf(snapshot.child("syrupName").getValue()));
        binding.description.setText(String.valueOf(snapshot.child("description").getValue()));
        binding.syrupBottleSize.setText(String.valueOf(snapshot.child("bottleSize").getValue()));
        syrupGenericName = String.valueOf(snapshot.child("genericName").getValue());
        binding.syrupGeneric.setText(syrupGenericName);
        binding.syrupBrandName.setText(String.valueOf(snapshot.child("brandName").getValue()));
        binding.syrupAdministration.setText(String.valueOf(snapshot.child("administrationOfTheMedicine").getValue()));
        binding.sideEffect.setText(String.valueOf(snapshot.child("sideEffect").getValue()));
        binding.storageCondition.setText(String.valueOf(snapshot.child("storageCondition").getValue()));
        syrupQuantity = Integer.parseInt(String.valueOf(snapshot.child("syrupQuantity").getValue()));
        calculatePrice(snapshot);
    }
    public void calculatePrice(@NonNull DataSnapshot snapshot) {
        unitPrice = Integer.parseInt(String.valueOf(snapshot.child("unitPrice").getValue()));
        syrupPrice = (bottle * unitPrice);
        binding.medicinePrice.setText(String.valueOf(syrupPrice));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        syrupDataExecutor.shutdown();
    }
}