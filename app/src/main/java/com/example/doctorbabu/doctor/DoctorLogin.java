package com.example.doctorbabu.doctor;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doctorbabu.LoginOptions;
import com.example.doctorbabu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DoctorLogin extends AppCompatActivity {
    TextInputLayout email,password;
    TextInputEditText emailText;
    TextView greetingText,secondGreetingText;
    ImageView image;
    Button signIn,signUp,forgetPass;
    FirebaseAuth auth;
    FirebaseUser user;
    ProgressBar progressCircular;
    FirebaseDatabase database;
    boolean patientCredential;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_login);
        viewBinding();
        stateObserver();
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callSignUp(view);
            }
        });
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }
    public void viewBinding()
    {
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        forgetPass = findViewById(R.id.forgetPass);
        signUp = findViewById(R.id.newUser);
        image = findViewById(R.id.logo);
        greetingText = findViewById(R.id.text1);
        secondGreetingText = findViewById(R.id.text2);
        progressCircular = findViewById(R.id.progressCircular);
        emailText = findViewById(R.id.emailText);
        signIn = findViewById(R.id.signIn);
        auth =  FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
    }
    public void stateObserver()
    {
        emailText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                email.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    public void signIn()
    {
        if(!validateEmail())
        {
            return;
        }
        if(isInternetAvailable(this)) {
            progressCircular.setVisibility(View.VISIBLE);
            String email = this.email.getEditText().getText().toString();
            String password = this.password.getEditText().getText().toString();
            firebaseUserCheck(email);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!patientCredential)
                    {
                        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    final Handler handler = new Handler();
                                    user = auth.getCurrentUser();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (user.isEmailVerified()) {
                                                SharedPreferences preferences = getSharedPreferences("loginDetails", MODE_PRIVATE);
                                                SharedPreferences.Editor editor = preferences.edit();
                                                editor.putString("loginAs", "doctor");
                                                editor.apply();
                                                progressCircular.setVisibility(View.GONE);
                                                Intent intent = new Intent(DoctorLogin.this, DoctorDashboard.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                progressCircular.setVisibility(View.GONE);
                                                resendEmail("Email not verified", "Press resend button to get a email with verification link", false);
                                            }
                                        }
                                    }, 5000);


                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressCircular.setVisibility(View.GONE);
                                Toast.makeText(DoctorLogin.this, "Wrong Credentials", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else
                    {
                        progressCircular.setVisibility(View.GONE);
                        Toast.makeText(DoctorLogin.this, "Wrong Credentials", Toast.LENGTH_LONG).show();
                    }
                }
            },1000);

        }
        else
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("No Internet").setMessage("Please check your internet connection and try again").setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    signIn();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialog.create().show();
        }
    }
    public void firebaseUserCheck(String email)
    {
        DatabaseReference reference = database.getReference("users");
        reference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    patientCredential = true;
                }
                else
                {
                    patientCredential = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private boolean validateEmail()
    {
        String value = email.getEditText().getText().toString();
        String emailPattern = "[a-zA-z0-9._-]+@[a-z]+\\.+[a-z]+"; //regex expression
        if(value.isEmpty())
        {
            email.setError("Field can't be empty");
            return false;
        }
        else if(!value.matches(emailPattern))
        {
            email.setError("Invalid email address");
            return false;
        }
        else
        {
            return true;
        }
    }
    public void resendEmail(String title, String message, boolean cancelable){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title).setMessage(message).setPositiveButton("Resend Email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                progressCircular.setVisibility(View.VISIBLE);
                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressCircular.setVisibility(View.GONE);
                        Toast.makeText(DoctorLogin.this, "Verification Email Sent!", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressCircular.setVisibility(View.GONE);
                        Toast.makeText(DoctorLogin.this, "Verification Email Sent Failed", Toast.LENGTH_LONG).show();
                    }
                });

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).setCancelable(cancelable);
        dialog.create().show();
    }
    public static boolean isInternetAvailable(Context context)
    {
        NetworkInfo info = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null)
        {
            Log.d(TAG,"no internet connection");
            return false;
        }
        else
        {
            if(info.isConnected())
            {
                Log.d(TAG," internet connection available...");
                return true;
            }
            else
            {
                Log.d(TAG," internet connection");
                return true;
            }

        }
    }
    public void callSignUp(View view)
    {
        Intent intent = new Intent(DoctorLogin.this, DoctorSignup.class);
        Pair[] pairs = new Pair[7];
        pairs[0] = new Pair<View,String>(image, "imageTransition");
        pairs[1] = new Pair<View,String>(greetingText, "textTransition1");
        pairs[2] = new Pair<View,String>(secondGreetingText, "textTransition2");
        pairs[3] = new Pair<View,String>(email, "usernameTransition");
        pairs[4] = new Pair<View,String>(password, "passwordTransition");
        pairs[5] = new Pair<View,String>(signIn, "signinTransition");
        pairs[6] = new Pair<View,String>(signUp, "signupTransition");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(DoctorLogin.this,pairs);
        startActivity(intent, options.toBundle());
    }
    public void onBackPressed()
    {
        Intent intent = new Intent(DoctorLogin.this, LoginOptions.class);
        startActivity(intent);
        finish();
    }
}