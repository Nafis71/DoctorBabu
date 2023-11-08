package com.example.doctorbabu.patient.DiagnoseReportUploadModule;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorbabu.databinding.ActivityDiagnosisReportUploadListBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class DiagnosisReportUploadList extends AppCompatActivity {
    ActivityDiagnosisReportUploadListBinding binding;
    Uri imagePath;
    Bitmap bitmap;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseAuth authentication;
    FirebaseUser user;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiagnosisReportUploadListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
        userId = authenticateUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.addReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });
    }

    public String authenticateUser() {
        authentication = FirebaseAuth.getInstance();
        user = authentication.getCurrentUser();
        assert user != null;
        return user.getUid();
    }

    public void choosePicture() {
        ImagePicker.with(this)
                .crop(1f, 1f)
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            imagePath = data.getData();
            uploadReport();
        }
    }

    public void uploadReport() {
        storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference("diagnoseReports");
        Snackbar uploadProgressbar = initializeUploadProgressbar();
        storageReference.child(userId).putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                uploadProgressbar.dismiss();
                getDownloadUrl(storageReference);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double percent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                String progressText = "Uploaded : " + percent + " %";
                setUploadProgressbar(uploadProgressbar, progressText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String progressText = "Upload failed, check your connection";
                setUploadProgressbar(uploadProgressbar, progressText);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        uploadProgressbar.dismiss();
                    }
                }, 2000);
            }
        });
    }

    public void getDownloadUrl(StorageReference storageReference) {
        storageReference.child(userId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            String reportLink;

            @Override
            public void onSuccess(Uri uri) {
                reportLink = uri.toString();
                saveLinkToDatabase(reportLink);
            }
        });

    }

    public void saveLinkToDatabase(String reportLink) {
        DatabaseReference reportReference = database.getReference("diagnoseReports");
        reportReference.child(userId).push().child("reportLink").setValue(reportLink);
    }

    public Snackbar initializeUploadProgressbar() {
        Snackbar uploadProgressbar = Snackbar.make(binding.parentLayout, "Uploading : ", Snackbar.LENGTH_INDEFINITE);
        uploadProgressbar.setText("Uploading...");
        uploadProgressbar.show();
        return uploadProgressbar;
    }

    public void setUploadProgressbar(Snackbar uploadProgressbar, String progressText) {
        uploadProgressbar.setText(progressText);
        uploadProgressbar.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}