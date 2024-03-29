package com.example.doctorbabu.patient.HomeModules;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.chatRoom.chatList;
import com.example.doctorbabu.databinding.FragmentHomeBinding;
import com.example.doctorbabu.patient.AlarmModules.MedicineReminder;
import com.example.doctorbabu.patient.AuthenticationModule.Login;
import com.example.doctorbabu.patient.DiagnoseReportUploadModule.DiagnosisReportUploadList;
import com.example.doctorbabu.patient.DoctorConsultationModule.DiagnosisTerms;
import com.example.doctorbabu.patient.DoctorConsultationModule.ViewAllDoctor;
import com.example.doctorbabu.patient.MedicinePurchaseModules.Cart;
import com.example.doctorbabu.patient.MedicinePurchaseModules.MedicineShop;
import com.example.doctorbabu.patient.PatientProfileModule.EditProfile;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Home extends Fragment {
    FirebaseUser user;
    String uId;
    BottomSheetDialog bookAppointmentSheet;
    Button buttonDialog;
    RadioButton english, bengali;
    Animation leftAnim, rightAnim;
    ExecutorService firebaseExecutor, animationExecutor, drawerExecutor, cartCounter, onlineStatusExecutor,messageCounterExecutor;
    ChipNavigationBar bottomNavigation;
    MaterialCardView generalPhysician, gynecologist, paediatrician, dermatologist, psychiatrist, cardiologist, nutritionist, ophthalmologist, neurologist;
    FragmentHomeBinding binding;
    Firebase firebase;
    ActionBarDrawerToggle toggle;
    FusedLocationProviderClient locationProviderClient;
    List<Address> addresses;
    int countedMessage =0;

    public Home() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebase = Firebase.getInstance();
        if (!isNetworkConnected()) {
            binding.mainLayout.setVisibility(View.GONE);
            binding.noInternetLayout.setVisibility(View.VISIBLE);
        } else {
            binding.mainLayout.setVisibility(View.VISIBLE);
            binding.noInternetLayout.setVisibility(View.GONE);
        }
        startFragment();
    }

    public void startFragment() {
        firebaseExecutor = Executors.newSingleThreadExecutor();
        animationExecutor = Executors.newSingleThreadExecutor();
        drawerExecutor = Executors.newSingleThreadExecutor();
        cartCounter = Executors.newSingleThreadExecutor();
        onlineStatusExecutor = Executors.newSingleThreadExecutor();
        messageCounterExecutor = Executors.newSingleThreadExecutor();
        locationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        firebaseExecutor.execute(this::firebaseAuth);
        PushDownAnim.setPushDownAnimTo(binding.consultantCard, binding.appointmentCard, binding.medicineReminderCard, binding.reportCard, binding.pendingAppointment, binding.medicineCard, binding.hospitalListCard,binding.message)
                .setScale(PushDownAnim.MODE_SCALE, 0.95f);

        loadImageSlider();
        animationExecutor.execute(new Runnable() {
            @Override
            public void run() {
                setAnimations();
            }
        });
        onlineStatusExecutor.execute(new Runnable() {
            @Override
            public void run() {
                setUserOnline();
            }
        });
        drawerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                toggle = new ActionBarDrawerToggle(requireActivity(), binding.drawerLayout, R.string.openDrawer, R.string.closeDrawer);
                binding.drawerLayout.addDrawerListener(toggle);
                toggle.syncState();
                binding.drawerLayout.setStatusBarBackgroundColor(Color.parseColor("#FDFEFE"));
                requireActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.toolBar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                binding.drawerLayout.openDrawer(GravityCompat.START);
                            }
                        });
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
                        } else if (item.getItemId() == R.id.navSignOut) {
                            binding.drawerLayout.closeDrawer(GravityCompat.START);
                            signOut();
                        }

                        return false;
                    }
                });
            }
        });
        binding.message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireActivity(), chatList.class);
                startActivity(intent);
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
        binding.hospitalListCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findNearbyEmergencyHospital();
            }
        });
        messageCounterExecutor.execute(new Runnable() {
            @Override
            public void run() {
                countedMessage = 0;
                messageCounter();
            }
        });
    }


    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences preferences = requireActivity().getSharedPreferences("loginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("loginAs", "");
        editor.apply();
        Intent intent = new Intent(requireContext(), Login.class);
        startActivity(intent);
        requireActivity().finish();
    }

    public void findNearbyEmergencyHospital() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if (location != null) {
                        Geocoder geo = new Geocoder(requireActivity(), Locale.getDefault());
                        try {
                            addresses = geo.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            assert addresses != null;
                            String latitude = String.valueOf(addresses.get(0).getLatitude());
                            String longitude = String.valueOf(addresses.get(0).getLongitude());
                            Uri gmmIntentUri = Uri.parse("geo:" + latitude + "," + longitude + "?z=10&q=emergency hospitals");
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    } else {
                        errorMessage("Location Not Found", "Please turn on location and wait");
                    }
                }
            });
        }
    }

    public void errorMessage(String title, String message) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(requireActivity());
        dialog.setTitle(title).setIcon(R.drawable.cross)
                .setMessage(message)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setCancelable(false);
        dialog.create().show();
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
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        DatabaseReference cartCounterReference = firebase.getDatabaseReference("medicineCart");
        cartCounterReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int countedCart = 0;
                if (snapshot.exists() && isAdded()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        countedCart += 1;
                    }
                    try {
                        binding.cartCounter.setText(String.valueOf(countedCart));
                        binding.cartCounter.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    if (isAdded()) {
                        binding.cartCounter.setVisibility(View.INVISIBLE);
                    }
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


    public void callEditProfile() {
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
        generalPhysician = appointmentView.findViewById(R.id.generalPhysician);
        gynecologist = appointmentView.findViewById(R.id.gynecologist);
        paediatrician = appointmentView.findViewById(R.id.paediatrician);
        dermatologist = appointmentView.findViewById(R.id.dermatologist);
        psychiatrist = appointmentView.findViewById(R.id.psychiatrist);
        cardiologist = appointmentView.findViewById(R.id.cardiologist);
        nutritionist = appointmentView.findViewById(R.id.nutritionist);
        ophthalmologist = appointmentView.findViewById(R.id.ophthalmologist);
        neurologist = appointmentView.findViewById(R.id.neurologist);
        generalPhysician.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookAppointmentSheet.dismiss();
                loadSpecialistDoctor("General Physician");
            }
        });
        gynecologist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookAppointmentSheet.dismiss();
                loadSpecialistDoctor("Gynecologist");
            }
        });
        paediatrician.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookAppointmentSheet.dismiss();
                loadSpecialistDoctor("Paediatrician");
            }
        });
        dermatologist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookAppointmentSheet.dismiss();
                loadSpecialistDoctor("Dermatologist");
            }
        });
        psychiatrist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookAppointmentSheet.dismiss();
                loadSpecialistDoctor("Psychiatrist");
            }
        });
        cardiologist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookAppointmentSheet.dismiss();
                loadSpecialistDoctor("Cardiologist");
            }
        });
        nutritionist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookAppointmentSheet.dismiss();
                loadSpecialistDoctor("Nutritionist");
            }
        });
        ophthalmologist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookAppointmentSheet.dismiss();
                loadSpecialistDoctor("Ophthalmologist");
            }
        });
        neurologist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookAppointmentSheet.dismiss();
                loadSpecialistDoctor("Neurologist");
            }
        });
        bookAppointmentSheet.setContentView(appointmentView);
        bookAppointmentSheet.show();
    }

    public void loadSpecialistDoctor(String specialist) {
        Intent intent = new Intent(requireActivity(), ViewAllDoctor.class);
        intent.putExtra("specialist", specialist);
        startActivity(intent);
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
    public void messageCounter() {
        DatabaseReference reference = firebase.getDatabaseReference("chatRoom");
        FirebaseUser user = firebase.getUserID();
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        countMessage(String.valueOf(snap.getKey()), user.getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void countMessage(String key, String userId) {
        DatabaseReference reference = firebase.getDatabaseReference("chatRoom");
        reference.child(userId).child(key).limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    countedMessage = 0;
                    for(DataSnapshot snap: snapshot.getChildren()){
                        if (String.valueOf(snap.child("seenStatus").getValue()).equalsIgnoreCase("unseen") && String.valueOf(snap.child("receiverId").getValue()).equalsIgnoreCase(userId)) {
                            countedMessage += 1;
                            binding.msgCounter.setText(String.valueOf(countedMessage));
                            binding.msgCounter.setVisibility(View.VISIBLE);
                        }else{
                            binding.msgCounter.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        messageCounterExecutor.execute(new Runnable() {
            @Override
            public void run() {
                messageCounter();
            }
        });
    }

    public void onDestroyView() {
        super.onDestroyView();
        firebaseExecutor.shutdown();
        animationExecutor.shutdown();
        drawerExecutor.shutdown();
        cartCounter.shutdown();
        onlineStatusExecutor.shutdown();
        messageCounterExecutor.shutdown();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected())) {
            //Do something
            return false;
        }
        return true;
    }


    public void setUserOnline() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("userModification", Context.MODE_PRIVATE);
        String code = preferences.getString("code", "null");
        if (code.equalsIgnoreCase("null")) {
            Firebase firebase = Firebase.getInstance();
            FirebaseUser user = firebase.getUserID();
            DatabaseReference reference = firebase.getDatabaseReference("users");
            reference.child(user.getUid()).child("onlineStatus").setValue(1);
            reference.child(user.getUid()).child("userId").setValue(user.getUid());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("code", "1");
            editor.apply();
        }
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