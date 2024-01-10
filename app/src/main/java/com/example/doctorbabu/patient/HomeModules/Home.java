package com.example.doctorbabu.patient.HomeModules;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.FragmentHomeBinding;
import com.example.doctorbabu.patient.AlarmModules.MedicineReminder;
import com.example.doctorbabu.patient.DiagnoseReportUploadModule.DiagnosisReportUploadList;
import com.example.doctorbabu.patient.DoctorConsultationModule.DiagnosisTerms;
import com.example.doctorbabu.patient.DoctorConsultationModule.ViewAllDoctor;
import com.example.doctorbabu.patient.MedicinePurchaseModules.Cart;
import com.example.doctorbabu.patient.MedicinePurchaseModules.MedicineShop;
import com.example.doctorbabu.patient.PatientProfileModule.EditProfile;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
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
    FirebaseUser user;
    String uId;
    BottomSheetDialog bookAppointmentSheet;
    Button buttonDialog;
    RadioButton english, bengali;
    Animation leftAnim, rightAnim;
    ExecutorService firebaseExecutor, imageSliderExecutor, animationExecutor, drawerExecutor, cartCounter;
    ChipNavigationBar bottomNavigation;

    FragmentHomeBinding binding;
    Firebase firebase;
    ActionBarDrawerToggle toggle;

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
        imageSliderExecutor = Executors.newSingleThreadExecutor();
        animationExecutor = Executors.newSingleThreadExecutor();
        drawerExecutor = Executors.newSingleThreadExecutor();
        cartCounter = Executors.newSingleThreadExecutor();
        firebaseExecutor.execute(this::firebaseAuth);
        PushDownAnim.setPushDownAnimTo(binding.consultantCard, binding.appointmentCard, binding.medicineReminderCard, binding.reportCard, binding.pendingAppointment, binding.medicineCard)
                .setScale(PushDownAnim.MODE_SCALE, 0.95f);

        imageSliderExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadImageSlider();
            }
        });
        animationExecutor.execute(new Runnable() {
            @Override
            public void run() {
                setAnimations();
            }
        });
        drawerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                toggle = new ActionBarDrawerToggle(requireActivity(), binding.drawerLayout, R.string.openDrawer, R.string.closeDrawer);
                binding.drawerLayout.addDrawerListener(toggle);
                toggle.syncState();
                binding.drawerLayout.setStatusBarBackgroundColor(Color.parseColor("#FDFEFE"));
                binding.toolBar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        binding.drawerLayout.openDrawer(GravityCompat.START);
                    }
                });
                binding.navBar.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.navVideoConsultation) {
                            binding.drawerLayout.closeDrawer(GravityCompat.START);
                            callDoctorFragment();
                        } else if (item.getItemId() == R.id.navBookAppointment) {
                            binding.drawerLayout.closeDrawer(GravityCompat.START);
                            callAllDoctorModule();
                        } else if (item.getItemId() == R.id.navPendingAppointment) {
                            binding.drawerLayout.closeDrawer(GravityCompat.START);
                            callPendingAppointment();
                        } else if (item.getItemId() == R.id.navPrescriptionHistory) {
                            binding.drawerLayout.closeDrawer(GravityCompat.START);
                            callPrescriptionHistory();
                        } else if (item.getItemId() == R.id.navPredictDisease) {
                            binding.drawerLayout.closeDrawer(GravityCompat.START);
                            callDiseasePredictor();
                        } else if (item.getItemId() == R.id.navMedicineReminder) {
                            binding.drawerLayout.closeDrawer(GravityCompat.START);
                            callMedicineReminder();
                        } else if (item.getItemId() == R.id.navDiagnosisReport) {
                            binding.drawerLayout.closeDrawer(GravityCompat.START);
                            callDiagnoseReportUploader();
                        } else if (item.getItemId() == R.id.navBuyMedicine) {
                            binding.drawerLayout.closeDrawer(GravityCompat.START);
                            callMedicineShop();
                        } else if (item.getItemId() == R.id.navEditProfile) {
                            binding.drawerLayout.closeDrawer(GravityCompat.START);
                            callEditProfile();
                        } else if (item.getItemId() == R.id.navChangeLanguage) {
                            binding.drawerLayout.closeDrawer(GravityCompat.START);
                            callLanguageChanger();
                        }

                        return false;
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.profilePicture.setOnClickListener(view -> callProfileFragment());
        binding.appointmentCard.setOnClickListener(view -> callAppointmentBottomSheet());
        binding.languageImage.setOnClickListener(view -> callLanguageChanger());
        binding.consultantCard.setOnClickListener(view -> {
            callDoctorFragment();
        });
        binding.medicineReminderCard.setOnClickListener(view -> callMedicineReminder());
        binding.reportCard.setOnClickListener(view -> callDiagnoseReportUploader());
        binding.pendingAppointment.setOnClickListener(view -> callPendingAppointment());
        binding.medicineCard.setOnClickListener(view -> callMedicineShop());
        cartCounter.execute(new Runnable() {
            @Override
            public void run() {
                setCartCounter();
            }
        });
        binding.cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callCart();
            }
        });

    }

    public void firebaseAuth() {
        firebase = Firebase.getInstance();
        user = firebase.getUserID();
        if (user != null) {
            uId = user.getUid();
            loadUserProfileData();
        }
    }

    public void setCartCounter() {
        FirebaseUser user = firebase.getUserID();
        DatabaseReference cartCounterReference = firebase.getDatabaseReference("medicineCart");
        cartCounterReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int countedCart = 0;
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        countedCart += 1;
                    }
                    binding.cartCounter.setText(String.valueOf(countedCart));
                    binding.cartCounter.setVisibility(View.VISIBLE);
                } else {
                    binding.cartCounter.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void loadUserProfileData() {
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
                throw error.toException();
            }
        });
    }


    public void callEditProfile(){
        Intent intent = new Intent(requireActivity(), EditProfile.class);
        intent.putExtra("uId", user.getUid());
        intent.putExtra("email", user.getEmail());
        startActivity(intent);
    }
    public void callDiseasePredictor() {
        Intent intent = new Intent(requireActivity(), DiagnosisTerms.class);
        startActivity(intent);
    }

    public void callPendingAppointment() {
        Intent intent = new Intent(requireActivity(), PendingAppointment.class);
        intent.putExtra("userID", user.getUid());
        startActivity(intent);

    }

    public void callPrescriptionHistory() {
        bottomNavigation = requireActivity().findViewById(R.id.bottomBar);
        bottomNavigation.setItemSelected(R.id.nav_history, true);
    }

    public void callMedicineReminder() {
        Intent intent = new Intent(requireActivity(), MedicineReminder.class);
        startActivity(intent);
    }

    public void callWelcomeSection() {
        binding.welcomeSection.setVisibility(View.VISIBLE);
    }

    public void callDiagnoseReportUploader() {
        Intent intent = new Intent(requireActivity(), DiagnosisReportUploadList.class);
        startActivity(intent);
    }

    public void callAllDoctorModule() {
        Intent intent = new Intent(requireActivity(), ViewAllDoctor.class);
        startActivity(intent);
    }

    public void callMedicineShop() {
        Intent intent = new Intent(requireActivity(), MedicineShop.class);
        startActivity(intent);
    }

    public void callCart() {
        Intent intent = new Intent(requireActivity(), Cart.class);
        startActivity(intent);
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

    public void setAnimations() {
        leftAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.left_animation);
        rightAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.right_anim);
        binding.appointmentCard.setAnimation(leftAnim);
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
        images.add(R.drawable.medicine_delivery_banner);
        sliderView = (SliderView) requireView().findViewById(R.id.imageSlider);
        sliderAdapter adapter = new sliderAdapter(images);
        sliderView.setSliderAdapter(adapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION);
        sliderView.startAutoCycle();
        binding.sliderCard.setAnimation(rightAnim);
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
        imageSliderExecutor.shutdown();
        animationExecutor.shutdown();
        drawerExecutor.shutdown();
        cartCounter.shutdown();
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