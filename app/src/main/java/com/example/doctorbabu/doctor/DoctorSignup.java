package com.example.doctorbabu.doctor;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.DatePickerDialog;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.doctorbabu.Databases.doctorHelper;
import com.example.doctorbabu.R;
import com.google.android.gms.tasks.OnCompleteListener;
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

import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Date;
import java.util.HashMap;

public class DoctorSignup extends AppCompatActivity {
    TextView signUpText,quoteText;
    TextInputLayout emailLayout, passwordLayout,fullNameLayout,titleLayout,bmdcLayout,nidLayout,confirmPasswordLayout,dateofBirthLayout;
    TextInputEditText fullName,email,bmdc,nid,confirmPassword,password,dateofBirth;
    Button signUp,signIn;
    AutoCompleteTextView title, district, area, doctorType, gender;
    ProgressBar progressCircular;
    FirebaseAuth auth;
    boolean nidExist, bmdcExist, nidAlreadyRegistered,bmdcAlreadyRegistered;
    Integer doctorID;
    String tempDocID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_signup);
        viewBinding();
        stateObserver();
        loadTitle();
        loadDistrict();
        loadDoctorType();
        loadGender();
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callSignIn(view);
            }
        });
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
        dateofBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCalender();
            }
        });
    }
    public void viewBinding()
    {
        signIn = findViewById(R.id.signIn);
        signUp = findViewById(R.id.signUp);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        fullNameLayout = findViewById(R.id.fullNameLayout);
        signUpText = findViewById(R.id.text1);
        quoteText = findViewById(R.id.text3);
        title = findViewById(R.id.title);
        district = findViewById(R.id.district);
        area = findViewById(R.id.area);
        doctorType = findViewById(R.id.doctorType);
        gender =  findViewById(R.id.gender);
        titleLayout = findViewById(R.id.titleLayout);
        bmdcLayout = findViewById(R.id.bmdcLayout);
        nidLayout = findViewById(R.id.nidLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        fullName = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        bmdc = findViewById(R.id.bmdc);
        nid = findViewById(R.id.nid);
        confirmPassword = findViewById(R.id.confirmPassword);
        password = findViewById(R.id.password);
        dateofBirth = findViewById(R.id.dateofBirth);
        dateofBirthLayout = findViewById(R.id.dateofBirthLayout);
        auth = FirebaseAuth.getInstance();
        progressCircular = findViewById(R.id.progressCircular);
    }
    public void stateObserver()
    {
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                title.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        fullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                fullNameLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                emailLayout.setError(null);
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
                    area.setText(null);
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
        dateofBirth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                dateofBirthLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        doctorType.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                doctorType.setError(null);
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
        bmdc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                bmdcLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        nid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                nidLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                passwordLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                confirmPasswordLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }
    public void loadCalender()
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
                dateofBirth.setText(date);
            }
        }, currentYear, currentMonth, currentDay);
        dialog.show();
    }
    public void loadTitle()
    {
        String items [] = {"Dr.","Prof. Dr.","Assoc. Prof. Dr.","Asst. Prof. Dr."};
        ArrayAdapter<String> adapter =  new ArrayAdapter<>(DoctorSignup.this,R.layout.drop_menu,items);
        title.setAdapter(adapter);
    }
    public void loadDistrict()
    {
        String [] items = {"Dhaka","Chittagong","Gazipur","Barishal","Jamalpur","Khulna","Rajshahi","Sherpur","Sylhet ","Rangpur"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorSignup.this,R.layout.drop_menu,items);
        district.setAdapter(adapter);
    }
    public void areaSelection(String area) {
        if (area.equals("Dhaka")) {
            String[] items = {"Badda", "Mirpur", "Dhanmondi", "Mohammadpur", "Demra", "Gulshan", "Khilgaon",
                    "Khilkhet", "Ramna ", "Savar"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorSignup.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Chittagong"))
        {
            String[] items = {"Mirsharai", "Mirsharai", "Patiya", "Raozan", "Sandwip", "Satkania",
                    "Sitakunda", "Banshkhali", "Boalkhali", "Chandanaish","Fatikchhari ","Hathazari ","Lohagara ","Pahartali",
                    "Bandarban","Patenga"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorSignup.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Gazipur"))
        {
            String[] items = {"Gazipur Sadar", "Kapasia", "Tongi town", "Sripur", "Kaliganj", "Kaliakior"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorSignup.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Barishal"))
        {
            String[] items = {"Barishal Sadar", "Banaripara", "Bakerganj", "Babuganj", "Gaurnadi", "Hizla",
                    "Mehendiganj", "Agailjhara", "Wazirpur", "Muladi"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorSignup.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Jamalpur"))
        {
            String[] items = {"Jamalpur Sadar", "Baksiganj ", "Dewanganj", "Islampur", "Madarganj", "Sarishabari"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorSignup.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Khulna"))
        {
            String[] items = {"Dumuria", "Batiaghata ", "Dacope", "Phultala", "Dighalia", "Koyra","Terokhada","Rupsha","Paikgachha"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorSignup.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Rajshahi"))
        {
            String[] items = {"Godagari", "Tanore", "Mohanpur", "Bagmara", "Durgapur", "Bagmara","Bagha","Charghat","Puthia","Paba "};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorSignup.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Sherpur"))
        {
            String[] items = {"Sherpur Sadar", "Nakla", "Sreebardi", "Nalitabari", "Jhenaigati"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorSignup.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Sylhet"))
        {
            String[] items = {"Sylhet Sadar", "Beanibazar", "Golapganj", "Companiganj", "Fenchuganj","Bishwanath","Bishwanath","Jaintiapur","Kanaighat","Balaganj"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorSignup.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Rangpur"))
        {
            String[] items = {"Rangpur Sadar", "Badarganj", "Kaunia", "Gangachhara", "Mithapukur","Taraganj","Pirganj","Pirgachha"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorSignup.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
    }
    public void loadDoctorType()
    {
        String[] items = {"Medical","Dental"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorSignup.this, R.layout.drop_menu, items);
        doctorType.setAdapter(adapter);
    }
    public void loadGender()
    {
        String  [] items = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(DoctorSignup.this,R.layout.drop_menu, items);
        gender.setAdapter(adapter);
    }

    public void signUp()
    {
        if(!validateNid() | !validateArea() | !validateBmdc() | !validateDistrict() | !validateEmail() | !validateGender() | !validateFullName()
        | !validateDoctorType() |!validateConfirmPassword() | !validatePassword() |!validateTitle() | !validateDateOfBirth())
        {
            return;
        }
        progressCircular.setVisibility(View.VISIBLE);
        String title = this.title.getText().toString();
        String fullName =  this.fullName.getText().toString();
        String email =  this.email.getText().toString();
        String district = this.district.getText().toString();
        String area =  this.area.getText().toString();
        String doctorType =  this.doctorType.getText().toString();
        String gender = this.gender.getText().toString();
        String bmdc = this.bmdc.getText().toString();
        String nid = this.nid.getText().toString();
        String dateofBirth = this.dateofBirth.getText().toString();
        String password = this.password.getText().toString();
        boolean netStatus = isInternetAvailable(DoctorSignup.this);
        if(netStatus)
        {
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        generateDoctorID();
                        FirebaseUser user = auth.getCurrentUser();
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                String doctorId=tempDocID;
                                String uId = user.getUid();
                                doctorHelper doctorHelper =  new doctorHelper(uId,doctorId,title,fullName,email,district,area,doctorType,gender,bmdc,nid,dateofBirth);
                                FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
                                DatabaseReference reference = database.getReference("doctorInfo");
                                reference.child(doctorId).setValue(doctorHelper);
                                reference = database.getReference("doctorID");
                                reference.child("id").setValue(String.valueOf(doctorID));
                                reference = database.getReference("callRoom");
                                HashMap<String,Object> roomSetup = new HashMap<>();
                                roomSetup.put("incoming","null"); roomSetup.put("isAvailable",false);
                                roomSetup.put("status",0);
                                reference.child(doctorId).setValue(roomSetup);
                                storeLoginInfo();
                                progressCircular.setVisibility(View.GONE);
                                AlertDialog.Builder dialog =  new AlertDialog.Builder(DoctorSignup.this);
                                //TODO Need to add email verification here;
                                dialog.setTitle("Account Created!").setMessage("Account created successfully").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        auth.signOut();
                                        Intent intent = new Intent(DoctorSignup.this, DoctorLogin.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }).setCancelable(false);
                                dialog.create().show();
                            }
                        },1000);
                    }
                }
            });
        }
        else
        {
            AlertDialog.Builder dialog =  new AlertDialog.Builder(DoctorSignup.this);
            dialog.setTitle("Connection Error").setMessage("Please Check your internet connection and try again").setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            dialog.create().show();
        }


    }

    public void generateDoctorID()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("doctorID");
        reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        DataSnapshot snapshot = task.getResult();
                        String docID = String.valueOf(snapshot.child("id").getValue());
                        Log.i("ID : ",docID);
                        tempDocID = "DB" + docID;
                        doctorID = Integer.parseInt(docID) + 1;
                    }
                }
            }
        });
    }
    public void storeLoginInfo()
    {
        SharedPreferences preferences = getSharedPreferences("loginDetails",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("loginAs","doctor");
        editor.apply();
    }

    public void callSignIn(View view) {
        Intent intent = new Intent(DoctorSignup.this, DoctorLogin.class);
        Pair[] pairs = new Pair[7];
        pairs[0] = new Pair<View, String>(quoteText, "textTransition2");
        pairs[1] = new Pair<View, String>(signUpText, "textTransition1");
        pairs[2] = new Pair<View, String>(emailLayout, "textTransition2");
        pairs[3] = new Pair<View, String>(fullNameLayout, "usernameTransition");
        pairs[4] = new Pair<View, String>(passwordLayout, "passwordTransition");
        pairs[5] = new Pair<View, String>(signUp, "signinTransition");
        pairs[6] = new Pair<View, String>(signIn, "signupTransition");
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(DoctorSignup.this, pairs);
        startActivity(intent, options.toBundle());
    }
    private boolean validateFullName()
    {
        String value = fullName.getText().toString();
        String check = "^[a-zA-Z\\u0020]*$";
        if(value.isEmpty())
        {
            fullNameLayout.setError("Field can't be empty");
            return false;
        }
        else if(!value.matches(check))
        {
            fullNameLayout.setError("Invalid Name");
            return false;
        }
        else
        {
            return true;
        }
    }
    private boolean validateEmail()
    {
        String value = email.getText().toString();
        String emailPattern = "[a-zA-z0-9._-]+@[a-z]+\\.+[a-z]+"; //regex expression
        if(value.isEmpty())
        {
            emailLayout.setError("Field can't be empty");
            return false;
        }
        else if(!value.matches(emailPattern))
        {
            emailLayout.setError("Invalid email address");
            return false;
        }
        else
        {
            return true;
        }
    }
    private boolean validatePassword()
    {
        String pass = password.getText().toString();
        String passwordSpecial = "^" +"(?=.*[@#$%^&+=])"+".{3,}"+"$";
        if(pass.isEmpty())
        {
            passwordLayout.setError("Password field can't be empty");
            return false;
        }
        else if(pass.length() <= 6)
        {
            passwordLayout.setError("Password needs to be atleast 6 characters");
            return false;
        }
        else if(!pass.matches(passwordSpecial))
        {
            passwordLayout.setError("Password is too weak. Add at least 1 special Character");
            return false;
        }
        else
        {
            return true;
        }
    }
    private boolean validateConfirmPassword()
    {
        String confirmPass = confirmPassword.getText().toString();
        if(confirmPass.isEmpty())
        {
            confirmPasswordLayout.setError("Confirm password field can't be empty");
            return false;
        }
        else if(!password.getText().toString().matches(confirmPassword.getText().toString()))
        {
            confirmPasswordLayout.setError("Password Didn't match");
            return false;
        }
        else
        {
            return true;
        }

    }
    private boolean validateDateOfBirth()
    {
        String value = dateofBirth.getText().toString();
        String [] splitText = value.split("/");
        int year = Integer.parseInt(splitText[0]);
        Year thisYear = Year.now();
        String thisYearString = thisYear.toString();
        int currentAge =  Integer.parseInt(thisYearString) - year - 1;
        if(value.equals("Birth Date"))
        {
            dateofBirthLayout.setError("Please choose a date");
            return false;
        }
        else if (currentAge < 18)
        {
            dateofBirthLayout.setError("Applicant must be over 18");
            return false;
        }
        else
        {
            return true;
        }

    }
    private boolean validateGender()
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
    private boolean validateDistrict()
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
    private boolean validateArea()
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
    private boolean validateDoctorType()
    {
        String type = doctorType.getText().toString();
        if(type.isEmpty())
        {
            doctorType.setError("Field can't be empty");
            return false;
        } else{
            return true;
        }
    }
    private boolean validateBmdc()
    {
        String value = bmdc.getText().toString();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("bmdc");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(value).exists())
                {
                    bmdcExist = true;
                }
                else
                {
                    bmdcLayout.setError("Invalid BMDC Number");
                    bmdcExist = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        reference = database.getReference("doctorInfo");
        reference.orderByChild("bmdc").equalTo(value).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    bmdcLayout.setError("Already Registered");
                    bmdcAlreadyRegistered = true;
                }
                else
                {
                    bmdcAlreadyRegistered = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(value.isEmpty())
        {
            bmdcLayout.setError("Field can't be empty");
            return false;
        }
        else if(!bmdcExist)
        {
            return false;
        }
        else if(bmdcAlreadyRegistered)
        {
            return false;
        }
        else{
            return true;
        }
    }
    private boolean validateNid()
    {
        String value = nid.getText().toString();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference reference = database.getReference("nid");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(value).exists())
                {
                    nidExist = true;
                }
                else
                {
                    nidLayout.setError("Invalid NID Number");
                    nidExist = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        reference = database.getReference("doctorInfo");
        reference.orderByChild("nid").equalTo(value).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    nidLayout.setError("Already Registered");
                    nidAlreadyRegistered = true;
                }
                else
                {
                    nidAlreadyRegistered = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(value.isEmpty())
        {
            nidLayout.setError("Field can't be empty");
            return false;
        }
        else if(!nidExist)
        {
            return false;
        }
        else if(nidAlreadyRegistered)
        {
            return false;
        }
        else{
            return true;
        }
    }
    private boolean validateTitle()
    {
        String value = title.getText().toString();
        if(value.isEmpty())
        {
            title.setError("Field can't be empty");
            return false;
        }
        else
        {
            return true;
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
}