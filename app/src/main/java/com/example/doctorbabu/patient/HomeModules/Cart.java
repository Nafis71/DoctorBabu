package com.example.doctorbabu.patient.HomeModules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.example.doctorbabu.Adapters.CartAdapter;
import com.example.doctorbabu.Adapters.MedicineAdapter;
import com.example.doctorbabu.DatabaseModels.CartModel;
import com.example.doctorbabu.DatabaseModels.MedicineModel;
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
    ExecutorService cartItemExecutor;
    CartAdapter adapter;
    ArrayList<CartModel> model;
    Firebase firebase;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        model = new ArrayList<>();
        firebase = Firebase.getInstance();
        user = firebase.getUserID();
        cartItemExecutor = Executors.newSingleThreadExecutor();
        cartItemExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadCartItem();
            }
        });
    }
    public void loadCartItem(){
        setRecyclerView();
        binding.cartRecyclerView.showShimmer();
        DatabaseReference cartItemReference = firebase.getDatabaseReference("medicineCart");
        cartItemReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snap : snapshot.getChildren()){
                        CartModel cartModel = snap.getValue(CartModel.class);
                        model.add(cartModel);
                    }
                    adapter.notifyDataSetChanged();
                    binding.cartRecyclerView.hideShimmer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });

    }
    public void setRecyclerView(){
        binding.cartRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false), R.layout.shimmer_layout_doctor_search);
        adapter = new CartAdapter(this, model);
        binding.cartRecyclerView.setAdapter(adapter);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}