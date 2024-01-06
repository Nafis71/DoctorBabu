package com.example.doctorbabu.patient.HomeModules;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
    String totalPrice, moneyIcon = "\u09F3";
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
                        initiateRewardProcess();
                    }
                });
            }
        });

    }

    public void initializeFirebase() {
        firebase = Firebase.getInstance();
        user = firebase.getUserID();
    }

    @SuppressLint("SetTextI18n")
    public void loadSelectedCards() {
        selectedCards = getIntent().getStringArrayListExtra("selectedCards");
        totalPrice = getIntent().getStringExtra("totalPrice");
        assert totalPrice != null;
        binding.totalPrice.setText(moneyIcon + Math.round(Double.parseDouble(totalPrice) + 60));
        binding.itemsTotalPrice.setText(moneyIcon + Math.round(Double.parseDouble(totalPrice)));
        binding.totalPayment.setText(moneyIcon + Math.round(Double.parseDouble(totalPrice) + 60));
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

    public void initiateRewardProcess() {
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
        DatabaseReference userRewardReference = firebase.getDatabaseReference("rewardPatient");
        userRewardReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    int userRewardPoint = Integer.parseInt(String.valueOf(snapshot.child("reward").getValue()));
                    if(userRewardPoint >= rewardPoint){
                        applyReward(rewardPoint);
                    }else{
                        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(Checkout.this);
                        dialog.setTitle("Insufficient Reward Point").setIcon(R.drawable.cross)
                                .setMessage("Your reward point is insufficient, try gaining more reward points via doctor consultation")
                                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                //do nothing
                                            }
                                        }).setCancelable(false);
                        dialog.create().show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void applyReward(int rewardPoint){
        launchMiniDialog();
        double rewardedPrice = Math.round(Double.parseDouble(totalPrice)- ((double) (5 * rewardPoint) /100));
        binding.discountedTotalPrice.setText(moneyIcon + Math.round (rewardedPrice + 60));
        binding.totalPayment.setText(moneyIcon + Math.round (rewardedPrice + 60));
        binding.discountAmount.setText(moneyIcon + Math.round(((double) (5 * rewardPoint) /100)));
        binding.discountAmount.setVisibility(View.VISIBLE);
        binding.discountAmountTitle.setVisibility(View.VISIBLE);
        binding.totalPrice.setTextColor(Color.parseColor("#000000"));
        binding.totalPrice.setTypeface(null,Typeface.NORMAL);
        binding.totalPrice.setPaintFlags( binding.totalPayment.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                notifyReward();
            }
        },500);
    }
    public void launchMiniDialog(){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.cart_loading_screen);
        dialog.setCancelable(false);
        dialog.show();
    }
    public void notifyReward(){
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(Checkout.this);
        dialog.setTitle("Reward Point Applied").setIcon(R.drawable.done)
                .setMessage("Your reward points have been applied successfully!")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                }).setCancelable(false);
        dialog.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        customerDataExecutor.shutdown();
        loadSelectedCardExecutor.shutdown();
    }
}