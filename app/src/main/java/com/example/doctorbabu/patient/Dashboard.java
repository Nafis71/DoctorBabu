package com.example.doctorbabu.patient;


import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.doctorbabu.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.dialog.MaterialDialogs;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Dashboard extends AppCompatActivity {
    ChipNavigationBar bottomNavigation;
    FragmentManager fm;
    boolean isOpenDoctorVideo = false,isOpenHome = false, isOpenHistory = false, isOpenProfile = false;
    String code = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        code = getIntent().getStringExtra("code");
        setContentView(R.layout.activity_dashboard);
        bottomNavigation = findViewById(R.id.bottomBar);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> bottomNavigation.setOnItemSelectedListener(id -> {
            if (id == R.id.nav_doctor_video) {
                if(!isOpenDoctorVideo){
                    loadFragment(new Doctor(), "doctor");
                    isOpenDoctorVideo = true;
                }
            } else if (id == R.id.nav_home) {
                if(!isOpenHome){
                    loadFragment(new Home(), "home");
                    isOpenHome = true;
                }
            } else if (id == R.id.nav_history) {
                if(!isOpenHistory)
                {
                    loadFragment(new PrescriptionHistory(),"history");
                    isOpenHistory = true;
                }

            } else if (id == R.id.nav_profile) {
                if(!isOpenProfile)
                {
                    loadFragment(new Profile(), "profile");
                    isOpenProfile = true;
                }

            }
        }));
        loadFragment(new Home(),"home");
        isOpenHome = true;
        bottomNavigation.setItemSelected(R.id.nav_home, true);

    }

    public void loadFragment(Fragment fragment, String tag) {
        fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction().addToBackStack(tag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.container, fragment);
        ft.commit();
    }

    public void onBackPressed() {
            String fragmentName;
            int count = getSupportFragmentManager().getBackStackEntryCount();
            if(count == 1) {
                fragmentName = getSupportFragmentManager().getBackStackEntryAt(0).getName();
            } else {
                fragmentName = getSupportFragmentManager().getBackStackEntryAt(count-2).getName();
            }
            Log.wtf("Fragment Name",fragmentName);
                if(count == 1 && fragmentName == "home") {
                    finish();
                } else {
                getSupportFragmentManager().popBackStack();
                if(fragmentName.equals("doctor")){
                    bottomNavigation.setItemSelected(R.id.nav_doctor_video,true);
                    isOpenHome = false;
                    isOpenProfile = false;
                    isOpenHistory = false;
                }else if(fragmentName.equals("history")){
                    bottomNavigation.setItemSelected(R.id.nav_history,true);
                    isOpenHome = false;
                    isOpenProfile = false;
                    isOpenDoctorVideo = false;
                } else if(fragmentName.equals("home")){
                    bottomNavigation.setItemSelected(R.id.nav_home,true);
                    isOpenHistory = false;
                    isOpenProfile = false;
                    isOpenDoctorVideo = false;
                } else if(fragmentName.equals("profile")){
                    bottomNavigation.setItemSelected(R.id.nav_profile,true);
                    isOpenHistory = false;
                    isOpenHome = false;
                    isOpenDoctorVideo = false;
                }
            }
    }

}