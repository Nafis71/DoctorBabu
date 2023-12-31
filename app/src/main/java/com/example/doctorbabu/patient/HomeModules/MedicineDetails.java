package com.example.doctorbabu.patient.HomeModules;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.Adapters.MedicineAdapter;
import com.example.doctorbabu.DatabaseModels.MedicineModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityMedicineDetailsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.aviran.cookiebar2.CookieBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedicineDetails extends AppCompatActivity {
    ActivityMedicineDetailsBinding binding;
    String medicineId;
    ExecutorService medicineDataExecutor, relativeMedicineListExecutor, cartExecutor;
    Firebase firebase;
    ArrayList<MedicineModel> model;
    MedicineAdapter adapter;
    String medicineGenericName;
    FirebaseUser user;
    Dialog dialog;
    ArrayList<String> sheetList;
    int sheet = 1, sheetSize, medicineQuantity;
    double medicinePrice, perPiecePrice;

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
        cartExecutor = Executors.newSingleThreadExecutor();
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
        binding.addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cartExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        addtoCart();
                    }
                });
            }
        });
    }

    public void addtoCart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.addToCart.setEnabled(false);
            }
        });
        checkCart();
    }

    public void checkCart() {
        int selectedSheets = sheet;
        user = firebase.getUserID();
        DatabaseReference checkCartReference = firebase.getDatabaseReference("medicineCart");
        checkCartReference.child(user.getUid()).child(medicineId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int medicineSheets = Integer.parseInt(String.valueOf(snapshot.child("medicineSheets").getValue()));
                    checkQuantity(medicineSheets + selectedSheets);
                } else {
                    checkQuantity(selectedSheets);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void checkQuantity(int medicineSheets) {
        DatabaseReference quantityReference = firebase.getDatabaseReference("medicineData");
        quantityReference.child(medicineId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int quantity = Integer.parseInt(String.valueOf(snapshot.child("medicineQuantity").getValue()));
                    if (quantity >= medicineSheets) {
                        saveToCart(medicineSheets);
                    } else {
                        CookieBar.build(MedicineDetails.this)
                                .setTitle("Not Enough Sheets")
                                .setMessage(quantity + " " + " Sheets available to purchase")
                                .setTitleColor(R.color.white)
                                .setBackgroundColor(R.color.dark_red)
                                .setCookiePosition(CookieBar.TOP)
                                .setDuration(2500)
                                .show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.addToCart.setEnabled(true);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });

    }

    public void saveToCart(int totalSheets) {
        DatabaseReference cartReference = firebase.getDatabaseReference("medicineCart");
        String medicineName = binding.medicineName.getText().toString();
        double totalPrice = Math.ceil(perPiecePrice * totalSheets * sheetSize);
        HashMap<String, String> data = new HashMap<>();
        data.put("medicineId", medicineId);
        data.put("medicineSheets", String.valueOf(totalSheets));
        data.put("totalPrice", String.valueOf(totalPrice));
        cartReference.child(user.getUid()).child(medicineId).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                CookieBar.build(MedicineDetails.this)
                        .setTitle("Added to cart")
                        .setMessage(totalSheets+" "+" Sheets of "+medicineName + " " + " added to cart successfully!")
                        .setTitleColor(R.color.white)
                        .setBackgroundColor(R.color.blue)
                        .setCookiePosition(CookieBar.TOP)
                        .setDuration(2500)
                        .show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                CookieBar.build(MedicineDetails.this)
                        .setTitle("Failed to add")
                        .setMessage("Please check your internet connection and try again")
                        .setTitleColor(R.color.white)
                        .setBackgroundColor(R.color.dark_red)
                        .setCookiePosition(CookieBar.TOP)
                        .setDuration(2500)
                        .show();
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.addToCart.setEnabled(true);
            }
        });
    }

    public void loadingScreen() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.loading_screen);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void generateSheets() {
        sheetList = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            if (i == 0) {
                String sheetString = (i + 1) + " " + "Sheet";
                sheetList.add(sheetString);
            } else {
                String sheetString = (i + 1) + " " + "Sheets";
                sheetList.add(sheetString);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.drop_menu, sheetList);
        binding.sheet.setAdapter(adapter);
        setSheetListener();
    }

    public void setSheetListener() {
        binding.sheet.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String sheetText = binding.sheet.getText().toString();
                String[] sheetTextArray = sheetText.split(" ");
                sheet = Integer.parseInt(sheetTextArray[0]);
                medicinePrice = (perPiecePrice * sheetSize * sheet);
                Formatter formatter = new Formatter();
                formatter.format("%.2f", medicinePrice);
                String price = formatter.toString();
                binding.medicinePrice.setText(price);
            }
        });
    }


    public void closeLoadingScreen() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                generateSheets();
                binding.medicineImage.setVisibility(View.VISIBLE);
                binding.purchaseLayout.setVisibility(View.VISIBLE);
                binding.relativeBrandLayout.setVisibility(View.VISIBLE);
                binding.descriptionLayout.setVisibility(View.VISIBLE);
                binding.medicalOverViewLayout.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        }, 2000);
    }

    public void loadMedicineData() {
        DatabaseReference reference = firebase.getDatabaseReference("medicineData");
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

    public void loadRelativeMedicines() {
        model = new ArrayList<>();
        binding.relativeMedicineRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false), R.layout.shimmer_layout_medicine);
        adapter = new MedicineAdapter(this, model);
        binding.relativeMedicineRecyclerView.setAdapter(adapter);
        DatabaseReference reference = firebase.getDatabaseReference("medicineData");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        if (String.valueOf(snap.child("genericName").getValue()).equalsIgnoreCase(medicineGenericName) && !String.valueOf(snap.child("medicineId").getValue()).equalsIgnoreCase(medicineId)) {
                            MedicineModel medicineModel = snap.getValue(MedicineModel.class);
                            model.add(medicineModel);
                        }
                    }
                    Collections.shuffle(model);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setViews(@NonNull DataSnapshot snapshot) {
        Glide.with(this).load(String.valueOf(snapshot.child("medicinePicture").getValue())).into(binding.medicineImage);
        binding.medicineName.setText(String.valueOf(snapshot.child("medicineName").getValue()));
        binding.medicineDosage.setText(String.valueOf(snapshot.child("medicineDosage").getValue()));
        medicineGenericName = String.valueOf(snapshot.child("genericName").getValue());
        binding.medicineGeneric.setText(medicineGenericName);
        binding.medicineBrandName.setText(String.valueOf(snapshot.child("brandName").getValue()));
        binding.medicineAdministration.setText(String.valueOf(snapshot.child("administrationOfTheMedicine").getValue()));
        binding.sideEffect.setText(String.valueOf(snapshot.child("sideEffect").getValue()));
        binding.overdoseEffect.setText(String.valueOf(snapshot.child("overdoseEffects").getValue()));
        binding.storageCondition.setText(String.valueOf(snapshot.child("storageCondition").getValue()));
        medicineQuantity = Integer.parseInt(String.valueOf(snapshot.child("medicineQuantity").getValue()));
        setAlcoholSafetyDescription(snapshot);
        setDrivingSafetyDescription(snapshot);
        setPregnancySafetyDescription(snapshot);
        setKidneySafetyDescription(snapshot);
        setLiverSafetyDescription(snapshot);
        calculatePrice(snapshot);
    }

    @SuppressLint("SetTextI18n")
    public void setAlcoholSafetyDescription(@NonNull DataSnapshot snapshot) {
        if (String.valueOf(snapshot.child("alcoholEffect").getValue()).equalsIgnoreCase("safe")) {
            binding.alcoholStatus.setText("Safe");
            binding.alcoholStatus.setTextColor(Color.parseColor("#52BE80"));
            binding.alcoholDescription.setText(R.string.alcoholSafe);
        } else {
            binding.alcoholStatus.setText("Unsafe");
            binding.alcoholStatus.setTextColor(Color.parseColor("#F08080"));
            binding.alcoholDescription.setText(R.string.alcoholUnSafe);
        }
    }

    @SuppressLint("SetTextI18n")
    public void setDrivingSafetyDescription(@NonNull DataSnapshot snapshot) {
        if (String.valueOf(snapshot.child("drivingEffect").getValue()).equalsIgnoreCase("safe")) {
            binding.drivingStatus.setText("Safe");
            binding.drivingStatus.setTextColor(Color.parseColor("#52BE80"));
            binding.drivingDescription.setText(R.string.drivingSafe);
        } else {
            binding.drivingStatus.setText("Unsafe");
            binding.drivingStatus.setTextColor(Color.parseColor("#F08080"));
            binding.drivingDescription.setText(R.string.drivingUnSafe);
        }
    }

    @SuppressLint("SetTextI18n")
    public void setPregnancySafetyDescription(@NonNull DataSnapshot snapshot) {
        if (String.valueOf(snapshot.child("pregnancyAndLactation").getValue()).equalsIgnoreCase("safe")) {
            binding.pregnancyStatus.setText("Safe");
            binding.pregnancyStatus.setTextColor(Color.parseColor("#52BE80"));
            binding.pregnancyDescription.setText(R.string.pregnancySafe);
        } else if (String.valueOf(snapshot.child("pregnancyAndLactation").getValue()).equalsIgnoreCase("caution")) {
            binding.pregnancyStatus.setText("Caution");
            binding.pregnancyStatus.setTextColor(Color.parseColor("#F1C40F"));
            binding.pregnancyDescription.setText(R.string.pregnancyCaution);
        } else if (String.valueOf(snapshot.child("pregnancyAndLactation").getValue()).equalsIgnoreCase("consult")) {
            binding.pregnancyStatus.setText("Consult");
            binding.pregnancyStatus.setTextColor(Color.parseColor("#52BE80"));
            binding.pregnancyDescription.setText(R.string.pregnancyConsult);
        } else {
            binding.pregnancyStatus.setText("Unsafe");
            binding.pregnancyStatus.setTextColor(Color.parseColor("#F08080"));
            binding.pregnancyDescription.setText(R.string.pregnancyUnSafe);
        }
    }

    @SuppressLint("SetTextI18n")
    public void setKidneySafetyDescription(@NonNull DataSnapshot snapshot) {
        if (String.valueOf(snapshot.child("kidneyEffect").getValue()).equalsIgnoreCase("safe")) {
            binding.kidneyStatus.setText("Safe");
            binding.kidneyStatus.setTextColor(Color.parseColor("#52BE80"));
            binding.kidneyDescription.setText(R.string.kidneySafe);
        } else if (String.valueOf(snapshot.child("kidneyEffect").getValue()).equalsIgnoreCase("caution")) {
            binding.kidneyStatus.setText("Caution");
            binding.kidneyStatus.setTextColor(Color.parseColor("#F1C40F"));
            binding.kidneyDescription.setText(R.string.kidneyCaution);
        } else {
            binding.kidneyStatus.setText("Unsafe");
            binding.kidneyStatus.setTextColor(Color.parseColor("#F08080"));
            binding.kidneyDescription.setText(R.string.kidneyUnSafe);
        }
    }

    @SuppressLint("SetTextI18n")
    public void setLiverSafetyDescription(@NonNull DataSnapshot snapshot) {
        if (String.valueOf(snapshot.child("liverEffect").getValue()).equalsIgnoreCase("safe")) {
            binding.liverStatus.setText("Safe");
            binding.liverStatus.setTextColor(Color.parseColor("#52BE80"));
            binding.liverDescription.setText(R.string.liverSafe);
        } else if (String.valueOf(snapshot.child("liverEffect").getValue()).equalsIgnoreCase("caution")) {
            binding.liverStatus.setText("Caution");
            binding.liverStatus.setTextColor(Color.parseColor("#F1C40F"));
            binding.liverDescription.setText(R.string.liverCaution);
        } else {
            binding.liverStatus.setText("Unsafe");
            binding.liverStatus.setTextColor(Color.parseColor("#F08080"));
            binding.liverDescription.setText(R.string.liverUnSafe);
        }
    }

    public void calculatePrice(@NonNull DataSnapshot snapshot) {
        perPiecePrice = Double.parseDouble(String.valueOf(snapshot.child("medicinePerPiecePrice").getValue()));
        sheetSize = Integer.parseInt(String.valueOf(snapshot.child("medicinePataSize").getValue()));
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