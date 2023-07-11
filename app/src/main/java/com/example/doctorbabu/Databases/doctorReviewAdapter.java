package com.example.doctorbabu.Databases;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;
import com.google.android.material.divider.MaterialDivider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class doctorReviewAdapter extends RecyclerView.Adapter<doctorReviewAdapter.myViewHolder> {
    Context context;
    ArrayList<doctorReviewModel> model;
    String fullName,photoUrl;
    public doctorReviewAdapter(Context context,ArrayList<doctorReviewModel> model)
    {
        this.model = model;
        this.context = context;
    }
    @NonNull
    @Override
    public doctorReviewAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_doctor_review,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull doctorReviewAdapter.myViewHolder holder, int position) {
            doctorReviewModel dbmodel = model.get(position);
            if(!dbmodel.getComment().equals("null"))
            {
                holder.comment.setText(dbmodel.getComment());
            }else{
                holder.comment.setVisibility(View.GONE);
                holder.divider.setVisibility(View.GONE);
            }
            holder.rating.setText(String.valueOf(dbmodel.getRating()));
            float rating = dbmodel.getRating();
            holder.ratingIndicator.setRating(rating);

            FirebaseDatabase database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app/");
            DatabaseReference reference = database.getReference("users");
            reference.child(dbmodel.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    fullName = String.valueOf(snapshot.child("fullName").getValue());
                    photoUrl = String.valueOf(snapshot.child("photoUrl").getValue());
                    Glide.with(context).load(photoUrl).into(holder.profilePicture);
                    holder.userName.setText(fullName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView profilePicture;
        TextView userName,rating,comment;
        RatingBar ratingIndicator;
        MaterialDivider divider;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            userName = itemView.findViewById(R.id.userName);
            rating = itemView.findViewById(R.id.rating);
            comment = itemView.findViewById(R.id.comment);
            ratingIndicator = itemView.findViewById(R.id.ratingIndicator);
            divider = itemView.findViewById(R.id.divider);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
