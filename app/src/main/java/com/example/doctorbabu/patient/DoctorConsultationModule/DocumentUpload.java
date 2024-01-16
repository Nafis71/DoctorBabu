package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityDocumentUploadBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DocumentUpload extends AppCompatActivity {
    ActivityDocumentUploadBinding binding;
    String doctorId, doctorTitle, doctorName, photoUrl;
    ExecutorService pdfUploader;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDocumentUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        doctorId = getIntent().getStringExtra("doctorId");
        doctorTitle = getIntent().getStringExtra("doctorTitle");
        doctorName = getIntent().getStringExtra("doctorName");
        photoUrl = getIntent().getStringExtra("photoUrl");
        pdfUploader = Executors.newSingleThreadExecutor();
        binding.yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.startingLayout.setVisibility(View.GONE);
                binding.uploadLayout.setVisibility(View.VISIBLE);
            }
        });
        binding.skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchActivity();
            }
        });
        pdfUploader.execute(new Runnable() {
            @Override
            public void run() {
                binding.pdfSelector.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectPdf();
                    }
                });
            }
        });

    }

    public void selectPdf() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf files"), 101);
    }

    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            //extracting name of the pdf file
            String uriString = uri.toString();
            File myfile = new File(uriString);
            String path = myfile.getAbsolutePath();
            String displayName = null;
            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = this.getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    assert cursor != null;
                    cursor.close();
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myfile.getName();
            }
            binding.upload.setVisibility(View.VISIBLE);
            binding.fileName.setText(displayName);
            binding.upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    uploadPdf(uri);
                }
            });
        }
    }

    public void uploadPdf(Uri data) {
        loadingScreen();
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference uploader = storage.getReference("pdfFiles");
        uploader.child(user.getUid()).child(binding.fileName.getText().toString()).putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.w("Dhukse??","Dhukse");
                uploader.child(user.getUid()).child(binding.fileName.getText().toString()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DatabaseReference reference = firebase.getDatabaseReference("appointmentDocuments");
                        HashMap<String, String> hashData = new HashMap<>();
                        hashData.put("fileName", binding.fileName.getText().toString());
                        hashData.put("fileUrl", uri.toString());
                        String uniqueKey = getUniqueKey();
                        reference.child(user.getUid()).child(uniqueKey).setValue(hashData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                closeLoadingScreen();
                                Intent intent = new Intent(DocumentUpload.this, CallDoctor.class);
                                intent.putExtra("doctorId", doctorId);
                                intent.putExtra("doctorTitle", doctorTitle);
                                intent.putExtra("doctorName", doctorName);
                                intent.putExtra("photoUrl", photoUrl);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                });
            }
        });
    }

    public String getUniqueKey(){
        return UUID.randomUUID().toString();
    }


    public void loadingScreen() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.loading_screen);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void closeLoadingScreen() {
        dialog.dismiss();
    }

    public void launchActivity() {
        Intent intent = new Intent(DocumentUpload.this, CheckoutDoctor.class);
        intent.putExtra("doctorId", doctorId);
        intent.putExtra("doctorTitle", doctorTitle);
        intent.putExtra("doctorName", doctorName);
        intent.putExtra("photoUrl", photoUrl);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        pdfUploader.shutdown();
    }
}