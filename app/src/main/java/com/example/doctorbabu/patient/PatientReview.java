package com.example.doctorbabu.patient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.doctorbabu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.UUID;

public class PatientReview extends AppCompatActivity {
    LottieAnimationView reviewAnimation;
    TextView reviewText;
    RatingBar ratingBar;
    TextInputEditText reviewTextField;
    TextInputLayout reviewTextInputLayout;
    Button postReview;
    RelativeLayout parentLayout;
    LinearProgressIndicator progressRail;
    String doctorId;
    int consultancyDone;
    int rewardPoint;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_review);
        doctorId = getIntent().getStringExtra("doctorId");
        viewBinding();
        loadReward();
        loadConsultancyPatient();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reviewAnimation.setVisibility(View.VISIBLE);
                reviewText.setVisibility(View.VISIBLE);
                ratingBar.setVisibility(View.VISIBLE);
                reviewTextField.setVisibility(View.VISIBLE);
                reviewTextInputLayout.setVisibility(View.VISIBLE);
                postReview.setVisibility(View.VISIBLE);
            }
        }, 5000);
        reviewTextField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                reviewTextField.setGravity(Gravity.START);
            }
        });
        postReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                post();
            }
        });
    }

    public void viewBinding() {
        reviewAnimation = findViewById(R.id.reviewAnimation);
        reviewText = findViewById(R.id.reviewText);
        ratingBar = findViewById(R.id.ratingBar);
        reviewTextField = findViewById(R.id.reviewTextField);
        reviewTextInputLayout = findViewById(R.id.reviewTextInputLayout);
        postReview = findViewById(R.id.postReview);
        parentLayout = findViewById(R.id.parentLayout);
        progressRail = findViewById(R.id.progressRail);
    }

    public void post() {
        if (!validateRatingBar()) {
            return;
        }
        progressRail.setVisibility(View.VISIBLE);
        String review = reviewTextField.getText().toString().trim();
        float rating = ratingBar.getRating();

        String uniqueID = getUniqueId();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        HashMap<String, Object> reviewData = new HashMap<>();
        assert user != null;
        if (review.isEmpty()) {
            reviewData.put("userId", user.getUid());
            reviewData.put("rating", rating);
            reviewData.put("comment", "null");
        } else {
            reviewData.put("userId", user.getUid());
            reviewData.put("rating", rating);
            reviewData.put("comment", review);
        }
        DatabaseReference reference = database.getReference("reviews");
        reference.child(doctorId).child(uniqueID).setValue(reviewData);
        reference = database.getReference("consultancyPatient");
        reference.child(user.getUid()).child("done").setValue(consultancyDone);
        Snackbar.make(parentLayout, "You have awarded 50 reward points", Snackbar.LENGTH_LONG).show();
        reference = database.getReference("rewardPatient");
        reference.child(user.getUid()).child("reward").setValue(String.valueOf(rewardPoint));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressRail.setVisibility(View.GONE);
                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(PatientReview.this);
                dialog.setTitle("Successful").setIcon(R.drawable.done)
                        .setMessage("Your review has been recorded,thanks!")
                        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        }).setCancelable(false);
                dialog.create().show();
            }
        }, 4000);
    }

    public void loadReward() {

        DatabaseReference reference = database.getReference("rewardPatient");
        reference.child(user.getUid()).child("reward").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String value = String.valueOf(snapshot.getValue());
                    rewardPoint = Integer.parseInt(value) + 50;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void loadConsultancyPatient() {
        DatabaseReference reference = database.getReference("consultancyPatient");
        reference.child(user.getUid()).child("done").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String value = String.valueOf(snapshot.getValue());
                    consultancyDone = Integer.parseInt(value) + 1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public boolean validateRatingBar() {
        float rating = ratingBar.getRating();
        if (rating == 0.0) {
            Snackbar.make(parentLayout, "Please Give a Review first to post your comment", Snackbar.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    public String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    public void onBackPressed() {
        DatabaseReference reference = database.getReference("consultancyPatient");
        reference.child(user.getUid()).child("done").setValue(consultancyDone);
        reference = database.getReference("rewardPatient");
        reference.child(user.getUid()).child("reward").setValue(String.valueOf(rewardPoint));
        finish();
    }
}