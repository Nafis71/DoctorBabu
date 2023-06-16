package com.example.doctorbabu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class pageAdapter extends FragmentStateAdapter {
    private String [] titles = new String[]{"Departments","Symptoms"};

    public pageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position)
        {
            case 0:
                return new Department();

            case 1:
                return new Symptom();
        }
        return new Department();
    }

    @Override
    public int getItemCount() {
       return titles.length;
    }
}
