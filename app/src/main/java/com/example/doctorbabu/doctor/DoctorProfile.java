package com.example.doctorbabu.doctor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.Adapters.doctorPastExperienceAdapter;
import com.example.doctorbabu.DatabaseModels.doctorPastExperienceModel;
import com.example.doctorbabu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;


public class DoctorProfile extends Fragment {

    private final int GALLERY_REQ_CODE = 1000;
    String doctorId;
    FirebaseDatabase database;
    TextInputLayout hospitalNameLayout, designationLayout, joiningDateLayout, leavingDateLayout, departmentLayout;
    AutoCompleteTextView hospitalName, designation, joiningDate, department, leavingDate;
    ImageView profilePicture, editProfilePicture, medicalDegreesEdit, specialtiesEdit, currentlyWorkingAtEdit, pastExperienceEdit, aboutYouEdit, signout;
    TextView doctorName, doctorDegree, medicalDegree, doctorSpecialties, period, doctorSpecialtiesDownField, bmdc,
            currentHospitalName, designationName, departmentName, workingStatusText, joinDateText, about;
    CheckBox MBBS, BMBS, MBChC, MBChB, MBBCh, MD, DO, DS, BCS, generalPhysician, gynecologist, paediatrician,
            dermatologist, psychiatrist, neurologist, ophthalmologist, nutritionist, cardiologist, workingStatus;
    Button confirmList;
    CardView currentlyWorkingCard;
    RatingBar ratingBar;
    BottomSheetDialog bottomSheetDoctorDegree, bottomSheetDoctorSpecialty, bottomSheetCurrentlyWorking, bottomSheetPastExperience;
    Uri filepath;
    Bitmap bitmap;
    StringBuilder stringBuilder;
    LinearLayout parentLayout;
    RecyclerView recyclerView;
    ArrayList<doctorPastExperienceModel> list;
    doctorPastExperienceAdapter recyclerAdapter;
    String[] namesofHospital = new String[]{"Shahid Suhrawardy Hospital", "Ad-Din Hospital", "Ahmed Medical Centre Ltd", "Aichi Hospital", "Al Anaiet Adhunik Hospital",
            "Al-Helal Speacialist Hospital", "Al-Jebel-E-Nur Heart Ltd", "Al- Rajhi Hospital", "Al-Ahsraf General Hospital", "Al-Biruni Hospital", "Al-Fateh Medical Sevices (Pvt) Ltd", "Al-Madina General Hospital (Pvt.) Ltd", "Al-Manar Hospital", "Al-Markazul Islami Hospital",
            "Appolo Hospital", "Arogya Niketan Hospital Ltd", "Bangabandhu Shiekh Mujib Medical University", "Bangkok Hospita", "Bangladesh Heart & Chest Hospital", "Bangladesh Medical College", "Bdm Hospital",
            "Birdem", "Brain & Maind Hospital Ltd", "Care Madical Center Ltd", "Chandshi Medical Centre", "Community Eye Hospital", "Community Maternity Hospital", "City Hospital (Pvt) Ltd", "Delta Medical Centre Ltd", "Dhaka General Hospital (Pvt) Ltd",
            "Dhaka Medical College & Hospital", "Dhaka National Hospital Ltd", "Doctor Babu"};
    String[] designations = new String[]{"Medical Officer", "HMO", "Intern", "MD Resident", "Post Graduate Trainee", "Emergency Medical Officer", "PGT",
            "RMO", "Chief Consultant", "IMO", "EMO", "Honourary Medical Officer", "Senior Medical Officer", "Aesthetic Laser Specialist"};
    String[] departments = new String[]{"Medicine", "Gynae", "Medicine & Surgery", "Internal Medicine", "Diabetics", "Covid Unit", "Dermatology", "Skin", "CO2 Laser", "Long Pulse",
            "Ophthalmology", "Human Resources", "Neurology", "Emergency", "Dental", "Prosthodontic", "Surgery", "Cardiology"};

    public DoctorProfile() {
        // Required empty public constructor
    }

    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = requireActivity().getSharedPreferences("loginDetails", Context.MODE_PRIVATE);
        doctorId = preferences.getString("doctorId", "loginAs");
        database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewBinding();
        getData();
        getCurrentWorkingInfo();
        getPastWorkingExperience();
        editProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        medicalDegreesEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDegrees();
            }
        });
        specialtiesEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSpecialty();
            }
        });
        currentlyWorkingAtEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCurrentlyWorkingDetails();
            }
        });
        pastExperienceEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPastExperience();
            }
        });
        aboutYouEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(requireActivity(), doctorAboutYou.class);
                intent.putExtra("DoctorId", doctorId);
                startActivity(intent);
            }
        });
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
    }

    public void viewBinding() {
        profilePicture = requireView().findViewById(R.id.profilePicture);
        editProfilePicture = requireView().findViewById(R.id.editProfilePicture);
        ratingBar = requireView().findViewById(R.id.ratingbar);
        doctorName = requireView().findViewById(R.id.doctorName);
        medicalDegreesEdit = requireView().findViewById(R.id.medicalDegreesEdit);
        doctorDegree = requireView().findViewById(R.id.doctorDegree);
        medicalDegree = requireView().findViewById(R.id.medicalDegree);
        specialtiesEdit = requireView().findViewById(R.id.specialtiesEdit);
        doctorSpecialties = requireView().findViewById(R.id.doctorSpecialties);
        doctorSpecialtiesDownField = requireView().findViewById(R.id.doctorSpecialtiesDownField);
        bmdc = requireView().findViewById(R.id.bmdc);
        parentLayout = requireView().findViewById(R.id.parentLayout);
        currentlyWorkingAtEdit = requireView().findViewById(R.id.currentlyWorkingAtEdit);
        currentHospitalName = requireView().findViewById(R.id.currentHospitalName);
        designationName = requireView().findViewById(R.id.designationName);
        departmentName = requireView().findViewById(R.id.departmentName);
        period = requireView().findViewById(R.id.period);
        workingStatusText = requireView().findViewById(R.id.workingStatusText);
        currentlyWorkingCard = requireView().findViewById(R.id.currentlyWorkingCard);
        joinDateText = requireView().findViewById(R.id.joinDateText);
        pastExperienceEdit = requireView().findViewById(R.id.pastExperienceEdit);
        aboutYouEdit = requireView().findViewById(R.id.aboutYouEdit);
        about = requireView().findViewById(R.id.about);
        signout = requireView().findViewById(R.id.signout);
    }

    public void getData() {
        DatabaseReference reference = database.getReference("doctorInfo");
        reference.child(doctorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fullName = snapshot.child("title").getValue() + " " + snapshot.child("fullName").getValue();
                doctorName.setText(fullName);
                bmdc.setText(String.valueOf(snapshot.child("bmdc").getValue()));
                if (!String.valueOf(snapshot.child("degrees").getValue()).equals("null")) {
                    doctorDegree.setVisibility(View.VISIBLE);
                    doctorDegree.setText(String.valueOf(snapshot.child("degrees").getValue()));
                    medicalDegree.setText(String.valueOf(snapshot.child("degrees").getValue()));
                } else {
                    doctorDegree.setVisibility(View.GONE);
                    medicalDegree.setText("No information found");
                }
                if (!String.valueOf(snapshot.child("specialty").getValue()).equals("null")) {
                    doctorSpecialties.setVisibility(View.VISIBLE);
                    doctorSpecialties.setText(String.valueOf(snapshot.child("specialty").getValue()));
                    doctorSpecialtiesDownField.setText(String.valueOf(snapshot.child("specialty").getValue()));
                } else {
                    doctorSpecialties.setVisibility(View.GONE);
                    doctorSpecialtiesDownField.setText("No information found");
                }
                if (!String.valueOf(snapshot.child("photoUrl").getValue()).equals("null")) {
                    Glide.with(requireContext()).load(String.valueOf(snapshot.child("photoUrl").getValue())).into(profilePicture);
                } else {
                    profilePicture.setImageResource(R.drawable.profile_picture);
                }
                if (!String.valueOf(snapshot.child("about").getValue()).equals("null")) {
                    about.setText(String.valueOf(snapshot.child("about").getValue()));
                } else {
                    about.setText("Write something about you");
                }
                ratingBar.setRating(Float.parseFloat(String.valueOf(snapshot.child("rating").getValue())));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        DatabaseReference referenceDegree = database.getReference("doctorDegree");
    }

    public void getCurrentWorkingInfo() {

        DatabaseReference reference = database.getReference("doctorCurrentlyWorking");
        reference.child(doctorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (!String.valueOf(snapshot.child("hospitalName").getValue()).equals("null")) {
                        workingStatusText.setVisibility(View.GONE);
                        currentHospitalName.setText(String.valueOf(snapshot.child("hospitalName").getValue()));
                        designationName.setText(String.valueOf(snapshot.child("designation").getValue()));
                        departmentName.setText(String.valueOf(snapshot.child("department").getValue()));
                        joinDateText.setText(String.valueOf(snapshot.child("joiningDate").getValue()));
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                String date = String.valueOf(snapshot.child("joiningDate").getValue());
                                String[] splitText = date.split("/");
                                int year = Integer.parseInt(splitText[0]);
                                int month = Integer.parseInt(splitText[1]);
                                int day = Integer.parseInt(splitText[2]);
                                LocalDate bday = LocalDate.of(year, month, day);
                                LocalDate today = LocalDate.now();
                                Period age = Period.between(bday, today);
                                String years = String.valueOf(age.getYears());
                                String months = String.valueOf(age.getMonths());
                                String yearText, monthText;
                                if (age.getYears() > 1 && age.getMonths() > 1) {
                                    yearText = " years ";
                                    monthText = " months";
                                } else if (age.getYears() < 1 && age.getMonths() > 1) {
                                    yearText = " year ";
                                    monthText = " months";
                                } else if (age.getYears() > 1 && age.getMonths() < 1) {
                                    yearText = " years ";
                                    monthText = " month";
                                } else {
                                    yearText = " year ";
                                    monthText = " month";
                                }
                                String result = years + yearText + months + monthText;
                                period.setText(result);
                                currentlyWorkingCard.setVisibility(View.VISIBLE);

                            }
                        }, 1500);
                    } else {
                        workingStatusText.setVisibility(View.VISIBLE);
                        currentlyWorkingCard.setVisibility(View.GONE);
                    }


                } else {
                    workingStatusText.setVisibility(View.VISIBLE);
                    currentlyWorkingCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    private void addDegrees() {
        bottomSheetDoctorDegree = new BottomSheetDialog(requireContext(), R.style.bottomSheetTheme);
        @SuppressLint("InflateParams")
        View doctorDegreeView = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_doctor_degrees, null);
        MBBS = doctorDegreeView.findViewById(R.id.MBBS);
        BMBS = doctorDegreeView.findViewById(R.id.BMBS);
        MBChC = doctorDegreeView.findViewById(R.id.MBChC);
        MBChB = doctorDegreeView.findViewById(R.id.MBChB);
        MBBCh = doctorDegreeView.findViewById(R.id.MBBCh);
        MD = doctorDegreeView.findViewById(R.id.MD);
        DO = doctorDegreeView.findViewById(R.id.DO);
        DS = doctorDegreeView.findViewById(R.id.DS);
        BCS = doctorDegreeView.findViewById(R.id.BCS);
        confirmList = doctorDegreeView.findViewById(R.id.confirmList);
        ArrayList<CheckBox> ids = new ArrayList<>();
        ids.add(MBBS);
        ids.add(BMBS);
        ids.add(MBChC);
        ids.add(MBChB);
        ids.add(MBBCh);
        ids.add(MD);
        ids.add(DO);
        ids.add(DS);
        ids.add(BCS);
        DatabaseReference reference = database.getReference("doctorDegree");
        reference.child(doctorId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        DataSnapshot snapshot = task.getResult();
                        for (CheckBox box : ids) {
                            if (!String.valueOf(snapshot.child(getResources().getResourceEntryName(box.getId())).getValue()).equals("null")) {
                                box.setChecked(true);
                            }
                        }

                    } else {
                        Toast.makeText(requireContext(), "No data found ", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(requireContext(), "Connection Error", Toast.LENGTH_SHORT).show();
            }
        });
        confirmList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stringBuilder = new StringBuilder();
                ArrayList<String> degreeList = new ArrayList<>();
                DatabaseReference reference = database.getReference("doctorDegree");
                for (CheckBox box : ids) {
                    if (box.isChecked()) {
                        reference.child(doctorId).child(getResources().getResourceEntryName(box.getId())).setValue(box.getText().toString().trim());
                        degreeList.add(box.getText().toString().trim());
                    } else {
                        reference.child(doctorId).child(getResources().getResourceEntryName(box.getId())).setValue("null");
                    }
                }
                if (degreeList.size() == 0) {
                    stringBuilder.append("null");
                } else {
                    for (int i = 0; i < degreeList.size(); i++) {
                        if (degreeList.size() - 1 == i) {
                            stringBuilder.append(degreeList.get(i));
                        } else {
                            stringBuilder.append(degreeList.get(i)).append(",");
                        }
                    }
                }
                reference = database.getReference("doctorInfo");
                reference.child(doctorId).child("degrees").setValue(stringBuilder.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        bottomSheetDoctorDegree.cancel();
                        Snackbar.make(parentLayout, "Information Updated Successfully", Snackbar.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        bottomSheetDoctorDegree.cancel();
                        Snackbar.make(parentLayout, "Information Updation Failed", Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });
        bottomSheetDoctorDegree.setContentView(doctorDegreeView);
        bottomSheetDoctorDegree.show();
    }

    private void addSpecialty() {
        bottomSheetDoctorSpecialty = new BottomSheetDialog(requireContext(), R.style.bottomSheetTheme);
        View doctorSpecialtyView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_doctor_specialty, null);
        generalPhysician = doctorSpecialtyView.findViewById(R.id.generalPhysician);
        gynecologist = doctorSpecialtyView.findViewById(R.id.gynecologist);
        paediatrician = doctorSpecialtyView.findViewById(R.id.paediatrician);
        dermatologist = doctorSpecialtyView.findViewById(R.id.dermatologist);
        psychiatrist = doctorSpecialtyView.findViewById(R.id.psychiatrist);
        neurologist = doctorSpecialtyView.findViewById(R.id.neurologist);
        ophthalmologist = doctorSpecialtyView.findViewById(R.id.ophthalmologist);
        nutritionist = doctorSpecialtyView.findViewById(R.id.nutritionist);
        cardiologist = doctorSpecialtyView.findViewById(R.id.cardiologist);
        confirmList = doctorSpecialtyView.findViewById(R.id.confirmList);
        DatabaseReference reference = database.getReference("doctorSpecialty");
        ArrayList<CheckBox> ids = new ArrayList<>();
        ids.add(generalPhysician);
        ids.add(gynecologist);
        ids.add(paediatrician);
        ids.add(dermatologist);
        ids.add(psychiatrist);
        ids.add(neurologist);
        ids.add(ophthalmologist);
        ids.add(nutritionist);
        ids.add(cardiologist);
        reference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (CheckBox box : ids) {
                        if (!String.valueOf(snapshot.child(getResources().getResourceEntryName(box.getId())).getValue()).equals("null")) {
                            box.setChecked(true);
                        }
                    }

                } else {
                    Toast.makeText(requireContext(), "Data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        confirmList.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                stringBuilder = new StringBuilder();
                ArrayList<String> doctorSpecialty = new ArrayList<>();
                for (CheckBox box : ids) {
                    if (box.isChecked()) {
                        reference.child(doctorId).child(getResources().getResourceEntryName(box.getId())).setValue(box.getText().toString());
                        doctorSpecialty.add(box.getText().toString());
                    } else {
                        reference.child(doctorId).child(getResources().getResourceEntryName(box.getId())).setValue("null");
                    }
                }
                if (doctorSpecialty.isEmpty()) {
                    stringBuilder.append("null");
                } else {
                    for (int i = 0; i < doctorSpecialty.size(); i++) {
                        if (doctorSpecialty.size() - 1 == i) {
                            stringBuilder.append(doctorSpecialty.get(i));
                        } else {
                            stringBuilder.append(doctorSpecialty.get(i)).append(",");
                        }
                    }
                }
                DatabaseReference writeReference = database.getReference("doctorInfo");
                writeReference.child(doctorId).child("specialty").setValue(stringBuilder.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        bottomSheetDoctorSpecialty.cancel();
                        Snackbar.make(parentLayout, "Information Updated Successfully", Snackbar.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        bottomSheetDoctorSpecialty.cancel();
                        Snackbar.make(parentLayout, "Information Updation Failed", Snackbar.LENGTH_LONG).show();
                    }
                });

            }
        });
        bottomSheetDoctorSpecialty.setContentView(doctorSpecialtyView);
        bottomSheetDoctorSpecialty.show();
    }

    private void addCurrentlyWorkingDetails() {
        bottomSheetCurrentlyWorking = new BottomSheetDialog(requireContext(), R.style.bottomSheetTheme);
        View currentlyWorkingView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_doctor_currently_working, null);
        hospitalNameLayout = currentlyWorkingView.findViewById(R.id.hospitalNameLayout);
        hospitalName = currentlyWorkingView.findViewById(R.id.hospitalName);
        designationLayout = currentlyWorkingView.findViewById(R.id.designationLayout);
        designation = currentlyWorkingView.findViewById(R.id.designation);
        joiningDateLayout = currentlyWorkingView.findViewById(R.id.joiningDateLayout);
        joiningDate = currentlyWorkingView.findViewById(R.id.joiningDate);
        workingStatus = currentlyWorkingView.findViewById(R.id.workingStatus);
        leavingDateLayout = currentlyWorkingView.findViewById(R.id.leavingDateLayout);
        departmentLayout = currentlyWorkingView.findViewById(R.id.departmentLayout);
        department = currentlyWorkingView.findViewById(R.id.department);
        leavingDate = currentlyWorkingView.findViewById(R.id.leavingDate);
        confirmList = currentlyWorkingView.findViewById(R.id.confirmList);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.drop_menu, namesofHospital);
        hospitalName.setAdapter(adapter);
        ArrayAdapter<String> designationAdapter = new ArrayAdapter<>(requireContext(), R.layout.drop_menu, designations);
        designation.setAdapter(designationAdapter);
        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(requireContext(), R.layout.drop_menu, departments);
        department.setAdapter(departmentAdapter);
        DatabaseReference reference = database.getReference("doctorCurrentlyWorking");
        reference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (!String.valueOf(snapshot.child("hospitalName").getValue()).equals("null")) {
                        hospitalName.setText(String.valueOf(snapshot.child("hospitalName").getValue()));
                        designation.setText(String.valueOf(snapshot.child("designation").getValue()));
                        department.setText(String.valueOf(snapshot.child("department").getValue()));
                        joiningDate.setText(String.valueOf(snapshot.child("joiningDate").getValue()));
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        joiningDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callDatePicker(1);
            }
        });
        leavingDateLayout.setVisibility(View.GONE);
        workingStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    leavingDateLayout.setVisibility(View.GONE);
                    leavingDate.setText(null);
                    leavingDateLayout.setError(null);
                } else {
                    leavingDateLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        leavingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callDatePicker(0);
            }
        });
        stateObserver();
        confirmList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateHospitalName() | !validateDesignation() | !validateDepartment() | !validateJoiningDate() | !validateLeavingDate()) {
                    return;
                }
                String hospital = hospitalName.getText().toString().trim();
                String dept = department.getText().toString().trim();
                String desig = designation.getText().toString().trim();
                String joinDate = joiningDate.getText().toString().trim();
                String leaveDate;
                if (!workingStatus.isChecked()) {
                    Random random = new Random();
                    String serialNumber = String.valueOf(random.nextInt(500) + 200);
                    leaveDate = leavingDate.getText().toString().trim();
                    DatabaseReference reference = database.getReference("doctorPastExperience");
                    HashMap<String, String> data = new HashMap<>();
                    data.put("hospitalName", hospital);
                    data.put("department", dept);
                    data.put("designation", desig);
                    data.put("joiningDate", joinDate);
                    data.put("leavingDate", leaveDate);
                    reference.child(doctorId).child(serialNumber).setValue(data);
                    HashMap<String, String> secondData = new HashMap<>();
                    secondData.put("hospitalName", "null");
                    secondData.put("department", "null");
                    secondData.put("designation", "null");
                    secondData.put("joiningDate", "null");
                    reference = database.getReference("doctorCurrentlyWorking");
                    reference.child(doctorId).setValue(secondData);
                    bottomSheetCurrentlyWorking.cancel();
                    Snackbar.make(parentLayout, "Information Updated Sucessfully", Snackbar.LENGTH_LONG).show();
                } else {
                    DatabaseReference reference = database.getReference("doctorCurrentlyWorking");
                    HashMap<String, String> data = new HashMap<>();
                    data.put("hospitalName", hospital);
                    data.put("department", dept);
                    data.put("designation", desig);
                    data.put("joiningDate", joinDate);
                    reference.child(doctorId).setValue(data);
                    bottomSheetCurrentlyWorking.cancel();
                    Snackbar.make(parentLayout, "Information Updated Sucessfully", Snackbar.LENGTH_LONG).show();
                }

            }
        });
        bottomSheetCurrentlyWorking.setContentView(currentlyWorkingView);
        bottomSheetCurrentlyWorking.show();
    }

    public void callDatePicker(int i) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis());
        String tempDate = formatter.format(date);
        String[] CurrentDate = tempDate.split("/");
        int currentYear = Integer.parseInt(CurrentDate[0]);
        int currentMonth = Integer.parseInt(CurrentDate[1]);
        int currentDay = Integer.parseInt(CurrentDate[2]);
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = String.valueOf(year) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(dayOfMonth);
                if (i == 1) {
                    joiningDate.setText(date);
                } else {
                    leavingDate.setText(date);
                }

            }
        }, currentYear, currentMonth, currentDay);
        dialog.show();
    }

    private void addPastExperience() {
        bottomSheetPastExperience = new BottomSheetDialog(requireContext(), R.style.bottomSheetTheme);
        View pastExperienceView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_doctor_past_experience, null);
        hospitalName = pastExperienceView.findViewById(R.id.hospitalName);
        hospitalNameLayout = pastExperienceView.findViewById(R.id.hospitalNameLayout);
        designation = pastExperienceView.findViewById(R.id.designation);
        designationLayout = pastExperienceView.findViewById(R.id.designationLayout);
        department = pastExperienceView.findViewById(R.id.department);
        departmentLayout = pastExperienceView.findViewById(R.id.departmentLayout);
        joiningDate = pastExperienceView.findViewById(R.id.joiningDate);
        joiningDateLayout = pastExperienceView.findViewById(R.id.joiningDateLayout);
        leavingDate = pastExperienceView.findViewById(R.id.leavingDate);
        leavingDateLayout = pastExperienceView.findViewById(R.id.leavingDateLayout);
        confirmList = pastExperienceView.findViewById(R.id.confirmList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.drop_menu, namesofHospital);
        hospitalName.setAdapter(adapter);
        ArrayAdapter<String> designationAdapter = new ArrayAdapter<>(requireContext(), R.layout.drop_menu, designations);
        designation.setAdapter(designationAdapter);
        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(requireContext(), R.layout.drop_menu, departments);
        department.setAdapter(departmentAdapter);
        joiningDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callDatePicker(1);
            }
        });
        leavingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callDatePicker(0);
            }
        });
        confirmList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateHospitalName() | !validatePastLeavingDate() | !validateJoiningDate() | !validateDepartment() | !validateDesignation()) {
                    return;
                }
                String doctorsHospitalName = hospitalName.getText().toString().trim();
                String doctorsDepartment = department.getText().toString().trim();
                String doctorsDesignation = designation.getText().toString().trim();
                String doctorsJoiningDate = joiningDate.getText().toString().trim();
                String doctorsLeavingDate = leavingDate.getText().toString().trim();
                HashMap<String, String> data = new HashMap<>();
                data.put("hospitalName", doctorsHospitalName);
                data.put("department", doctorsDepartment);
                data.put("designation", doctorsDesignation);
                data.put("joiningDate", doctorsJoiningDate);
                data.put("leavingDate", doctorsLeavingDate);
                DatabaseReference reference = database.getReference("doctorPastExperience");
                Random random = new Random();
                String serialNumber = String.valueOf(random.nextInt(500));
                reference.child(doctorId).child(serialNumber).setValue(data);
                bottomSheetPastExperience.cancel();
                Snackbar.make(parentLayout, "Information Added Successfully", Snackbar.LENGTH_LONG).show();
            }
        });
        bottomSheetPastExperience.setContentView(pastExperienceView);
        bottomSheetPastExperience.show();
    }

    public void getPastWorkingExperience() {
        list = new ArrayList<>();
        recyclerAdapter = new doctorPastExperienceAdapter(requireContext(), list);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.smoothScrollToPosition(recyclerAdapter.getItemCount());
        DatabaseReference reference = database.getReference("doctorPastExperience");
        reference.child(doctorId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        doctorPastExperienceModel model = snap.getValue(doctorPastExperienceModel.class);
                        list.add(model);
                    }
                    recyclerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void chooseImage() {
        try {
            Intent gallery = new Intent(Intent.ACTION_PICK);
            gallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery, GALLERY_REQ_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == GALLERY_REQ_CODE) {
            filepath = data.getData();
            try {
                InputStream inputStream = requireActivity().getContentResolver().openInputStream(filepath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                profilePicture.setImageBitmap(bitmap);
                Snackbar snack = Snackbar.make(parentLayout, "first text", Snackbar.LENGTH_INDEFINITE);
                snack.setText("Uploading");
                snack.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference uploader = storage.getReference("profileImage");
                uploader.child(doctorId).putFile(filepath).addOnSuccessListener(taskSnapshot -> uploader.child(doctorId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        FirebaseDatabase database = FirebaseDatabase
                                .getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
                        DatabaseReference reference = database.getReference("doctorInfo");
                        reference.child(doctorId).child("photoUrl").setValue(uri.toString());
                        snack.setText("Uploaded Successfully");
                        final Handler handler = new Handler();
                        handler.postDelayed(() -> snack.dismiss(), 1000);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                })).addOnProgressListener(snapshot -> {
                    float percent = (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    snack.setText("Uploaded: " + (int) percent + "%");
                }).addOnFailureListener(e -> {

                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean validateHospitalName() {
        String value = hospitalName.getText().toString().trim();
        if (value.isEmpty()) {
            hospitalNameLayout.setError("Field Can't be empty");
            return false;
        } else {
            return true;
        }
    }

    private boolean validateDesignation() {
        String value = designation.getText().toString().trim();
        if (value.isEmpty()) {
            designationLayout.setError("Field Can't be empty");
            return false;
        } else {
            return true;
        }
    }

    private boolean validateDepartment() {
        String value = department.getText().toString().trim();
        if (value.isEmpty()) {
            departmentLayout.setError("Field Can't be empty");
            return false;
        } else {
            return true;
        }
    }

    private boolean validateJoiningDate() {
        String value = joiningDate.getText().toString().trim();
        if (value.isEmpty()) {
            joiningDateLayout.setError("Field Can't be empty");
            return false;
        } else {
            return true;
        }
    }

    private boolean validateLeavingDate() {
        String value = leavingDate.getText().toString().trim();
        if (workingStatus.isChecked()) {
            return true;
        } else {
            if (value.isEmpty()) {
                leavingDateLayout.setError("Field Can't be empty");
                return false;
            } else {
                return true;
            }
        }
    }

    private boolean validatePastLeavingDate() {
        String value = leavingDate.getText().toString().trim();
        if (value.isEmpty()) {
            leavingDateLayout.setError("Field Can't be empty");
            return false;
        } else {
            return true;
        }

    }

    private void stateObserver() {
        hospitalName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                hospitalNameLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        designation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                designationLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        department.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                departmentLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        joiningDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                joiningDateLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        leavingDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                leavingDateLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences preferences = requireActivity().getSharedPreferences("loginDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("loginAs", "");
        editor.apply();
        Intent intent = new Intent(requireContext(), DoctorLogin.class);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_doctor_profile, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        return view;
    }
}