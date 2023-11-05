package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.doctorInfoModel;
import com.example.doctorbabu.DatabaseModels.doctorSearchResultModel;
import com.example.doctorbabu.DatabaseModels.joiningDates;
import com.example.doctorbabu.DatabaseModels.leavingDates;
import com.example.doctorbabu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;

public class DoctorInfo extends Fragment {
    FirebaseDatabase database;
    ViewPager2 vPager;
    TabLayout tabs;
    CardView videoCall;
    TextView doctorNameView, doctorDegreeView,
            doctorSpecialtiesView, currentlyWorkingView, ratingView, totalExperienceView, bmdcView, onlineStatusView, offlineStatusView;
    ImageView profilePictureView, goback, outlinedLove, onlineStatusBanner, offlineStatusBanner;

    String doctorId, doctorName, doctorTitle, doctorDegree, doctorSpecialty, currentlyWorking, photoUrl, bmdc;
    RelativeLayout parentLayout;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();
    float rating;
    private final String[] titles = new String[]{"Info", "Experience", "Reviews"};

    public DoctorInfo() {
        // Required empty public constructor
    }

    public DoctorInfo(doctorSearchResultModel model) {
        this.doctorId = model.getDoctorId();
        this.doctorName = model.getFullName();
        this.doctorDegree = model.getDegrees();
        this.currentlyWorking = model.getSpecialty();
        this.photoUrl = model.getPhotoUrl();
        this.rating = model.getRating();
        this.bmdc = model.getBmdc();
        this.doctorSpecialty = model.getSpecialty();
        this.doctorTitle = model.getTitle();
    }
    public DoctorInfo(doctorInfoModel model) {
        this.doctorId = model.getDoctorId();
        this.doctorName = model.getFullName();
        this.doctorDegree = model.getDegrees();
        this.currentlyWorking = model.getCurrentlyWorking();
        this.photoUrl = model.getPhotoUrl();
        this.rating = model.getRating();
        this.bmdc = model.getBmdc();
        this.doctorSpecialty = model.getSpecialty();
        this.doctorTitle = model.getTitle();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewBinding();
        loadData();
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, new Doctor(1)).commit();
            }
        });
        outlinedLove.setOnClickListener(new View.OnClickListener() {
            boolean toggleButton = true;

            @Override
            public void onClick(View view) {

                if (toggleButton) {
                    outlinedLove.setImageResource(R.drawable.filledlove);
                    toggleButton = false;
                    addFavourite();
                } else {
                    outlinedLove.setImageResource(R.drawable.blanklove);
                    toggleButton = true;
                }
            }
        });
        videoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireActivity(), CheckoutDoctor.class);
                intent.putExtra("doctorId", doctorId);
                intent.putExtra("doctorTitle", doctorTitle);
                intent.putExtra("doctorName", doctorName);
                intent.putExtra("doctorDegree", doctorDegree);
                intent.putExtra("doctorSpecialty", doctorSpecialty);
                intent.putExtra("doctorCurrentlyWorking", currentlyWorking);
                intent.putExtra("photoUrl", photoUrl);
                startActivity(intent);
            }
        });
    }

    public void viewBinding() {
        doctorNameView = requireView().findViewById(R.id.doctorName);
        doctorDegreeView = requireView().findViewById(R.id.doctorDegree);
        doctorSpecialtiesView = requireView().findViewById(R.id.doctorSpecialties);
        currentlyWorkingView = requireView().findViewById(R.id.currentlyWorking);
        ratingView = requireView().findViewById(R.id.rating);
        totalExperienceView = requireView().findViewById(R.id.totalExperience);
        bmdcView = requireView().findViewById(R.id.bmdc);
        profilePictureView = requireView().findViewById(R.id.profilePicture);
        vPager = requireView().findViewById(R.id.vPager);
        tabs = requireView().findViewById(R.id.tabs);
        goback = requireView().findViewById(R.id.goback);
        outlinedLove = requireView().findViewById(R.id.outlinedLove);
        onlineStatusBanner = requireView().findViewById(R.id.onlineStatusBanner);
        onlineStatusView = requireView().findViewById(R.id.onlineStatus);
        offlineStatusBanner = requireView().findViewById(R.id.offlineStatusBanner);
        offlineStatusView = requireView().findViewById(R.id.offlineStatus);
        videoCall = requireView().findViewById(R.id.videoCall);
        parentLayout = requireView().findViewById(R.id.parentLayout);
    }

    public void loadData() {
        String doctorName = this.doctorTitle + this.doctorName;
        doctorNameView.setText(doctorName);
        doctorDegreeView.setText(doctorDegree);
        doctorSpecialtiesView.setText(doctorSpecialty);
        currentlyWorkingView.setText(currentlyWorking);
        ratingView.setText(String.valueOf(rating));
        bmdcView.setText(bmdc);
        Glide.with(requireContext()).load(photoUrl).into(profilePictureView);
        DatabaseReference reference = database.getReference("doctorInfo");
        reference.child(doctorId).child("onlineStatus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = String.valueOf(snapshot.getValue());
                    if (Integer.parseInt(status) != 0) {
                        onlineStatusBanner.setVisibility(View.VISIBLE);
                        onlineStatusView.setVisibility(View.VISIBLE);
                        offlineStatusBanner.setVisibility(View.GONE);
                        offlineStatusView.setVisibility(View.GONE);
                    } else {
                        offlineStatusBanner.setVisibility(View.VISIBLE);
                        offlineStatusView.setVisibility(View.VISIBLE);
                        onlineStatusBanner.setVisibility(View.GONE);
                        onlineStatusView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        ArrayList<Integer> periods = new ArrayList<>();
        DatabaseReference experienceReference = database.getReference("doctorPastExperience");
        experienceReference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    joiningDates model = snap.getValue(joiningDates.class);
                    assert model != null;
                    leavingDates model2 = snap.getValue(leavingDates.class);
                    assert model2 != null;
                    String joiningDate = model.getJoiningDate();
                    String leavingDate = model2.getLeavingDate();
                    String[] splitText = joiningDate.split("/");
                    int year = Integer.parseInt(splitText[0]);
                    int month = Integer.parseInt(splitText[1]);
                    int day = Integer.parseInt(splitText[2]);
                    LocalDate beginningDay = LocalDate.of(year, month, day);
                    splitText = leavingDate.split("/");
                    year = Integer.parseInt(splitText[0]);
                    month = Integer.parseInt(splitText[1]);
                    day = Integer.parseInt(splitText[2]);
                    LocalDate endDay = LocalDate.of(year, month, day);
                    Period period = Period.between(beginningDay, endDay);
                    String years = String.valueOf(period.getYears());
                    periods.add(Integer.parseInt(years));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        experienceReference = database.getReference("doctorCurrentlyWorking");
        experienceReference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!String.valueOf(snapshot.child("joiningDate").getValue()).equals("null")) {
                    String date = String.valueOf(snapshot.child("joiningDate").getValue());
                    String[] splitText = date.split("/");
                    int year = Integer.parseInt(splitText[0]);
                    int month = Integer.parseInt(splitText[1]);
                    int day = Integer.parseInt(splitText[2]);
                    LocalDate bday = LocalDate.of(year, month, day);
                    LocalDate today = LocalDate.now();
                    Period period = Period.between(bday, today);
                    String years = String.valueOf(period.getYears());
                    periods.add(Integer.parseInt(years));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int totalExperience = 0;
                for (int i = 0; i < periods.size(); i++) {
                    totalExperience = totalExperience + periods.get(i);
                }
                String totalExperienceString = String.valueOf(totalExperience) + "+ years";
                totalExperienceView.setText(totalExperienceString);
            }
        }, 1000);
        doctorInfoPageAdapter adapter = new doctorInfoPageAdapter(requireActivity(), doctorId);
        vPager.setAdapter(adapter);
        new TabLayoutMediator(tabs, vPager, ((tab, position) -> tab.setText(titles[position]))).attach();
    }

    public void addFavourite() {
        HashMap<String, String> data = new HashMap<>();
        data.put("doctorId", doctorId);
        data.put("photoUrl", photoUrl);
        data.put("fullName", doctorName);
        data.put("title", doctorTitle);
        data.put("specialty", doctorSpecialty);
        DatabaseReference reference = database.getReference();
        reference.child("favouriteDoctors").child(user.getUid()).child(doctorId).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Snackbar.make(parentLayout, doctorTitle + " " + doctorName + " added to your favourite list", Snackbar.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doctor_info, container, false);
    }
}