package com.example.doctorbabu.patient.HomeModules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.doctorbabu.Adapters.MedicineAdapter;
import com.example.doctorbabu.DatabaseModels.MedicineModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityMedicineShopBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedicineShop extends AppCompatActivity {
    ActivityMedicineShopBinding binding;
    MedicineAdapter octMedicineAdapter;
    ArrayList<MedicineModel> octModel;
    ArrayList<String> genericNames;
    ExecutorService octExecutor,cartCounter;
    Firebase firebase;
    int countedCart = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineShopBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebase = Firebase.getInstance();
        loadImageSlider();
        octExecutor = Executors.newSingleThreadExecutor();
        cartCounter = Executors.newSingleThreadExecutor();
        octExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadOCTMedicines();
            }
        });
        cartCounter.execute(new Runnable() {
            @Override
            public void run() {
                setCartCounter();
            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        binding.cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MedicineShop.this,Cart.class);
                startActivity(intent);
            }
        });
    }

    public void setCartCounter(){
        FirebaseUser user = firebase.getUserID();
        DatabaseReference cartCounterReference = firebase.getDatabaseReference("medicineCart");
        cartCounterReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                countedCart = 0;
                if(snapshot.exists()){
                    for(DataSnapshot snap : snapshot.getChildren()){
                        countedCart += 1;
                    }
                    binding.cartCounter.setText(String.valueOf(countedCart));
                    binding.cartCounter.setVisibility(View.VISIBLE);
                } else {
                    binding.cartCounter.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void loadOCTMedicines(){
        octModel = new ArrayList<>();
        genericNames = new ArrayList<>();
        loadGenericNames(genericNames);
        binding.octMedicineRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false), R.layout.shimmer_layout_medicine);
        octMedicineAdapter = new MedicineAdapter(this, octModel);
        binding.octMedicineRecyclerView.setAdapter(octMedicineAdapter);
        binding.octMedicineRecyclerView.showShimmer();
        DatabaseReference reference = firebase.getDatabaseReference("medicineData");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snap : snapshot.getChildren()){
                        if(genericNames.contains(String.valueOf(snap.child("genericName").getValue()))){
                            MedicineModel model = snap.getValue(MedicineModel.class);
                            octModel.add(model);
                        }
                    }
                    Collections.shuffle(octModel);
                    octMedicineAdapter.notifyDataSetChanged();
                    binding.octMedicineRecyclerView.hideShimmer();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void loadGenericNames(ArrayList<String> genericNames){
        genericNames.add("Acetaminophen");
        genericNames.add("Ibuprofen");
        genericNames.add("Fexofenadine");
        genericNames.add("Loratadine");
        genericNames.add("Hydrocortisone creams");
        genericNames.add("Dextromethorphan");
        genericNames.add("Pseudoephedrine");
        genericNames.add("Bismuth subsalicylate");
        genericNames.add("Diphenhydramine");
        genericNames.add("Pseudoephedrine");
        genericNames.add("Paracetamol");
    }

    public void loadImageSlider() {
        ArrayList<Integer> images = new ArrayList<>();
        images.add(R.drawable.medicine_banner_1);
        images.add(R.drawable.medicine_banner_2);
        images.add(R.drawable.medicine_banner_3);
        sliderAdapter adapter = new sliderAdapter(images);
        binding.imageSlider.setSliderAdapter(adapter);
        binding.imageSlider.setIndicatorAnimation(IndicatorAnimationType.WORM);
        binding.imageSlider.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION);
        binding.imageSlider.startAutoCycle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        octExecutor.shutdown();
        cartCounter.shutdown();
    }
}