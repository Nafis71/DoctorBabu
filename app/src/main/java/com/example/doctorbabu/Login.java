package com.example.doctorbabu;

import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    Button callSignUp,signIn;
    ImageView image;
    TextView greetingText, secondGreetingText, forgetPass;
    TextInputLayout userEmail,userPassword;
    ProgressBar progressBar;
    FirebaseAuth auth;
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            if(currentUser.isEmailVerified())
            {
                Intent intent = new Intent(Login.this,Dashboard.class);
                startActivity(intent);
                finish();
            }
        }
    }
    private boolean validateEmail()
    {
        String value = userEmail.getEditText().getText().toString();
        String emailPattern = "[a-zA-z0-9._-]+@[a-z]+\\.+[a-z]+"; //regix expression
        if(value.isEmpty())
        {
            userEmail.setError("Field can't be empty");
            return false;
        }
        else if(!value.matches(emailPattern))
        {
            userEmail.setError("Invalid email address");
            return false;
        }
        else
        {
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
        callSignUp = findViewById(R.id.newUser);
        image = findViewById(R.id.logo);
        greetingText = findViewById(R.id.text1);
        secondGreetingText = findViewById(R.id.text2);
        userEmail = findViewById(R.id.email);
        userPassword = findViewById(R.id.password);
        signIn = findViewById(R.id.signIn);
        forgetPass = findViewById(R.id.forgetPass);
        progressBar = findViewById(R.id.progressCircular);
        auth = FirebaseAuth.getInstance();
        callSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this,SignUp.class);
                Pair[] pairs = new Pair[7];
                pairs[0] = new Pair<View,String>(image, "imageTransition");
                pairs[1] = new Pair<View,String>(greetingText, "textTransition1");
                pairs[2] = new Pair<View,String>(secondGreetingText, "textTransition2");
                pairs[3] = new Pair<View,String>(userEmail, "usernameTransition");
                pairs[4] = new Pair<View,String>(userPassword, "passwordTransition");
                pairs[5] = new Pair<View,String>(signIn, "signinTransition");
                pairs[6] = new Pair<View,String>(callSignUp, "signupTransition");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Login.this,pairs);
                startActivity(intent, options.toBundle()); //Bundle is a class in android which is used to pass data from one activity to another activity within an android application.


            }
        });

    }
    public void logIn(View view)
    {
        if(!validateEmail())
        {
            return;
        }
        else
        {
            boolean internetStatus = isInternetAvailable(this);
            if(internetStatus) {
                progressBar.setVisibility(View.VISIBLE);
                String email = userEmail.getEditText().getText().toString();
                String password = userPassword.getEditText().getText().toString();
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = auth.getCurrentUser();
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(user.isEmailVerified())
                                            {
                                                progressBar.setVisibility(View.GONE);
                                                Intent intent = new Intent(Login.this, Dashboard.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                            else
                                            {
                                                progressBar.setVisibility(View.GONE);
                                                AlertDialog.Builder dialog = new AlertDialog.Builder(Login.this);
                                                dialog.setTitle("Email Not Verified").setMessage("Please verify your email first");
                                                dialog.setPositiveButton("Resend Email", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if(internetStatus) {
                                                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    dialog.cancel();
                                                                    Toast.makeText(Login.this, "Email Sent!", Toast.LENGTH_LONG).show();
                                                                }
                                                            });
                                                        } else {
                                                            positiveAlertDialog("No Internet Connection","Please Check your internet connection and try again","Ok");
                                                        }
                                                    }
                                                });
                                                dialog.create().show();
                                            }
                                        }
                                    },6000);



                                    } else {
                                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                                Toast.makeText(Login.this, "Wrong Credentials",
                                                        Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                    }
                            }
                        });
            }
            else
            {
                positiveAlertDialog("Network Issue","No internet Connection!","Ok");
            }

        }
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
    public void positiveAlertDialog(String title,String message,String button)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }


}