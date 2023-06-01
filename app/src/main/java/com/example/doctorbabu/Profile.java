package com.example.doctorbabu;
import static android.content.ContentValues.TAG;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;

public class Profile extends Fragment {

    FirebaseAuth auth;
    FirebaseUser user;
    TextView email,phone,address,gender,height,weight,age,verificationStatus,fullName,alergy,medicalInfo,bloodGroupInfo, allergyList,medicalHistoryList,bloodGroupList;
    ImageView userprofilePicture,verifyTickSign,notVerifyImg;
    Button editProfile,allergyListConfirm,medicalListConfirm,bloodConfirmList;
    ProgressBar loadingCircle;
    FlexboxLayout flex,flex2;
    BottomSheetDialog bottomSheetDialog,bottomSheetDialogMedicalList,bottomSheetDialogBloodList;
    CheckBox drug,cloth,dust,food,asthma,cancer,diabetics,heartDisease,highBp,migraine,stroke,ulcer,aPositive,bPositive,oPositive,abPositive,aNegative,bNegative,oNegative,abNegative;
    public Profile() {
        // Required empty public constructor
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingCircle = (ProgressBar) getView().findViewById(R.id.progress_circular);
        loadingCircle.setVisibility(View.VISIBLE);
        getData();
        getUserprofile();
        loadingCircle.setVisibility(View.GONE);

    }

    public void viewBinding()
    {
        email = (TextView) getView().findViewById(R.id.userEmail);
        userprofilePicture = (ImageView) getView().findViewById(R.id.profilePicture);
        verificationStatus = (TextView) getView().findViewById(R.id.verifyStatus);
        verifyTickSign = (ImageView) getView().findViewById(R.id.tickSign);
        notVerifyImg = (ImageView) getView().findViewById(R.id.notVerified);
        editProfile = (Button) getView().findViewById(R.id.editProfile);
        fullName = (TextView) getView().findViewById(R.id.userName);
        flex = (FlexboxLayout) getView().findViewById(R.id.alergyHistory);
        flex2 = (FlexboxLayout) getView().findViewById(R.id.pastMedicalHistory);
        alergy =(TextView) getView().findViewById(R.id.alergy);
        medicalInfo = (TextView) getView().findViewById(R.id.pastMedicalHistoryInfo);
        bloodGroupInfo = (TextView) getView().findViewById(R.id.bloodGroupInfo);
        phone = (TextView) getView().findViewById(R.id.userPhone);
        address = (TextView) getView().findViewById(R.id.userAddress);
        gender = (TextView) getView().findViewById(R.id.userGender);
        height = (TextView) getView().findViewById(R.id.userHeight);
        weight = (TextView) getView().findViewById(R.id.userWeight);
        age = (TextView) getView().findViewById(R.id.userAge);
        allergyList = (TextView) getView().findViewById(R.id.alergyHistoryInfo);
        medicalHistoryList = (TextView) getView().findViewById(R.id.pastMedicalHistoryText);
        bloodGroupList = (TextView) getView().findViewById(R.id.bloodGroupText);
    }

    public void getData()
    {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
        ImageView logOut = (ImageView) getView().findViewById(R.id.signOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(),Login.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        viewBinding();
        userAlergyHistory();
        userPastMedicalHistory();
//        address = (TextView) getView().findViewById(R.id.userAddress);
//        age = (TextView) getView().findViewById(R.id.userAge);
        email.setText(user.getEmail());
        if(!user.isEmailVerified())
        {
            verifyTickSign.setVisibility(View.GONE);
            verificationStatus.setText("Email not verified");
            notVerifyImg.setVisibility(View.VISIBLE);
            alertDialog("Email Not verified","Do you want a mail to verify your email?","Resend","Cancel");
        }
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),EditProfile.class);
                intent.putExtra("uId",user.getUid());
                intent.putExtra("email",user.getEmail());
                startActivity(intent);
            }
        });
        allergyList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               allergyList();
            }
        });
        medicalHistoryList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                medicalList();
            }
        });
        bloodGroupList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bloodList();
            }
        });
    }
    public void getUserprofile()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("users");
        reference.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        DataSnapshot snapshot = task.getResult();
                          fullName.setText(String.valueOf(snapshot.child("fullName").getValue()));
                          phone.setText(String.valueOf(snapshot.child("phone").getValue()));
                          String dbAddress = String.valueOf(snapshot.child("address").getValue());
                          if(!dbAddress.equals("null"))
                          {
                              address.setText(dbAddress);
                          }
                          else
                          {
                              address.setText("Address is not set");
                          }
                          String dbGender = String.valueOf(snapshot.child("gender").getValue());
                          if(!dbGender.equals("null"))
                          {
                              gender.setText(dbGender);
                          }
                          else
                          {
                              gender.setText("Gender is not set");
                          }
                          String dbHeight = String.valueOf(snapshot.child("height").getValue());
                          if(!dbHeight.equals("null"))
                          {
                              height.setText(dbHeight);
                          }
                          else
                          {
                              height.setText("Height is not set");
                          }
                          String dbWeight = String.valueOf(snapshot.child("weight").getValue());
                          if(!dbWeight.equals("null"))
                          {
                              weight.setText(dbWeight);
                          }
                          else
                          {
                              weight.setText("Weight is not set");
                          }
                           String birthDate = String.valueOf(snapshot.child("age").getValue());
                          if(!birthDate.equals("null"))
                          {
                              LocalDate date = LocalDate.parse(birthDate);
                              int year = date.getYear();
                              Year thisYear = Year.now();
                              String thisYearString = thisYear.toString();
                              int currentAge =  Integer.parseInt(thisYearString)- year;
                              age.setText(String.valueOf(currentAge));

                          }
                          else
                          {
                              age.setText("Age is not set");
                          }
                        String photoUrl = String.valueOf(snapshot.child("photoUrl").getValue());
                        if(!photoUrl.equals("null"))
                        {
                            Glide.with(getContext()).load(photoUrl).into(userprofilePicture);
                            userprofilePicture.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            userprofilePicture.setImageResource(R.drawable.profile_picture);
                            userprofilePicture.setVisibility(View.VISIBLE);
                        }
                        //TODO I have to finish this and make some changes to the UI also!

                    }
                    else
                    {
                        Toast.makeText(getActivity(), "Couldn't load user's profile picture", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(getActivity(), "Database Read Error", Toast.LENGTH_LONG).show();
                }
            }}).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"Database Read Error");
            }
        });
        reference = database.getReference("bloodgroup");
        reference.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        DataSnapshot snapshot = task.getResult();
                        if(!String.valueOf(snapshot.child("group").getValue()).equals("null"))
                        {
                            bloodGroupInfo.setText(String.valueOf(snapshot.child("group").getValue()));
                        }
                        else
                        {
                            bloodGroupInfo.setText("No information found");
                        }

                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }
  public void userAlergyHistory(){
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("alergy");
        reference.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        DataSnapshot snapshot = task.getResult();
                        ArrayList<String> list = new ArrayList<>();
                        if(!String.valueOf(snapshot.child("cloth").getValue()).equals("null"))
                        {
                            list.add(String.valueOf(snapshot.child("cloth").getValue()));
                        }
                        if(!String.valueOf(snapshot.child("drug").getValue()).equals("null"))
                        {
                            list.add(String.valueOf(snapshot.child("drug").getValue()));
                        }
                        if(!String.valueOf(snapshot.child("dust").getValue()).equals("null"))
                        {
                            list.add(String.valueOf(snapshot.child("dust").getValue()));
                        }
                        if(!String.valueOf(snapshot.child("food").getValue()).equals("null"))
                        {
                            list.add(String.valueOf(snapshot.child("food").getValue()));
                        }
                        alergyListAdd(list);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
  }
  public void alergyListAdd(ArrayList <String> list)
  {
      int size = list.size();
      if(size != 0)
      {
          alergy.setVisibility(View.GONE);
          for(int i=0;i<size;i++)
          {
              TextView text = new TextView(getContext());FlexboxLayout.LayoutParams layout =
                  new FlexboxLayout.LayoutParams(
                          FlexboxLayout.LayoutParams.WRAP_CONTENT,
                          FlexboxLayout.LayoutParams.WRAP_CONTENT);

              layout.setLayoutDirection(FlexDirection.ROW);
              layout.setMarginStart(10);
              text.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
              text.setLayoutParams(layout);
              if(i == size-1)
              {
                  text.setText(list.get(i));
              }
              else
              {
                  String value = list.get(i) + ",";
                  text.setText(value);
              }
              flex.addView(text);
          }
      }
      else
      {
          alergy.setText("No information found");
          alergy.setVisibility(View.VISIBLE);
      }

  }
    public void userPastMedicalHistory()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("medicalhistory");
        reference.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        DataSnapshot snapshot = task.getResult();
                        ArrayList<String> medicalHistoryList = new ArrayList<>();
                        if(!String.valueOf(snapshot.child("asthma").getValue()).equals("null"))
                        {
                            medicalHistoryList.add(String.valueOf(snapshot.child("asthma").getValue()));
                        }
                        if(!String.valueOf(snapshot.child("cancer").getValue()).equals("null"))
                        {
                            medicalHistoryList.add(String.valueOf(snapshot.child("cancer").getValue()));
                        }
                        if(!String.valueOf(snapshot.child("diabetics").getValue()).equals("null"))
                        {
                            medicalHistoryList.add(String.valueOf(snapshot.child("diabetics").getValue()));
                        }
                        if(!String.valueOf(snapshot.child("heartdisease").getValue()).equals("null"))
                        {
                            medicalHistoryList.add(String.valueOf(snapshot.child("heartdisease").getValue()));
                        }
                        if(!String.valueOf(snapshot.child("migraine").getValue()).equals("null"))
                        {
                            medicalHistoryList.add(String.valueOf(snapshot.child("migraine").getValue()));
                        }
                        if(!String.valueOf(snapshot.child("stroke").getValue()).equals("null"))
                        {
                            medicalHistoryList.add(String.valueOf(snapshot.child("stroke").getValue()));
                        }
                        if(!String.valueOf(snapshot.child("ulcer").getValue()).equals("null"))
                        {
                            medicalHistoryList.add(String.valueOf(snapshot.child("ulcer").getValue()));
                        }
                        if(!String.valueOf(snapshot.child("highbp").getValue()).equals("null"))
                        {
                            medicalHistoryList.add(String.valueOf(snapshot.child("highbp").getValue()));
                        }
                        medicalHistoryListAdd(medicalHistoryList);
                    }
                    else
                    {
                        Toast.makeText(getContext(), "No information has been found!", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getContext(), "Couldn't fetch medical info!", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Couldn't fetch medical info!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void medicalHistoryListAdd(ArrayList <String> list)
    {
        int size = list.size();
        if(size != 0)
        {
            medicalInfo.setVisibility(View.GONE);
            for(int i=0;i<size;i++)
            {
                TextView text = new TextView(getContext());FlexboxLayout.LayoutParams layout =
                    new FlexboxLayout.LayoutParams(
                            FlexboxLayout.LayoutParams.WRAP_CONTENT,
                            FlexboxLayout.LayoutParams.WRAP_CONTENT);

                layout.setLayoutDirection(FlexDirection.ROW);
                layout.setMarginStart(10);
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
                text.setLayoutParams(layout);
                if(i == size-1)
                {
                    text.setText(list.get(i));
                }
                else
                {
                    String value = list.get(i) + ",";
                    text.setText(value);
                }
                flex2.addView(text);
            }
        }
        else
        {
            medicalInfo.setText("No information found");
            medicalInfo.setVisibility(View.VISIBLE);
        }

    }

    public void allergyList(){
        bottomSheetDialog = new BottomSheetDialog(getContext(),R.style.bottomSheetTheme);
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_alergylist,null);
        drug = bottomSheetView.findViewById(R.id.drug);
        dust = bottomSheetView.findViewById(R.id.dust);
        cloth = bottomSheetView.findViewById(R.id.cloth);
        food = bottomSheetView.findViewById(R.id.food);
        allergyListConfirm = bottomSheetView.findViewById(R.id.confirmList);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("alergy");
        reference.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            if(task.getResult().exists())
                            {
                                DataSnapshot snapshot = task.getResult();
                                drug.setChecked(!String.valueOf(snapshot.child("drug").getValue()).equals("null"));
                                dust.setChecked(!String.valueOf(snapshot.child("dust").getValue()).equals("null"));
                                cloth.setChecked(!String.valueOf(snapshot.child("cloth").getValue()).equals("null"));
                                food.setChecked(!String.valueOf(snapshot.child("food").getValue()).equals("null"));
                            }
                            else
                            {
                                Toast.makeText(getContext(), "Data don't exist", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(getContext(), "Database load error", Toast.LENGTH_SHORT).show();
                        }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Couldn't fetch alergy history",e.toString());
            }
        });
        allergyListConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(drug.isChecked())
                {
                    reference.child(user.getUid()).child("drug").setValue("Drug");
                }
                else
                {
                    reference.child(user.getUid()).child("drug").setValue("null");
                }
                if(dust.isChecked())
                {
                    reference.child(user.getUid()).child("dust").setValue("Dust");
                }
                else
                {
                    reference.child(user.getUid()).child("dust").setValue("null");
                }
                if(cloth.isChecked())
                {
                    reference.child(user.getUid()).child("cloth").setValue("Cloth");
                }
                else
                {
                    reference.child(user.getUid()).child("cloth").setValue("null");
                }
                if(food.isChecked())
                {
                    reference.child(user.getUid()).child("food").setValue("Food");
                }
                else
                {
                    reference.child(user.getUid()).child("food").setValue("null");
                }
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("Done").setMessage("Allergy History has been updated!");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        bottomSheetDialog.cancel();
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .detach(Profile.this)
                                .commitNowAllowingStateLoss();
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .attach(Profile.this)
                                .commitNow();
                    }
                }).setCancelable(false);
                dialog.create().show();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    public void medicalList()
    {
        bottomSheetDialogMedicalList = new BottomSheetDialog(getContext(),R.style.bottomSheetTheme);
        View bottomSheet = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_medical_history_list,null);
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
        reference.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isComplete())
                {
                    if(task.getResult().exists())
                    {
                        DataSnapshot snapshot = task.getResult();
                        asthma.setChecked(!String.valueOf(snapshot.child("asthma").getValue()).equals("null"));
                        cancer.setChecked(!String.valueOf(snapshot.child("cancer").getValue()).equals("null"));
                        diabetics.setChecked(!String.valueOf(snapshot.child("diabetics").getValue()).equals("null"));
                        heartDisease.setChecked(!String.valueOf(snapshot.child("heartdisease").getValue()).equals("null"));
                        highBp.setChecked(!String.valueOf(snapshot.child("highbp").getValue()).equals("null"));
                        migraine.setChecked(!String.valueOf(snapshot.child("migraine").getValue()).equals("null"));
                        stroke.setChecked(!String.valueOf(snapshot.child("stroke").getValue()).equals("null"));
                        ulcer.setChecked(!String.valueOf(snapshot.child("ulcer").getValue()).equals("null"));
                    }
                    else
                    {
                        Toast.makeText(getContext(), "Data don't exist", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getContext(), "Database load error", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Couldn't fetch medical history",e.toString());
            }
        });
        medicalListConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(asthma.isChecked())
                {
                    reference.child(user.getUid()).child("asthma").setValue("Asthma");
                }
                else
                {
                    reference.child(user.getUid()).child("asthma").setValue("null");
                }
                if(cancer.isChecked())
                {
                    reference.child(user.getUid()).child("cancer").setValue("Cancer");
                }
                else
                {
                    reference.child(user.getUid()).child("cancer").setValue("null");
                }
                if(diabetics.isChecked())
                {
                    reference.child(user.getUid()).child("diabetics").setValue("Diabetics");
                }
                else
                {
                    reference.child(user.getUid()).child("diabetics").setValue("null");
                }
                if(heartDisease.isChecked())
                {
                    reference.child(user.getUid()).child("heartdisease").setValue("Heart Disease");
                }
                else
                {
                    reference.child(user.getUid()).child("heartdisease").setValue("null");
                }
                if(highBp.isChecked())
                {
                    reference.child(user.getUid()).child("highbp").setValue("High BP");
                }
                else
                {
                    reference.child(user.getUid()).child("highbp").setValue("null");
                }
                if(migraine.isChecked())
                {
                    reference.child(user.getUid()).child("migraine").setValue("Migraine");
                }
                else
                {
                    reference.child(user.getUid()).child("migraine").setValue("null");
                }
                if(stroke.isChecked())
                {
                    reference.child(user.getUid()).child("stroke").setValue("Stroke");
                }
                else
                {
                    reference.child(user.getUid()).child("stroke").setValue("null");
                }
                if(ulcer.isChecked())
                {
                    reference.child(user.getUid()).child("ulcer").setValue("Ulcer");
                }
                else
                {
                    reference.child(user.getUid()).child("ulcer").setValue("null");
                }
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle("Done").setMessage("Medical History has been updated!");
                dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        bottomSheetDialogMedicalList.cancel();
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .detach(Profile.this)
                                .commitNowAllowingStateLoss();
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .attach(Profile.this)
                                .commitNow();
                    }
                }).setCancelable(false);
                dialog.create().show();

            }
        });
        bottomSheetDialogMedicalList.setContentView(bottomSheet);
        bottomSheetDialogMedicalList.show();
    }

    public void bloodList(){
        bottomSheetDialogBloodList = new BottomSheetDialog(getContext(),R.style.bottomSheetTheme);
        View bloodList = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_blood_group_list,null);
        aPositive = bloodList.findViewById(R.id.aPositive);
        bPositive = bloodList.findViewById(R.id.bPositive);
        oPositive = bloodList.findViewById(R.id.oPositive);
        abPositive = bloodList.findViewById(R.id.abPositive);
        aNegative = bloodList.findViewById(R.id.aNegative);
        bNegative = bloodList.findViewById(R.id.bNegative);
        oNegative = bloodList.findViewById(R.id.oNegative);
        abNegative = bloodList.findViewById(R.id.abNegative);
        bloodConfirmList =bloodList.findViewById(R.id.confirmList);
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("bloodgroup");
        reference.child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        DataSnapshot snapshot = task.getResult();
                        aPositive.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("A+"));
                        bPositive.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("B+"));
                        oPositive.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("O+"));
                        abPositive.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("AB+"));
                        aNegative.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("A-"));
                        bNegative.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("B-"));
                        oNegative.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("O-"));
                        abNegative.setChecked(String.valueOf(snapshot.child("group").getValue()).equals("AB-"));
                    }
                    else
                    {
                        Toast.makeText(getContext(), "Data don't exist", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getContext(), "Couldn't fetch blood info", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Database Connection Error",e.toString());
            }
        });
        bloodConfirmList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int count = 0;
                if(aPositive.isChecked() && bPositive.isChecked() | oPositive.isChecked() | aNegative.isChecked()
                        | bNegative.isChecked() | oNegative.isChecked() | abNegative.isChecked() | abPositive.isChecked())
                {
                    Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_SHORT).show();
                }
                else if(bPositive.isChecked() && aPositive.isChecked() | oPositive.isChecked() | aNegative.isChecked()
                        | bNegative.isChecked() | oNegative.isChecked() | abNegative.isChecked() | abPositive.isChecked())
                {
                    Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_LONG).show();
                }
                else if(oPositive.isChecked() && aPositive.isChecked() | bPositive.isChecked() | aNegative.isChecked()
                        | bNegative.isChecked() | oNegative.isChecked() | abNegative.isChecked() | abPositive.isChecked())
                {
                    Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_LONG).show();
                }
                else if(abPositive.isChecked()&& aPositive.isChecked() | aPositive.isChecked() | aNegative.isChecked()
                        | bNegative.isChecked() | oNegative.isChecked() | abNegative.isChecked() | bPositive.isChecked())
                {
                    Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_LONG).show();
                }
                else if(aNegative.isChecked() && aPositive.isChecked() | bPositive.isChecked() | oPositive.isChecked()
                        | bNegative.isChecked() | oNegative.isChecked() | abNegative.isChecked() | abPositive.isChecked())
                {
                    Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_LONG).show();
                }
                else if(bNegative.isChecked() && aPositive.isChecked() | bPositive.isChecked() | oPositive.isChecked()
                        | aNegative.isChecked() | oNegative.isChecked() | abNegative.isChecked() | abPositive.isChecked())
                {
                    Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_LONG).show();
                }
                else if(oNegative.isChecked() && aPositive.isChecked() | bPositive.isChecked() | oPositive.isChecked()
                        | aNegative.isChecked() | bNegative.isChecked() | abNegative.isChecked() | abPositive.isChecked())
                {
                    Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_LONG).show();
                }
                else if(abNegative.isChecked() && aPositive.isChecked() | bPositive.isChecked() | oPositive.isChecked()
                        | aNegative.isChecked() | bNegative.isChecked() | oNegative.isChecked() | abPositive.isChecked())
                {
                    Toast.makeText(getContext(), "Please Select only one", Toast.LENGTH_LONG).show();
                }
                else
                {
                    if(aPositive.isChecked())
                    {
                        reference.child(user.getUid()).child("group").setValue("A+");
                    }
                    else if(oPositive.isChecked())
                    {
                        reference.child(user.getUid()).child("group").setValue("O+");
                    }
                    else if(bPositive.isChecked())
                    {
                        reference.child(user.getUid()).child("group").setValue("B+");
                    }
                    else if(abPositive.isChecked())
                    {
                        reference.child(user.getUid()).child("group").setValue("AB+");
                    }
                    else if(aNegative.isChecked())
                    {
                        reference.child(user.getUid()).child("group").setValue("A-");
                    }
                    else if(bNegative.isChecked())
                    {
                        reference.child(user.getUid()).child("group").setValue("B-");
                    }
                    else if(oNegative.isChecked())
                    {
                        reference.child(user.getUid()).child("group").setValue("O-");
                    }
                    else if(abNegative.isChecked())
                    {
                        reference.child(user.getUid()).child("group").setValue("AB-");
                    }
                    else
                    {
                        reference.child(user.getUid()).child("group").setValue("null");
                    }
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                    dialog.setTitle("Done").setMessage("Blood Group has been updated!");
                    dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            bottomSheetDialogBloodList.cancel();
                            getActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .detach(Profile.this)
                                    .commitNowAllowingStateLoss();
                            getActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .attach(Profile.this)
                                    .commitNow();
                        }
                    }).setCancelable(false);
                    dialog.create().show();
                }
            }
        });
        bottomSheetDialogBloodList.setContentView(bloodList);
        bottomSheetDialogBloodList.show();
    }

    public void alertDialog(String title,String message,String button,String button2)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getContext(), "Email Sent",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to send",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).setNegativeButton(button2, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }
}