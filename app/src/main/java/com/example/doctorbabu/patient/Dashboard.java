package com.example.doctorbabu.patient;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;

import android.os.Bundle;
import android.util.Log;


import com.example.doctorbabu.R;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class Dashboard extends AppCompatActivity {
    ChipNavigationBar bottomNavigation;
    FragmentManager fm;
    int check = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        bottomNavigation = findViewById(R.id.bottomBar);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> bottomNavigation.setOnItemSelectedListener(id -> {
            if (id == R.id.nav_doctor_video) {
                loadFragment(new Doctor(), false);
            } else if (id == R.id.nav_home) {
                loadFragment(new Home(), false);
            } else if (id == R.id.nav_history) {
                loadFragment(new PrescriptionHistory(), false);
            } else if (id == R.id.nav_profile) {
                loadFragment(new Profile(), false);
            }
        }));

//        loadFragment(new Home(),true);
        bottomNavigation.setItemSelected(R.id.nav_home, true);
        bottomNavigation.showBadge(R.id.nav_doctor_video, 24);


    }

    public void loadFragment(Fragment fragment, boolean flag) {
        fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        if (flag) {
            if (check == 0) {
                ft.add(R.id.container, fragment);
                check++;
            }
        } else {
            ft.replace(R.id.container, fragment);
        }
        ft.commit();

    }

    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Quitting App").setMessage("Are you sure you want to quit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        dialog.create().show();
    }

}