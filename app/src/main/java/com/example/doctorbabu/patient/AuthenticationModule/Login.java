package com.example.doctorbabu.patient.AuthenticationModule;

import static android.content.ContentValues.TAG;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.DoctorConsultationModule.Dashboard;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.processphoenix.ProcessPhoenix;

public class Login extends AppCompatActivity {
    Button callSignUp, signIn, forgetPass, confirm;
    ImageView image;
    TextView greetingText, secondGreetingText;
    TextInputLayout userEmail, userPassword, forgetEmail;
    ProgressBar progressBar;
    SwitchCompat languageSwitch;
    BottomSheetDialog bottomSheetForgetPass;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseUser user;
    boolean doctorCredentials;

    public static boolean isInternetAvailable(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        if (info == null) {
            Log.d(TAG, "no internet connection");
            return false;
        } else {
            if (info.isConnected()) {
                Log.d(TAG, " internet connection available...");
                return true;
            } else {
                Log.d(TAG, " internet connection");
                return true;
            }

        }
    }

    private boolean validateEmail() {
        String value = userEmail.getEditText().getText().toString();
        String emailPattern = "[a-zA-z0-9._-]+@[a-z]+\\.+[a-z]+"; //regix expression
        if (value.isEmpty()) {
            userEmail.setError("Field can't be empty");
            return false;
        } else if (!value.matches(emailPattern)) {
            userEmail.setError("Invalid email address");
            return false;
        } else {
            userEmail.setError(null);
            userEmail.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //hooking
        viewBinding();
        auth = FirebaseAuth.getInstance();
        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgetPassword();
            }
        });
        callSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logIn();
            }
        });
        languageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (languageSwitch.isChecked()) {
                    SharedPreferences preferences = getSharedPreferences("language", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("lang", "bn");
                    editor.apply();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
                    dialog.setTitle("Language Change").setMessage("Language is changing to Bangla, Please wait....").setCancelable(false);
                    dialog.create().show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            restart();
                        }
                    }, 1500);


                } else {
                    SharedPreferences preferences = getSharedPreferences("language", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("lang", "en");
                    editor.apply();
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
                    dialog.setTitle("Language Change").setMessage("Language is changing to English, Please wait....").setCancelable(false);
                    dialog.create().show();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            restart();
                        }
                    }, 1500);
                }
            }
        });
    }

    public void viewBinding() {
        callSignUp = findViewById(R.id.newUser);
        image = findViewById(R.id.logo);
        greetingText = findViewById(R.id.text1);
        secondGreetingText = findViewById(R.id.text2);
        userEmail = findViewById(R.id.email);
        userPassword = findViewById(R.id.password);
        signIn = findViewById(R.id.signIn);
        forgetPass = findViewById(R.id.forgetPass);
        progressBar = findViewById(R.id.progressCircular);
        languageSwitch = findViewById(R.id.languageSwitch);
        database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        String language = language();
        if (language.equals("bn")) {
            greetingText.setTextSize(26);
            secondGreetingText.setTextSize(24);
            languageSwitch.setChecked(true);
        }

    }

    public String language() {
        SharedPreferences preferences = getSharedPreferences("language", MODE_PRIVATE);
        return preferences.getString("lang", "");
    }

    public void forgetPassword() {
        bottomSheetForgetPass = new BottomSheetDialog(Login.this, R.style.bottomSheetTheme);
        View bottomSheetView = LayoutInflater.from(Login.this).inflate(R.layout.bottom_sheet_forget_password, null);
        forgetEmail = bottomSheetView.findViewById(R.id.forgetEmail);
        confirm = bottomSheetView.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = forgetEmail.getEditText().getText().toString();
                auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
                            dialog.setTitle("Done").setMessage("Password Reset Link has been sent to your email.").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            dialog.create().show();
                        } else {
                            AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
                            dialog.setTitle("Failed").setMessage("Failed to send email to this address").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            dialog.create().show();
                        }
                    }
                });
            }
        });
        bottomSheetForgetPass.setContentView(bottomSheetView);
        bottomSheetForgetPass.show();

    }

    public void logIn() {
        if (!validateEmail()) {
            return;
        } else {
            boolean internetStatus = isInternetAvailable(this);
            if (internetStatus) {
                progressBar.setVisibility(View.VISIBLE);
                String email = userEmail.getEditText().getText().toString().trim();
                String password = userPassword.getEditText().getText().toString().trim();
                firebaseUserCheck(email);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!doctorCredentials) {
                            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        final Handler handler = new Handler();
                                        FirebaseUser user = auth.getCurrentUser();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                assert user != null;
                                                if (user.isEmailVerified()) {
                                                    storeLoginInfo();
                                                    progressBar.setVisibility(View.GONE);
                                                    Intent intent = new Intent(Login.this, Dashboard.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    progressBar.setVisibility(View.GONE);
                                                    resendEmail("Email not verified", "Press resend button to get a email with verification link", false);
                                                }
                                            }
                                        }, 5000);

                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(Login.this, "Wrong Credentials", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Login.this, "Wrong Credentials", Toast.LENGTH_LONG).show();
                        }
                    }
                }, 1000);
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("No Internet").setMessage("Please check your internet connection and try again").setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        logIn();
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
    }

    public void firebaseUserCheck(String email) {
        DatabaseReference reference = database.getReference("doctorInfo");
        reference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                doctorCredentials = snapshot.exists();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void storeLoginInfo() {
        SharedPreferences preferences = getSharedPreferences("loginDetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("loginAs", "patient");
        editor.apply();
    }

    public void resendEmail(String title, String message, boolean cancelable) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(title).setMessage(message).setPositiveButton("Resend Email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                progressBar.setVisibility(View.VISIBLE);
                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, "Verification Email Sent!", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login.this, "Verification Email Sent Failed", Toast.LENGTH_LONG).show();
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

    public void signUp() {
        Intent intent = new Intent(Login.this, SignUp.class);
        Pair[] pairs = new Pair[7];
        pairs[0] = new Pair<View, String>(image, "imageTransition");
        pairs[1] = new Pair<View, String>(greetingText, "textTransition1");
        pairs[2] = new Pair<View, String>(secondGreetingText, "textTransition2");
        pairs[3] = new Pair<View, String>(userEmail, "usernameTransition");
        pairs[4] = new Pair<View, String>(userPassword, "passwordTransition");
        pairs[5] = new Pair<View, String>(signIn, "signinTransition");
        pairs[6] = new Pair<View, String>(callSignUp, "signupTransition");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Login.this, pairs);
        startActivity(intent, options.toBundle()); //Bundle is a class in android which is used to pass data from one activity to another activity within an android application.
    }

    public void onBackPressed() {
        Intent intent = new Intent(Login.this, LoginOptions.class);
        startActivity(intent);
        finish();
    }

    public void restart() {
        ProcessPhoenix.triggerRebirth(Login.this);
    }

}