package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.messageModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.MedicinePurchaseModules.MyDocumentViewer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class messageAdapter extends RecyclerView.Adapter<messageAdapter.myViewHolder> {
    final private int LEFT_CHAT = 0;
    final private int RIGHT_CHAT = 1;

    Context context;
    ArrayList<messageModel> model;

    public messageAdapter(Context context, ArrayList<messageModel> model) {
        this.context = context;
        this.model = model;

    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == RIGHT_CHAT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new myViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new myViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        messageModel dbmodel = model.get(position);
        holder.setIsRecyclable(false);
        if (dbmodel.getMessageType().equalsIgnoreCase("image")) {
            holder.layout.setBackgroundColor(Color.parseColor("#F4F6F6"));
            holder.show_image.setVisibility(View.VISIBLE);
            holder.showMessage.setVisibility(View.GONE);
            Glide.with(context).load(dbmodel.getMessage()).into(holder.show_image);
        } else {
            holder.showMessage.setText(dbmodel.getMessage());
        }
        Firebase firebase = Firebase.getInstance();
        SharedPreferences preferences = context.getSharedPreferences("loginDetails", Context.MODE_PRIVATE);
        String userType = preferences.getString("loginAs", "null");
        DatabaseReference reference;
        if (userType.equalsIgnoreCase("patient")) {
            reference = firebase.getDatabaseReference("doctorInfo");

        } else {
            reference = firebase.getDatabaseReference("users");
        }

        reference.child(dbmodel.getSenderId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Glide.with(context).load(String.valueOf(snapshot.child("photoUrl").getValue())).into(holder.profilePicture);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        holder.show_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, MyDocumentViewer.class);
                intent.putExtra("imageUrl",dbmodel.getMessage());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
        String userId;
        assert fuser != null;
        AppCompatActivity activity = (AppCompatActivity) context;
        SharedPreferences preferences = activity.getSharedPreferences("loginDetails", Context.MODE_PRIVATE);
        String userType = preferences.getString("loginAs", "null");
        if (userType.equalsIgnoreCase("patient")) {
            userId = fuser.getUid();
        } else {
            userId = preferences.getString("doctorId", "null");
        }
        if (model.get(position).getSenderId().equals(userId)) {
            return RIGHT_CHAT;
        } else {
            return LEFT_CHAT;
        }
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture, show_image;
        TextView showMessage;
        RelativeLayout layout;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            showMessage = itemView.findViewById(R.id.show_message);
            show_image = itemView.findViewById(R.id.show_image);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
