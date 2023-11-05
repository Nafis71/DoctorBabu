package com.example.doctorbabu.patient.HomeModules;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.doctorbabu.R;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;

public class sliderAdapter extends SliderViewAdapter<SliderViewAdapter.ViewHolder> {

    ArrayList<Integer> images;

    sliderAdapter(ArrayList<Integer> images) {
        this.images = images;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        ImageView imageView;
        imageView = viewHolder.itemView.findViewById(R.id.sliderImage);
        imageView.setImageResource(images.get(position));
    }

    @Override
    public int getCount() {
        return images.size();
    }

    public static class Holder extends SliderViewAdapter.ViewHolder {
        ImageView imageView;

        public Holder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.sliderImage);
        }
    }
}
