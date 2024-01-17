package com.example.doctorbabu.patient.DoctorConsultationModule;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.doctorbabu.Adapters.PdfAdapter;
import com.example.doctorbabu.DatabaseModels.PdfModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityDocumentUploadBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.aviran.cookiebar2.CookieBar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DocumentUpload extends AppCompatActivity {
    ActivityDocumentUploadBinding binding;
    String doctorId, doctorTitle, doctorName, photoUrl;
    ExecutorService pdfUploader;
    Dialog dialog;
    Uri uri;
    ArrayList<PdfModel> pdfModels;
    ArrayList<String> pdfNames;
    PdfAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDocumentUploadBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        pdfModels = new ArrayList<>();
        pdfNames = new ArrayList<>();
        adapter = new PdfAdapter(this,pdfModels,pdfNames,binding.upload,binding.fileName);
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
                        if((adapter.getItemCount()+1) <=3){
                            selectPdf();
                        }else{
                            errorMessage("Limit exceeded","You can only select pdf upto 3 only");
                        }
                    }
                });
            }
        });
        binding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadPdf(uri);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {  //catching the file from activity
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();
            //extracting name of the pdf file
            String uriString = uri.toString();
            File myfile = new File(uriString);
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
            if(pdfNames.contains(displayName)){
                errorMessage("Duplicate Selection",displayName +" is already selected!");
                return;
            }
            PdfModel model = new PdfModel();
            model.setUri(uri);
            model.setFileName(displayName);
            pdfModels.add(model);
            setRecyclerView();
            binding.fileName.setVisibility(View.GONE);
        }
    }

    public void setRecyclerView(){
        binding.pdfRecyclerView.setVisibility(View.VISIBLE);
        binding.pdfRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        binding.pdfRecyclerView.setAdapter(adapter);
    }

    public void uploadPdf(Uri data) {
        loadingScreen();
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference uploader = storage.getReference("pdfFiles");
        for(int i =0; i<pdfModels.size();i++){
            int index = i;
            uploader.child(user.getUid()).child(pdfNames.get(index)).putFile(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    uploader.child(user.getUid()).child(pdfNames.get(index)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            DatabaseReference reference = firebase.getDatabaseReference("appointmentDocuments");
                            HashMap<String, String> hashData = new HashMap<>();
                            hashData.put("fileName", pdfNames.get(index));
                            hashData.put("fileUrl", uri.toString());
                            String uniqueKey = getUniqueKey();
                            reference.child(user.getUid()).child(uniqueKey).setValue(hashData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    closeLoadingScreen();
                                    launchActivity();
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    public void errorMessage(String title, String message) {
        CookieBar.build(this)
                .setTitle(title)
                .setMessage(message)
                .setTitleColor(R.color.white)
                .setBackgroundColor(R.color.dark_red)
                .setCookiePosition(CookieBar.TOP)  // Cookie will be displayed at the Top
                .show();
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
        Intent intent = new Intent(DocumentUpload.this, CallDoctor.class);
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