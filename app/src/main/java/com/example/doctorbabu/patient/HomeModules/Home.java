package com.example.doctorbabu.patient.HomeModules;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.FragmentHomeBinding;
import com.example.doctorbabu.patient.AlarmModules.MedicineReminder;
import com.example.doctorbabu.patient.DiagnoseReportUploadModule.DiagnosisReportUploadList;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.thekhaeng.pushdownanim.PushDownAnim;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Home extends Fragment {
    FirebaseAuth auth;
    FirebaseUser user;
    String uId;
    BottomSheetDialog bookAppointmentSheet;
    Button buttonDialog;
    RadioButton english, bengali;
    Animation leftAnim, rightAnim;
    ExecutorService firebaseExecutor;
    ChipNavigationBar bottomNavigation;

    FragmentHomeBinding binding;

    public Home() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseExecutor = Executors.newSingleThreadExecutor();
        firebaseExecutor.execute(this::firebaseAuth);
        PushDownAnim.setPushDownAnimTo(binding.consultantCard, binding.appointmentCard, binding.medicineReminderCard, binding.reportCard,binding.pendingAppointment)
                .setScale(PushDownAnim.MODE_SCALE, 0.95f);

        loadImageSlider();
        setAnimations();
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.profilePicture.setOnClickListener(v -> callProfileFragment());
        binding.appointmentCard.setOnClickListener(v -> callAppointmentBottomSheet());
        binding.languageImage.setOnClickListener(v -> callLanguageChanger());
        binding.consultantCard.setOnClickListener(view1 -> {
            callDoctorFragment();
        });
        binding.medicineReminderCard.setOnClickListener(view -> callMedicineReminder());
        binding.reportCard.setOnClickListener(view -> callDiagnoseReportUploader());
        binding.pendingAppointment.setOnClickListener(view -> callPendingAppointment());
    }

    public void firebaseAuth() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user != null) {
            uId = user.getUid();
            loadUserProfileData();
        }
    }

    public void loadUserProfileData() {
        Firebase firebase = Firebase.getInstance();
        DatabaseReference reference = firebase.getDatabaseReference("users");
        reference.child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isAdded()) {
                    if (!String.valueOf(snapshot.child("photoUrl").getValue()).equals("null")) {
                        Glide.with(requireContext()).load(String.valueOf(snapshot.child("photoUrl").getValue())).into(binding.profilePicture);
                        binding.profilePicture.setVisibility(View.VISIBLE);
                    } else {
                        binding.profilePicture.setImageResource(R.drawable.profile_picture);
                        binding.profilePicture.setVisibility(View.VISIBLE);
                    }
                    binding.userName.setText(String.valueOf(snapshot.child("fullName").getValue()));
                    callWelcomeSection();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void callPendingAppointment() {
        Intent intent = new Intent(requireActivity(), PendingAppointment.class);
        intent.putExtra("userID", user.getUid());
        startActivity(intent);

    }

    public void callMedicineReminder() {
        Intent intent = new Intent(requireActivity(), MedicineReminder.class);
        startActivity(intent);
    }

    public void callWelcomeSection() {
        binding.welcomeSection.setVisibility(View.VISIBLE);
        binding.welcomeAnimation.playAnimation();

    }

    public void callDiagnoseReportUploader() {
        Intent intent = new Intent(requireActivity(), DiagnosisReportUploadList.class);
        startActivity(intent);
    }

    public void setAnimations() {
        leftAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.left_animation);
        rightAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.right_anim);
        binding.appointmentCard.setAnimation(leftAnim);
        binding.sliderCard.setAnimation(rightAnim);
        binding.consultantCard.setAnimation(rightAnim);
        binding.medicineReminderCard.setAnimation(leftAnim);
        binding.reportCard.setAnimation(leftAnim);
        binding.pendingAppointment.setAnimation(leftAnim);
        binding.onlineCosultantCard.setAnimation(rightAnim);
        binding.medicineCard.setAnimation(rightAnim);
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

    public void callDoctorFragment() {
        bottomNavigation = requireActivity().findViewById(R.id.bottomBar);
        bottomNavigation.setItemSelected(R.id.nav_doctor_video, true);
    }

    public void callProfileFragment() {
        bottomNavigation = requireActivity().findViewById(R.id.bottomBar);
        bottomNavigation.setItemSelected(R.id.nav_profile, true);
    }


    public void callAppointmentBottomSheet() {
        bookAppointmentSheet = new BottomSheetDialog(requireContext(), R.style.bottomSheetTheme);
        View appointmentView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_book_doctor, null);
        bookAppointmentSheet.setContentView(appointmentView);
        bookAppointmentSheet.show();
    }


    public void callLanguageChanger() {
        Dialog dialog = new Dialog(requireContext());
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
        buttonDialog.setOnClickListener(v -> {
            if (english.isChecked()) {
                SharedPreferences preferences1 = requireActivity().getSharedPreferences("language", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences1.edit();
                editor.putString("lang", "en");
                editor.apply();

            } else {
                SharedPreferences preferences1 = requireActivity().getSharedPreferences("language", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences1.edit();
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

        });
        dialog.show();
    }

    public void onDestroyView() {
        super.onDestroyView();
        firebaseExecutor.shutdown();
        binding = null;
    }

    public void restart() {
        ProcessPhoenix.triggerRebirth(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}