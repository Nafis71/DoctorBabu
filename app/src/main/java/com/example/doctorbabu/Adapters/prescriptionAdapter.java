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
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.DatabaseModels.prescriptionMedicineModel;
import com.example.doctorbabu.DatabaseModels.prescriptionModel;
import com.example.doctorbabu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class prescriptionAdapter extends RecyclerView.Adapter<prescriptionAdapter.myViewHolder> {
    Context context;
    ArrayList<prescriptionModel> model;
    TextView doctorName, degree, specialty, bmdc, prescriptionDate, diagnosis, patientAge, pName, pGender, pWeight;
    ShimmerRecyclerView prescriptionRecycler;
    ArrayList<prescriptionMedicineModel> rmodel = new ArrayList<>();
    prescriptionMedicineAdapter adapter;
    String prescriptionId, doctorId, patientId, title, fullName, date, nameWithTitle, doctorSpecialties, doctorBmdc, age, weight, patientNames, patientGender, doctorPhoto, doctorDegree, patientDiagnosis;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");

    public prescriptionAdapter(Context context, ArrayList<prescriptionModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public prescriptionAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_line_design_prescription_list, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull prescriptionAdapter.myViewHolder holder, int position) {
        prescriptionModel dbmodel = model.get(position);
        date = dbmodel.getDate();
        prescriptionId = dbmodel.getPrescriptionId();
        doctorId = dbmodel.getPrescribedBy();
        patientId = dbmodel.getPrescribedTo();
        patientDiagnosis = dbmodel.getDiagnosis();
        holder.date.setText(dbmodel.getDate());
        holder.diagnosisResult.setText(dbmodel.getDiagnosis());
        holder.prescriptionNumber.setText(String.valueOf(position + 1));
        DatabaseReference reference = database.getReference("doctorInfo");
        reference.child(doctorId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        DataSnapshot snapshot = task.getResult();
                        title = String.valueOf(snapshot.child("title").getValue());
                        fullName = String.valueOf(snapshot.child("fullName").getValue());
                        doctorSpecialties = String.valueOf(snapshot.child("specialty").getValue());
                        doctorBmdc = String.valueOf(snapshot.child("bmdc").getValue());
                        doctorPhoto = String.valueOf(snapshot.child("photoUrl").getValue());
                        doctorDegree = String.valueOf(snapshot.child("degrees").getValue());
                        nameWithTitle = title + " " + fullName;
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
                    age = calculatedYears;
                    weight = String.valueOf(snapshot.child("weight").getValue());
                    patientNames = String.valueOf(snapshot.child("fullName").getValue());
                    patientGender = String.valueOf(snapshot.child("gender").getValue());
                }
            }
        });
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convertXMLtoPDF();
//                AppCompatActivity activity = (AppCompatActivity) context;
//                activity.getSupportFragmentManager().beginTransaction().replace(R.id.container,new ViewPrescription(prescriptionId,doctorId,nameWithTitle,doctorDegree,doctorSpecialties,doctorBmdc,date,patientDiagnosis,age,patientNames,patientGender,weight)).addToBackStack("prescription").commit();
            }
        });


    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public void convertXMLtoPDF() {
        AppCompatActivity activity = (AppCompatActivity) context;
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_layout, null);
        prescriptionRecycler = view.findViewById(R.id.prescriptionRecycler);
        doctorName = view.findViewById(R.id.doctorName);
        doctorName.setText(nameWithTitle);
        degree = view.findViewById(R.id.doctorDegree);
        degree.setText(doctorDegree);
        specialty = view.findViewById(R.id.doctorSpecialties);
        specialty.setText(doctorSpecialties);
        bmdc = view.findViewById(R.id.bmdc);
        bmdc.setText(doctorBmdc);
        prescriptionDate = view.findViewById(R.id.date);
        prescriptionDate.setText(date);
        diagnosis = view.findViewById(R.id.diagnosis);
        diagnosis.setText(patientDiagnosis);
        patientAge = view.findViewById(R.id.age);
        patientAge.setText(age);
        pName = view.findViewById(R.id.patientName);
        pName.setText(patientNames);
        pGender = view.findViewById(R.id.gender);
        pGender.setText(patientGender);
        pWeight = view.findViewById(R.id.weight);
        pWeight.setText(weight);

        prescriptionRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false), R.layout.shimmer_layout_recently_viewed);
        adapter = new prescriptionMedicineAdapter(context, rmodel);
        prescriptionRecycler.setAdapter(adapter);
        DatabaseReference medicineDetailsReference = database.getReference("prescription");
        medicineDetailsReference.child(patientId).child(prescriptionId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
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
                        String fileName = "Prescription.pdf";
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

    public static class myViewHolder extends RecyclerView.ViewHolder {
        TextView prescriptionNumber, date, doctorName, diagnosisResult;
        Button view;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            prescriptionNumber = itemView.findViewById(R.id.prescriptionNumber);
            date = itemView.findViewById(R.id.date);
            doctorName = itemView.findViewById(R.id.doctorName);
            diagnosisResult = itemView.findViewById(R.id.diagnosisResult);
            view = itemView.findViewById(R.id.download);
        }
    }
}
