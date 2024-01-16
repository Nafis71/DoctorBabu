package com.example.doctorbabu.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.DatabaseModels.doctorInfoModel;
import com.example.doctorbabu.DatabaseModels.prescriptionMedicineModel;
import com.example.doctorbabu.DatabaseModels.prescriptionModel;
import com.example.doctorbabu.DatabaseModels.userHelper;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.todkars.shimmer.ShimmerRecyclerView;

import org.aviran.cookiebar2.CookieBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class prescriptionAdapter extends RecyclerView.Adapter<prescriptionAdapter.myViewHolder> {
    Context context;
    ArrayList<prescriptionModel> model;
    ShimmerRecyclerView prescriptionRecycler;
    ArrayList<prescriptionMedicineModel> rmodel = new ArrayList<>();
    prescriptionMedicineAdapter adapter;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");

    public prescriptionAdapter(Context context, ArrayList<prescriptionModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public prescriptionAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_prescription_list, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull prescriptionAdapter.myViewHolder holder, int position) {
        prescriptionModel dbModel = model.get(position);
        final doctorInfoModel[] doctorModel = new doctorInfoModel[1];
        HashMap<String,String> userInfo= new HashMap<>();
        String doctorId = dbModel.getPrescribedBy();
        String patientId = dbModel.getPrescribedTo();
        holder.date.setText(dbModel.getDate());
        holder.diagnosisResult.setText(dbModel.getDiagnosis());
        holder.prescriptionNumber.setText(String.valueOf(position + 1));
        DatabaseReference reference = database.getReference("doctorInfo");
        reference.child(doctorId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        DataSnapshot snapshot = task.getResult();
                        doctorModel[0] = snapshot.getValue(doctorInfoModel.class);
                        assert doctorModel[0] != null;
                        String nameWithTitle = doctorModel[0].getTitle() + " " + doctorModel[0].getFullName();
                        holder.doctorName.setText(nameWithTitle);
                    }
                }
            }
        });
        DatabaseReference userReference = database.getReference("users");
        userReference.child(patientId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    String birthDate = String.valueOf(snapshot.child("birthDate").getValue());
                    String[] splitText = birthDate.split("/");
                    int year = Integer.parseInt(splitText[0]);
                    int month = Integer.parseInt(splitText[1]);
                    int day = Integer.parseInt(splitText[2]);
                    LocalDate from = LocalDate.of(year, month, day);
                    LocalDate to = LocalDate.now();
                    Period calculateAge = Period.between(from, to);
                    String calculatedYears = String.valueOf(calculateAge.getYears());
                    userInfo.put("age",calculatedYears);
                    userInfo.put("weight",String.valueOf(snapshot.child("weight").getValue()));
                    userInfo.put("patientName",String.valueOf(snapshot.child("fullName").getValue()));
                    userInfo.put("patientGender",String.valueOf(snapshot.child("gender").getValue()));
                }
            }
        });
        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convertXMLtoPDF(dbModel,doctorModel[0],userInfo);
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePrescription(dbModel);
            }
        });


    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public void convertXMLtoPDF(prescriptionModel dbmodel,doctorInfoModel doctorModel,HashMap<String,String> userInfo) {
        TextView doctorName, degree, specialty, bmdc, prescriptionDate, diagnosis, patientAge, pName, pGender, pWeight;
        AppCompatActivity activity = (AppCompatActivity) context;
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_layout, null);
        prescriptionRecycler = view.findViewById(R.id.prescriptionRecycler);
        String nameWithTitle = doctorModel.getTitle() + doctorModel.getFullName();
        doctorName = view.findViewById(R.id.doctorName);
        doctorName.setText(nameWithTitle);
        degree = view.findViewById(R.id.doctorDegree);
        degree.setText(doctorModel.getDegrees());
        specialty = view.findViewById(R.id.doctorSpecialties);
        specialty.setText(doctorModel.getSpecialty());
        bmdc = view.findViewById(R.id.bmdc);
        bmdc.setText(doctorModel.getBmdc());
        prescriptionDate = view.findViewById(R.id.date);
        prescriptionDate.setText(dbmodel.getDate());
        diagnosis = view.findViewById(R.id.diagnosis);
        diagnosis.setText(dbmodel.getDiagnosis());
        patientAge = view.findViewById(R.id.age);
        patientAge.setText(userInfo.get("age"));
        pName = view.findViewById(R.id.patientName);
        pName.setText(userInfo.get("patientName"));
        pGender = view.findViewById(R.id.gender);
        pGender.setText(userInfo.get("patientGender"));
        pWeight = view.findViewById(R.id.weight);
        pWeight.setText(userInfo.get("weight"));
        rmodel.clear();
        prescriptionRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false), R.layout.shimmer_layout_recently_viewed);
        adapter = new prescriptionMedicineAdapter(context, rmodel);
        prescriptionRecycler.setAdapter(adapter);
        DatabaseReference medicineDetailsReference = database.getReference("prescription");
        medicineDetailsReference.child(dbmodel.getPrescribedTo()).child(dbmodel.getPrescriptionId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        DataSnapshot snapshot = task.getResult();
                        String medicineName = String.valueOf(snapshot.child("medicineName").getValue());
                        String medicineDetails = String.valueOf(snapshot.child("medicineDetails").getValue());
                        int medicineCounter = Integer.parseInt(String.valueOf(snapshot.child("medicineCounter").getValue()));
                        if (medicineCounter > 1) {
                            String[] tempMedicineName = medicineName.split(",");
                            String[] tempMedicineDetails = medicineDetails.split(",");
                            for (int i = 0; i < medicineCounter; i++) {
                                prescriptionMedicineModel model = new prescriptionMedicineModel();
                                model.setMedicineName(tempMedicineName[i]);
                                model.setMedicineDetails(tempMedicineDetails[i]);
                                rmodel.add(model);
                            }
                            adapter.notifyDataSetChanged();

                        } else {
                            prescriptionMedicineModel model2 = new prescriptionMedicineModel();
                            model2.setMedicineName(medicineName);
                            model2.setMedicineDetails(medicineDetails);
                            rmodel.add(model2);
                            adapter.notifyDataSetChanged();
                        }
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            context.getDisplay().getRealMetrics(displayMetrics);
                        } else {
                            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        }
                        view.measure(View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.EXACTLY),
                                View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, View.MeasureSpec.EXACTLY));
                        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
                        PdfDocument document = new PdfDocument();
                        int viewWidth = view.getMeasuredWidth();
                        int viewHeight = view.getMeasuredHeight();
                        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(viewWidth, viewHeight, 1).create();
                        PdfDocument.Page page = document.startPage(pageInfo);
                        Canvas canvas = page.getCanvas();
                        view.draw(canvas);
                        document.finishPage(page);
                        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        String uniqueKey = getUniqueKey();
                        String fileName = dbmodel.getDate() + doctorName.getText().toString() + uniqueKey + ".pdf";
                        File file = new File(downloadsDir, fileName);
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(file);
                            document.writeTo(fileOutputStream);
                            document.close();
                            fileOutputStream.close();
                            AppCompatActivity activity = (AppCompatActivity) context;
                            CookieBar.build(activity)
                                    .setTitle("Download")
                                    .setMessage("Prescription Downloaded Successfuly")
                                    .setTitleColor(R.color.white)
                                    .setBackgroundColor(R.color.blue)
                                    .setCookiePosition(CookieBar.TOP)  // Cookie will be displayed at the Top
                                    .show();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            CookieBar.build(activity)
                                    .setTitle("Permission Needed")
                                    .setMessage("Please Give Storage Permission")
                                    .setTitleColor(R.color.white)
                                    .setBackgroundColor(R.color.blue)
                                    .setCookiePosition(CookieBar.TOP)  // Cookie will be displayed at the Top
                                    .show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        });

    }

    public void deletePrescription(prescriptionModel dbModel){
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        DatabaseReference deleteReference = firebase.getDatabaseReference("prescription");
        deleteReference.child(user.getUid()).child(dbModel.getPrescriptionId()).removeValue();
        AppCompatActivity activity = (AppCompatActivity) context;
    }
    public String getUniqueKey() {
        Random random = new Random();
        return String.valueOf(random.nextInt());
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        TextView prescriptionNumber, date, doctorName, diagnosisResult;
        ImageView delete;
        MaterialCardView download;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            prescriptionNumber = itemView.findViewById(R.id.prescriptionNumber);
            date = itemView.findViewById(R.id.date);
            doctorName = itemView.findViewById(R.id.doctorName);
            diagnosisResult = itemView.findViewById(R.id.diagnosisResult);
            download = itemView.findViewById(R.id.download);
            delete = itemView.findViewById(R.id.delete);
        }
    }
}
