package com.example.doctorbabu;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.doctorbabu.Databases.userHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.net.PasswordAuthentication;

public class SignUp extends AppCompatActivity {
    private ImageView image;
    private TextView signupText,slogan;
    private TextInputLayout name, email, phone, pass, confirmPass;
    private Button signup, signin, buttonDialog;
    private ProgressBar loading;
    private FirebaseAuth auth;



    private boolean validateFullName()
    {
        String value = name.getEditText().getText().toString();
        if(value.isEmpty())
        {
            name.setError("Field can't be empty");
            return false;
        }
        else
        {
            name.setError(null);
            name.setErrorEnabled(false);
            return true;
        }
    }
    private boolean validateEmail()
    {
        String value = email.getEditText().getText().toString();
        String emailPattern = "[a-zA-z0-9._-]+@[a-z]+\\.+[a-z]+"; //regix expression
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
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        }
    }
    private boolean validatePhone()
    {
        String value = phone.getEditText().getText().toString();
        if(value.isEmpty())
        {
            phone.setError("Field can't be empty");
            return false;
        }
        else
        {
            phone.setError(null);
            phone.setErrorEnabled(false);
            return true;
        }
    }
    private boolean validatePassword()
    {
        String password = pass.getEditText().getText().toString();
        String passwordSpecial = "^" +"(?=.*[@#$%^&+=])"+".{3,}"+"$";
        if(password.isEmpty())
        {
            pass.setError("Password field can't be empty");
            return false;
        }
        else if(password.length() <= 6)
        {
            pass.setError("Password needs to be atleast 6 characters");
            return false;
        }
        else if(!password.matches(passwordSpecial))
        {
            pass.setError("Password is too weak. Add at least 1 special Character");
            return false;
        }
        else
        {
            pass.setError(null);
            pass.setErrorEnabled(false);
            return true;
        }
    }
    private boolean validateConfirmPassword()
    {
        String confirmPassword = confirmPass.getEditText().getText().toString();
        if(confirmPassword.isEmpty())
        {
            confirmPass.setError("Confirm password field can't be empty");
            return false;
        }
        else if(!pass.getEditText().getText().toString().matches(confirmPass.getEditText().getText().toString()))
        {
            confirmPass.setError("Password Didn't match");
            return false;
        }
        else
        {
            confirmPass.setError(null);
            confirmPass.setErrorEnabled(false);
            return true;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        name = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        pass = findViewById(R.id.password);
        confirmPass = findViewById(R.id.confirmPassword);
        signup = findViewById(R.id.signUp);
        signin = findViewById(R.id.signIn);
        signupText = findViewById(R.id.text1);
        slogan = findViewById(R.id.text3);
        image = findViewById(R.id.logo);
        loading = findViewById(R.id.progressCircular);
        auth = FirebaseAuth.getInstance();
    }
    public void register(View view)
    {
        if(!validateFullName() | !validateEmail() | !validatePassword() | !validateConfirmPassword() | !validatePhone())
        {
            return;
        }
        String fullName = name.getEditText().getText().toString();
        String userEmail = email.getEditText().getText().toString();
        String userPhone = phone.getEditText().getText().toString();
        String userPass = pass.getEditText().getText().toString();
        signup.setVisibility(View.GONE);
        signin.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_box);
        dialog.setCancelable(false);
        buttonDialog = dialog.findViewById(R.id.dialogButton);
        boolean internetStatus = isInternetAvailable(this);
        if(internetStatus)
        {
            auth.createUserWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = auth.getCurrentUser();
                                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        String uid = user.getUid();
                                        database(fullName, userEmail, userPhone, uid);
                                        loading.setVisibility(View.GONE);
                                        dialog.show();
                                        buttonDialog.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                dialog.dismiss();
                                                Intent intent = new Intent(SignUp.this, Login.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Email not sent" + e.getMessage());
                                    }
                                });


                            } else {
                                Toast.makeText(SignUp.this, "Registration failed.",
                                        Toast.LENGTH_SHORT).show();
                                signup.setVisibility(View.VISIBLE);
                                signin.setVisibility(View.VISIBLE);
                                loading.setVisibility(View.GONE);
                            }
                        }
                    });
        } else
        {
            loading.setVisibility(View.GONE);
            alertDialog();

        }

    }
    public void callSignIn(View view)
    {
        Intent intent = new Intent(SignUp.this,Login.class);
        Pair[] pairs = new Pair[7];
        pairs[0] = new Pair<View,String>(image,"imageTransition");
        pairs[1] = new Pair<View,String>(signupText,"textTransition1");
        pairs[2] = new Pair<View,String>(slogan,"textTransition2");
        pairs[3] = new Pair<View,String>(name,"usernameTransition");
        pairs[4] = new Pair<View,String>(pass,"passwordTransition");
        pairs[5] = new Pair<View,String>(signup,"signinTransition");
        pairs[6] = new Pair<View,String>(signin,"signupTransition");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SignUp.this,pairs);
        startActivity(intent,options.toBundle());

    }

    public void database(String fullName, String userEmail, String userPhone, String uid)
    {

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = rootNode.getReference("users");
        userHelper addUser = new userHelper(fullName, userEmail ,userPhone);
        reference.child(uid).setValue(addUser).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                return;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"Database Write Error");
            }
        });
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
    public void alertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this); builder.setTitle("Network Issue").setMessage("No internet Connection!").setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.cancel();
            signup.setVisibility(View.VISIBLE);
            signin.setVisibility(View.VISIBLE);
        }});
        builder.create().show();
    }

}