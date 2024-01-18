package com.example.doctorbabu.patient.PatientProfileModule;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.doctorbabu.Adapters.prescriptionAdapter;
import com.example.doctorbabu.DatabaseModels.prescriptionModel;
import com.example.doctorbabu.databinding.FragmentPrescriptionHistoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrescriptionHistory extends Fragment {
    prescriptionAdapter adapter;
    ArrayList<prescriptionModel> list = new ArrayList<>();
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
    FragmentPrescriptionHistoryBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    ExecutorService loadPrescription;


    public PrescriptionHistory() {
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadPrescription = Executors.newSingleThreadExecutor();
        loadPrescription.execute(new Runnable() {
            @Override
            public void run() {
                loadPrescriptionList();
            }
        });
    }
    public void onStart(){
        super.onStart();
    }
    public void loadPrescriptionList(){
        binding.prescriptionRecycler.showShimmer();
        binding.prescriptionRecycler.setLayoutManager(new LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false));
        adapter = new prescriptionAdapter(requireContext(),list);
        DatabaseReference reference = database.getReference("prescription");
        reference.child(user.getUid()).orderByChild("time").limitToLast(100).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if(snapshot.exists() && isAdded())
                {
                    for(DataSnapshot snap : snapshot.getChildren()){
                        prescriptionModel model = snap.getValue(prescriptionModel.class);
                        list.add(model);
                    }
                    binding.prescriptionRecycler.setAdapter(adapter);
                    binding.prescriptionRecycler.hideShimmer();
                } else{
                    binding.prescriptionRecycler.hideShimmer();
                    binding.noPrescriptionLayout.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPrescriptionHistoryBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    public void onDestroyView(){
        super.onDestroyView();
        binding =null;
        loadPrescription.shutdownNow();
    }
}