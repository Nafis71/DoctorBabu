package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.doctorbabu.Adapters.viewAllDoctorAdapter;
import com.example.doctorbabu.DatabaseModels.doctorInfoModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityViewAllDoctorBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ViewAllDoctor extends AppCompatActivity {
    viewAllDoctorAdapter adapter;
    ArrayList<doctorInfoModel> doctors = new ArrayList<>();
    ArrayList<String> doctorTitle;
    BottomSheetDialog filterSheet;
    ExecutorService allDoctorLoadExecutor, searchDoctor, filterExecutor;
    ActivityViewAllDoctorBinding binding;
    String specialist = null;
    RangeSlider consultationFeeRange;
    TextView rangeMaxAmount, rangeMinAmount;
    MaterialButton applyFilter;
    RatingBar ratingBar;
    RadioGroup radioGroup;
    RadioButton maleRadio, femaleRadio;
    MaterialCheckBox doctor, assistantDoctor, assistantProfessorDoctor, professorDoctor;
    int consultationMaxAmount, consultationMinAmount;
    Firebase firebase;
    String gender = "";
    float rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewAllDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebase = Firebase.getInstance();
        doctorTitle = new ArrayList<>();
        allDoctorLoadExecutor = Executors.newSingleThreadExecutor();
        searchDoctor = Executors.newSingleThreadExecutor();
        filterExecutor = Executors.newSingleThreadExecutor();
        allDoctorLoadExecutor.execute(this::loadData);
        binding.back.setOnClickListener(view -> {
            specialist = null;
            finish();
        });
        specialist = getIntent().getStringExtra("specialist");
        searchDoctor.execute(new Runnable() {
            @Override
            public void run() {
                binding.searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean isFocused) {
                        if (isFocused) {
                            searchDoctor();
                        }
                    }
                });
            }
        });
        filterExecutor.execute(new Runnable() {
            @Override
            public void run() {
                binding.filter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bringFilterBottomSheet();
                    }
                });
            }
        });
    }

    public void bringFilterBottomSheet() {
        filterSheet = new BottomSheetDialog(this, R.style.bottomSheetTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_design_search_option, null);
        filterSheet.setContentView(view);
        consultationFeeRange = view.findViewById(R.id.consultationFeeRange);
        rangeMaxAmount = view.findViewById(R.id.rangeMaxAmount);
        rangeMinAmount = view.findViewById(R.id.rangeMinAmount);
        applyFilter = view.findViewById(R.id.applyFilter);
        radioGroup = view.findViewById(R.id.radioGroupLayout);
        ratingBar = view.findViewById(R.id.ratingBar);
        doctor = view.findViewById(R.id.doctor);
        assistantDoctor = view.findViewById(R.id.assistantDoctor);
        assistantProfessorDoctor = view.findViewById(R.id.assistantProfessorDoctor);
        professorDoctor = view.findViewById(R.id.professorDoctor);
        maleRadio = view.findViewById(R.id.maleRadio);
        femaleRadio = view.findViewById(R.id.femaleRadio);
        setLabelFormatter();
        consultationFeeChangeListener();
        getDoctorTitle();
        setDoctorTitleListener();
        setGenderListener();
        setRatingListener();
        setApplyFilter();
        filterSheet.show();
    }

    public void setApplyFilter() {
        applyFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = 0;
                filterSheet.dismiss();
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.doctorRecyclerView.showShimmer();
                consultationMaxAmount = Integer.parseInt(rangeMaxAmount.getText().toString().trim());
                consultationMinAmount = Integer.parseInt(rangeMinAmount.getText().toString().trim());
                ArrayList<doctorInfoModel> filteredList = new ArrayList<>();
                adapter = new viewAllDoctorAdapter(ViewAllDoctor.this, filteredList);
                for (doctorInfoModel doctor : doctors) {
                    if (Integer.parseInt(doctor.getConsultationFee()) >= consultationMinAmount && Integer.parseInt(doctor.getConsultationFee()) <= consultationMaxAmount) {
                        if (!doctorTitle.isEmpty()) {
                            if(doctorTitle.contains(doctor.getTitle())){
                                if (!gender.isEmpty()) {
                                    if(doctor.getGender().equalsIgnoreCase(gender)){
                                        if (doctor.getRating() >= rating) {
                                            filteredList.add(doctor);
                                            count++;
                                        }
                                    }
                                }else{
                                    if (doctor.getRating() >= rating) {
                                        filteredList.add(doctor);
                                        count++;
                                    }
                                }
                            }
                        } else {
                            if (!gender.isEmpty()) {
                                if(doctor.getGender().equalsIgnoreCase(gender)){
                                    if (doctor.getRating() >= rating) {
                                        filteredList.add(doctor);
                                        count++;
                                    }
                                }
                            } else {
                                if (doctor.getRating() >= rating) {
                                    filteredList.add(doctor);
                                    count++;
                                }
                            }
                        }
                    }
                }// for loop ends here
                Handler handler = new Handler();
                if (filteredList.isEmpty()) {
                    int finalCount = count;
                    handler.postDelayed(() -> {
                        binding.doctorCount.setText(String.valueOf(finalCount));
                        binding.progressBar.setVisibility(View.GONE);
                        binding.doctorRecyclerView.hideShimmer();
                        binding.doctorRecyclerView.setVisibility(View.GONE);
                    }, 200);

                } else {
                    int finalCount = count;
                    handler.postDelayed(() -> {
                        binding.doctorRecyclerView.setAdapter(adapter);
                        binding.doctorRecyclerView.hideShimmer();
                        binding.progressBar.setVisibility(View.GONE);
                        binding.doctorCount.setText(String.valueOf(finalCount));
                    }, 1000);
                }
            }
        });
    }

    public void getDoctorTitle() {
        if (!doctorTitle.isEmpty()) {
            if (doctorTitle.contains("Dr.")) {
                doctor.setChecked(true);
            }
            if (doctorTitle.contains("Asst.")) {
                assistantDoctor.setChecked(true);
            }
            if (doctorTitle.contains("Asst. Prof.")) {
                assistantProfessorDoctor.setChecked(true);
            }
            if (doctorTitle.contains("Prof. Dr.")) {
                professorDoctor.setChecked(true);
            }
        }
    }

    public void setDoctorTitleListener() {
        doctor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    doctorTitle.add("Dr.");
                } else {
                    doctorTitle.remove("Dr.");
                }
            }
        });
        assistantDoctor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    doctorTitle.add("Asst.");
                } else {
                    doctorTitle.remove("Asst.");
                }
            }
        });
        assistantProfessorDoctor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    doctorTitle.add("Asst. Prof.");
                } else {
                    doctorTitle.remove("Asst. Prof.");
                }
            }
        });
        professorDoctor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    doctorTitle.add("Prof. Dr.");
                } else {
                    doctorTitle.remove("Prof. Dr.");
                }
            }
        });
    }

    public void setGenderListener() {
        if (!gender.isEmpty()) {
            if (gender.equalsIgnoreCase("female")) {
                femaleRadio.setChecked(true);
            } else {
                maleRadio.setChecked(true);
            }
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedRadio) {
                RadioButton checkedRadioButton = (RadioButton) radioGroup.findViewById(checkedRadio);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                if (isChecked) {
                    gender = checkedRadioButton.getText().toString();
                }
            }
        });
    }

    public void setRatingListener() {
        if (rating != 0.0) {
            ratingBar.setRating(rating);
        }
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float value, boolean hasChanged) {
                if (hasChanged) {
                    rating = value;
                }
            }
        });
    }


    public void consultationFeeChangeListener() {
        consultationFeeRange.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                List<Float> values = slider.getValues();
                rangeMaxAmount.setText(String.valueOf(Integer.parseInt(String.valueOf(Math.round(values.get(1))))));
                rangeMinAmount.setText(String.valueOf(Integer.parseInt(String.valueOf(Math.round(values.get(0))))));
            }
        });
    }

    public void setLabelFormatter() {
        consultationFeeRange.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                Locale BANGLADESH = new Locale("en", "BD");
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(BANGLADESH);
                return currencyFormat.format(value);
            }
        });
    }


    protected void onStart() {
        super.onStart();
        binding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData();
                        binding.swipe.setRefreshing(false);
                    }
                }, 1000);

            }
        });
    }

    public void loadData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        DatabaseReference reference = firebase.getDatabaseReference("doctorInfo");
        if (specialist == null) {
            loadAllDoctor(reference);
        } else {
            loadSpecificSpecialistDoctor(reference);
        }
    }

    public void searchDoctor() {
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

    private void filterList(String searchedDoctor) {
        int count = 0;
        ArrayList<doctorInfoModel> filteredList = new ArrayList<>();
        binding.doctorRecyclerView.setLayoutManager(new LinearLayoutManager(ViewAllDoctor.this, LinearLayoutManager.VERTICAL, false), R.layout.shimmer_layout_available_doctor);
        adapter = new viewAllDoctorAdapter(this, filteredList);
        binding.doctorRecyclerView.showShimmer();
        for (doctorInfoModel doctor : doctors) {
            if (doctor.getFullName().toLowerCase().contains(searchedDoctor.toLowerCase()) | doctor.getSpecialty().toLowerCase().contains(searchedDoctor.toLowerCase()) | doctor.getDoctorId().toLowerCase().contains(searchedDoctor.toLowerCase())) {
                filteredList.add(doctor);
                count++;
            }
        }
        Handler handler = new Handler(Looper.getMainLooper());
        if (filteredList.isEmpty()) {
            int finalCount = count;
            handler.postDelayed(() -> {
                binding.doctorCount.setText(String.valueOf(finalCount));
                binding.progressBar.setVisibility(View.GONE);
                binding.doctorRecyclerView.hideShimmer();
                binding.doctorRecyclerView.setVisibility(View.GONE);
            }, 200);

        } else {
            int finalCount = count;
            handler.postDelayed(() -> {
                binding.doctorRecyclerView.setAdapter(adapter);
                binding.doctorRecyclerView.hideShimmer();
                binding.progressBar.setVisibility(View.GONE);
                binding.doctorCount.setText(String.valueOf(finalCount));
            }, 1000);
        }
    }

    protected void loadAllDoctor(DatabaseReference loadAllDataReference) {
        binding.doctorRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false), R.layout.shimmer_layout_available_doctor);
        binding.doctorRecyclerView.showShimmer();
        adapter = new viewAllDoctorAdapter(this, doctors);
        binding.doctorRecyclerView.setAdapter(adapter);
        loadAllDataReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                doctors.clear();
                if (snapshot.exists()) {
                    binding.doctorCount.setText(String.valueOf(snapshot.getChildrenCount()));
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        doctorInfoModel model = snap.getValue(doctorInfoModel.class);
                        doctors.add(model);
                        count++;
                    }
                    Collections.shuffle(doctors);
                    binding.doctorCount.setText(String.valueOf(count));
                    adapter.notifyDataSetChanged();
                    binding.doctorRecyclerView.hideShimmer();
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    protected void loadSpecificSpecialistDoctor(DatabaseReference specificSpecialistReference) {
        binding.doctorRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.doctorRecyclerView.showShimmer();
        adapter = new viewAllDoctorAdapter(this, doctors);
        binding.doctorRecyclerView.setAdapter(adapter);
        specificSpecialistReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctors.clear();
                int count = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        doctorInfoModel model = snap.getValue(doctorInfoModel.class);
                        assert model != null;
                        if (model.getSpecialty().contains(specialist)) {
                            doctors.add(model);
                            count += 1;
                        }
                    }
                    binding.doctorCount.setText(String.valueOf(count));
                    adapter.notifyDataSetChanged();
                    binding.doctorRecyclerView.hideShimmer();
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });

    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        allDoctorLoadExecutor.shutdown();
    }

    public void onBackPressed() {
        specialist = null;
        finish();
    }
}