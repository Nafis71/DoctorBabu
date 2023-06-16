package com.example.doctorbabu;

import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.Databases.userHelper;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditProfile extends AppCompatActivity {
    String uid,userGender,email;
    ImageView backButton,editButton,profileImage;
    RadioGroup radioGroup;
    RadioButton radioButton,male,female;
    TextInputEditText userName,userAddress, birthDate,userHeight,userWeight,userPhone;
    AutoCompleteTextView district,area;
    TextInputLayout fullNameLayout, phoneLayout, addressLayout, ageLayout,heightLayout, weightLayout,districtLayout,areaLayout;
    ProgressBar loadingCircle;
    Uri filepath;
    Bitmap bitmap;
    Button update;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding();
        districtSelection();
        readFirebaseUserData();
        stateObserver();
        birthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat formatter= new SimpleDateFormat("yyyy/MM/dd");
                Date date = new Date(System.currentTimeMillis());
                String tempDate = formatter.format(date);
                String[] CurrentDate = tempDate.split("/");
                int currentYear = Integer.parseInt(CurrentDate[0]);
                int currentMonth = Integer.parseInt(CurrentDate[1]);
                int currentDay = Integer.parseInt(CurrentDate[2]);
                DatePickerDialog dialog = new DatePickerDialog(EditProfile.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date = String.valueOf(year)+"/"+String.valueOf(month+1)+"/"+String.valueOf(dayOfMonth);
                        birthDate.setText(date);
                    }
                }, currentYear, currentMonth, currentDay);
                dialog.show();
            }
        });

    }
    public void viewBinding()
    {
        uid = getIntent().getStringExtra("uId");
        email = getIntent().getStringExtra("email");
        setContentView(R.layout.activity_edit_profile);
        backButton = findViewById(R.id.back);
        radioGroup = findViewById(R.id.radioG);
        userName = findViewById(R.id.fullName);
        userAddress = findViewById(R.id.address);
        userPhone = findViewById(R.id.phone);
        birthDate = findViewById(R.id.age);
        userHeight = findViewById(R.id.height);
        userWeight = findViewById(R.id.weight);
        loadingCircle = findViewById(R.id.progress_circular);
        editButton = findViewById(R.id.editPicture);
        profileImage = findViewById(R.id.profilePicture);
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        update = findViewById(R.id.update);
        district = findViewById(R.id.district);
        area = findViewById(R.id.area);
        fullNameLayout = findViewById(R.id.fullNameLayout);
        addressLayout = findViewById(R.id.addressLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        ageLayout = findViewById(R.id.ageLayout);
        heightLayout = findViewById(R.id.heightLayout);
        weightLayout = findViewById(R.id.weightLayout);
        districtLayout = findViewById(R.id.districtLayout);
        areaLayout = findViewById(R.id.areaLayout);
    }
    public void districtSelection()
    {
        String [] items = {"Dhaka","Chittagong","Gazipur","Barishal","Jamalpur","Khulna","Rajshahi","Sherpur","Sylhet ","Rangpur"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfile.this,R.layout.drop_menu,items);
        district.setAdapter(adapter);
    }
    public void areaSelection(String area) {
        if (area.equals("Dhaka")) {
            String[] items = {"Badda", "Mirpur", "Dhanmondi", "Mohammadpur", "Demra", "Gulshan", "Khilgaon",
                    "Khilkhet", "Ramna ", "Savar"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfile.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Chittagong"))
        {
            String[] items = {"Mirsharai", "Mirsharai", "Patiya", "Raozan", "Sandwip", "Satkania",
                    "Sitakunda", "Banshkhali", "Boalkhali", "Chandanaish","Fatikchhari ","Hathazari ","Lohagara ","Pahartali",
                    "Bandarban","Patenga"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfile.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Gazipur"))
        {
            String[] items = {"Gazipur Sadar", "Kapasia", "Tongi town", "Sripur", "Kaliganj", "Kaliakior"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfile.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Barishal"))
        {
            String[] items = {"Barishal Sadar", "Banaripara", "Bakerganj", "Babuganj", "Gaurnadi", "Hizla",
                    "Mehendiganj", "Agailjhara", "Wazirpur", "Muladi"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfile.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Jamalpur"))
        {
            String[] items = {"Jamalpur Sadar", "Baksiganj ", "Dewanganj", "Islampur", "Madarganj", "Sarishabari"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfile.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Khulna"))
        {
            String[] items = {"Dumuria", "Batiaghata ", "Dacope", "Phultala", "Dighalia", "Koyra","Terokhada","Rupsha","Paikgachha"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfile.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Rajshahi"))
        {
            String[] items = {"Godagari", "Tanore", "Mohanpur", "Bagmara", "Durgapur", "Bagmara","Bagha","Charghat","Puthia","Paba "};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfile.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Sherpur"))
        {
            String[] items = {"Sherpur Sadar", "Nakla", "Sreebardi", "Nalitabari", "Jhenaigati"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfile.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Sylhet"))
        {
            String[] items = {"Sylhet Sadar", "Beanibazar", "Golapganj", "Companiganj", "Fenchuganj","Bishwanath","Bishwanath","Jaintiapur","Kanaighat","Balaganj"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfile.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
        else if(area.equals("Rangpur"))
        {
            String[] items = {"Rangpur Sadar", "Badarganj", "Kaunia", "Gangachhara", "Mithapukur","Taraganj","Pirganj","Pirgachha"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfile.this, R.layout.drop_menu, items);
            this.area.setAdapter(adapter);
        }
    }
    public void stateObserver()
    {
        userName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                fullNameLayout.setError(null);
                fullNameLayout.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        userAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                addressLayout.setError(null);
                addressLayout.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        userPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                phoneLayout.setError(null);
                phoneLayout.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        userHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                heightLayout.setError(null);
                heightLayout.setErrorEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        userWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                weightLayout.setError(null);
                weightLayout.setErrorEnabled(false);
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
    }
    public boolean validateFullName()
    {
        String value = userName.getText().toString();
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
    public boolean validateAddress()
    {
        String value = userAddress.getText().toString();
        if(value.isEmpty())
        {
            addressLayout.setError("Field can't be empty");
            return false;
        }
        if(value.equals("Not set"))
        {
            addressLayout.setError("Please update your address");
            return false;
        }
        else
        {
            return true;
        }
    }
    public boolean validatePhone()
    {
        String value = userPhone.getText().toString();
        if(value.isEmpty())
        {
            phoneLayout.setError("Field can't be empty");
            return false;
        }
        else if(value.length() > 11)
        {
            phoneLayout.setError("Phone number exceeds limit");
            return  false;
        }
        else if(value.length() < 11)
        {
            phoneLayout.setError("Invalid Phone Number");
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean validateHeight()
    {
        String value =  userHeight.getText().toString();
        if(value.isEmpty())
        {
            heightLayout.setError("Field can't be empty");
            return false;
        }
        else if(Float.parseFloat(value) < 121.92)
        {
            heightLayout.setError("Invalid height");
            return false;
        }
        else if(Float.parseFloat(value) > 243.84)
        {
            heightLayout.setError("Invalid height");
            return false;
        }
        else
        {
            return true;
        }
    }
    public boolean validateWeight()
    {
        String value = userWeight.getText().toString();
        if(value.isEmpty())
        {
            weightLayout.setError("Field can't be empty");
            return false;
        }
        else if(Float.parseFloat(value) > 200.00)
        {
            weightLayout.setError("Invalid weight");
            return false;
        }
        else if(Float.parseFloat(value) < 30.00)
        {
            weightLayout.setError("Invalid weight");
            return false;
        }
        else
        {
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
    public void browse(View view)
    {
        ImagePicker.with(this)
                .crop(1f, 1f)	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            filepath = data.getData();
            try{
                InputStream inputStream = getContentResolver().openInputStream(filepath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                profileImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        super.onActivityResult(requestCode, resultCode, data);
    }
    public void firebaseUpdateData(View view)
    {
        if(!validateFullName() | !validateHeight() | !validateWeight() | !validateAddress() | !validatePhone()|!validateUserArea()|!validateUserDistrict())
        {
            return;
        }
        String fullname = userName.getText().toString();
        String address  = userAddress.getText().toString();
        String phone  = userPhone.getText().toString();
        String birthdate = birthDate.getText().toString();
        String height = userHeight.getText().toString();
        String weight = userWeight.getText().toString();
        String userDistrict = district.getText().toString();
        String userArea = area.getText().toString();
        String gender = userGender;
        if(filepath != null) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle("Uploading...");
            dialog.show();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference uploader = storage.getReference("profileImage");
            uploader.child(uid).putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    uploader.child(uid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            dialog.dismiss();
                            userHelper userData = new userHelper(fullname, email, phone,birthdate,gender,height,weight,address,userDistrict,userArea);
                            userData.setPhotoUrl(uri.toString());
                            FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
                            DatabaseReference reference = database.getReference("users");
                            reference.child(uid).setValue(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    alertDialog();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(EditProfile.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfile.this, "Can't write to database", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    float percent = (100 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    dialog.setMessage("Uploaded: " + (int) percent + "%");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfile.this, "Update Failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            loadingCircle.setVisibility(View.VISIBLE);
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
            DatabaseReference reference = database.getReference("users");
            reference.child(uid).child("address").setValue(address);
            reference.child(uid).child("birthDate").setValue(birthdate);
            reference.child(uid).child("email").setValue(email);
            reference.child(uid).child("fullName").setValue(fullname);
            reference.child(uid).child("gender").setValue(gender);
            reference.child(uid).child("height").setValue(height);
            reference.child(uid).child("phone").setValue(phone);
            reference.child(uid).child("weight").setValue(weight);
            reference.child(uid).child("district").setValue(userDistrict);
            reference.child(uid).child("area").setValue(userArea);
            loadingCircle.setVisibility(View.GONE);
            alertDialog();
        }
    }

    public void readFirebaseUserData()
    {
        loadingCircle.setVisibility(View.VISIBLE);
        DatabaseReference reference = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");
        reference.child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                { loadingCircle.setVisibility(View.GONE);
                    if(task.getResult().exists())
                    {
                        DataSnapshot snapShot = task.getResult();
                        userName.setText(String.valueOf(snapShot.child("fullName").getValue()));
                        userPhone.setText(String.valueOf(snapShot.child("phone").getValue()));
                        district.setText(String.valueOf(snapShot.child("district").getValue()));
                        area.setText(String.valueOf(snapShot.child("area").getValue()));
                        districtSelection();
                        areaSelection(district.getText().toString());
                        Log.i("distric loaded","District");
                        String address = String.valueOf(snapShot.child("address").getValue());
                        if(!address.equals("null"))
                        {
                            userAddress.setText(address);
                        }
                        else
                        {
                            userAddress.setText("Not set");
                        }
                        String birthDate = String.valueOf(snapShot.child("birthDate").getValue());
                        if(!birthDate.equals("null"))
                        {
                            EditProfile.this.birthDate.setText(birthDate);
                        }
                        else
                        {
                            EditProfile.this.birthDate.setText("Not set");
                        }
                        String height = String.valueOf(snapShot.child("height").getValue());
                        if(!height.equals("null"))
                        {
                            userHeight.setText(height);
                        }
                        else
                        {
                            userHeight.setText("Not set");
                        }
                        String weight = String.valueOf(snapShot.child("weight").getValue());
                        if(!height.equals("null"))
                        {
                            userWeight.setText(weight);
                        }
                        else
                        {
                            userWeight.setText("Not set");
                        }
                        String photoUrl = String.valueOf(snapShot.child("photoUrl").getValue());
                        if(!photoUrl.equals("null"))
                        {
                            Glide.with(EditProfile.this).load(photoUrl).into(profileImage);
                             //loading image from firebase using glide tool.
                        }
                        else
                        {
                           profileImage.setImageResource(R.drawable.profile_picture);
                        }
                        String gender = String.valueOf(snapShot.child("gender").getValue());
                        if(!gender.equals("null"))
                        {
                            if(gender.equals("Male"))
                            {
                                male.setChecked(true);
                                userGender = "Male";
                            }
                            else
                            {
                                female.setChecked(true);
                                userGender = "Female";
                            }
                        }
                        else
                        {
                           return;
                        }

                    }
                    else
                    {
                        Toast.makeText(EditProfile.this,"User doesn't exist",Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(EditProfile.this,"Failed to read data from the server",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"Database Read Error");
            }
        });
    }
    public void back(View view)
    {
        finish();
    }
    public void checkButton(View view)  //for radio button
    {
        int radioId = radioGroup.getCheckedRadioButtonId(); //check which button is pressed
        radioButton = findViewById(radioId);
        userGender = radioButton.getText().toString();
        if(userGender.equals("Male")||userGender.equals("পুরুষ"))
        {
            userGender = "Male";
        }
        else
        {
            userGender = "Female";
        }
    }
    public void alertDialog()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(EditProfile.this);
        dialog.setTitle("Updated")
                .setMessage("Information updated successfully")
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        dialog.create().show();
    }
}