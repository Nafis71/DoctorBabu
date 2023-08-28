package com.example.doctorbabu.Databases;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.DoctorInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class availableDoctorAdapter extends RecyclerView.Adapter<availableDoctorAdapter.myViewHolder> {

    Context context;
    ArrayList<doctorInfoModel> model;
    String userId;
    Bitmap avatar;
    StringBuilder stringBuilder = new StringBuilder();

    public availableDoctorAdapter(Context context, ArrayList<doctorInfoModel> model, String userId) {
        this.context = context;
        this.model = model;
        this.userId = userId;
    }

    public static Bitmap eraseColor(Bitmap src, int color) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap b = src.copy(Bitmap.Config.ARGB_8888, true);
        b.setHasAlpha(true);

        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < width * height; i++) {
            if (pixels[i] == color) {
                pixels[i] = 0;
            }
        }

        b.setPixels(pixels, 0, width, 0, 0, width, height);

        return b;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_available_doctor, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        doctorInfoModel dbModel = model.get(position);
        stringBuilder.append(dbModel.getTitle()).append(" ").append(dbModel.getFullName());
        holder.doctorName.setText(stringBuilder.toString());
        stringBuilder.setLength(0);
        holder.doctordegree.setText(dbModel.getDegrees());
        holder.rating.setText(String.valueOf(dbModel.getRating()));
        holder.doctorSpecialties.setText(dbModel.getSpecialty());
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference currentlyWorkingReference = database.getReference("doctorCurrentlyWorking");
        currentlyWorkingReference.child(dbModel.getDoctorId()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        DataSnapshot snapshot = task.getResult();
                        if (!String.valueOf(snapshot.child("hospitalName").getValue()).equals("null")) {
                            holder.currentlyWorking.setText(String.valueOf(snapshot.child("hospitalName").getValue()));
                        } else {
                            holder.currentlyWorking.setText("N/A");
                        }
                    }
                }
            }
        });
        if (!dbModel.photoUrl.equals("null")) {
            Glide.with(context).load(dbModel.getPhotoUrl()).into(holder.profilePicture);
            holder.profilePicture.setBackgroundColor(Color.parseColor("#DDDDDD"));
//            holder.profilePicture.setAlpha(50);
        }
        holder.card.setOnClickListener(view -> {
            DatabaseReference reference = database.getReference();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String currentTime = sdf.format(new Date());
            HashMap<String, Object> data = new HashMap<>();
            data.put("doctorId", dbModel.getDoctorId());
            data.put("photoUrl", dbModel.getPhotoUrl());
            data.put("time", currentTime);
            reference.child("recentlyViewed").child(userId).child(dbModel.getDoctorId()).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    AppCompatActivity activity = (AppCompatActivity) context;
                    activity.getSupportFragmentManager().beginTransaction().addToBackStack("doctorProfile")
                            .replace(R.id.container, new DoctorInfo(dbModel.getDoctorId(), dbModel.getFullName(), dbModel.getTitle(), dbModel.getDegrees(), dbModel.getSpecialty(), holder.currentlyWorking.getText().toString(), dbModel.getPhotoUrl(), dbModel.getRating(), dbModel.getBmdc())).commit();
                }
            });

        });

    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        LinearLayout card;
        ImageView profilePicture;
        TextView doctorName, doctordegree, rating, doctorSpecialties, currentlyWorking;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            doctorName = itemView.findViewById(R.id.doctorName);
            doctordegree = itemView.findViewById(R.id.doctorDegree);
            rating = itemView.findViewById(R.id.rating);
            doctorSpecialties = itemView.findViewById(R.id.doctorSpecialties);
            currentlyWorking = itemView.findViewById(R.id.currentlyWorking);
            card = itemView.findViewById(R.id.card);
        }
    }


}
