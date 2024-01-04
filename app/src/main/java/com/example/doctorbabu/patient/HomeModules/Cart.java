package com.example.doctorbabu.patient.HomeModules;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.doctorbabu.Adapters.CartAdapter;
import com.example.doctorbabu.Adapters.SelectedCard;
import com.example.doctorbabu.DatabaseModels.CartModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityCartBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Cart extends AppCompatActivity {
    ActivityCartBinding binding;
    ExecutorService cartItemExecutor,removeExecutor;
    CartAdapter adapter;
    ArrayList<CartModel> model;
    Firebase firebase;
    FirebaseUser user;
    SelectedCard selectedCard;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        model = new ArrayList<>();
        firebase = Firebase.getInstance();
        user = firebase.getUserID();
        cartItemExecutor = Executors.newSingleThreadExecutor();
        removeExecutor = Executors.newSingleThreadExecutor();
        cartItemExecutor.execute(new Runnable() {
            @Override
            public void run() {
                setRecyclerView();
            }
        });

        removeExecutor.execute(new Runnable() {
            @Override
            public void run() {
                binding.remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeFromCart();
                    }
                });
            }
        });
    }

    public void loadCartItem() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.cartRecyclerView.showShimmer();
            }
        });

        DatabaseReference cartItemReference = firebase.getDatabaseReference("medicineCart");
        cartItemReference.child(user.getUid()).orderByChild("medicineId").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                model.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        CartModel cartModel = snap.getValue(CartModel.class);
                        model.add(cartModel);
                        binding.cartRecyclerView.setAdapter(adapter);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.cartRecyclerView.hideShimmer();
                        }
                    });
                }else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.cartRecyclerView.hideShimmer();
                            binding.parentLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            binding.recyclerViewLayout.setVisibility(View.GONE);
                            binding.noDataLayout.setVisibility(View.VISIBLE);
                        }
                    });

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void removeFromCart(){
        loadingScreen();
        binding.checkoutLayout.setVisibility(View.GONE);
        DatabaseReference deleteSelectedMedicine = firebase.getDatabaseReference("medicineCart");
        ArrayList<String> cards = selectedCard.getCards();
        cards.forEach((card) -> deleteSelectedMedicine.child(user.getUid()).child(card).removeValue());
        adapter.resetCalculatedPrice();
        binding.remove.setVisibility(View.GONE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cartItemExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        loadCartItem();
                    }
                });
            }
        },1000);
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
                dialog.dismiss();
            }
        }, 1000);
    }

    public void setRecyclerView() {
        selectedCard = SelectedCard.getInstance();
        selectedCard.resetCards();
        binding.cartRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false), R.layout.shimmer_layout_doctor_search);
        adapter = new CartAdapter(this, model, selectedCard,binding.remove,binding.checkoutLayout,binding.totalPrice);
        loadCartItem();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        cartItemExecutor.shutdown();
        removeExecutor.shutdown();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}