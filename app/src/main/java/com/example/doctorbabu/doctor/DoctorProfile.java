package com.example.doctorbabu.doctor;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import androidx.fragment.app.Fragment;
import com.example.doctorbabu.R;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;


public class DoctorProfile extends Fragment {

String doctorId;
FirebaseDatabase database;
TextInputLayout hospitalNameLayout,designationLayout,joiningDateLayout,leavingDateLayout,departmentLayout;
AutoCompleteTextView hospitalName,designation,joiningDate,department,leavingDate;
ImageView profilePicture,editProfilePicture,medicalDegreesEdit,specialtiesEdit,currentlyWorkingAtEdit;
TextView doctorName,doctorDegree,medicalDegree,doctorSpecialties,period,doctorSpecialtiesDownField,bmdc,currentHospitalName,designationName,departmentName;
CheckBox MBBS,BMBS,MBChC,MBChB,MBBCh,MD,DO,DS,BCS,generalPhysician,gynecologist,paediatrician,
        dermatologist,psychiatrist,neurologist,ophthalmologist,nutritionist,cardiologist,workingStatus;
Button confirmList;
RatingBar ratingBar;
BottomSheetDialog bottomSheetDoctorDegree,bottomSheetDoctorSpecialty,bottomSheetCurrentlyWorking;
Uri filepath;
Bitmap bitmap;
StringBuilder stringBuilder;
LinearLayout parentLayout;

    public DoctorProfile() {
        // Required empty public constructor
    }
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = requireActivity().getSharedPreferences("loginDetails", Context.MODE_PRIVATE);
        doctorId = preferences.getString("doctorId","loginAs");
        Log.i("DoctorID : ",doctorId);
        database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewBinding();
        getData();
        getCurrentWorkingInfo();
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
    }
    public void viewBinding() {
        profilePicture = (ImageView) requireView().findViewById(R.id.profilePicture);
        editProfilePicture = (ImageView) requireView().findViewById(R.id.editProfilePicture);
        ratingBar = (RatingBar) requireView().findViewById(R.id.ratingbar);
        doctorName = (TextView) requireView().findViewById(R.id.doctorName);
        medicalDegreesEdit = (ImageView) requireView().findViewById(R.id.medicalDegreesEdit);
        doctorDegree = (TextView) requireView().findViewById(R.id.doctorDegree);
        medicalDegree = (TextView) requireView().findViewById(R.id.medicalDegree);
        specialtiesEdit = (ImageView) requireView().findViewById(R.id.specialtiesEdit);
        doctorSpecialties = (TextView) requireView().findViewById(R.id.doctorSpecialties);
        doctorSpecialtiesDownField = (TextView) requireView().findViewById(R.id.doctorSpecialtiesDownField);
        bmdc = (TextView) requireView().findViewById(R.id.bmdc);
        parentLayout = requireView().findViewById(R.id.parentLayout);
        currentlyWorkingAtEdit = (ImageView) requireView().findViewById(R.id.currentlyWorkingAtEdit);
        currentHospitalName = (TextView) requireView().findViewById(R.id.currentHospitalName);
        designationName = (TextView) requireView().findViewById(R.id.designationName);
        departmentName = (TextView) requireView().findViewById(R.id.departmentName);
        period = (TextView) requireView().findViewById(R.id.period);
    }
    public void getData()
    {
        DatabaseReference reference = database.getReference("doctorInfo");
        reference.child(doctorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fullName = snapshot.child("title").getValue() +" "+ snapshot.child("fullName").getValue();
                doctorName.setText(fullName);
                bmdc.setText(String.valueOf(snapshot.child("bmdc").getValue()));
                if(!String.valueOf(snapshot.child("degrees").getValue()).equals("null"))
                {
                    doctorDegree.setVisibility(View.VISIBLE);
                    doctorDegree.setText(String.valueOf(snapshot.child("degrees").getValue()));
                    medicalDegree.setText(String.valueOf(snapshot.child("degrees").getValue()));
                }
                else {
                    doctorDegree.setVisibility(View.GONE);
                    medicalDegree.setText("No information found");
                }
                if(!String.valueOf(snapshot.child("specialty").getValue()).equals("null"))
                {
                    doctorSpecialties.setVisibility(View.VISIBLE);
                    doctorSpecialties.setText(String.valueOf(snapshot.child("specialty").getValue()));
                    doctorSpecialtiesDownField.setText(String.valueOf(snapshot.child("specialty").getValue()));
                }
                else {
                    doctorSpecialties.setVisibility(View.GONE);
                    doctorSpecialtiesDownField.setText("No information found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        DatabaseReference referenceDegree =  database.getReference("doctorDegree");
    }
    public void getCurrentWorkingInfo()
    {

        DatabaseReference reference = database.getReference("doctorCurrentlyWorking");
        reference.child(doctorId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    currentHospitalName.setText(String.valueOf(snapshot.child("hospitalName").getValue()));
                    designationName.setText(String.valueOf(snapshot.child("designation").getValue()));
                    departmentName.setText(String.valueOf(snapshot.child("department").getValue()));
                    String date = String.valueOf(snapshot.child("joiningDate").getValue());
                    String [] splitText = date.split("/");
                    int year = Integer.parseInt(splitText[0]);
                    int month = Integer.parseInt(splitText[1]);
                    int day = Integer.parseInt(splitText[2]);
                    LocalDate bday = LocalDate.of(year,month,day);
                    LocalDate today = LocalDate.now();
                    Period age = Period.between(bday, today);
                    String years = String.valueOf(age.getYears());
                    String months = String.valueOf(age.getMonths());
                    String yearText, monthText;
                    if(age.getYears() > 1 && age.getMonths() > 1)
                    {
                        yearText = " years "; monthText =" months";
                    }
                    else if(age.getYears() < 1 && age.getMonths() > 1)
                    {
                        yearText = " year "; monthText = " months";
                    }
                    else if(age.getYears() > 1 && age.getMonths() < 1)
                    {
                        yearText = " years "; monthText = " month";
                    }
                    else
                    {
                        yearText = " year "; monthText = " month";
                    }
                    String result = years+yearText+months+monthText;
                    period.setText(result);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }
    private void addDegrees()
    {
        bottomSheetDoctorDegree = new BottomSheetDialog(requireContext(),R.style.bottomSheetTheme);
        View doctorDegreeView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_doctor_degrees,null);
        MBBS = doctorDegreeView.findViewById(R.id.MBBS);
        BMBS =  doctorDegreeView.findViewById(R.id.BMBS);
        MBChC = doctorDegreeView.findViewById(R.id.MBChC);
        MBChB = doctorDegreeView.findViewById(R.id.MBChB);
        MBBCh = doctorDegreeView.findViewById(R.id.MBBCh);
        MD = doctorDegreeView.findViewById(R.id.MD);
        DO = doctorDegreeView.findViewById(R.id.DO);
        DS = doctorDegreeView.findViewById(R.id.DS);
        BCS = doctorDegreeView.findViewById(R.id.BCS);
        confirmList = doctorDegreeView.findViewById(R.id.confirmList);
        DatabaseReference reference = database.getReference("doctorDegree");
        reference.child(doctorId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        DataSnapshot snapshot = task.getResult();
                        if(!String.valueOf(snapshot.child("MBBS").getValue()).equals("null"))
                        {
                           MBBS.setChecked(true);
                        }
                        if(!String.valueOf(snapshot.child("BCS").getValue()).equals("null"))
                        {
                            BCS.setChecked(true);
                        }
                        if(!String.valueOf(snapshot.child("BMBS").getValue()).equals("null"))
                        {
                            BMBS.setChecked(true);
                        }
                        if(!String.valueOf(snapshot.child("DO").getValue()).equals("null"))
                        {
                            DO.setChecked(true);
                        }
                        if(!String.valueOf(snapshot.child("DS").getValue()).equals("null"))
                        {
                            DS.setChecked(true);
                        }
                        if(!String.valueOf(snapshot.child("MBBCh").getValue()).equals("null"))
                        {
                            MBBCh.setChecked(true);
                        }
                        if(!String.valueOf(snapshot.child("MBChB").getValue()).equals("null"))
                        {
                            MBChB.setChecked(true);
                        }
                        if(!String.valueOf(snapshot.child("MBChC").getValue()).equals("null"))
                        {
                            MBChC.setChecked(true);
                        }
                        if(!String.valueOf(snapshot.child("MD").getValue()).equals("null"))
                        {
                            MD.setChecked(true);
                        }

                    }
                    else
                    {
                        Toast.makeText(requireContext(), "No data found ", Toast.LENGTH_SHORT).show();
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
                ArrayList <String> degreeList = new ArrayList<>();
               DatabaseReference reference =database.getReference("doctorDegree");
                if(MBBS.isChecked())
                {
                    reference.child(doctorId).child("MBBS").setValue("MBBS");
                    degreeList.add(MBBS.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("MBBS").setValue("null");
                }
                if(BCS.isChecked())
                {
                    reference.child(doctorId).child("BCS").setValue("BCS");
                    degreeList.add(BCS.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("BCS").setValue("null");
                }
                if(BMBS.isChecked())
                {
                    reference.child(doctorId).child("BMBS").setValue("BMBS");
                    degreeList.add(BMBS.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("BMBS").setValue("null");
                }
                if(DO.isChecked())
                {
                    reference.child(doctorId).child("DO").setValue("DO");
                    degreeList.add(DO.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("DO").setValue("null");
                }
                if(DS.isChecked())
                {
                    reference.child(doctorId).child("DS").setValue("DS");
                    degreeList.add(DS.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("DS").setValue("null");
                }
                if(MBBCh.isChecked())
                {
                    reference.child(doctorId).child("MBBCh").setValue("MBBCh");
                    degreeList.add(MBBCh.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("MBBCh").setValue("null");
                }
                if(MBChB.isChecked())
                {
                    reference.child(doctorId).child("MBChB").setValue("MBChB");
                    degreeList.add(MBBS.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("MBChB").setValue("null");
                }
                if(MBChC.isChecked())
                {
                    reference.child(doctorId).child("MBChC").setValue("MBChC");
                    degreeList.add(MBChC.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("MBChC").setValue("null");
                }
                if(MD.isChecked())
                {
                    reference.child(doctorId).child("MD").setValue("MD");
                    degreeList.add(MD.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("MD").setValue("null");
                }
                if(degreeList.size() == 0)
                {
                    stringBuilder.append("null");
                }
                else
                {
                    for(int i =0; i<degreeList.size();i++)
                    {
                        if(degreeList.size()-1 == i  )
                        {
                            stringBuilder.append(degreeList.get(i));
                        }
                        else
                        {
                            stringBuilder.append(degreeList.get(i)).append(",");
                        }
                    }
                }
                reference = database.getReference("doctorInfo");
                reference.child(doctorId).child("degrees").setValue(stringBuilder.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        bottomSheetDoctorDegree.cancel();
                        Snackbar.make(parentLayout,"Information Updated Successfully",Snackbar.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        bottomSheetDoctorDegree.cancel();
                        Snackbar.make(parentLayout,"Information Updation Failed",Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        });
        bottomSheetDoctorDegree.setContentView(doctorDegreeView);
        bottomSheetDoctorDegree.show();
    }

    private void addSpecialty(){
        bottomSheetDoctorSpecialty = new BottomSheetDialog(requireContext(),R.style.bottomSheetTheme);
        View doctorSpecialtyView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_doctor_specialty,null);
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
        reference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    if(!String.valueOf(snapshot.child("cardiologist").getValue()).equals("null"))
                    {
                        cardiologist.setChecked(true);
                    }
                    if(!String.valueOf(snapshot.child("dermatologist").getValue()).equals("null"))
                    {
                        dermatologist.setChecked(true);
                    }
                    if(!String.valueOf(snapshot.child("generalPhysician").getValue()).equals("null"))
                    {
                        generalPhysician.setChecked(true);
                    }
                    if(!String.valueOf(snapshot.child("gynecologist").getValue()).equals("null"))
                    {
                        gynecologist.setChecked(true);
                    }
                    if(!String.valueOf(snapshot.child("neurologist").getValue()).equals("null"))
                    {
                        neurologist.setChecked(true);
                    }
                    if(!String.valueOf(snapshot.child("nutritionist").getValue()).equals("null"))
                    {
                        nutritionist.setChecked(true);
                    }
                    if(!String.valueOf(snapshot.child("ophthalmologist").getValue()).equals("null"))
                    {
                        ophthalmologist.setChecked(true);
                    }
                    if(!String.valueOf(snapshot.child("paediatrician").getValue()).equals("null"))
                    {
                        paediatrician.setChecked(true);
                    }
                    if(!String.valueOf(snapshot.child("psychiatrist").getValue()).equals("null"))
                    {
                        psychiatrist.setChecked(true);
                    }

                }
                else
                {
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
                if(cardiologist.isChecked())
                {
                    reference.child(doctorId).child("cardiologist").setValue(cardiologist.getText().toString());
                    doctorSpecialty.add(cardiologist.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("cardiologist").setValue("null");
                }
                if(dermatologist.isChecked())
                {
                    reference.child(doctorId).child("dermatologist").setValue(dermatologist.getText().toString());
                    doctorSpecialty.add(dermatologist.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("dermatologist").setValue("null");
                }
                if(generalPhysician.isChecked())
                {
                    reference.child(doctorId).child("generalPhysician").setValue(generalPhysician.getText().toString());
                    doctorSpecialty.add(generalPhysician.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("generalPhysician").setValue("null");
                }
                if(gynecologist.isChecked())
                {
                    reference.child(doctorId).child("gynecologist").setValue(gynecologist.getText().toString());
                    doctorSpecialty.add(gynecologist.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("gynecologist").setValue("null");
                }
                if(neurologist.isChecked())
                {
                    reference.child(doctorId).child("neurologist").setValue(neurologist.getText().toString());
                    doctorSpecialty.add(neurologist.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("neurologist").setValue("null");
                }
                if(nutritionist.isChecked())
                {
                    reference.child(doctorId).child("nutritionist").setValue(nutritionist.getText().toString());
                    doctorSpecialty.add(nutritionist.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("nutritionist").setValue("null");
                }
                if(ophthalmologist.isChecked())
                {
                    reference.child(doctorId).child("ophthalmologist").setValue(ophthalmologist.getText().toString());
                    doctorSpecialty.add(ophthalmologist.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("ophthalmologist").setValue("null");
                }
                if(paediatrician.isChecked())
                {
                    reference.child(doctorId).child("paediatrician").setValue(paediatrician.getText().toString());
                    doctorSpecialty.add(paediatrician.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("paediatrician").setValue("null");
                }
                if(psychiatrist.isChecked())
                {
                    reference.child(doctorId).child("psychiatrist").setValue(psychiatrist.getText().toString());
                    doctorSpecialty.add(psychiatrist.getText().toString());
                }
                else
                {
                    reference.child(doctorId).child("psychiatrist").setValue("null");
                }
                if(doctorSpecialty.isEmpty())
                {
                    stringBuilder.append("null");
                }
                else
                {
                    for (int i = 0; i<doctorSpecialty.size(); i++)
                    {
                        if(doctorSpecialty.size()-1 == i)
                        {
                            stringBuilder.append(doctorSpecialty.get(i));
                        }
                        else
                        {
                            stringBuilder.append(doctorSpecialty.get(i)).append(",");
                        }
                    }
                }
                DatabaseReference writeReference = database.getReference("doctorInfo");
                writeReference.child(doctorId).child("specialty").setValue(stringBuilder.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        bottomSheetDoctorSpecialty.cancel();
                        Snackbar.make(parentLayout,"Information Updated Successfully",Snackbar.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        bottomSheetDoctorSpecialty.cancel();
                        Snackbar.make(parentLayout,"Information Updation Failed",Snackbar.LENGTH_LONG).show();
                    }
                });

            }
        });
        bottomSheetDoctorSpecialty.setContentView(doctorSpecialtyView);
        bottomSheetDoctorSpecialty.show();
    }
    private  void addCurrentlyWorkingDetails()
    {
        bottomSheetCurrentlyWorking = new BottomSheetDialog(requireContext(),R.style.bottomSheetTheme);
        View currentlyWorkingView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_doctor_currently_working,null);
        hospitalNameLayout = currentlyWorkingView.findViewById(R.id.hospitalNameLayout);
        hospitalName = currentlyWorkingView.findViewById(R.id.hospitalName);
        designationLayout = currentlyWorkingView.findViewById(R.id.designationLayout);
        designation = currentlyWorkingView.findViewById(R.id.designation);
        joiningDateLayout =currentlyWorkingView.findViewById(R.id.joiningDateLayout);
        joiningDate = currentlyWorkingView.findViewById(R.id.joiningDate);
        workingStatus = currentlyWorkingView.findViewById(R.id.workingStatus);
        leavingDateLayout = currentlyWorkingView.findViewById(R.id.leavingDateLayout);
        departmentLayout =currentlyWorkingView.findViewById(R.id.departmentLayout);
        department = currentlyWorkingView.findViewById(R.id.department);
        leavingDate = currentlyWorkingView.findViewById(R.id.leavingDate);
        confirmList = currentlyWorkingView.findViewById(R.id.confirmList);
        String [] items = new String[]{"Shahid Suhrawardy Hospital","Ad-Din Hospital","Ahmed Medical Centre Ltd","Aichi Hospital","Al Anaiet Adhunik Hospital",
                "Al-Helal Speacialist Hospital","Al-Jebel-E-Nur Heart Ltd","Al- Rajhi Hospital","Al-Ahsraf General Hospital","Al-Biruni Hospital","Al-Fateh Medical Sevices (Pvt) Ltd","Al-Madina General Hospital (Pvt.) Ltd","Al-Manar Hospital","Al-Markazul Islami Hospital",
                "Appolo Hospital","Arogya Niketan Hospital Ltd","Bangabandhu Shiekh Mujib Medical University","Bangkok Hospita","Bangladesh Heart & Chest Hospital","Bangladesh Medical College","Bdm Hospital",
                "Birdem","Brain & Maind Hospital Ltd","Care Madical Center Ltd","Chandshi Medical Centre","Community Eye Hospital","Community Maternity Hospital","City Hospital (Pvt) Ltd","Delta Medical Centre Ltd","Dhaka General Hospital (Pvt) Ltd",
                "Dhaka Medical College & Hospital","Dhaka National Hospital Ltd","Doctor Babu"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),R.layout.drop_menu,items);
        hospitalName.setAdapter(adapter);

        String [] designations = new String[]{"Medical Officer","HMO","Intern","MD Resident","Post Graduate Trainee","Emergency Medical Officer","PGT",
                "RMO","Chief Consultant","IMO","EMO","Honourary Medical Officer","Senior Medical Officer","Aesthetic Laser Specialist"};
        ArrayAdapter<String> designationAdapter = new ArrayAdapter<>(requireContext(),R.layout.drop_menu,designations);
        designation.setAdapter(designationAdapter);

        String[] departments = new String[]{"Medicine","Gynae","Medicine & Surgery","Internal Medicine","Diabetics","Covid Unit","Dermatology","Skin","CO2 Laser","Long Pulse",
        "Ophthalmology","Human Resources","Neurology","Emergency","Dental","Prosthodontic","Surgery","Cardiology"};
        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(requireContext(),R.layout.drop_menu,departments);
        department.setAdapter(departmentAdapter);
        joiningDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callDatePicker(view,1);
            }
        });
        leavingDateLayout.setVisibility(View.GONE);
        workingStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked)
                {
                    leavingDateLayout.setVisibility(View.GONE);
                    leavingDate.setText(null);
                    leavingDateLayout.setError(null);
                }
                else {
                    leavingDateLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        leavingDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callDatePicker(view,0);
            }
        });
        stateObserver();
        confirmList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateHospitalName() | !validateDesignation()|!validateDepartment()|!validateJoiningDate() |!validateLeavingDate())
                {
                    return;
                }
                String hospital = hospitalName.getText().toString().trim();
                String dept = department.getText().toString().trim();
                String desig = designation.getText().toString().trim();
                String joinDate = joiningDate.getText().toString().trim();
                String leaveDate;
                if(!workingStatus.isChecked())
                {
                    leaveDate = leavingDate.getText().toString().trim();
                }
                DatabaseReference reference = database.getReference("doctorCurrentlyWorking");
                reference.child(doctorId).child("department").setValue(dept);
                reference.child(doctorId).child("designation").setValue(desig);
                reference.child(doctorId).child("joiningDate").setValue(joinDate);
                reference.child(doctorId).child("hospitalName").setValue(hospital);
                bottomSheetCurrentlyWorking.cancel();
                Snackbar.make(parentLayout,"Information Updated Sucessfully",Snackbar.LENGTH_LONG).show();
            }
        });
        bottomSheetCurrentlyWorking.setContentView(currentlyWorkingView);
        bottomSheetCurrentlyWorking.show();
    }
    public void callDatePicker(View view,int i)
    {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis());
        String tempDate = formatter.format(date);
        String[] CurrentDate = tempDate.split("/");
        int currentYear = Integer.parseInt(CurrentDate[0]);
        int currentMonth = Integer.parseInt(CurrentDate[1]);
        int currentDay = Integer.parseInt(CurrentDate[2]);
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = String.valueOf(year)+"/"+String.valueOf(month+1)+"/"+String.valueOf(dayOfMonth);
                if(i==1)
                {
                    joiningDate.setText(date);
                }
                else
                {
                    leavingDate.setText(date);
                }

            }
        }, currentYear, currentMonth, currentDay);
        dialog.show();
    }
    public void chooseImage()
    {
        ImagePicker.with(this)
                .crop(1f, 1f)	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        assert data != null;
        filepath = data.getData();
        try{
            InputStream inputStream = requireActivity().getContentResolver().openInputStream(filepath);
            bitmap = BitmapFactory.decodeStream(inputStream);
            profilePicture.setImageBitmap(bitmap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    private boolean validateHospitalName()
    {
        String value = hospitalName.getText().toString().trim();
        if(value.isEmpty())
        {
            hospitalNameLayout.setError("Field Can't be empty");
            return false;
        }
        else
        {
            return true;
        }
    }
    private boolean validateDesignation()
    {
        String value = designation.getText().toString().trim();
        if(value.isEmpty())
        {
            designationLayout.setError("Field Can't be empty");
            return false;
        }
        else
        {
            return true;
        }
    }
    private boolean validateDepartment()
    {
        String value = department.getText().toString().trim();
        if(value.isEmpty())
        {
            departmentLayout.setError("Field Can't be empty");
            return false;
        }
        else
        {
            return true;
        }
    }
    private boolean validateJoiningDate()
    {
        String value = joiningDate.getText().toString().trim();
        if(value.isEmpty())
        {
            joiningDateLayout.setError("Field Can't be empty");
            return false;
        }
        else
        {
            return true;
        }
    }
    private boolean validateLeavingDate()
    {
        String value = leavingDate.getText().toString().trim();
        if(workingStatus.isChecked())
        {
            return true;
        }
        else
        {
            if(value.isEmpty())
            {
                leavingDateLayout.setError("Field Can't be empty");
                return false;
            }
            else
            {
                return true;
            }
        }
    }
    private void stateObserver()
    {
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_doctor_profile, container, false);
    }
}