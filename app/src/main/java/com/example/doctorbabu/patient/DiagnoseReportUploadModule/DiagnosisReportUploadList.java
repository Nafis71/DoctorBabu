package com.example.doctorbabu.patient.DiagnoseReportUploadModule;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.doctorbabu.Adapters.diagnoseReportAdapter;
import com.example.doctorbabu.Adapters.SelectedCard;
import com.example.doctorbabu.DatabaseModels.diagnoseReportModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityDiagnosisReportUploadListBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class DiagnosisReportUploadList extends AppCompatActivity {
    ActivityDiagnosisReportUploadListBinding binding;
    Uri imagePath;
    BottomSheetDialog bottomSheetDialog;
    FirebaseStorage storage;
    FirebaseAuth authentication;
    FirebaseUser user;
    Firebase firebase;
    String userId;
    TextInputEditText reportName;
    TextInputLayout reportNameLayout;
    Button chooseReport;
    diagnoseReportAdapter recyclerViewAdapter;
    ArrayList<diagnoseReportModel> reportModel;
    SelectedCard selectedCards;
    diagnoseReportModel model;
    HashMap<String,String> data;

    String diagnoseReportName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiagnosisReportUploadListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userId = authenticateUser();
        data = new HashMap<>();
        showDiagnoseReports();
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebase = Firebase.getInstance();
        binding.addReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheet();
            }
        });
        binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteReports();
            }
        });
    }

    public String authenticateUser() {
        authentication = FirebaseAuth.getInstance();
        user = authentication.getCurrentUser();
        assert user != null;
        return user.getUid();
    }


    public void showDiagnoseReports() {
        reportModel = new ArrayList<>();
        selectedCards = SelectedCard.getInstance();
        binding.diagnosisReportsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2), R.layout.shimmer_layout_diagnose_report);
        recyclerViewAdapter = new diagnoseReportAdapter(this, reportModel, selectedCards,binding.delete);
        binding.diagnosisReportsRecyclerView.setAdapter(recyclerViewAdapter);
        binding.diagnosisReportsRecyclerView.showShimmer();
        fetchData();
    }

    public void fetchData() {
        firebase =  Firebase.getInstance();
        DatabaseReference fetchDiagnosisReport = firebase.getDatabaseReference("diagnoseReports");
        fetchDiagnosisReport.child(userId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reportModel.clear();
                if(snapshot.exists()){
                    for(DataSnapshot snap : snapshot.getChildren()){
                        model = diagnoseReportModel.getInstance();
                        model = snap.getValue(diagnoseReportModel.class);
                        reportModel.add(model);
                    }
                    recyclerViewAdapter.notifyDataSetChanged();
                    binding.diagnosisReportsRecyclerView.setVisibility(View.VISIBLE);
                    binding.nodataImage.setVisibility(View.GONE);
                    binding.diagnosisReportsRecyclerView.hideShimmer();
                } else{
                    binding.diagnosisReportsRecyclerView.hideShimmer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fetchData();
            }
        });

    }

    public void deleteReports(){
        DatabaseReference deleteDiagnosisReport = firebase.getDatabaseReference("diagnoseReports");
        ArrayList<String> cards = selectedCards.getCards();
        cards.forEach((card) -> deleteDiagnosisReport.child(userId).child(card).removeValue());
        binding.delete.setVisibility(View.GONE);
    }

    public boolean validateReportName(String reportName) {
        return !reportName.equals("");
    }


    public void showBottomSheet() {
        bottomSheetDialog = new BottomSheetDialog(this, R.style.bottomSheetTheme);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_diagnosis_report, null);
        reportName = bottomSheetView.findViewById(R.id.reportName);
        reportNameLayout = bottomSheetView.findViewById(R.id.reportNameLayout);
        chooseReport = bottomSheetView.findViewById(R.id.chooseReport);
        chooseReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                diagnoseReportName = reportName.getText().toString();
                if (validateReportName(diagnoseReportName)) {
                    choosePicture();
                } else {
                    reportNameLayout.setError("Report name is required!");
                }
            }
        });
        reportName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isFocused) {
                final float scale = getResources().getDisplayMetrics().density;
                int pixels = (int) (500 * scale + 0.5f);
                if (isFocused) {
                    bottomSheetView.setMinimumHeight(pixels);
                }
            }
        });
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
        bottomSheetTextWatcher();
    }

    public void bottomSheetTextWatcher() {
        reportName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                reportNameLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
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
        DatabaseReference reportReference = firebase.getDatabaseReference("diagnoseReports");
        String uniqueKey = getUniqueKey();
        data.put("reportId",uniqueKey);
        data.put("reportName",diagnoseReportName);
        data.put("reportLink",reportLink);
        reportReference.child(userId).child(uniqueKey).setValue(data);
        data.clear();
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

    public String getUniqueKey(){
        return UUID.randomUUID().toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}