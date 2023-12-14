package com.example.doctorbabu.patient.HomeModules;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityMedicineShopBinding;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;

public class MedicineShop extends AppCompatActivity {
    ActivityMedicineShopBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineShopBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadImageSlider();
    }
    public void loadImageSlider() {
        ArrayList<Integer> images = new ArrayList<>();
        images.add(R.drawable.medicine_banner_1);
        images.add(R.drawable.medicine_banner_2);
        images.add(R.drawable.medicine_banner_3);
        sliderAdapter adapter = new sliderAdapter(images);
        binding.imageSlider.setSliderAdapter(adapter);
        binding.imageSlider.setIndicatorAnimation(IndicatorAnimationType.WORM);
        binding.imageSlider.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION);
        binding.imageSlider.startAutoCycle();
    }
}