package com.example.doctorbabu.patient.MedicinePurchaseModules;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.doctorbabu.Adapters.ShopByPrescriptionAdapter;
import com.example.doctorbabu.DatabaseModels.PdfModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityShopByPrescriptionBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thekhaeng.pushdownanim.PushDownAnim;

import org.aviran.cookiebar2.CookieBar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShopByPrescription extends AppCompatActivity {
    ActivityShopByPrescriptionBinding binding;
    ArrayList<PdfModel> contents;
    ArrayList<String> fileNames;
    ShopByPrescriptionAdapter adapter;
    ExecutorService contentChooser,userDataExecutor,uploadExecutor;
    Dialog dialog;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopByPrescriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        contents = new ArrayList<>();
        fileNames = new ArrayList<>();
        adapter = new ShopByPrescriptionAdapter(this, contents, fileNames, binding.noteHeader, binding.placeOrder,binding.checkoutLayout,binding.contentLayout);
        contentChooser = Executors.newSingleThreadExecutor();
        userDataExecutor = Executors.newSingleThreadExecutor();
        uploadExecutor = Executors.newSingleThreadExecutor();
        userDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getUserData();
            }
        });
        contentChooser.execute(new Runnable() {
            @Override
            public void run() {
                binding.pdf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if((adapter.getItemCount()+1) <= 3){
                            selectPdf();
                        }else{
                            errorMessage("Limit exceeded","You can only select upto 3 prescriptions only");
                        }
                    }
                });
            }
        });
        contentChooser.execute(new Runnable() {
            @Override
            public void run() {
                binding.camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if((adapter.getItemCount()+1) <= 3){
                            selectPicture();
                        }else{
                            errorMessage("Limit exceeded","You can only select upto 3 prescriptions only");
                        }
                    }
                });
            }
        });
        uploadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                binding.placeOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        uploadPdf();
                    }
                });
            }
        });
        binding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        initiateTextWatcher();
        PushDownAnim.setPushDownAnimTo(binding.camera, binding.pdf, binding.placeOrder)
                .setScale(PushDownAnim.MODE_SCALE, 0.95f);

    }

    public void selectPdf() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf files"), 101);
    }

    public void selectPicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image files"), 102);
    }

    @SuppressLint("Range")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {  //catching the file from activity
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 || requestCode == 102) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
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
                if (fileNames.contains(displayName)) {
                    errorMessage("Duplicate Selection", displayName + " is already selected!");
                    return;
                }
                PdfModel model = new PdfModel();
                model.setUri(uri);
                model.setFileName(displayName);
                if (requestCode == 101) {
                    model.setFileType("pdf");
                } else {
                    model.setFileType("image");
                }
                contents.add(model);
                setRecyclerView();
                binding.noteHeader.setVisibility(View.GONE);
                binding.placeOrder.setVisibility(View.VISIBLE);
                binding.checkoutLayout.setVisibility(View.GONE);
                binding.contentLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public void getUserData(){
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        DatabaseReference reference = firebase.getDatabaseReference("users");
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String address = String.valueOf(snapshot.child("address").getValue());
                    if(address.equalsIgnoreCase("null")){
                        binding.deliveryAddress.setText("N/A");
                    }else{
                        binding.deliveryAddress.setText(address);
                    }
                    binding.phoneNumber.setText(String.valueOf(snapshot.child("phone").getValue()));
                    binding.customerName.setText(String.valueOf(snapshot.child("fullName").getValue()));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void uploadPdf() {
        if(binding.deliveryAddress.getText().toString().isEmpty()){
            binding.deliveryAddressTextLayout.setError("Must provide with address");
            return;
        }
        if(binding.phoneNumber.getText().toString().isEmpty()){
            binding.phoneNumberTextLayout.setError("Must provide with phone number");
        }
        loadingScreen();
        String address = Objects.requireNonNull(binding.deliveryAddress.getText()).toString();
        String phoneNumber = Objects.requireNonNull(binding.phoneNumber.getText()).toString();
        String customerName = binding.customerName.getText().toString();
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference uploader = storage.getReference("shopByPrescription");
        for(int i =0; i<contents.size();i++){
            int index = i;
            uploader.child(user.getUid()).child(fileNames.get(index)).putFile(contents.get(index).getUri()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    uploader.child(user.getUid()).child(fileNames.get(index)).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            DatabaseReference reference = firebase.getDatabaseReference("shopByPrescription");
                            HashMap<String, String> hashData = new HashMap<>();
                            hashData.put("fileName", fileNames.get(index));
                            hashData.put("fileUrl", uri.toString());
                            hashData.put("fileType",contents.get(index).getFileType());
                            hashData.put("customerId,",user.getUid());
                            hashData.put("customerName",customerName);
                            hashData.put("customerPhone",phoneNumber);
                            hashData.put("deliveryAddress",address);
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

    public void launchActivity(){
        Intent intent = new Intent(this, PurchaseCompletion.class);
        startActivity(intent);
        finish();
    }

    public void initiateTextWatcher(){
        binding.deliveryAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.deliveryAddressTextLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        binding.phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.phoneNumberTextLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
    public String getUniqueKey(){
        return UUID.randomUUID().toString();
    }
    public void setRecyclerView() {
        binding.contentRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.contentRecyclerView.setAdapter(adapter);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        contentChooser.shutdown();
    }
}