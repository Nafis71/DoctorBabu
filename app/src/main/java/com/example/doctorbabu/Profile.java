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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Profile extends Fragment {

    FirebaseAuth auth;
    FirebaseUser user;
    TextView email,phone,verificationStatus,fullName,alergy;
    ImageView userprofilePicture,verifyTickSign,notVerifyImg;
    Button editProfile,addUserPersonalHistory;
    ProgressBar loadingCircle;
    FlexboxLayout flex;
    ArrayList<String> alergies = new ArrayList<String>();
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
        email = (TextView) getView().findViewById(R.id.userEmail);
        userprofilePicture = (ImageView) getView().findViewById(R.id.profilePicture);
        verificationStatus = (TextView) getView().findViewById(R.id.verifyStatus);
        verifyTickSign = (ImageView) getView().findViewById(R.id.tickSign);
        notVerifyImg = (ImageView) getView().findViewById(R.id.notVerified);
        editProfile = (Button) getView().findViewById(R.id.editProfile);
        fullName = (TextView) getView().findViewById(R.id.userName);
        flex = (FlexboxLayout) getView().findViewById(R.id.alergyHistory);
        alergy =(TextView) getView().findViewById(R.id.alergy);
        userAlergyHistory();
//        phone = (TextView) getView().findViewById(R.id.userPhone);
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
//                        phone.setText(String.valueOf(snapshot.child("phone").getValue()));
//                        address.setText(String.valueOf(snapshot.child("address").getValue()));
//                        age.setText(String.valueOf(snapshot.child("age").getValue()));
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
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"Database Read Error");
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
          alergy.setVisibility(View.VISIBLE);
      }

//      TextView text = new TextView(getContext());
//      TextView text2 = new TextView(getContext());
//    FlexboxLayout.LayoutParams layout =
//              new FlexboxLayout.LayoutParams(
//                      FlexboxLayout.LayoutParams.WRAP_CONTENT,
//                      FlexboxLayout.LayoutParams.WRAP_CONTENT);
//
//      layout.setLayoutDirection(FlexDirection.ROW);
//      layout.setMarginStart(10);
//      text.setLayoutParams(layout);
//      text2.setLayoutParams(layout);
//      text.setText(cloth);
//      text2.setText(drug);
//      flex.addView(text);
//      flex.addView(text2);

//// Right subregions contains a Textview
//      TextView tv = new TextView(getContext());
//      TextView tv2 = new TextView(getContext());
//      tv.setLayoutParams(lpRight);
//      tv2.setLayoutParams(lpRight);
//      tv.setText("TV");
//      tv.remove
//      tv2.setText("TV2");
//      flex.addView(tv);
//      flex.addView(tv2);
//      flex.remo


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