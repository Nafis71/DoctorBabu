package com.example.doctorbabu.Databases;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class availableDoctorAdapter extends FirebaseRecyclerAdapter<availableDoctorModel,availableDoctorAdapter.myViewHolder> {

    public availableDoctorAdapter(@NonNull FirebaseRecyclerOptions<availableDoctorModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull availableDoctorModel model) {
            if(model.getRating()<5.00)
            {
                String fullName = model.getTitle()+" "+ model.getFullName();
                holder.doctorName.setText(fullName);
                holder.doctorType.setText(model.getDoctorType());
                holder.rating.setText(String.valueOf(model.getRating()));
                if(!model.getPhotoUrl().equals("null"))
                {
                    Glide.with(holder.profilePicture.getContext()).load(model.getPhotoUrl()).into(holder.profilePicture);
                }
            }
            else {
                holder.card.removeAllViews();
            }

    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row_design_available_doctor,parent,false);
        return new myViewHolder(view);
    }

    public class myViewHolder extends RecyclerView.ViewHolder{
        ImageView profilePicture;
        TextView doctorName,doctorType,rating;
        CardView card;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            doctorName =itemView.findViewById(R.id.doctorName);
            doctorType =itemView.findViewById(R.id.doctorType);
            rating =  itemView.findViewById(R.id.rating);
            card = itemView.findViewById(R.id.card);
        }
    }
}
