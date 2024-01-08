package com.example.doctorbabu.patient.HomeModules;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.Adapters.SyrupAdapter;
import com.example.doctorbabu.DatabaseModels.MedicineModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivitySyrupDetailsBinding;
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
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyrupDetails extends AppCompatActivity {
    ActivitySyrupDetailsBinding binding;
    Dialog dialog;
    ArrayList<String> bottleList;
    ArrayList<MedicineModel> model;
    SyrupAdapter adapter;
    ExecutorService syrupDataExecutor, cartExecutor, cartCounterExecutor, relativeSyrupListExecutor, countExecutor;
    Firebase firebase;
    FirebaseUser user;
    int bottle = 1, unitPrice, syrupQuantity, countedCart, count;
    double syrupPrice;
    String syrupId, syrupGenericName;
    boolean hasCheckedRelativeHerbal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySyrupDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingScreen();
        syrupId = getIntent().getStringExtra("syrupId");
        firebase = Firebase.getInstance();
        user = firebase.getUserID();
        syrupDataExecutor = Executors.newSingleThreadExecutor();
        cartExecutor = Executors.newSingleThreadExecutor();
        cartCounterExecutor = Executors.newSingleThreadExecutor();
        relativeSyrupListExecutor = Executors.newSingleThreadExecutor();
        countExecutor = Executors.newSingleThreadExecutor();
        syrupDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadSyrupData();
            }
        });
        relativeSyrupListExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadRelativeSyrups("syrupData");
            }
        });
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
        cartCounterExecutor.execute(new Runnable() {
            @Override
            public void run() {
                setCartCounter();
            }
        });
        countExecutor.execute(new Runnable() {
            @Override
            public void run() {
                countMedicineViewData();
            }
        });
        binding.cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SyrupDetails.this, Cart.class);
                startActivity(intent);
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
                binding.addToCart.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        }, 2000);
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
        int selectedbottles = bottle;
        DatabaseReference checkCartReference = firebase.getDatabaseReference("medicineCart");
        checkCartReference.child(user.getUid()).child(syrupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int quantity = Integer.parseInt(String.valueOf(snapshot.child("quantity").getValue()));
                    checkSyrupQuantity(quantity + selectedbottles, "syrupData");
                } else {
                    checkSyrupQuantity(selectedbottles, "syrupData");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void checkSyrupQuantity(int selectedbottles, String databaseReference) {
        DatabaseReference quantityReference = firebase.getDatabaseReference(databaseReference);
        quantityReference.child(syrupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int quantity = Integer.parseInt(String.valueOf(snapshot.child("syrupQuantity").getValue()));
                    if (quantity >= selectedbottles) {
                        saveToCart(selectedbottles, databaseReference);
                    } else {
                        CookieBar.build(SyrupDetails.this)
                                .setTitle("Not Enough Bottles Available")
                                .setMessage(quantity + " " + " Bottles available to purchase")
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
                } else {
                    checkSyrupQuantity(selectedbottles, "herbalSyrupData");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });

    }

    public void saveToCart(int selectedbottles, String databaseReference) {
        DatabaseReference cartReference = firebase.getDatabaseReference("medicineCart");
        String syrupName = binding.syrupName.getText().toString();
        double totalPrice = selectedbottles * unitPrice;
        HashMap<String, String> data = new HashMap<>();
        data.put("medicineId", syrupId);
        data.put("quantity", String.valueOf(selectedbottles));
        data.put("totalPrice", String.valueOf(totalPrice));
        if (databaseReference.equalsIgnoreCase("syrupData")) {
            data.put("medicineType", "syrup");
        } else {
            data.put("medicineType", "herbalSyrup");
        }
        cartReference.child(user.getUid()).child(syrupId).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                CookieBar.build(SyrupDetails.this)
                        .setTitle("Added to cart")
                        .setMessage(selectedbottles + " " + " Bottles of " + syrupName + " " + " added to cart successfully!")
                        .setTitleColor(R.color.white)
                        .setBackgroundColor(R.color.blue)
                        .setCookiePosition(CookieBar.TOP)
                        .setDuration(2500)
                        .show();
                setCartCounter();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                CookieBar.build(SyrupDetails.this)
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

    public void countMedicineViewData() {
        DatabaseReference countReference = firebase.getDatabaseReference("trackMedicine");
        countReference.child(syrupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        count += Integer.parseInt(String.valueOf(snap.child("count").getValue()));
                    }
                    binding.viewingStats.setText(String.valueOf(count));
                } else {
                    binding.viewingStats.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void setCartCounter() {
        DatabaseReference cartCounterReference = firebase.getDatabaseReference("medicineCart");
        cartCounterReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                countedCart = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        countedCart += 1;
                    }
                    binding.cartCounters.setText(String.valueOf(countedCart));
                    binding.cartCounters.setVisibility(View.VISIBLE);
                } else {
                    binding.cartCounters.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
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
        reference.child(syrupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    setViews(snapshot);
                } else {
                    loadHerbalSyrup();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void loadHerbalSyrup() {
        DatabaseReference reference = firebase.getDatabaseReference("herbalSyrupData");
        reference.child(syrupId).addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void loadRelativeSyrups(String databaseReference) {
        model = new ArrayList<>();
        binding.relativeMedicineRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false), R.layout.shimmer_layout_medicine);
        adapter = new SyrupAdapter(this, model);
        binding.relativeMedicineRecyclerView.setAdapter(adapter);
        DatabaseReference reference = firebase.getDatabaseReference(databaseReference);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        if (String.valueOf(snap.child("genericName").getValue()).equalsIgnoreCase(syrupGenericName) && !String.valueOf(snap.child("medicineId").getValue()).equalsIgnoreCase(syrupId)) {
                            MedicineModel syrupModel = snap.getValue(MedicineModel.class);
                            model.add(syrupModel);
                        }
                    }
                    if (model.size() == 0 && !hasCheckedRelativeHerbal) {
                        hasCheckedRelativeHerbal = true;
                        loadRelativeSyrups("herbalSyrupData");
                    }
                    Collections.shuffle(model);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void setViews(@NonNull DataSnapshot snapshot) {
        Glide.with(this).load(String.valueOf(snapshot.child("medicinePicture").getValue())).into(binding.syrupImage);
        binding.syrupName.setText(String.valueOf(snapshot.child("medicineName").getValue()));
        binding.description.setText(String.valueOf(snapshot.child("description").getValue()));
        binding.syrupBottleSize.setText(String.valueOf(snapshot.child("bottleSize").getValue()));
        syrupGenericName = String.valueOf(snapshot.child("genericName").getValue());
        binding.syrupGeneric.setText(syrupGenericName);
        binding.syrupBrandName.setText(String.valueOf(snapshot.child("brandName").getValue()));
        binding.syrupAdministration.setText(String.valueOf(snapshot.child("administrationOfTheMedicine").getValue()));
        binding.sideEffect.setText(String.valueOf(snapshot.child("sideEffect").getValue()));
        binding.storageCondition.setText(String.valueOf(snapshot.child("storageCondition").getValue()));
        syrupQuantity = Integer.parseInt(String.valueOf(snapshot.child("medicineQuantity").getValue()));
        calculatePrice(snapshot);
    }

    public void calculatePrice(@NonNull DataSnapshot snapshot) {
        unitPrice = Integer.parseInt(String.valueOf(snapshot.child("unitPrice").getValue()));
        syrupPrice = (bottle * unitPrice);
        binding.medicinePrice.setText(String.valueOf(syrupPrice));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cartCounterExecutor.execute(new Runnable() {
            @Override
            public void run() {
                setCartCounter();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        syrupDataExecutor.shutdown();
        relativeSyrupListExecutor.shutdown();
        cartExecutor.shutdown();
        cartCounterExecutor.shutdown();
        countExecutor.shutdown();
    }
}