package com.example.doctorbabu.patient.MedicinePurchaseModules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.doctorbabu.Adapters.AllMedicineAdapter;
import com.example.doctorbabu.Adapters.MedicineSearchAdapter;
import com.example.doctorbabu.Adapters.TabletAdapter;
import com.example.doctorbabu.Adapters.SyrupAdapter;
import com.example.doctorbabu.DatabaseModels.MedicineModel;
import com.example.doctorbabu.DatabaseModels.MedicineSearchModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityMedicineShopBinding;
import com.example.doctorbabu.patient.HomeModules.sliderAdapter;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedicineShop extends AppCompatActivity {
    ActivityMedicineShopBinding binding;
    TabletAdapter octMedicineAdapter;
    SyrupAdapter syrupAdapter;
    SyrupAdapter herbalSyrupAdapter;
    MedicineSearchAdapter medicineSearchAdapter;
    AllMedicineAdapter allMedicineAdapter;
    ArrayList<MedicineModel> octModel;
    ArrayList<MedicineModel> syrupModels;
    ArrayList<MedicineSearchModel> syrupList;
    ArrayList<MedicineSearchModel> tabletList;
    ArrayList<MedicineSearchModel> medicineList;
    ArrayList<MedicineModel> herbalSyrupModels;
    ArrayList<MedicineModel> allMedicines;
    ArrayList<String> genericNames;
    ExecutorService octExecutor,cartCounter,syrupExecutor,herbalSyrupExecutor,searchExecutor, imageSliderExecutor,allMedicineExecutor,orderCounter;
    Firebase firebase;
    int countedCart = 0,countedOrder=0;
    int loadedMedicine = 0;
    boolean isSearchActive;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineShopBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebase = Firebase.getInstance();
        medicineList = new ArrayList<>();
        allMedicines = new ArrayList<>();
        loadingScreen();
        imageSliderExecutor = Executors.newSingleThreadExecutor();
        octExecutor = Executors.newSingleThreadExecutor();
        cartCounter = Executors.newSingleThreadExecutor();
        syrupExecutor = Executors.newSingleThreadExecutor();
        herbalSyrupExecutor = Executors.newSingleThreadExecutor();
        searchExecutor = Executors.newSingleThreadExecutor();
        allMedicineExecutor = Executors.newSingleThreadExecutor();
        orderCounter = Executors.newSingleThreadExecutor();
        imageSliderExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadImageSlider();
            }
        });
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
        orderCounter.execute(new Runnable() {
            @Override
            public void run() {
                setOrderCounter();
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
        allMedicineExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadAllMedicines();
            }
        });
        closeLoadingScreen();
        PushDownAnim.setPushDownAnimTo(binding.octAll, binding.cart,binding.syrupAll,binding.herbalAll,binding.uploadPrescription)
                .setScale(PushDownAnim.MODE_SCALE, 0.95f);
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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
        binding.octAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCategorialMedicineList("tablet");
            }
        });
        binding.syrupAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCategorialMedicineList("syrup");
            }
        });
        binding.herbalAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchCategorialMedicineList("herbalSyrup");
            }
        });
        binding.uploadPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MedicineShop.this,ShopByPrescription.class);
                startActivity(intent);
            }
        });
        binding.orderList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MedicineShop.this, OrderList.class);
                startActivity(intent);
            }
        });
    }

    public void launchCategorialMedicineList(String medicineType){
        Intent intent = new Intent(MedicineShop.this, SeeAll.class);
        intent.putExtra("medicineType",medicineType);
        startActivity(intent);
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
                binding.header.setVisibility(View.VISIBLE);
                binding.mainBody.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        }, 2000);
    }

    public void setSearch(){
        binding.searchRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false), R.layout.shimmer_layout_medicine_search_layout);
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
                    binding.allMedicineLayout.setVisibility(View.GONE);
                    binding.searchDataLayout.setVisibility(View.VISIBLE);
                    binding.mainBody.setBackgroundColor(Color.parseColor("#ffffff"));
                    searchMedicine();
                }
            }
        });
        binding.searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                restoreView();
                Toast.makeText(MedicineShop.this, "CLose button pressed", Toast.LENGTH_SHORT).show();
                return true;
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
                            MedicineSearchModel model = snap.getValue(MedicineSearchModel.class);
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

    public void loadAllMedicines(){
        while (true){
            Log.w("Loaded Medicine",String.valueOf(loadedMedicine));
            if(loadedMedicine == 3){
                allMedicines.addAll(syrupModels);
                allMedicines.addAll(herbalSyrupModels);
                Collections.shuffle(allMedicines);
                binding.allMedicineRecyclerView.setLayoutManager(new GridLayoutManager(MedicineShop.this, 3));
                allMedicineAdapter = new AllMedicineAdapter(MedicineShop.this,allMedicines);
                binding.allMedicineRecyclerView.setAdapter(allMedicineAdapter);
                break;
            }
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
                            MedicineSearchModel model = snap.getValue(MedicineSearchModel.class);
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
            public boolean onQueryTextSubmit(String searchQuery) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                if (!searchQuery.isEmpty() && !searchQuery.equals(" ") && !searchQuery.equals("[") && !searchQuery.equals("]")) {
                    binding.searchDataLayout.setVisibility(View.GONE);
                    binding.searchResultTxt.setVisibility(View.VISIBLE);
                    filterList(searchQuery);
                } else {
                    binding.searchResultTxt.setVisibility(View.GONE);
                    binding.searchDataLayout.setVisibility(View.VISIBLE);
                    binding.noDataLayout.setVisibility(View.GONE);
                    binding.searchRecyclerView.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    public void filterList(String searchQuery){
        String combinedName;
        medicineList.clear();
        ArrayList<MedicineSearchModel> filteredList = new ArrayList<>();
        medicineSearchAdapter = new MedicineSearchAdapter(this, filteredList);
        medicineList.addAll(syrupList);
        medicineList.addAll(tabletList);
        for (MedicineSearchModel medicine : medicineList) {
            combinedName = medicine.getMedicineName()+" "+medicine.getMedicineDosage();
            if (combinedName.toLowerCase().contains(searchQuery.toLowerCase())) {
                filteredList.add(medicine);
            }else if(medicine.getMedicineName().toLowerCase().contains(searchQuery.toLowerCase()) | medicine.getMedicineDosage().toLowerCase().contains(searchQuery.toLowerCase()) | medicine.getGenericName().toLowerCase().contains(searchQuery.toLowerCase())){
                filteredList.add(medicine);
            }
        }
        Handler handler = new Handler();
        if (filteredList.isEmpty()) {
            handler.postDelayed(() -> {
                binding.searchRecyclerView.setVisibility(View.GONE);
                binding.noDataLayout.setVisibility(View.VISIBLE);
            }, 200);

        } else {
            handler.postDelayed(() -> {
                binding.searchRecyclerView.setAdapter(medicineSearchAdapter);
                binding.searchRecyclerView.setVisibility(View.VISIBLE);
                binding.noDataLayout.setVisibility(View.GONE);
            }, 200);
        }
    }

    public void setOrderCounter(){
        FirebaseUser user = firebase.getUserID();
        DatabaseReference cartCounterReference = firebase.getDatabaseReference("medicineOrders");
        cartCounterReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                countedOrder = 0;
                if(snapshot.exists()){
                    for(DataSnapshot snap : snapshot.getChildren()){
                        countedOrder += 1;
                    }
                    binding.orderCounter.setText(String.valueOf(countedOrder));
                    binding.orderCounter.setVisibility(View.VISIBLE);
                } else {
                    binding.orderCounter.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void setCartCounter(){
        FirebaseUser user = firebase.getUserID();
        DatabaseReference cartCounterReference = firebase.getDatabaseReference("medicineCart");
        cartCounterReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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
                MedicineModel model;
                if(snapshot.exists()){
                    for(DataSnapshot snap : snapshot.getChildren()){
                        if(genericNames.contains(String.valueOf(snap.child("genericName").getValue()))){
                            model = snap.getValue(MedicineModel.class);
                            octModel.add(model); //for oct tablets
                        }
                        model = snap.getValue(MedicineModel.class); //for all tablets
                        allMedicines.add(model);
                    }
                    loadedMedicine++;
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
                    loadedMedicine++;
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
                    loadedMedicine++;
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
        genericNames.add("Fexofenadine Hydrochloride");
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
    public void restoreView(){
        binding.header.setVisibility(View.VISIBLE);
        binding.medicineDeliveryBannerLayout.setVisibility(View.VISIBLE);
        binding.octMedicineLayout.setVisibility(View.VISIBLE);
        binding.sliderLayout.setVisibility(View.VISIBLE);
        binding.syrupLayout.setVisibility(View.VISIBLE);
        binding.herbalLayout.setVisibility(View.VISIBLE);
        binding.prescriptionLayout.setVisibility(View.VISIBLE);
        binding.allMedicineLayout.setVisibility(View.VISIBLE);
        binding.mainBody.setBackgroundColor(Color.parseColor("#F8F9F9"));
        binding.searchRecyclerView.setVisibility(View.GONE);
        binding.searchDataLayout.setVisibility(View.GONE);
        binding.noDataLayout.setVisibility(View.GONE);
        binding.searchResultTxt.setVisibility(View.GONE);
        binding.searchView.setQuery("",false);
        binding.searchView.clearFocus();
    }

    @Override
    public void onBackPressed() {
        if(isSearchActive){
            isSearchActive = false;
            restoreView();
        }else{
            loadedMedicine = 0;
            finish();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        cartCounter.execute(new Runnable() {
            @Override
            public void run() {
                setCartCounter();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loadedMedicine = 0;
        octExecutor.shutdown();
        cartCounter.shutdown();
        syrupExecutor.shutdown();
        imageSliderExecutor.shutdown();
        herbalSyrupExecutor.shutdown();
        searchExecutor.shutdown();
        allMedicineExecutor.shutdown();
        orderCounter.shutdown();
    }
}