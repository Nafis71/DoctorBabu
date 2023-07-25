package com.example.doctorbabu.patient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class doctorInfoPageAdapter extends FragmentStateAdapter {
    private final String[] titles = new String[]{"Info", "Experience", "Reviews"};
    String doctorId;

    public doctorInfoPageAdapter(@NonNull FragmentActivity fragmentActivity, String doctorId) {
        super(fragmentActivity);
        this.doctorId = doctorId;
    }

    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DoctorExtraInfo(doctorId);
            case 1:
                return new DoctorExperience(doctorId);
            case 2:
                return new DoctorReview(doctorId);
        }
        return new DoctorExtraInfo(doctorId);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
