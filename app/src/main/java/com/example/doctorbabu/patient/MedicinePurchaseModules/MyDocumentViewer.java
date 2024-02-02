
package com.example.doctorbabu.patient.MedicinePurchaseModules;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityMyDocumentViewerBinding;

import uk.co.senab.photoview.PhotoViewAttacher;

public class MyDocumentViewer extends AppCompatActivity {
    ActivityMyDocumentViewerBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyDocumentViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String image = getIntent().getStringExtra("imageUrl");
        PhotoViewAttacher pAttacher;
        pAttacher = new PhotoViewAttacher(binding.image);
        pAttacher.update();
        Glide.with(this).load(image).into(binding.image);
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}