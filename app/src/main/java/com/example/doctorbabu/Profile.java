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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Profile extends Fragment {

    FirebaseAuth auth;
    FirebaseUser user;
    TextView email,phone,address,gender,height,weight,age,verificationStatus,fullName,alergy,medicalInfo,bloodGroupInfo,alergyList;
    ImageView userprofilePicture,verifyTickSign,notVerifyImg;
    Button editProfile,alergyListConfirm;
    ProgressBar loadingCircle;
    FlexboxLayout flex,flex2;
    BottomSheetDialog bottomSheetDialog;
    CheckBox drug,cloth,dust,food;
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
        alergyList = (TextView) getView().findViewById(R.id.alergyHistoryInfo);
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
        alergyList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog = new BottomSheetDialog(getContext(),R.style.bottomSheetTheme);
                View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_alergylist,null);
                drug = bottomSheetView.findViewById(R.id.drug);
                dust = bottomSheetView.findViewById(R.id.dust);
                cloth = bottomSheetView.findViewById(R.id.cloth);
                food = bottomSheetView.findViewById(R.id.food);
                alergyListConfirm = bottomSheetView.findViewById(R.id.confirmList);
                drug.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(drug.isChecked())
                        {
                            Toast.makeText(getContext(), "Drug is checked", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
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
                        bloodGroupInfo.setText(String.valueOf(snapshot.child("group").getValue()));
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
                        String cloth = String.valueOf(snapshot.child("Cloth").getValue());
                        String drug = String.valueOf(snapshot.child("Drug").getValue());
                        ArrayList<String> list = new ArrayList<>();
                        if(!cloth.equals("null"))
                        {
                            list.add(cloth);
                        }
                        if(!drug.equals("null"))
                        {
                            list.add(drug);
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
                        String asthma = String.valueOf(snapshot.child("asthma").getValue());
                        String cancer = String.valueOf(snapshot.child("cancer").getValue());
                        String diabetics = String.valueOf(snapshot.child("diabetics").getValue());
                        String heartDisease = String.valueOf(snapshot.child("heartdisease").getValue());
                        String migrane = String.valueOf(snapshot.child("migrane").getValue());
                        String stroke = String.valueOf(snapshot.child("stroke").getValue());
                        String ulcer = String.valueOf(snapshot.child("ulcer").getValue());
                        String highBp = String.valueOf(snapshot.child("highbp").getValue());
                        if(!asthma.equals("null"))
                        {
                            medicalHistoryList.add(asthma);
                        }
                        if(!cancer.equals("null"))
                        {
                            medicalHistoryList.add(cancer);
                        }
                        if(!diabetics.equals("null"))
                        {
                            medicalHistoryList.add(diabetics);
                        }
                        if(!heartDisease.equals("null"))
                        {
                            medicalHistoryList.add(heartDisease);
                        }
                        if(!migrane.equals("null"))
                        {
                            medicalHistoryList.add(migrane);
                        }
                        if(!stroke.equals("null"))
                        {
                            medicalHistoryList.add(stroke);
                        }
                        if(!ulcer.equals("null"))
                        {
                            medicalHistoryList.add(ulcer);
                        }
                        if(!highBp.equals("null"))
                        {
                            medicalHistoryList.add(highBp);
                        }
                        Log.i("medical history output: ", String.valueOf(medicalHistoryList));
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