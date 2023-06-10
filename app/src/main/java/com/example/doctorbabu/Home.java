package com.example.doctorbabu;

import static android.content.Intent.getIntent;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.Locale;

public class Home extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;
    String uId;
    ImageView profilePicture,languageImage;
    BottomSheetDialog bookAppointmentSheet;
    CardView appointmentCard;
    Button buttonDialog;
    RadioButton english,bengali;

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
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callProfileFragment();
            }
        });
        appointmentCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callAppointmentBottomSheet();
            }
        });
        languageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callLanguageChenger();
            }
        });
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
        appointmentCard = (CardView) getView().findViewById(R.id.appointmentCard);
        languageImage = (ImageView) getView().findViewById(R.id.languageImage);
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
    public void callProfileFragment() {
        BottomNavigationView bottomNavigation = getActivity().findViewById(R.id.bottomView);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, new Profile());
        ft.commit();
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
    }
    public void callAppointmentBottomSheet()
    {
        bookAppointmentSheet = new BottomSheetDialog(getContext(),R.style.bottomSheetTheme);
        View appointmentView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_book_doctor,null);
        bookAppointmentSheet.setContentView(appointmentView);
        bookAppointmentSheet.show();
    }
    public void callLanguageChenger()
    {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.custom_language_dialog);
        dialog.setCancelable(true);
        buttonDialog = dialog.findViewById(R.id.dialogButton);
        english = dialog.findViewById(R.id.english);
        bengali = dialog.findViewById(R.id.bengali);
        FirebaseDatabase database = FirebaseDatabase.getInstance
                ("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("appLanguage");
        reference.child(uId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful() && task.getResult().exists())
                {
                    DataSnapshot snapshot = task.getResult();
                    if(String.valueOf(snapshot.getValue()).equals("en"))
                    {
                        english.setChecked(true);
                    }
                    else
                    {
                        bengali.setChecked(true);
                    }

                }
            }
        });
        buttonDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(english.isChecked())
                {
                    reference.child(uId).setValue("en");
                }
                else
                {
                    reference.child(uId).setValue("bn");
                }

                dialog.cancel();
                ProcessPhoenix.triggerRebirth(getActivity());
            }
        });
        dialog.show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}