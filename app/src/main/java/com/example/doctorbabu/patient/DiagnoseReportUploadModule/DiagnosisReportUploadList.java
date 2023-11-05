package com.example.doctorbabu.patient.DiagnoseReportUploadModule;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.example.doctorbabu.databinding.ActivityDiagnosisReportUploadListBinding;

public class DiagnosisReportUploadList extends AppCompatActivity {
    ActivityDiagnosisReportUploadListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiagnosisReportUploadListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}