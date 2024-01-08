package com.example.doctorbabu.patient.HomeModules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.example.doctorbabu.Adapters.TabletAdapter;
import com.example.doctorbabu.Adapters.SyrupAdapter;
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
    TabletAdapter octMedicineAdapter;
    SyrupAdapter syrupAdapter;
    SyrupAdapter herbalSyrupAdapter;
    ArrayList<MedicineModel> octModel;
    ArrayList<MedicineModel> syrupModels;
    ArrayList<MedicineModel> syrupList;
    ArrayList<MedicineModel> tabletList;
    ArrayList<MedicineModel> herbalSyrupModels;
    ArrayList<String> genericNames;
    ExecutorService octExecutor,cartCounter,syrupExecutor,herbalSyrupExecutor,searchExecutor;
    Firebase firebase;
    int countedCart = 0;
    boolean isSearchActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineShopBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebase = Firebase.getInstance();
        loadImageSlider();
        octExecutor = Executors.newSingleThreadExecutor();
        cartCounter = Executors.newSingleThreadExecutor();
        syrupExecutor = Executors.newSingleThreadExecutor();
        herbalSyrupExecutor = Executors.newSingleThreadExecutor();
        searchExecutor = Executors.newSingleThreadExecutor();
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
        syrupExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadSyrups();
            }
        });
        herbalSyrupExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadHerbalSyrups();
            }
        });
        searchExecutor.execute(new Runnable() {
            @Override
            public void run() {
                setSearch();
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

    public void setSearch(){
        binding.searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    isSearchActive = true;
                    binding.header.setVisibility(View.GONE);
                    binding.medicineDeliveryBannerLayout.setVisibility(View.GONE);
                    binding.octMedicineLayout.setVisibility(View.GONE);
                    binding.sliderLayout.setVisibility(View.GONE);
                    binding.syrupLayout.setVisibility(View.GONE);
                    binding.herbalLayout.setVisibility(View.GONE);
                    binding.prescriptionLayout.setVisibility(View.GONE);
                    binding.mainBody.setBackgroundColor(Color.parseColor("#ffffff"));
                    searchMedicine();
                }
            }
        });
        tabletList = new ArrayList<>();
        syrupList = new ArrayList<>();
        getAllSyrupData();
        getAllTabletData();
    }

    public void getAllSyrupData(){
        ArrayList<String> SyrupReference = new ArrayList<>();
        SyrupReference.add("syrupData");
        SyrupReference.add("herbalSyrupData");
        for(String databaseReference : SyrupReference){
            DatabaseReference reference = firebase.getDatabaseReference(databaseReference);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot snap : snapshot.getChildren()){
                            MedicineModel model = snap.getValue(MedicineModel.class);
                            syrupList.add(model);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        }
    }
    public void getAllTabletData(){
        ArrayList<String> tabletReference = new ArrayList<>();
        tabletReference.add("tabletData");
        for(String databaseReference : tabletReference){
            DatabaseReference reference = firebase.getDatabaseReference(databaseReference);
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        for(DataSnapshot snap : snapshot.getChildren()){
                            MedicineModel model = snap.getValue(MedicineModel.class);
                            tabletList.add(model);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        }
    }

    public void searchMedicine(){
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                if (!searchQuery.isEmpty() && !searchQuery.equals(" ")) {
                    binding.searchRecyclerView.setVisibility(View.VISIBLE);
                    binding.searchRecyclerView.showShimmer();
//                    filterList(searchQuery);
                } else {
                    binding.searchRecyclerView.setVisibility(View.GONE);
                }
                return true;
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
                throw error.toException();
            }
        });
    }

    public void loadOCTMedicines(){
        octModel = new ArrayList<>();
        genericNames = new ArrayList<>();
        loadGenericNames(genericNames);
        binding.octMedicineRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false), R.layout.shimmer_layout_medicine);
        octMedicineAdapter = new TabletAdapter(this, octModel);
        binding.octMedicineRecyclerView.setAdapter(octMedicineAdapter);
        binding.octMedicineRecyclerView.showShimmer();
        DatabaseReference reference = firebase.getDatabaseReference("tabletData");
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
                } else {
                    binding.octMedicineRecyclerView.hideShimmer();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void loadSyrups(){
        syrupModels = new ArrayList<>();
        binding.syrupRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false), R.layout.shimmer_layout_medicine);
        syrupAdapter = new SyrupAdapter(this, syrupModels);
        binding.syrupRecyclerView.setAdapter(syrupAdapter);
        binding.syrupRecyclerView.showShimmer();
        DatabaseReference syrupReference = firebase.getDatabaseReference("syrupData");
        syrupReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snap : snapshot.getChildren()){
                       MedicineModel model = snap.getValue(MedicineModel.class);
                       syrupModels.add(model);
                    }
                    Collections.shuffle(syrupModels);
                    syrupAdapter.notifyDataSetChanged();
                    binding.syrupRecyclerView.hideShimmer();
                } else{
                    binding.syrupRecyclerView.hideShimmer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }
    public void loadHerbalSyrups(){
        herbalSyrupModels = new ArrayList<>();
        binding.herbalRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false), R.layout.shimmer_layout_medicine);
        herbalSyrupAdapter = new SyrupAdapter(this, herbalSyrupModels);
        binding.herbalRecyclerView.setAdapter(herbalSyrupAdapter);
        binding.herbalRecyclerView.showShimmer();
        DatabaseReference syrupReference = firebase.getDatabaseReference("herbalSyrupData");
        syrupReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snap : snapshot.getChildren()){
                        MedicineModel model = snap.getValue(MedicineModel.class);
                        herbalSyrupModels.add(model);
                    }
                    Collections.shuffle(herbalSyrupModels);
                    herbalSyrupAdapter.notifyDataSetChanged();
                    binding.herbalRecyclerView.hideShimmer();
                } else{
                    binding.herbalRecyclerView.hideShimmer();
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
        syrupExecutor.shutdown();
        herbalSyrupExecutor.shutdown();
        searchExecutor.shutdown();
    }
}