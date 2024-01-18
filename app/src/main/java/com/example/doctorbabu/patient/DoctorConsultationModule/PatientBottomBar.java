package com.example.doctorbabu.patient.DoctorConsultationModule;


import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.HomeModules.Home;
import com.example.doctorbabu.patient.PatientProfileModule.PrescriptionHistory;
import com.example.doctorbabu.patient.PatientProfileModule.Profile;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PatientBottomBar extends AppCompatActivity {
    ChipNavigationBar bottomNavigation;
    FragmentManager fm;
    boolean isOpenDoctorVideo = false, isOpenHome = false, isOpenHistory = false, isOpenProfile = false, isBackPressed = false, isAdded;
    String code = null;
    int count, fragmentId = -1;
    String fragmentName;
    ExecutorService executorService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        code = getIntent().getStringExtra("code");
        setContentView(R.layout.activity_patient_bottom_bar);
        bottomNavigation = findViewById(R.id.bottomBar);
        executorService = Executors.newSingleThreadExecutor();
        try{
            executorService.execute(() -> bottomNavigation.setOnItemSelectedListener(id -> {
                if (id == R.id.nav_doctor_video) {
                    if (!isOpenDoctorVideo && !isBackPressed) {
                        count = getSupportFragmentManager().getBackStackEntryCount();
                        for (int i = 0; i < count; i++) {
                            fragmentName = getSupportFragmentManager().getBackStackEntryAt(i).getName();
                            if (fragmentName.equals("doctor")) {
                                fragmentId = getSupportFragmentManager().getBackStackEntryAt(i).getId();
                                break;
                            }
                        }
                        if (fragmentId != -1) {
                            getSupportFragmentManager().popBackStack(fragmentId, 100);
                            navBarEnableDisable("doctor");
                        } else {
                            loadFragment(new Doctor(), "doctor");
                            navBarEnableDisable("doctor");
                        }

                    }
                } else if (id == R.id.nav_home) {
                    if (!isOpenHome && !isBackPressed) {
                        count = getSupportFragmentManager().getBackStackEntryCount();
                        for (int i = 0; i < count; i++) {
                            fragmentName = getSupportFragmentManager().getBackStackEntryAt(i).getName();
                            assert fragmentName != null;
                            if (fragmentName.equals("home")) {
                                fragmentId = getSupportFragmentManager().getBackStackEntryAt(i).getId();
                                break;
                            }
                        }
                        if (fragmentId != -1) {
                            getSupportFragmentManager().popBackStack(fragmentId, 200);
                            navBarEnableDisable("home");
                        } else {
                            loadFragment(new Home(), "home");
                            navBarEnableDisable("home");
                        }
                    }
                } else if (id == R.id.nav_history) {
                    if (!isOpenHistory && !isBackPressed) {
                        count = getSupportFragmentManager().getBackStackEntryCount();
                        for (int i = 0; i < count; i++) {
                            fragmentName = getSupportFragmentManager().getBackStackEntryAt(i).getName();
                            assert fragmentName != null;
                            if (fragmentName.equals("history")) {
                                fragmentId = getSupportFragmentManager().getBackStackEntryAt(i).getId();
                                break;
                            }
                        }
                        if (fragmentId != -1) {
                            getSupportFragmentManager().popBackStack(fragmentId, 300);
                            navBarEnableDisable("history");
                        } else {
                            loadFragment(new PrescriptionHistory(), "history");
                            navBarEnableDisable("history");
                        }
                    }

                } else if (id == R.id.nav_profile) {
                    if (!isOpenProfile && !isBackPressed) {
                        count = getSupportFragmentManager().getBackStackEntryCount();
                        for (int i = 0; i < count; i++) {
                            fragmentName = getSupportFragmentManager().getBackStackEntryAt(i).getName();
                            assert fragmentName != null;
                            if (fragmentName.equals("profile")) {
                                fragmentId = getSupportFragmentManager().getBackStackEntryAt(i).getId();
                                break;
                            }
                        }
                        if (fragmentId != -1) {
                            getSupportFragmentManager().popBackStack(fragmentId, 400);
                            navBarEnableDisable("profile");
                        } else {
                            loadFragment(new Profile(), "profile");
                            navBarEnableDisable("profile");
                        }
                    }
                }
                fragmentId = -1;
                fragmentName = null;
            }));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        loadFragment(new Home(),"home");
        isOpenHome = true;
    }

    public void loadFragment(Fragment fragment, String tag) {
        fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction().addToBackStack(tag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.container, fragment);
        ft.commit();
    }

    public void onBackPressed() {
        String fragmentName;
        isBackPressed = true;
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 1) {
            fragmentName = getSupportFragmentManager().getBackStackEntryAt(0).getName();
        } else {
            fragmentName = getSupportFragmentManager().getBackStackEntryAt(count - 2).getName();
        }
        if (count == 1 && Objects.equals(fragmentName, "home")) {
            finish();
        } else {
            getSupportFragmentManager().popBackStack();
            switch (Objects.requireNonNull(fragmentName)) {
                case "doctor":
                    bottomNavigation.setItemSelected(R.id.nav_doctor_video, true);
                    navBarEnableDisable("doctor");
                    break;
                case "history":
                    bottomNavigation.setItemSelected(R.id.nav_history, true);
                    navBarEnableDisable("history");
                    break;
                case "home":
                    bottomNavigation.setItemSelected(R.id.nav_home, true);
                    navBarEnableDisable("home");
                    break;
                case "profile":
                    bottomNavigation.setItemSelected(R.id.nav_profile, true);
                    navBarEnableDisable("profile");
                    break;
            }
        }
    }

    private void navBarEnableDisable(String tag) {
        switch (tag) {
            case "doctor":
                isOpenDoctorVideo = true;
                isOpenHome = false;
                isOpenProfile = false;
                isOpenHistory = false;
                isBackPressed = false;
                break;
            case "home":
                isOpenHome = true;
                isOpenHistory = false;
                isOpenProfile = false;
                isOpenDoctorVideo = false;
                isBackPressed = false;
                break;
            case "history":
                isOpenHistory = true;
                isOpenHome = false;
                isOpenProfile = false;
                isOpenDoctorVideo = false;
                isBackPressed = false;
                break;
            case "profile":
                isOpenProfile = true;
                isOpenHistory = false;
                isOpenHome = false;
                isOpenDoctorVideo = false;
                isBackPressed = false;
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}