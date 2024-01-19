package com.example.doctorbabu.patient.PatientProfileModule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.doctorbabu.Adapters.prescriptionAdapter;
import com.example.doctorbabu.DatabaseModels.prescriptionModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.FragmentPrescriptionHistoryBinding;
import com.example.doctorbabu.patient.DoctorConsultationModule.ViewAllDoctor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrescriptionHistory extends Fragment {
    prescriptionAdapter adapter;
    ArrayList<prescriptionModel> list = new ArrayList<>();
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
    FragmentPrescriptionHistoryBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    ExecutorService loadPrescription, searchExecutor;
    ChipNavigationBar bottomNavigation;
    String doctorName;


    public PrescriptionHistory() {
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bottomNavigation = requireActivity().findViewById(R.id.bottomBar);
        bottomNavigation.dismissBadge(R.id.nav_history);
        SharedPreferences preferences = requireActivity().getSharedPreferences("prescriptionCounter", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("counter", 0);
        editor.apply();
        loadPrescription = Executors.newSingleThreadExecutor();
        searchExecutor = Executors.newSingleThreadExecutor();
        loadPrescription.execute(new Runnable() {
            @Override
            public void run() {
                loadPrescriptionList();
            }
        });
        searchExecutor.execute(new Runnable() {
            @Override
            public void run() {
                searchPrescription();
            }
        });
    }

    public void onStart() {
        super.onStart();
    }

    public void loadPrescriptionList() {
        binding.prescriptionRecycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false),R.layout.shimmer_layout_prescription_list);
        adapter = new prescriptionAdapter(requireContext(), list,binding.recyclerLayout, binding.noPrescriptionLayout);
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.prescriptionRecycler.showShimmer();
            }
        });
        DatabaseReference reference = database.getReference("prescription");
        reference.child(user.getUid()).limitToFirst(1000).orderByChild("time").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if (snapshot.exists() && isAdded()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        prescriptionModel model = snap.getValue(prescriptionModel.class);
                        list.add(model);
                        Collections.reverse(list);
                    }
                    binding.prescriptionRecycler.setAdapter(adapter);
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.prescriptionRecycler.hideShimmer();
                        }
                    });
                    binding.recyclerLayout.setVisibility(View.VISIBLE);
                } else {
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.prescriptionRecycler.hideShimmer();
                        }
                    });
                    binding.noPrescriptionLayout.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });

    }

    public void searchPrescription() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.isEmpty() && !query.equals(" ")) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    filterList(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    public void filterList(String searchQuery) {
        ArrayList<prescriptionModel> filteredList = new ArrayList<>();
        binding.prescriptionRecycler.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false), R.layout.shimmer_layout_prescription_list);
        adapter = new prescriptionAdapter(requireActivity(), filteredList, binding.recyclerLayout, binding.noPrescriptionLayout);
        binding.prescriptionRecycler.showShimmer();
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
        for (prescriptionModel prescription : list) {
            reference.child(prescription.getPrescribedBy()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DataSnapshot snapshot = task.getResult();
                        doctorName = String.valueOf(snapshot.child("title").getValue()) + String.valueOf(snapshot.child("fullName").getValue());
                        if (doctorName.toLowerCase().contains(searchQuery.toLowerCase()) | prescription.getDate().toLowerCase().contains(searchQuery.toLowerCase()) | prescription.getDiagnosis().toLowerCase().contains(searchQuery.toLowerCase())) {
                            filteredList.add(prescription);
                        }
                    }
                }
            });
        }
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (filteredList.isEmpty()) {
                    Log.w("doctorName",doctorName);
                    binding.progressBar.setVisibility(View.GONE);
                    binding.prescriptionRecycler.hideShimmer();
                    binding.prescriptionRecycler.setVisibility(View.GONE);
                    MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireActivity());
                    dialog.setTitle("No prescriptions found!").setIcon(R.drawable.cross)
                            .setMessage("No prescription is currently matching your criteria, try again.")
                            .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    binding.prescriptionRecycler.setVisibility(View.VISIBLE);
                                    loadPrescription.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            loadPrescriptionList();
                                        }
                                    });
                                }
                            }).setCancelable(false);
                    dialog.create().show();
                } else {
                    handler.postDelayed(() -> {
                        binding.prescriptionRecycler.setAdapter(adapter);
                        binding.prescriptionRecycler.hideShimmer();
                        binding.progressBar.setVisibility(View.GONE);
                    }, 300);
                }

            }
        },3100);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPrescriptionHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        loadPrescription.shutdownNow();
        searchExecutor.shutdown();
    }
}