package com.example.doctorbabu.patient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Home extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;
    String uId;
    ImageView profilePicture, languageImage;
    BottomSheetDialog bookAppointmentSheet;
    CardView appointmentCard, sliderCard, consultantCard, medicineReminderCard, reportCard, appointmentHistory, onlineCosultantCard, medicineCard;
    Button buttonDialog;
    RadioButton english, bengali;
    Animation leftAnim, rightAnim;
    ExecutorService imageSliderExecutor;


    public Home() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imageSliderExecutor = Executors.newSingleThreadExecutor();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imageSliderExecutor.execute(this::loadImageSlider);
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
                callLanguageChanger();
            }
        });
        consultantCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(requireActivity(), AiDoctor.class);
//                startActivity(intent);
            }
        });

    }

    public void firebaseAuth() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            uId = user.getUid();
        }
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("users");
        reference.child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isAdded()) {
                    if (!String.valueOf(snapshot.child("photoUrl").getValue()).equals("null")) {
                        Glide.with(requireContext()).load(String.valueOf(snapshot.child("photoUrl").getValue())).into(profilePicture);
                        profilePicture.setVisibility(View.VISIBLE);
                    } else {
                        profilePicture.setImageResource(R.drawable.profile_picture);
                        profilePicture.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void viewBinding() {
        profilePicture = requireView().findViewById(R.id.profilePicture);
        appointmentCard = requireView().findViewById(R.id.appointmentCard);
        languageImage = requireView().findViewById(R.id.languageImage);
        sliderCard = requireView().findViewById(R.id.sliderCard);
        consultantCard = requireView().findViewById(R.id.consultantCard);
        medicineReminderCard = requireView().findViewById(R.id.medicineReminderCard);
        reportCard = requireView().findViewById(R.id.reportCard);
        appointmentHistory = requireView().findViewById(R.id.appointmentHistory);
        onlineCosultantCard = requireView().findViewById(R.id.onlineCosultantCard);
        medicineCard = requireView().findViewById(R.id.medicineCard);
        leftAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.left_animation);
        rightAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.right_anim);
        appointmentCard.setAnimation(leftAnim);
        sliderCard.setAnimation(rightAnim);
        consultantCard.setAnimation(rightAnim);
        medicineReminderCard.setAnimation(leftAnim);
        reportCard.setAnimation(leftAnim);
        appointmentHistory.setAnimation(leftAnim);
        onlineCosultantCard.setAnimation(rightAnim);
        medicineCard.setAnimation(rightAnim);

    }

    public void loadImageSlider() {
        SliderView sliderView;
        ArrayList<Integer> images = new ArrayList<>();
        images.add(R.drawable.banner1);
        images.add(R.drawable.banner2);
        images.add(R.drawable.banner3);
        sliderView = (SliderView) requireView().findViewById(R.id.imageSlider);
        sliderAdapter adapter = new sliderAdapter(images);
        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION);
        sliderView.startAutoCycle();
    }

    public void callProfileFragment() {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, new Profile());
        ft.commit();
    }

    public void callAppointmentBottomSheet() {
        bookAppointmentSheet = new BottomSheetDialog(requireContext(), R.style.bottomSheetTheme);
        View appointmentView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_book_doctor, null);
        bookAppointmentSheet.setContentView(appointmentView);
        bookAppointmentSheet.show();
    }

    public void callLanguageChanger() {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.custom_language_dialog);
        dialog.setCancelable(true);
        buttonDialog = dialog.findViewById(R.id.dialogButton);
        english = dialog.findViewById(R.id.english);
        bengali = dialog.findViewById(R.id.bengali);
        SharedPreferences preferences = requireActivity().getSharedPreferences("language", Context.MODE_PRIVATE);
        String language = preferences.getString("lang", "");
        if (language.equals("en")) {
            english.setChecked(true);
        } else if (language.equals("bn")) {
            bengali.setChecked(true);
        } else {
            english.setChecked(true);
        }
        buttonDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (english.isChecked()) {
                    SharedPreferences preferences = requireActivity().getSharedPreferences("language", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("lang", "en");
                    editor.apply();

                } else {
                    SharedPreferences preferences = requireActivity().getSharedPreferences("language", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("lang", "bn");
                    editor.apply();
                }
                dialog.cancel();
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Language Change").setMessage("Language is changing, Please wait....").setCancelable(false);
                builder.create().show();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        restart();
                    }
                }, 1500);

            }
        });
        dialog.show();
    }

    public void onDestroyView() {
        super.onDestroyView();
        imageSliderExecutor.shutdown();
    }

    public void restart() {
        ProcessPhoenix.triggerRebirth(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
}