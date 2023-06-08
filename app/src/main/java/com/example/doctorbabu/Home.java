package com.example.doctorbabu;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;

public class Home extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;
    String uId;
    ImageView profilePicture;

    public Home() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadImageSlider();
        viewBinding();
        firebaseAuth();
    }
    public void firebaseAuth()
    {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(user != null)
        {
            uId = user.getUid();
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("users");
        reference.child(uId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        DataSnapshot snapshot = task.getResult();
                        if(!String.valueOf(snapshot.child("photoUrl").getValue()).equals("null")) {
                            Glide.with(getContext()).load(String.valueOf(snapshot.child("photoUrl").getValue())).into(profilePicture);
                            profilePicture.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            profilePicture.setImageResource(R.drawable.profile_picture);
                            profilePicture.setVisibility(View.VISIBLE);
                        }

                    }
                }
                else
                {
                    Toast.makeText(getContext(), "Profile picture couldn't be loaded", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void viewBinding()
    {
        profilePicture = (ImageView) getView().findViewById(R.id.profilePicture);
    }
    public void loadImageSlider()
    {
        SliderView sliderView;
        ArrayList<Integer> images =  new ArrayList<>();
        images.add(R.drawable.banner1);
        images.add(R.drawable.banner2);
        images.add(R.drawable.banner3);
        sliderView = (SliderView) getView().findViewById(R.id.imageSlider);
        sliderAdapter adapter = new sliderAdapter(images);
        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION);
        sliderView.startAutoCycle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}