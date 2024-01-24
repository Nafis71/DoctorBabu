package com.example.doctorbabu.patient.MedicinePurchaseModules;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.doctorbabu.Adapters.AllMedicineAdapter;
import com.example.doctorbabu.Adapters.MedicineSearchAdapter;
import com.example.doctorbabu.DatabaseModels.MedicineModel;
import com.example.doctorbabu.DatabaseModels.MedicineSearchModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivitySeeAllBinding;
import com.google.firebase.auth.FirebaseUser;
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
    ExecutorService medicineDataExecutor, cartCounter, searchExecutor;
    AllMedicineAdapter adapter;
    ArrayList<MedicineModel> medicineModels;
    ArrayList<MedicineSearchModel> medicineList;
    MedicineSearchAdapter medicineSearchAdapter;
    ArrayList<String> genericNames;
    Firebase firebase;
    Dialog dialog;
    String medicineType;
    int countedCart;
    boolean isSearchActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeeAllBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        medicineType = getIntent().getStringExtra("medicineType");
        firebase = Firebase.getInstance();
        loadingScreen();
        medicineDataExecutor = Executors.newSingleThreadExecutor();
        cartCounter = Executors.newSingleThreadExecutor();
        searchExecutor = Executors.newSingleThreadExecutor();
        medicineDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadAllMedicineData();
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
                Intent intent = new Intent(SeeAll.this, Cart.class);
                startActivity(intent);
            }
        });
        searchExecutor.execute(new Runnable() {
            @Override
            public void run() {
                binding.search.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isSearchActive = true;
                        binding.header.setVisibility(View.GONE);
                        binding.recyclerViewLayout.setVisibility(View.GONE);
                        binding.searchDataLayout.setVisibility(View.VISIBLE);
                        binding.searchLayout.setVisibility(View.VISIBLE);
                        setSearch();
                        binding.searchView.requestFocus();
                    }
                });
            }
        });

    }

    public void loadingScreen() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.loading_screen);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void closeLoadingScreen() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.header.setVisibility(View.VISIBLE);
                binding.recyclerViewLayout.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        }, 1000);
    }

    public void setSearch() {
        binding.searchRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false), R.layout.shimmer_layout_medicine_search_layout);
        binding.searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                if (isFocused) {
                    showInputMethod(view.findFocus());
                    searchMedicine();
                }
            }
        });
        binding.searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                restoreView();
                return true;
            }
        });
    }
    private void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    public void searchMedicine() {
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

    public void filterList(String searchQuery) {
        String combinedName;
        ArrayList<MedicineSearchModel> filteredList = new ArrayList<>();
        medicineSearchAdapter = new MedicineSearchAdapter(this, filteredList);
        for (MedicineSearchModel medicine : medicineList) {
            combinedName = medicine.getMedicineName() + " " + medicine.getMedicineDosage();
            if (combinedName.toLowerCase().contains(searchQuery.toLowerCase())) {
                filteredList.add(medicine);
            } else if (medicine.getMedicineName().toLowerCase().contains(searchQuery.toLowerCase()) | medicine.getMedicineDosage().toLowerCase().contains(searchQuery.toLowerCase()) | medicine.getGenericName().toLowerCase().contains(searchQuery.toLowerCase())) {
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

    public void setCartCounter() {
        FirebaseUser user = firebase.getUserID();
        DatabaseReference cartCounterReference = firebase.getDatabaseReference("medicineCart");
        cartCounterReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                countedCart = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
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

    public void loadAllMedicineData() {
        medicineModels = new ArrayList<>();
        medicineList = new ArrayList<>();
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false));
        adapter = new AllMedicineAdapter(this, medicineModels);
        if (medicineType.equalsIgnoreCase("tablet")) {
            genericNames = new ArrayList<>();
            loadGenericNames(genericNames);
            DatabaseReference reference = firebase.getDatabaseReference("tabletData");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int counter = 0;
                    if (snapshot.exists()) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            MedicineModel model = snap.getValue(MedicineModel.class);
                            MedicineSearchModel searchModel = snap.getValue(MedicineSearchModel.class);
                            assert model != null;
                            if (genericNames.contains(model.getGenericName())) {
                                medicineModels.add(model);
                                medicineList.add(searchModel);
                                counter += 1;
                            }
                        }
                        Collections.shuffle(medicineModels);
                        binding.descriptionTxt.setText("Showing " + counter + " OCT Medicine List");
                        binding.searchView.setQueryHint("Search for OCT Medicines");
                        binding.recyclerView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        } else if (medicineType.equalsIgnoreCase("syrup")) {
            DatabaseReference reference = firebase.getDatabaseReference("syrupData");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int counter = 0;
                    if (snapshot.exists()) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            MedicineModel model = snap.getValue(MedicineModel.class);
                            MedicineSearchModel searchModel = snap.getValue(MedicineSearchModel.class);
                            medicineModels.add(model);
                            medicineList.add(searchModel);
                            counter += 1;
                        }
                        Collections.shuffle(medicineModels);
                        binding.descriptionTxt.setText("Showing " + counter + " Syrup Medicine List");
                        binding.searchView.setQueryHint("Search for Syrup Medicines");
                        binding.recyclerView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        } else {
            DatabaseReference reference = firebase.getDatabaseReference("herbalSyrupData");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int counter = 0;
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            MedicineModel model = snap.getValue(MedicineModel.class);
                            MedicineSearchModel searchModel = snap.getValue(MedicineSearchModel.class);
                            medicineModels.add(model);
                            medicineList.add(searchModel);
                            counter += 1;
                        }
                        Collections.shuffle(medicineModels);
                        binding.descriptionTxt.setText("Showing " + counter + " Herbal Syrup Medicine List");
                        binding.searchView.setQueryHint("Search for Herbal Syrup Medicines");
                        binding.recyclerView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        }
        closeLoadingScreen();
    }

    public void loadGenericNames(ArrayList<String> genericNames) {
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

    public void restoreView() {
        binding.searchView.setQuery("", false);
        binding.searchView.clearFocus();
        binding.header.setVisibility(View.VISIBLE);
        binding.recyclerViewLayout.setVisibility(View.VISIBLE);
        binding.searchRecyclerView.setVisibility(View.GONE);
        binding.noDataLayout.setVisibility(View.GONE);
        binding.searchDataLayout.setVisibility(View.GONE);
        binding.searchResultTxt.setVisibility(View.GONE);
        binding.searchLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (isSearchActive) {
            isSearchActive = false;
            restoreView();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        medicineDataExecutor.shutdown();
        cartCounter.shutdown();
    }
}