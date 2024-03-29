package com.example.doctorbabu.patient.MedicinePurchaseModules;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
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
    ArrayList<String> medicineQuantity;
    int backupRewardPoint;
    boolean hasAppliedReward;
    int quantity;

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
                onBackPressed();
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
        binding.placeOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placeOrder();
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
        medicineQuantity = new ArrayList<>();
        binding.checkoutMedicineRecyclerView.setLayoutManager(new LinearLayoutManager(Checkout.this, LinearLayoutManager.VERTICAL, false), R.layout.shimmer_layout_medicine);
        adapter = new CheckoutAdapter(this, checkoutModels, medicineQuantity);
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
        if (!rewardPoint.isEmpty()) {
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
                if (snapshot.exists()) {
                    int userRewardPoint = Integer.parseInt(String.valueOf(snapshot.child("reward").getValue()));
                    if (userRewardPoint >= rewardPoint) {
                        backupRewardPoint = userRewardPoint;
                        userRewardReference.child(user.getUid()).child("reward").setValue(String.valueOf(userRewardPoint - rewardPoint)).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                hasAppliedReward = true;
                                applyReward(rewardPoint);
                            }
                        });
                    } else {
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
    public void applyReward(int rewardPoint) {
        launchMiniDialog();
        double rewardedPrice = Math.round(Double.parseDouble(totalPrice) - ((double) (5 * rewardPoint) / 100));
        if (rewardedPrice < 0) {
            binding.discountedTotalPrice.setText(moneyIcon + (60));
        } else {
            binding.discountedTotalPrice.setText(moneyIcon + Math.round(rewardedPrice + 60));
        }
        binding.totalPayment.setText(moneyIcon + Math.round(rewardedPrice + 60));
        binding.discountAmount.setText(moneyIcon + Math.round(((double) (5 * rewardPoint) / 100)));
        binding.discountAmount.setVisibility(View.VISIBLE);
        binding.discountAmountTitle.setVisibility(View.VISIBLE);
        binding.totalPrice.setTextColor(Color.parseColor("#000000"));
        binding.totalPrice.setTypeface(null, Typeface.NORMAL);
        binding.totalPrice.setPaintFlags(binding.totalPayment.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        binding.rewardPointTextLayout.setVisibility(View.GONE);
        binding.applyReward.setVisibility(View.INVISIBLE);
        binding.rewardPointTitle.setVisibility(View.GONE);
        binding.rewardPointHeader.setVisibility(View.INVISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
                notifyReward();
            }
        }, 500);
    }

    public void launchMiniDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.cart_loading_screen);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void notifyReward() {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(Checkout.this);
        dialog.setTitle("Reward Point Applied").setIcon(R.drawable.done)
                .setMessage("Your reward points have been applied successfully!")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        binding.addedRewardPoint.setVisibility(View.VISIBLE);
                        binding.rewardLottie.setVisibility(View.VISIBLE);
                        binding.rewardLottie.playAnimation();
                        binding.rewardLottie.addAnimatorListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(@NonNull Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(@NonNull Animator animator) {
                                binding.rewardLottie.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(@NonNull Animator animator) {

                            }

                            @Override
                            public void onAnimationRepeat(@NonNull Animator animator) {

                            }
                        });
                    }
                }).setCancelable(false);
        dialog.create().show();
    }

    public boolean validateDeliveryAddress() {
        String deliveryAddress = binding.deliveryAddress.getText().toString().trim();
        if (deliveryAddress.isEmpty() || deliveryAddress.equalsIgnoreCase("null")) {
            binding.deliveryAddressTextLayout.setError("Must provide with a delivery address");
            return false;
        }
        return true;
    }

    public boolean validatePhoneNumber() {
        String phoneNumber = binding.phoneNumber.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            binding.phoneNumberTextLayout.setError("Must provide with a delivery address");
            return false;
        }
        return true;
    }

    public void placeOrder() {
        if (!validateDeliveryAddress() || !validatePhoneNumber()) {
            return;
        }
        DatabaseReference uploadReference = firebase.getDatabaseReference("medicineOrders");
        DatabaseReference cartReference = firebase.getDatabaseReference("medicineCart");
        String customerName = binding.customerName.getText().toString();
        String deliveryAddress = binding.deliveryAddress.getText().toString();
        String phoneNumber = binding.phoneNumber.getText().toString();
        String totalPrice;
        if (hasAppliedReward) {
            totalPrice = binding.discountedTotalPrice.getText().toString();
        } else {
            totalPrice = binding.totalPrice.getText().toString();
        }
        int index = 0;
        for (CartModel product : checkoutModels) {
            Clock clock = Clock.systemDefaultZone();
            long milliSeconds = clock.millis();
            String orderId = getOrderId();
            HashMap<String, Object> data = new HashMap<>();
            data.put("customerName", customerName);
            data.put("deliveryAddress", deliveryAddress);
            data.put("phoneNumber", phoneNumber);
            data.put("totalPrice", totalPrice);
            data.put("orderId", orderId);
            data.put("orderTime", milliSeconds);
            data.put("medicineType", product.getMedicineType());
            data.put("productId", product.getMedicineId());
            data.put("productQuantity", product.getQuantity());
            data.put("trackOrder", 0);
            uploadReference.child(user.getUid()).child(orderId).setValue(data);
            cartReference.child(user.getUid()).child(product.getMedicineId()).removeValue();
            DatabaseReference medicineInfoReference;
            if (product.getMedicineType().equalsIgnoreCase("tablet")) {
                medicineInfoReference = firebase.getDatabaseReference("tabletData");
            } else if (product.getMedicineType().equalsIgnoreCase("syrup")) {
                medicineInfoReference = firebase.getDatabaseReference("syrupData");
            } else {
                medicineInfoReference = firebase.getDatabaseReference("herbalSyrupData");
            }
            int purchasingQuantity = Integer.parseInt(product.getQuantity());
            int productQuantity = Integer.parseInt(medicineQuantity.get(index));
            int finalQuantity = productQuantity - purchasingQuantity;
            medicineInfoReference.child(product.getMedicineId()).child("medicineQuantity").setValue(String.valueOf(finalQuantity));
            index++;
        }
        launchActivity();

    }

    public void launchActivity() {
        Intent intent = new Intent(this, PurchaseCompletion.class);
        startActivity(intent);
        finish();
    }

    public String getOrderId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void onBackPressed() {
        launchMiniDialog();
        if (hasAppliedReward) {
            DatabaseReference rewardReference = firebase.getDatabaseReference("rewardPatient");
            rewardReference.child(user.getUid()).child("reward").setValue(String.valueOf(backupRewardPoint)).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 700);
                }
            });
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customerDataExecutor.shutdown();
        loadSelectedCardExecutor.shutdown();
    }
}