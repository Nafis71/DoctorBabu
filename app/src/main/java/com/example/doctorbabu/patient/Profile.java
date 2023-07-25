package com.example.doctorbabu.patient;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.FragmentProfileBinding;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Year;
import java.util.ArrayList;
import java.util.Formatter;

import timber.log.Timber;


public class Profile extends Fragment {

    FirebaseAuth auth;
    FirebaseUser user;
    BottomSheetDialog bottomSheetDialog, bottomSheetDialogMedicalList, bottomSheetDialogBloodList;
    CheckBox drug, cloth, dust, food, asthma, cancer, diabetics, heartDisease, highBp, migraine, stroke,
            ulcer, aPositive, bPositive, oPositive, abPositive, aNegative, bNegative, oNegative, abNegative;
    Button allergyListConfirm, medicalListConfirm, bloodConfirmList;
    FragmentProfileBinding binding;
    Thread dataThread;

    public Profile() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isAdded()) {
            dataThread = new Thread(this::getData);
            dataThread.start();
            Thread userProfileThread = new Thread(this::getUserprofile);
            userProfileThread.start();
            Thread profileInfoThread = new Thread(this::profileInfoVerify);
            profileInfoThread.start();
        }
    }

    public void profileInfoVerify() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("patientProfileTrack");
        reference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                String medicalHistory = String.valueOf(snapshot.child("medicalHistory").getValue());
                String allergyInfo = String.valueOf(snapshot.child("allergyInfo").getValue());
                String profilePicture = String.valueOf(snapshot.child("profilePicture").getValue());
                String bloodGroupInfo = String.valueOf(snapshot.child("bloodGroupInfo").getValue());
                if (!medicalHistory.equals("null")) {
                    count++;
                }
                if (!allergyInfo.equals("null")) {
                    count++;
                }
                if (!profilePicture.equals("null")) {
                    count++;
                }
                if (!bloodGroupInfo.equals("null")) {
                    count++;
                }
                if (count == 4) {
                    binding.warningCard.setVisibility(View.GONE);
                } else {
                    binding.warningCard.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void getData() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            requireActivity().finish();
        }
        binding.signOut.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences preferences = requireActivity().getSharedPreferences("loginDetails", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("loginAs", "");
            editor.apply();
            Intent intent = new Intent(requireContext(), Login.class);
            startActivity(intent);
            requireActivity().finish();
        });
        userAllergyHistory();
        userPastMedicalHistory();
        binding.userEmail.setText(user.getEmail());
        binding.editProfile.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), EditProfile.class);
            intent.putExtra("uId", user.getUid());
            intent.putExtra("email", user.getEmail());
            startActivity(intent);
        });
        binding.alergyHistoryInfo.setOnClickListener(view -> allergyList());
        binding.pastMedicalHistoryText.setOnClickListener(view -> medicalList());
        binding.bloodGroupText.setOnClickListener(view -> bloodList());
    }

    public void getUserprofile() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("users");
        reference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.userName.setText(String.valueOf(snapshot.child("fullName").getValue()));
                binding.userPhone.setText(String.valueOf(snapshot.child("phone").getValue()));
                binding.district.setText(String.valueOf(snapshot.child("district").getValue()));
                binding.area.setText(String.valueOf(snapshot.child("area").getValue()));
                String dbAddress = String.valueOf(snapshot.child("address").getValue());
                if (!dbAddress.equals("null")) {
                    binding.userAddress.setText(dbAddress);
                } else {
                    binding.userAddress.setText(String.valueOf(R.string.addressNotSet));
                }
                binding.userGender.setText(String.valueOf(snapshot.child("gender").getValue()));
                binding.userHeight.setText(String.valueOf(snapshot.child("height").getValue()));
                binding.userWeight.setText(String.valueOf(snapshot.child("weight").getValue()));
                String birthDate = String.valueOf(snapshot.child("birthDate").getValue());
                String[] splitText = birthDate.split("/");
                int year = Integer.parseInt(splitText[0]);
                Year thisYear = Year.now();
                String thisYearString = thisYear.toString();
                int currentAge = Integer.parseInt(thisYearString) - year - 1;
                binding.userAge.setText(String.valueOf(currentAge));
                String photoUrl = String.valueOf(snapshot.child("photoUrl").getValue());
                if (!photoUrl.equals("null")) {
                    Glide.with(requireContext()).load(photoUrl).into(binding.profilePicture);
                    binding.profilePicture.setVisibility(View.VISIBLE);
                    FirebaseDatabase rootnode = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
                    DatabaseReference ref = rootnode.getReference("patientProfileTrack");
                    ref.child(user.getUid()).child("profilePicture").setValue("Completed");
                } else {
                    binding.profilePicture.setImageResource(R.drawable.profile_picture);
                    binding.profilePicture.setVisibility(View.VISIBLE);
                    FirebaseDatabase rootnode = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
                    DatabaseReference ref = rootnode.getReference("patienProfileTrack");
                    ref.child(user.getUid()).child("profilePicture").setValue("null");
                }
                binding.bmi.setText(bodyMassIndex());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        reference = database.getReference("bloodgroup");
        reference.child(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    if (!String.valueOf(snapshot.child("group").getValue()).equals("null")) {
                        binding.bloodGroupInfo.setText(String.valueOf(snapshot.child("group").getValue()));
                        binding.bloodGroupInfo.setTextColor(Color.parseColor("#1C2833"));
                        FirebaseDatabase rootnode = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
                        DatabaseReference ref = rootnode.getReference("patientProfileTrack");
                        ref.child(user.getUid()).child("bloodGroupInfo").setValue("Completed");
                    } else {
                        binding.bloodGroupInfo.setText(String.valueOf(R.string.noInfoFound));
                        FirebaseDatabase rootnode = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
                        DatabaseReference ref = rootnode.getReference("patientProfileTrack");
                        ref.child(user.getUid()).child("bloodGroupInfo").setValue("null");
                    }

                }
            }
        }).addOnFailureListener(e -> {

        });
        reference = database.getReference("appointmentPatient");
        reference.child(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    binding.appointmentDone.setText(String.valueOf(snapshot.child("done").getValue()));
                }
            }
        });
        reference = database.getReference("consultancyPatient");
        reference.child(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    binding.appointmentPending.setText(String.valueOf(snapshot.child("done").getValue()));
                }
            }
        });
        reference = database.getReference("rewardPatient");
        reference.child(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    binding.rewardAmount.setText(String.valueOf(snapshot.child("reward").getValue()));
                }
            }
        });

    }

    public void userAllergyHistory() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("allergy");
        reference.child(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    ArrayList<String> list = new ArrayList<>();
                    if (!String.valueOf(snapshot.child("cloth").getValue()).equals("null")) {
                        list.add(String.valueOf(snapshot.child("cloth").getValue()));
                    }
                    if (!String.valueOf(snapshot.child("drug").getValue()).equals("null")) {
                        list.add(String.valueOf(snapshot.child("drug").getValue()));
                    }
                    if (!String.valueOf(snapshot.child("dust").getValue()).equals("null")) {
                        list.add(String.valueOf(snapshot.child("dust").getValue()));
                    }
                    if (!String.valueOf(snapshot.child("food").getValue()).equals("null")) {
                        list.add(String.valueOf(snapshot.child("food").getValue()));
                    }
                    allergyListAdd(list);
                }
            }
        }).addOnFailureListener(e -> {

        });
    }

    public void allergyListAdd(ArrayList<String> list) {
        int size = list.size();
        if (size != 0) {
            binding.allergy.setVisibility(View.GONE);
            FirebaseDatabase rootnode = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
            DatabaseReference ref = rootnode.getReference("patientProfileTrack");
            ref.child(user.getUid()).child("allergyInfo").setValue("Completed");
            for (int i = 0; i < size; i++) {
                TextView text = new TextView(getContext());
                FlexboxLayout.LayoutParams layout =
                        new FlexboxLayout.LayoutParams(
                                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                                FlexboxLayout.LayoutParams.WRAP_CONTENT);

                layout.setLayoutDirection(FlexDirection.ROW);
                layout.setMarginStart(10);
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                text.setLayoutParams(layout);
                text.setText(list.get(i));
                text.setBackgroundResource(R.drawable.card_corner);
                text.setPadding(7, 7, 7, 7);
                text.setTextColor(Color.parseColor("#1C2833"));
                binding.allergyHistory.addView(text);
            }
        } else {
            binding.allergy.setText(String.valueOf(R.string.noInfoFound));
            binding.allergy.setVisibility(View.VISIBLE);
            FirebaseDatabase rootnode = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
            DatabaseReference ref = rootnode.getReference("patientProfileTrack");
            ref.child(user.getUid()).child("allergyInfo").setValue("null");
        }

    }

    public void userPastMedicalHistory() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("medicalhistory");
        reference.child(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    ArrayList<String> medicalHistoryList = new ArrayList<>();
                    if (!String.valueOf(snapshot.child("asthma").getValue()).equals("null")) {
                        medicalHistoryList.add(String.valueOf(snapshot.child("asthma").getValue()));
                    }
                    if (!String.valueOf(snapshot.child("cancer").getValue()).equals("null")) {
                        medicalHistoryList.add(String.valueOf(snapshot.child("cancer").getValue()));
                    }
                    if (!String.valueOf(snapshot.child("diabetics").getValue()).equals("null")) {
                        medicalHistoryList.add(String.valueOf(snapshot.child("diabetics").getValue()));
                    }
                    if (!String.valueOf(snapshot.child("heartdisease").getValue()).equals("null")) {
                        medicalHistoryList.add(String.valueOf(snapshot.child("heartdisease").getValue()));
                    }
                    if (!String.valueOf(snapshot.child("migraine").getValue()).equals("null")) {
                        medicalHistoryList.add(String.valueOf(snapshot.child("migraine").getValue()));
                    }
                    if (!String.valueOf(snapshot.child("stroke").getValue()).equals("null")) {
                        medicalHistoryList.add(String.valueOf(snapshot.child("stroke").getValue()));
                    }
                    if (!String.valueOf(snapshot.child("ulcer").getValue()).equals("null")) {
                        medicalHistoryList.add(String.valueOf(snapshot.child("ulcer").getValue()));
                    }
                    if (!String.valueOf(snapshot.child("highbp").getValue()).equals("null")) {
                        medicalHistoryList.add(String.valueOf(snapshot.child("highbp").getValue()));
                    }
                    medicalHistoryListAdd(medicalHistoryList);
                } else {
                    Toast.makeText(getContext(), "No information has been found!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Couldn't fetch medical info!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Couldn't fetch medical info!", Toast.LENGTH_SHORT).show());
    }

    public void medicalHistoryListAdd(ArrayList<String> list) {
        int size = list.size();
        if (size != 0) {
            binding.pastMedicalHistoryInfo.setVisibility(View.GONE);
            FirebaseDatabase rootnode = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
            DatabaseReference ref = rootnode.getReference("patientProfileTrack");
            ref.child(user.getUid()).child("medicalHistory").setValue("Completed");
            for (int i = 0; i < size; i++) {
                TextView text = new TextView(getContext());
                FlexboxLayout.LayoutParams layout =
                        new FlexboxLayout.LayoutParams(
                                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                                FlexboxLayout.LayoutParams.WRAP_CONTENT);

                layout.setLayoutDirection(FlexDirection.ROW);
                layout.setMarginStart(10);
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                text.setLayoutParams(layout);
                text.setText(list.get(i));
                text.setBackgroundResource(R.drawable.card_corner);
                text.setPadding(7, 7, 7, 7);
                text.setTextColor(Color.parseColor("#1C2833"));
                binding.pastMedicalHistory.addView(text);
            }
        } else {
            binding.pastMedicalHistoryInfo.setText(String.valueOf(R.string.noInfoFound));
            binding.pastMedicalHistoryInfo.setVisibility(View.VISIBLE);
            FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
            DatabaseReference ref = rootNode.getReference("patientProfileTrack");
            ref.child(user.getUid()).child("medicalHistory").setValue("null");
        }

    }

    public void allergyList() {
        bottomSheetDialog = new BottomSheetDialog(requireContext(), R.style.bottomSheetTheme);
        @SuppressLint("InflateParams")
        View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_alergylist, null);
        drug = bottomSheetView.findViewById(R.id.drug);
        dust = bottomSheetView.findViewById(R.id.dust);
        cloth = bottomSheetView.findViewById(R.id.cloth);
        food = bottomSheetView.findViewById(R.id.food);
        allergyListConfirm = bottomSheetView.findViewById(R.id.confirmList);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("allergy");
        reference.child(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    drug.setChecked(!String.valueOf(snapshot.child("drug").getValue()).equals("null"));
                    dust.setChecked(!String.valueOf(snapshot.child("dust").getValue()).equals("null"));
                    cloth.setChecked(!String.valueOf(snapshot.child("cloth").getValue()).equals("null"));
                    food.setChecked(!String.valueOf(snapshot.child("food").getValue()).equals("null"));
                } else {
                    Toast.makeText(getContext(), "Data don't exist", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Database load error", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
        });
        allergyListConfirm.setOnClickListener(view -> {
            if (drug.isChecked()) {
                reference.child(user.getUid()).child("drug").setValue("Drug");
            } else {
                reference.child(user.getUid()).child("drug").setValue("null");
            }
            if (dust.isChecked()) {
                reference.child(user.getUid()).child("dust").setValue("Dust");
            } else {
                reference.child(user.getUid()).child("dust").setValue("null");
            }
            if (cloth.isChecked()) {
                reference.child(user.getUid()).child("cloth").setValue("Cloth");
            } else {
                reference.child(user.getUid()).child("cloth").setValue("null");
            }
            if (food.isChecked()) {
                reference.child(user.getUid()).child("food").setValue("Food");
            } else {
                reference.child(user.getUid()).child("food").setValue("null");
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setTitle("Done").setMessage("Allergy History has been updated!");
            dialog.setPositiveButton("Ok", (dialogInterface, i) -> {
                bottomSheetDialog.cancel();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .detach(Profile.this)
                        .commitNowAllowingStateLoss();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .attach(Profile.this)
                        .commitNow();
            }).setCancelable(false);
            dialog.create().show();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    public void medicalList() {
        bottomSheetDialogMedicalList = new BottomSheetDialog(requireContext(), R.style.bottomSheetTheme);
        @SuppressLint("InflateParams") View bottomSheet = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_medical_history_list, null);
        asthma = bottomSheet.findViewById(R.id.asthma);
        cancer = bottomSheet.findViewById(R.id.cancer);
        diabetics = bottomSheet.findViewById(R.id.diabetics);
        heartDisease = bottomSheet.findViewById(R.id.heartDisease);
        highBp = bottomSheet.findViewById(R.id.highBp);
        migraine = bottomSheet.findViewById(R.id.migraine);
        stroke = bottomSheet.findViewById(R.id.stroke);
        ulcer = bottomSheet.findViewById(R.id.ulcer);
        medicalListConfirm = bottomSheet.findViewById(R.id.confirmList);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("medicalhistory");
        reference.child(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isComplete()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    asthma.setChecked(!String.valueOf(snapshot.child("asthma").getValue()).equals("null"));
                    cancer.setChecked(!String.valueOf(snapshot.child("cancer").getValue()).equals("null"));
                    diabetics.setChecked(!String.valueOf(snapshot.child("diabetics").getValue()).equals("null"));
                    heartDisease.setChecked(!String.valueOf(snapshot.child("heartdisease").getValue()).equals("null"));
                    highBp.setChecked(!String.valueOf(snapshot.child("highbp").getValue()).equals("null"));
                    migraine.setChecked(!String.valueOf(snapshot.child("migraine").getValue()).equals("null"));
                    stroke.setChecked(!String.valueOf(snapshot.child("stroke").getValue()).equals("null"));
                    ulcer.setChecked(!String.valueOf(snapshot.child("ulcer").getValue()).equals("null"));
                } else {
                    Toast.makeText(getContext(), "Data don't exist", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Database load error", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
        });
        medicalListConfirm.setOnClickListener(view -> {
            if (asthma.isChecked()) {
                reference.child(user.getUid()).child("asthma").setValue("Asthma");
            } else {
                reference.child(user.getUid()).child("asthma").setValue("null");
            }
            if (cancer.isChecked()) {
                reference.child(user.getUid()).child("cancer").setValue("Cancer");
            } else {
                reference.child(user.getUid()).child("cancer").setValue("null");
            }
            if (diabetics.isChecked()) {
                reference.child(user.getUid()).child("diabetics").setValue("Diabetics");
            } else {
                reference.child(user.getUid()).child("diabetics").setValue("null");
            }
            if (heartDisease.isChecked()) {
                reference.child(user.getUid()).child("heartdisease").setValue("Heart Disease");
            } else {
                reference.child(user.getUid()).child("heartdisease").setValue("null");
            }
            if (highBp.isChecked()) {
                reference.child(user.getUid()).child("highbp").setValue("High BP");
            } else {
                reference.child(user.getUid()).child("highbp").setValue("null");
            }
            if (migraine.isChecked()) {
                reference.child(user.getUid()).child("migraine").setValue("Migraine");
            } else {
                reference.child(user.getUid()).child("migraine").setValue("null");
            }
            if (stroke.isChecked()) {
                reference.child(user.getUid()).child("stroke").setValue("Stroke");
            } else {
                reference.child(user.getUid()).child("stroke").setValue("null");
            }
            if (ulcer.isChecked()) {
                reference.child(user.getUid()).child("ulcer").setValue("Ulcer");
            } else {
                reference.child(user.getUid()).child("ulcer").setValue("null");
            }
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setTitle("Done").setMessage("Medical History has been updated!");
            dialog.setPositiveButton("Ok", (dialogInterface, i) -> {
                bottomSheetDialogMedicalList.cancel();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .detach(Profile.this)
                        .commitNowAllowingStateLoss();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .attach(Profile.this)
                        .commitNow();
            }).setCancelable(false);
            dialog.create().show();

        });
        bottomSheetDialogMedicalList.setContentView(bottomSheet);
        bottomSheetDialogMedicalList.show();
    }

    public void bloodList() {
        bottomSheetDialogBloodList = new BottomSheetDialog(requireContext(), R.style.bottomSheetTheme);
        @SuppressLint("InflateParams") View bloodList = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_blood_group_list, null);
        aPositive = bloodList.findViewById(R.id.aPositive);
        bPositive = bloodList.findViewById(R.id.bPositive);
        oPositive = bloodList.findViewById(R.id.oPositive);
        abPositive = bloodList.findViewById(R.id.abPositive);
        aNegative = bloodList.findViewById(R.id.aNegative);
        bNegative = bloodList.findViewById(R.id.bNegative);
        oNegative = bloodList.findViewById(R.id.oNegative);
        abNegative = bloodList.findViewById(R.id.abNegative);
        bloodConfirmList = bloodList.findViewById(R.id.confirmList);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("bloodgroup");
        reference.child(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.getResult().exists()) {
                    DataSnapshot snapshot = task.getResult();
                    aPositive.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("A+"));
                    bPositive.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("B+"));
                    oPositive.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("O+"));
                    abPositive.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("AB+"));
                    aNegative.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("A-"));
                    bNegative.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("B-"));
                    oNegative.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("O-"));
                    abNegative.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("AB-"));
                } else {
                    Toast.makeText(getContext(), "Data don't exist", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Couldn't fetch blood info", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Timber.tag("Database Connection Error").i(e.toString()));
        bloodConfirmList.setOnClickListener(view -> {
            if (aPositive.isChecked() && bPositive.isChecked() | oPositive.isChecked() | aNegative.isChecked()
                    | bNegative.isChecked() | oNegative.isChecked() | abNegative.isChecked() | abPositive.isChecked()) {
                Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_SHORT).show();
            } else if (bPositive.isChecked() && aPositive.isChecked() | oPositive.isChecked() | aNegative.isChecked()
                    | bNegative.isChecked() | oNegative.isChecked() | abNegative.isChecked() | abPositive.isChecked()) {
                Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_LONG).show();
            } else if (oPositive.isChecked() && aPositive.isChecked() | bPositive.isChecked() | aNegative.isChecked()
                    | bNegative.isChecked() | oNegative.isChecked() | abNegative.isChecked() | abPositive.isChecked()) {
                Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_LONG).show();
            } else if (abPositive.isChecked() && aPositive.isChecked() | aPositive.isChecked() | aNegative.isChecked()
                    | bNegative.isChecked() | oNegative.isChecked() | abNegative.isChecked() | bPositive.isChecked()) {
                Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_LONG).show();
            } else if (aNegative.isChecked() && aPositive.isChecked() | bPositive.isChecked() | oPositive.isChecked()
                    | bNegative.isChecked() | oNegative.isChecked() | abNegative.isChecked() | abPositive.isChecked()) {
                Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_LONG).show();
            } else if (bNegative.isChecked() && aPositive.isChecked() | bPositive.isChecked() | oPositive.isChecked()
                    | aNegative.isChecked() | oNegative.isChecked() | abNegative.isChecked() | abPositive.isChecked()) {
                Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_LONG).show();
            } else if (oNegative.isChecked() && aPositive.isChecked() | bPositive.isChecked() | oPositive.isChecked()
                    | aNegative.isChecked() | bNegative.isChecked() | abNegative.isChecked() | abPositive.isChecked()) {
                Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_LONG).show();
            } else if (abNegative.isChecked() && aPositive.isChecked() | bPositive.isChecked() | oPositive.isChecked()
                    | aNegative.isChecked() | bNegative.isChecked() | oNegative.isChecked() | abPositive.isChecked()) {
                Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_LONG).show();
            } else {
                if (aPositive.isChecked()) {
                    reference.child(user.getUid()).child("group").setValue("A+");
                } else if (oPositive.isChecked()) {
                    reference.child(user.getUid()).child("group").setValue("O+");
                } else if (bPositive.isChecked()) {
                    reference.child(user.getUid()).child("group").setValue("B+");
                } else if (abPositive.isChecked()) {
                    reference.child(user.getUid()).child("group").setValue("AB+");
                } else if (aNegative.isChecked()) {
                    reference.child(user.getUid()).child("group").setValue("A-");
                } else if (bNegative.isChecked()) {
                    reference.child(user.getUid()).child("group").setValue("B-");
                } else if (oNegative.isChecked()) {
                    reference.child(user.getUid()).child("group").setValue("O-");
                } else if (abNegative.isChecked()) {
                    reference.child(user.getUid()).child("group").setValue("AB-");
                } else {
                    reference.child(user.getUid()).child("group").setValue("null");
                }
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("Done").setMessage("Blood Group has been updated!");
                dialog.setPositiveButton("Ok", (dialogInterface, i) -> {
                    bottomSheetDialogBloodList.cancel();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .detach(Profile.this)
                            .commitNowAllowingStateLoss();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .attach(Profile.this)
                            .commitNow();
                }).setCancelable(false);
                dialog.create().show();
            }
        });
        bottomSheetDialogBloodList.setContentView(bloodList);
        bottomSheetDialogBloodList.show();
    }

    public String bodyMassIndex() {
        Formatter fm = new Formatter();
        double bmi = Double.parseDouble(binding.userWeight.getText().toString()) / Math.pow((Double.parseDouble(binding.userHeight.getText().toString()) / 100), 2);
        fm.format("%.2f", bmi);
        return String.valueOf(fm);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}