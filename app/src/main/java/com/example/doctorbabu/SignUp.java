package com.example.doctorbabu;

import static android.content.ContentValues.TAG;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.doctorbabu.Databases.userHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SignUp extends AppCompatActivity {
    private ImageView image;
    private TextView signupText,slogan;
    private TextInputLayout name, email, phone, pass, confirmPass,height,weight,dateofBirth,address,districtLayout,areaLayout;
    private TextInputEditText birthDate, passTextfield, fullNameTextfield, emailTextfield,phoneTextfield, heightTextfield,weightTextfield,confirmPasswordTextfield;
    private AutoCompleteTextView gender,district,area;
    private Button signup, signin, buttonDialog;
    private ProgressBar loading;
    private FirebaseAuth auth;
    private boolean validateFullName()
    {
        String value = name.getEditText().getText().toString();
        String check = "^[a-zA-Z\\u0020]*$";
        if(value.isEmpty())
        {
            name.setError("Field can't be empty");
            return false;
        }
        else if(!value.matches(check))
        {
            name.setError("Invalid Name");
            return false;
        }
        else
        {
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
            return true;
        }

    }
    private boolean validateUserAge()
    {
        String age = birthDate.getText().toString();
        if(age.equals("Enter Date of Birth"))
        {
            dateofBirth.setError("This field can't be empty");
            return false;
        }
        else
        {
            return true;
        }
    }
    private boolean validateUserHeight() {
        String userHeight = height.getEditText().getText().toString();
        if (userHeight.isEmpty()) {
            height.setError("Field can't be empty");
            return false;
        } else if (Float.parseFloat(userHeight) > 243.84) {
            height.setError("Invalid height!");
            return false;
        } else if (Float.parseFloat(userHeight) < 121.92) {
            height.setError("Invalid height");
            return false;
        } else{
            return true;
        }
    }
    private boolean validateUserWeight()
    {
        String userWeight = weight.getEditText().getText().toString();
        if(userWeight.isEmpty()) {
            weight.setError("Field can't be empty");
            return false;
        } else if(Float.parseFloat(userWeight) > 200.00){
            weight.setError("Invalid weight!");
            return false;
        } else if(Float.parseFloat(userWeight)  <  30.00 ){
            weight.setError("Invalid weight");
            return false;
        } else{
            return true;
        }
    }
    private boolean validateUserGender()
    {
        String userGender =  gender.getText().toString();
        if(userGender.isEmpty())
        {
            gender.setError("Field can't be empty");
            return false;
        } else{
            return true;
        }
    }
    private boolean validateUserDistrict()
    {
        String userDistrict =  district.getText().toString();
        if(userDistrict.isEmpty())
        {
            district.setError("Field can't be empty");
            return false;
        } else{
            return true;
        }
    }
    private boolean validateUserArea()
    {
        String userArea = area.getText().toString();
        if(userArea.isEmpty())
        {
            area.setError("Field can't be empty");
            return false;
        } else{
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        viewBinding();
        genderSelection();
        districtSelection();
        stateObserver();
        birthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDate();
            }
        });

    }
    public void viewBinding()
    {
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
        birthDate = findViewById(R.id.dateofBirth);
        dateofBirth = findViewById(R.id.birthDate);
        height = findViewById(R.id.height);
        weight = findViewById(R.id.weight);
        gender = findViewById(R.id.gender);
        passTextfield = findViewById(R.id.passTextfield);
        fullNameTextfield = findViewById(R.id.fullNameTextfield);
        emailTextfield = findViewById(R.id.emailTextfield);
        phoneTextfield = findViewById(R.id.phoneTextfield);
        heightTextfield = findViewById(R.id.heightTextfield);
        weightTextfield = findViewById(R.id.weightTextfield);
        confirmPasswordTextfield = findViewById(R.id.confirmPasswordTextfield);
        districtLayout = findViewById(R.id.districtLayout);
        district = findViewById(R.id.district);
        area = findViewById(R.id.area);
    }
    public void genderSelection()
    {
        String  [] items = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(SignUp.this,R.layout.drop_menu, items);
        gender.setAdapter(adapter);
    }
    public void districtSelection()
    {
        String [] items = {"Dhaka","Faridpur","Gazipur","Gopalganj","Jamalpur","Kishoreganj","Rajshahi",
                "Sherpur","Tangail ","Pabna"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(SignUp.this,R.layout.drop_menu,items);
        district.setAdapter(adapter);
    }
    public void areaSelection(String area) {
        if (area.equals("Dhaka")) {
            String[] items = {"Badda", "Mirpur", "Dhanmondi", "Mohammadpur", "Demra", "Gulshan", "Khilgaon",
                    "Khilkhet", "Ramna ", "Savar"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(SignUp.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        if(area.equals("Chittagong"))
        {
            String[] items = {"Mirsharai", "Mirsharai", "Patiya", "Raozan", "Sandwip", "Satkania",
                    "Sitakunda", "Banshkhali", "Boalkhali", "Chandanaish","Fatikchhari ","Hathazari ","Lohagara ","Pahartali",
                    "Bandarban","Patenga"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(SignUp.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
    }
    public void stateObserver()
    {
        fullNameTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                name.setError(null);
                name.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        emailTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                email.setError(null);
                email.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        phoneTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                phone.setError(null);
                phone.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        district.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                district.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
               areaSelection(district.getText().toString());
            }
        });
        area.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                area.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        heightTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                height.setError(null);
                height.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        weightTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                weight.setError(null);
                weight.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        passTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                pass.setError(null);
                pass.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        confirmPasswordTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                confirmPass.setError(null);
                confirmPass.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        birthDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                dateofBirth.setError(null);
                dateofBirth.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        gender.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                gender.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    public void getDate()
    {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis());
        String tempDate = formatter.format(date);
        String[] CurrentDate = tempDate.split("/");
        int currentYear = Integer.parseInt(CurrentDate[0]);
        int currentMonth = Integer.parseInt(CurrentDate[1]);
        int currentDay = Integer.parseInt(CurrentDate[2]);
        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = String.valueOf(year)+"/"+String.valueOf(month+1)+"/"+String.valueOf(dayOfMonth);
                birthDate.setText(date);
            }
        }, currentYear, currentMonth, currentDay);
        dialog.show();
    }
    public void register(View view)
    {
        if(!validateFullName() | !validateEmail() | !validatePassword() | !validateConfirmPassword()
                | !validatePhone() | !validateUserAge() | !validateUserHeight()
                    | !validateUserWeight() | !validateUserGender()| !validateUserArea() | !validateUserDistrict())
        {
            return;
        }
        String fullName = name.getEditText().getText().toString();
        String userEmail = email.getEditText().getText().toString();
        String userPhone = phone.getEditText().getText().toString();
        String userPass = pass.getEditText().getText().toString();
        String userBirthdate = birthDate.getText().toString();
        String userGender = gender.getText().toString();
        String userHeight = height.getEditText().getText().toString();
        String userWeight = weight.getEditText().getText().toString();
        String userDistrict = district.getText().toString();
        String userArea = area.getText().toString();
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
                                assert user != null;
                                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        String uid = user.getUid();
                                        database(fullName, userEmail, userPhone, uid, userBirthdate, userGender, userHeight,userWeight,userDistrict,userArea);
                                        miscellaneousAdd(user.getUid());
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

    public void database(String fullName, String userEmail, String userPhone, String uid, String userBirthdate, String userGender, String userHeight, String userWeight, String userDistrict, String userArea)
    {

        FirebaseDatabase rootNode = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = rootNode.getReference("users");
        userHelper addUser = new userHelper(fullName, userEmail ,userPhone,userBirthdate,userGender,userHeight,userWeight,userDistrict,userArea);
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
    public void miscellaneousAdd(String userId)
    {
        FirebaseDatabase rootnode = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = rootnode.getReference("allergy");
        reference.child(userId).child("cloth").setValue("null");
        reference.child(userId).child("drug").setValue("null");
        reference.child(userId).child("dust").setValue("null");
        reference.child(userId).child("food").setValue("null");
        reference = rootnode.getReference("medicalhistory");
        reference.child(userId).child("asthma").setValue("null");
        reference.child(userId).child("cancer").setValue("null");
        reference.child(userId).child("diabetics").setValue("null");
        reference.child(userId).child("heartdisease").setValue("null");
        reference.child(userId).child("highbp").setValue("null");
        reference.child(userId).child("migraine").setValue("null");
        reference.child(userId).child("stroke").setValue("null");
        reference = rootnode.getReference("bloodgroup");
        reference.child(userId).child("group").setValue("null");
        reference = rootnode.getReference("appointmentPatient");
        reference.child(userId).child("done").setValue("0");
        reference.child(userId).child("pending").setValue("0");
        reference = rootnode.getReference("rewardPatient");
        reference.child(userId).child("reward").setValue("50");
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