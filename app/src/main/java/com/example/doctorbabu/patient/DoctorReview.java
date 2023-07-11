package com.example.doctorbabu.patient;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.doctorbabu.Databases.doctorReviewAdapter;
import com.example.doctorbabu.Databases.doctorReviewModel;
import com.example.doctorbabu.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


public class DoctorReview extends Fragment {
    RecyclerView recyclerView;
    String doctorId;
    doctorReviewAdapter adapter;
    ArrayList<doctorReviewModel> list;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");


    public DoctorReview() {
        // Required empty public constructor
    }

    public DoctorReview(String doctorId) {
        this.doctorId = doctorId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        list =  new ArrayList<>();
        adapter = new doctorReviewAdapter(requireContext(),list);
        recyclerView.setAdapter(adapter);
        DatabaseReference reference = database.getReference("reviews");
        reference.child(doctorId).orderByChild("rating").limitToLast(20).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if(snapshot.exists())
                {
                    for(DataSnapshot snap : snapshot.getChildren())
                    {
                        doctorReviewModel model = snap.getValue(doctorReviewModel.class);
                        list.add(model);
                    }
                    Collections.reverse(list);
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
        return inflater.inflate(R.layout.fragment_doctor_review, container, false);
    }
}