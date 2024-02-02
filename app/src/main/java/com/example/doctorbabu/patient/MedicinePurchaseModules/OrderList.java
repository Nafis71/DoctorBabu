package com.example.doctorbabu.patient.MedicinePurchaseModules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.example.doctorbabu.Adapters.MedicationOrderListAdapter;
import com.example.doctorbabu.DatabaseModels.MedicationOrderListModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityOrderListBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderList extends AppCompatActivity {
    ActivityOrderListBinding binding;
    MedicationOrderListAdapter adapter;
    ArrayList<MedicationOrderListModel> orderListModels;
    ExecutorService orderListExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        orderListModels = new ArrayList<>();
        orderListExecutor = Executors.newSingleThreadExecutor();
        orderListExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadMedicationOrder();
            }
        });
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void loadMedicationOrder(){
        binding.medicationRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));
        adapter = new MedicationOrderListAdapter(this,orderListModels);
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        DatabaseReference reference = firebase.getDatabaseReference("medicineOrders");
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snap: snapshot.getChildren()){
                        MedicationOrderListModel model = snap.getValue(MedicationOrderListModel.class);
                        orderListModels.add(model);
                    }
                    binding.medicationRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    @Override
    protected void onDestroy() {
        orderListExecutor.shutdown();
        super.onDestroy();
    }
}