package com.example.doctorbabu.Databases;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.aviran.cookiebar2.CookieBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

public class prescriptionAdapter extends RecyclerView.Adapter<prescriptionAdapter.myViewHolder> {
    Context context;
    ArrayList<prescriptionModel> model;
    String doctorId,patientId,title,fullName,date,nameWithTitle,doctorSpecialties,doctorBmdc,age,weight,patientNames,patientGender,doctorPhoto,doctorDegree,patientDiagnosis;
    TextView doctorName,degree,specialty,bmdc,prescriptionDate,diagnosis,patientAge,pName,pGender,pWeight;
    ImageView profilePicture;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
    public prescriptionAdapter(Context context, ArrayList<prescriptionModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public prescriptionAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_line_design_prescription_list,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull prescriptionAdapter.myViewHolder holder, int position) {
        prescriptionModel dbmodel = model.get(position);
        date = dbmodel.getDate();
        doctorId = dbmodel.getPrescribedBy();
        patientId = dbmodel.getPrescribedTo();
        patientDiagnosis =dbmodel.getDiagnosis();
        holder.date.setText(dbmodel.getDate());
        holder.diagnosisResult.setText(dbmodel.getDiagnosis());
        holder.prescriptionNumber.setText(String.valueOf(position+1));
        DatabaseReference reference = database.getReference("doctorInfo");
        reference.child(doctorId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        DataSnapshot snapshot = task.getResult();
                        title = String.valueOf(snapshot.child("title").getValue());
                        fullName = String.valueOf(snapshot.child("fullName").getValue());
                        doctorSpecialties = String.valueOf(snapshot.child("specialty").getValue());
                        doctorBmdc = String.valueOf(snapshot.child("bmdc").getValue());
                        doctorPhoto = String.valueOf(snapshot.child("photoUrl").getValue());
                        doctorDegree = String.valueOf(snapshot.child("degrees").getValue());
                        nameWithTitle = title+" "+fullName;
                        holder.doctorName.setText(nameWithTitle);
                    }
                }
            }
        });
        DatabaseReference userReference = database.getReference("users");
        userReference.child(patientId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful())
                {
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
                    String years = calculatedYears + " years old";
                    age = years;
                    weight = String.valueOf(snapshot.child("weight").getValue());
                    patientNames = String.valueOf(snapshot.child("fullName").getValue());
                    patientGender = String.valueOf(snapshot.child("gender").getValue());
                }
            }
        });
        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convertXMLtoPDF();
            }
        });


    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        TextView prescriptionNumber,date,doctorName,diagnosisResult;
        Button download;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            prescriptionNumber = itemView.findViewById(R.id.prescriptionNumber);
            date = itemView.findViewById(R.id.date);
            doctorName = itemView.findViewById(R.id.doctorName);
            diagnosisResult = itemView.findViewById(R.id.diagnosisResult);
            download = itemView.findViewById(R.id.download);
        }
    }
    public void convertXMLtoPDF(){
        View view = LayoutInflater.from(context).inflate(R.layout.pdf_layout,null);
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
        pGender =view.findViewById(R.id.gender);
        pGender.setText(patientGender);
        pWeight = view.findViewById(R.id.weight);
        pWeight.setText(weight);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            context.getDisplay().getRealMetrics(displayMetrics);
        }else{
            AppCompatActivity activity = (AppCompatActivity) context;
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        }
        view.measure(View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels,View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels,View.MeasureSpec.EXACTLY));
        view.layout(0,0,displayMetrics.widthPixels,displayMetrics.heightPixels);
        PdfDocument document = new PdfDocument();
        int viewWidth = view.getMeasuredWidth();
        int viewHeight = view.getMeasuredHeight();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(viewWidth,viewHeight,1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        view.draw(canvas);
        document.finishPage(page);
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = "Prescription.pdf";
        File file = new File(downloadsDir, fileName);
        try{
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
                    .setCookiePosition(CookieBar.TOP)  // Cookie will be displayed at the bottom
                    .show();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
