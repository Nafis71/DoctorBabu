package com.example.doctorbabu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Dashboard extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseUser user;
    BottomNavigationView bottomNavigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(user== null)
        {
            Intent intent = new Intent(Dashboard.this,Login.class);
            startActivity(intent);
            finish();
        }
        bottomNavigation = findViewById(R.id.bottomView);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                int count = 0;
                if(id == R.id.nav_home)
                {
                    loadFragment(new Home());
                }
                else if(id == R.id.nav_doctor)
                {
                    loadFragment(new Doctor());
                }
                else if(id == R.id.nav_history)
                {
                    loadFragment(new PrescriptionHistory());
                }
                else
                {
                    loadFragment(new Profile());
                }
                return true;
            }
        });
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }
    public void loadFragment(Fragment fragment)
    {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.container,fragment);
        ft.addToBackStack(null);
        ft.commit();
    }
}