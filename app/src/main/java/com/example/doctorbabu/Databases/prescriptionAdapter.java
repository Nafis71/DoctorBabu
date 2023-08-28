package com.example.doctorbabu.Databases;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.ViewPrescription;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

public class prescriptionAdapter extends RecyclerView.Adapter<prescriptionAdapter.myViewHolder> {
    Context context;
    ArrayList<prescriptionModel> model;
    String doctorId,patientId,title,fullName,date,nameWithTitle,doctorSpecialties,doctorBmdc,age,weight,patientNames,patientGender,doctorPhoto,doctorDegree,patientDiagnosis;
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
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity) context;
                activity.getSupportFragmentManager().beginTransaction().replace(R.id.container,new ViewPrescription()).commit();
            }
        });


    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        TextView prescriptionNumber,date,doctorName,diagnosisResult;
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

    @Override
    public int getItemCount() {
        return model.size();
    }
}
