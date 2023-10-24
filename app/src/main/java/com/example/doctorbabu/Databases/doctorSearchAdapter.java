package com.example.doctorbabu.Databases;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.Doctor;
import com.example.doctorbabu.patient.DoctorInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class doctorSearchAdapter extends RecyclerView.Adapter<doctorSearchAdapter.myViewHolder> {
    Context context;
    String userId;
    ArrayList<doctorSearchResultModel> doctorSearchModel;
    androidx.appcompat.widget.SearchView searchBox;

    public doctorSearchAdapter(Context context, ArrayList<doctorSearchResultModel> doctorSearchModel,String userId,androidx.appcompat.widget.SearchView searchBox) {
        this.context = context;
        this.doctorSearchModel = doctorSearchModel;
        this.userId = userId;
        this.searchBox = searchBox;
    }

    @NonNull
    @Override
    public doctorSearchAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_doctor_search, parent, false);
        return new myViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull doctorSearchAdapter.myViewHolder holder, int position) {
        doctorSearchResultModel model = doctorSearchModel.get(position);
        Glide.with(context).load(model.getPhotoUrl()).into(holder.profilePicture);
        holder.doctorName.setText(model.getTitle() + " " + model.getFullName() + " " +"("+model.getDoctorId()+")");
        holder.doctorDepartment.setText(model.getSpecialty());
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBox.setQuery("",false);
                searchBox.clearFocus();
                keepSearchedDoctorRecord(model);
            }
        });

    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView doctorName, doctorDepartment;
        RelativeLayout card;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            doctorName = itemView.findViewById(R.id.doctorName);
            doctorDepartment = itemView.findViewById(R.id.doctorDepartment);
            card = itemView.findViewById(R.id.card);
        }
    }

    @Override
    public int getItemCount() {
        return doctorSearchModel.size();
    }

    protected void keepSearchedDoctorRecord(doctorSearchResultModel model){
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference reference = database.getReference();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentTime = sdf.format(new Date());
        HashMap<String, Object> data = new HashMap<>();
        data.put("doctorId", model.getDoctorId());
        data.put("photoUrl", model.getPhotoUrl());
        data.put("time", currentTime);
        reference.child("recentlyViewed").child(userId).child(model.getDoctorId()).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                AppCompatActivity activity = (AppCompatActivity) context;
                activity.getSupportFragmentManager().beginTransaction().addToBackStack("doctorProfile")
                        .replace(R.id.container, new DoctorInfo(model)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
            }
        });
    }

}
