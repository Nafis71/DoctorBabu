package com.example.doctorbabu.patient;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.doctorbabu.Databases.prescriptionAdapter;
import com.example.doctorbabu.Databases.prescriptionModel;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.FragmentPrescriptionHistoryBinding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PrescriptionHistory extends Fragment {
    prescriptionAdapter adapter;
    ArrayList<prescriptionModel> list = new ArrayList<>();
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
    FragmentPrescriptionHistoryBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();


    public PrescriptionHistory() {
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.prescriptionRecycler.showShimmer();
        binding.prescriptionRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new prescriptionAdapter(requireContext(),list);
        binding.prescriptionRecycler.setAdapter(adapter);
    }
    public void onStart(){
        super.onStart();
        loadPrescriptionList();

    }
    public void loadPrescriptionList(){
        DatabaseReference reference = database.getReference("prescription");
        reference.child(user.getUid()).orderByChild("date").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if(snapshot.exists())
                {
                    for(DataSnapshot snap : snapshot.getChildren()){
                        prescriptionModel model = snap.getValue(prescriptionModel.class);
                        list.add(model);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
    }
}