package com.example.doctorbabu.patient.MedicinePurchaseModules;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;

import com.example.doctorbabu.Adapters.PdfAdapter;
import com.example.doctorbabu.Adapters.ShopByPrescriptionAdapter;
import com.example.doctorbabu.DatabaseModels.PdfModel;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityShopByPrescriptionBinding;

import org.aviran.cookiebar2.CookieBar;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShopByPrescription extends AppCompatActivity {
    ActivityShopByPrescriptionBinding binding;
    ArrayList<PdfModel> contents;
    ArrayList<String> fileNames;
    ShopByPrescriptionAdapter adapter;
    ExecutorService contentChooser;
    Dialog dialog;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShopByPrescriptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        contents = new ArrayList<>();
        fileNames = new ArrayList<>();
        adapter = new ShopByPrescriptionAdapter(this,contents,fileNames,binding.noteHeader,binding.placeOrder);
        contentChooser = Executors.newSingleThreadExecutor();
        contentChooser.execute(new Runnable() {
            @Override
            public void run() {
                binding.pdf.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectPdf();
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
                        selectPicture();
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
        if (requestCode == 101 || requestCode == 102 && resultCode == RESULT_OK && data != null && data.getData() != null) {
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
            if(fileNames.contains(displayName)){
                errorMessage("Duplicate Selection",displayName +" is already selected!");
                return;
            }
            PdfModel model = new PdfModel();
            model.setUri(uri);
            model.setFileName(displayName);
            if(requestCode == 101){
                model.setFileType("pdf");
            }else{
                model.setFileType("image");
            }
            contents.add(model);
            setRecyclerView();
            binding.noteHeader.setVisibility(View.GONE);
        }
    }
    public void setRecyclerView(){
        binding.contentRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
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