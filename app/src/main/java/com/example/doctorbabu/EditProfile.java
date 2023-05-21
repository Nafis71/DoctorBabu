package com.example.doctorbabu;

import static android.content.ContentValues.TAG;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.Databases.profileEditUserHelper;
import com.example.doctorbabu.Databases.userHelper;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.InputStream;

public class EditProfile extends AppCompatActivity {
    String uid,userGender,email;
    ImageView backButton,editButton,profileImage;
    RadioGroup radioGroup;
    RadioButton radioButton,male,female;
    TextInputEditText userName,userAddress,userAge,userHeight,userWeight,userPhone;
    ProgressBar loadingCircle;
    Uri filepath;
    Bitmap bitmap;
    Button update;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uid = getIntent().getStringExtra("uId");
        email = getIntent().getStringExtra("email");
        setContentView(R.layout.activity_edit_profile);
        backButton = findViewById(R.id.back);
        radioGroup = findViewById(R.id.radioG);
        userName = findViewById(R.id.fullName);
        userAddress = findViewById(R.id.address);
        userPhone = findViewById(R.id.phone);
        userAge = findViewById(R.id.age);
        userHeight = findViewById(R.id.height);
        userWeight = findViewById(R.id.weight);
        loadingCircle = findViewById(R.id.progress_circular);
        editButton = findViewById(R.id.editPicture);
        profileImage = findViewById(R.id.profilePicture);
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        update = findViewById(R.id.update);
        readFirebaseUserData();
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

        String fullname = userName.getText().toString();
        String address  = userAddress.getText().toString();
        String phone  = userPhone.getText().toString();
        String age = userAge.getText().toString();
        String height = userHeight.getText().toString();
        String weight = userWeight.getText().toString();
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
                            userHelper userData = new userHelper(fullname, email, phone);
                            userData.setAddress(address);
                            userData.setAge(age);
                            userData.setHeight(height);
                            userData.setWeight(weight);
                            userData.setGender(gender);
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
            reference.child(uid).child("age").setValue(age);
            reference.child(uid).child("email").setValue(email);
            reference.child(uid).child("fullName").setValue(fullname);
            reference.child(uid).child("gender").setValue(gender);
            reference.child(uid).child("height").setValue(height);
            reference.child(uid).child("phone").setValue(phone);
            reference.child(uid).child("weight").setValue(weight);
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
                        String address = String.valueOf(snapShot.child("address").getValue());
                        if(!address.equals("null"))
                        {
                            userAddress.setText(address);
                        }
                        else
                        {
                            userAddress.setText("Not set");
                        }
                        String age = String.valueOf(snapShot.child("age").getValue());
                        if(!age.equals("null"))
                        {
                            userAge.setText(age);
                        }
                        else
                        {
                            userAge.setText("Not set");
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
                            }
                            else
                            {
                                female.setChecked(true);
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