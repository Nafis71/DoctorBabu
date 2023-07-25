package com.example.doctorbabu.doctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.doctorbabu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class doctorAboutYou extends AppCompatActivity {
    TextInputLayout aboutyouLayout;
    TextInputEditText aboutyou;
    ImageView goBack;
    String doctorId;
    Button confirmList, clear;
    ProgressBar progressCircular;
    LinearLayout parentLayout;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_about_you);
        viewBinding();
        loadData();
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        confirmList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                aboutyou.setText(null);
            }
        });
        stateObserver();
    }

    public void viewBinding() {
        aboutyouLayout = findViewById(R.id.aboutyouLayout);
        aboutyou = findViewById(R.id.aboutyou);
        goBack = findViewById(R.id.goBack);
        confirmList = findViewById(R.id.confirmList);
        parentLayout = findViewById(R.id.parentLayout);
        progressCircular = findViewById(R.id.progressCircular);
        clear = findViewById(R.id.clear);
    }

    public void stateObserver() {
        aboutyou.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                aboutyouLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void loadData() {
        doctorId = getIntent().getStringExtra("DoctorId");
        DatabaseReference reference = database.getReference("doctorInfo");
        reference.child(doctorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !String.valueOf(snapshot.child("about").getValue()).equals("null")) {
                    aboutyou.setText(String.valueOf(snapshot.child("about").getValue()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    private boolean validateAboutYou() {
        String value = aboutyou.getText().toString().trim();
        if (value.isEmpty()) {
            aboutyouLayout.setError("Field can't be empty");
            return false;
        } else if (value.length() > 5500) {
            aboutyouLayout.setError("Limit exceeded");
            return false;
        } else {
            return true;
        }
    }

    public void saveData() {
        if (!validateAboutYou()) {
            return;
        }
        progressCircular.setVisibility(View.VISIBLE);
        confirmList.setVisibility(View.GONE);
        String about = aboutyou.getText().toString().trim();
        DatabaseReference reference = database.getReference("doctorInfo");
        reference.child(doctorId).child("about").setValue(about).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(parentLayout, "Information Updated", Snackbar.LENGTH_LONG).show();
                        progressCircular.setVisibility(View.GONE);
                        confirmList.setVisibility(View.VISIBLE);
                    }
                }, 2000);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(parentLayout, "Failed to update", Snackbar.LENGTH_LONG).show();
                progressCircular.setVisibility(View.GONE);
                confirmList.setVisibility(View.VISIBLE);
            }
        });

    }
}