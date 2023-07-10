package com.example.doctorbabu.doctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.Doctor;
import com.example.doctorbabu.patient.Home;
import com.example.doctorbabu.patient.PrescriptionHistory;
import com.example.doctorbabu.patient.Profile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class DoctorDashboard extends AppCompatActivity {
    BottomNavigationView bottomNavigation;
    FragmentManager fm;
    int check = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);
        bottomNavigation = findViewById(R.id.bottomView);
        loadFragment(new DoctorProfile(),true);
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                int count = 0;
                if(id == R.id.nav_home)
                {
                    if(bottomNavigation.getSelectedItemId() != R.id.nav_home)
                        loadFragment(new DoctorHome(),false);

                }
                else if(id == R.id.nav_call)
                {
                    if(bottomNavigation.getSelectedItemId() != R.id.nav_call)
                        loadFragment(new DoctorCallRoom(),false);
                }
                else if(id == R.id.nav_history)
                {
                    if(bottomNavigation.getSelectedItemId() != R.id.nav_history)
                        loadFragment(new PrescriptionHistory(),false);
                }
                else
                {
                    if(bottomNavigation.getSelectedItemId() != R.id.nav_profile)
                        loadFragment(new DoctorProfile(),false);
                }
                return true;
            }
        });
    }
    public void loadFragment(Fragment fragment, boolean flag)
    {
        fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction().setReorderingAllowed(true).addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if(flag)
        {
            if(check == 0)
            {
                ft.add(R.id.container,fragment);
                check++;
            }
            else
            {
                ft.replace(R.id.container,fragment);
            }
        }
        else
        {
            ft.replace(R.id.container,fragment);
        }
        ft.commit();

    }
    public void onBackPressed()
    {
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