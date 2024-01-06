package com.example.doctorbabu.patient.HomeModules;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.doctorbabu.Adapters.CheckoutAdapter;
import com.example.doctorbabu.DatabaseModels.CartModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityCheckoutBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Checkout extends AppCompatActivity {
    ActivityCheckoutBinding binding;
    ArrayList<String> selectedCards;
    Dialog dialog;
    String totalPrice;
    ExecutorService customerDataExecutor, checkoutListExecutor, loadSelectedCardExecutor, rewardExecutor;
    Firebase firebase;
    FirebaseUser user;
    CheckoutAdapter adapter;
    ArrayList<CartModel> checkoutModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingScreen();
        loadSelectedCards();
        initializeFirebase();
        customerDataExecutor = Executors.newSingleThreadExecutor();
        checkoutListExecutor = Executors.newSingleThreadExecutor();
        loadSelectedCardExecutor = Executors.newSingleThreadExecutor();
        rewardExecutor = Executors.newSingleThreadExecutor();
        loadSelectedCardExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadSelectedCards();
            }
        });
        customerDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadCustomerData();
            }
        });
        checkoutListExecutor.execute(new Runnable() {
            @Override
            public void run() {
                initializeCheckoutRecyclerView();
            }
        });
        closeLoadingScreen();
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        rewardExecutor.execute(new Runnable() {
            @Override
            public void run() {
                binding.applyReward.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addReward();
                    }
                });
            }
        });

    }

    public void initializeFirebase() {
        firebase = Firebase.getInstance();
        user = firebase.getUserID();
    }

    public void loadSelectedCards() {
        selectedCards = getIntent().getStringArrayListExtra("selectedCards");
        totalPrice = getIntent().getStringExtra("totalPrice");
        assert totalPrice != null;
        binding.totalPrice.setText(String.valueOf(Math.round(Double.parseDouble(totalPrice) + 60)));
        binding.itemsTotalPrice.setText(String.valueOf(Math.round(Double.parseDouble(totalPrice))));
        binding.totalPayment.setText(String.valueOf(Math.round(Double.parseDouble(totalPrice) + 60)));
        String totalCheckout = "(" + selectedCards.size() + ")";
        binding.totalCheckOut.setText(totalCheckout);
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
                binding.bodyLayout.setVisibility(View.VISIBLE);
                binding.placeOrderLayout.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        }, 2000);
    }

    public void loadCustomerData() {
        DatabaseReference customerReference = firebase.getDatabaseReference("users");
        customerReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    setDeliveryDetailsViews(snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void setDeliveryDetailsViews(DataSnapshot snapshot) {
        binding.customerName.setText(String.valueOf(snapshot.child("fullName").getValue()));
        binding.deliveryAddress.setText(String.valueOf(snapshot.child("address").getValue()));
        binding.phoneNumber.setText(String.valueOf(snapshot.child("phone").getValue()));
    }

    public void initializeCheckoutRecyclerView() {
        checkoutModels = new ArrayList<>();
        binding.checkoutMedicineRecyclerView.setLayoutManager(new LinearLayoutManager(Checkout.this, LinearLayoutManager.VERTICAL, false), R.layout.shimmer_layout_medicine);
        adapter = new CheckoutAdapter(this, checkoutModels);
        binding.checkoutMedicineRecyclerView.showShimmer();
        checkCart();
    }

    public void checkCart() {
        DatabaseReference cartReference = firebase.getDatabaseReference("medicineCart");
        for (String medicineId : selectedCards) {
            cartReference.child(user.getUid()).child(medicineId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        CartModel cartModel = snapshot.getValue(CartModel.class);
                        checkoutModels.add(cartModel);
                        binding.checkoutMedicineRecyclerView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        }
        binding.checkoutMedicineRecyclerView.hideShimmer();
    }

    public void addReward() {
        String rewardPoint = binding.rewardPoint.getText().toString().trim();
        if(!rewardPoint.isEmpty()){
            if (Integer.parseInt(rewardPoint) >= 100) {
                checkReward(Integer.parseInt(rewardPoint));
            } else {
                binding.rewardPointTextLayout.setError("Minimum reward points that can be used is 100");
            }
        }
    }

    public void checkReward(int rewardPoint) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        customerDataExecutor.shutdown();
        loadSelectedCardExecutor.shutdown();
    }
}