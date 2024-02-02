package com.example.doctorbabu.patient.MedicinePurchaseModules;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.doctorbabu.Adapters.MyDocumentAdapter;
import com.example.doctorbabu.DatabaseModels.diagnoseReportModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.databinding.ActivityMyDocumentBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyDocument extends AppCompatActivity {

    ActivityMyDocumentBinding binding;
    MyDocumentAdapter adapter;
    ArrayList<diagnoseReportModel> reportModels;
    ExecutorService documentLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyDocumentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        reportModels = new ArrayList<>();
        documentLoader = Executors.newSingleThreadExecutor();
        documentLoader.execute(new Runnable() {
            @Override
            public void run() {
                loadDocuments();
            }
        });
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    public void loadDocuments(){
        binding.reportRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));
        adapter = new MyDocumentAdapter(this,reportModels);
        Firebase firebase =  Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        DatabaseReference reference = firebase.getDatabaseReference("diagnoseReports");
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot snap: snapshot.getChildren()){
                        diagnoseReportModel model = snap.getValue(diagnoseReportModel.class);
                        reportModels.add(model);
                    }
                    binding.reportRecyclerView.setAdapter(adapter);
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
        documentLoader.shutdown();
        super.onDestroy();
    }
}