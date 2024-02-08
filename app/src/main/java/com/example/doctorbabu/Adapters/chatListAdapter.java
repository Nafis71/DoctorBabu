package com.example.doctorbabu.Adapters;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.chatListModel;
import com.example.doctorbabu.DatabaseModels.messageModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.chatRoom.chatRoom;
import com.example.doctorbabu.encryption.AES;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.NoSuchPaddingException;

public class chatListAdapter extends RecyclerView.Adapter<chatListAdapter.myViewHolder> {
    Context context;
    ArrayList<chatListModel> model;
    AES aes = new AES();
    byte[] secretKey;
    String userId, chatKey, receiverChatKey, senderId;
    Firebase firebase = Firebase.getInstance();

    public chatListAdapter(Context context, ArrayList<chatListModel> model, String userId) {
        this.context = context;
        this.model = model;
        this.userId = userId;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_chat_list, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        final int[] counter = {0};
        chatListModel dbmodel = model.get(position);
        try {
            loadKeys();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        AppCompatActivity activity = (AppCompatActivity) context;
        DatabaseReference reference;
        SharedPreferences preferences = activity.getSharedPreferences("loginDetails", MODE_PRIVATE);
        String userType = preferences.getString("loginAs", "null");
        if (userType.equalsIgnoreCase("patient")) {
            reference = firebase.getDatabaseReference("doctorInfo");

        } else {
            reference = firebase.getDatabaseReference("users");
        }
        Glide.with(context).load(dbmodel.getPhotoUrl()).into(holder.profilePicture);
        holder.userName.setText(dbmodel.getFullName());
        reference.child(dbmodel.getUserId()).child("onlineStatus").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = String.valueOf(snapshot.getValue());
                    if (Integer.parseInt(status) != 0) {
                        holder.onlineStatus.setText("Online");
                        holder.onlineStatus.setTextColor(Color.parseColor("#28B463"));
                    } else {
                        holder.onlineStatus.setText("Offline");
                        holder.onlineStatus.setTextColor(Color.parseColor("#666666"));
                    }

                } else {
                    reference.child(dbmodel.getUserId()).child("onlineStatus").setValue(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        DatabaseReference chatReference = firebase.getDatabaseReference("chatRoom");
        chatReference.child(userId).child(dbmodel.getUserId()).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        chatKey = snap.getKey();
                        messageModel model = snap.getValue(messageModel.class);
                        assert model != null;
                        senderId = model.getSenderId();
                        if (model.getSenderId().equals(dbmodel.getUserId()) && model.getReceiverId().equals(userId)) {
                            counter[0] = 0;
                            String decryptedMessage = null;
                            try {
                                decryptedMessage = aes.decryption(model.getMessage(), secretKey);
                            } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
                                holder.message.setText("No messages");
                            }
                            if (decryptedMessage != null) {
                                if (model.getSeenStatus().equals("unseen")) {
                                    String message;
                                    if(model.getMessageType().equalsIgnoreCase("image")){
                                        message = "Sent a photo" + " - New message";
                                    } else{
                                        message = decryptedMessage + " - New message";
                                    }
                                    holder.message.setTypeface(Typeface.DEFAULT_BOLD);
                                    holder.message.setText(message);
                                } else {
                                    holder.message.setTypeface(Typeface.DEFAULT);
                                    holder.message.setText(decryptedMessage);
                                }
                            } else {
                                holder.message.setText("No messages");
                            }


                        } else if (!model.getReceiverId().equals(userId) && dbmodel.getUserId().equals(model.getReceiverId())) {
                            String decryptedMessage = null;
                            try {
                                decryptedMessage = aes.decryptionSender(model.getMessage(), secretKey);
                            } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                            if (decryptedMessage != null) {
                                String message;
                                if(model.getMessageType().equalsIgnoreCase("image")){
                                    message = "You: " + " Sent a photo";
                                } else{
                                    message = "You: " + decryptedMessage;
                                }
                                holder.message.setText(message);
                            } else {
                                holder.message.setText("No messages");
                            }

                        } else {
                            holder.message.setText("No Messages");
                        }
                    }
                } else {
                    holder.message.setText("No Messages");
                    counter[0] = 1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchChatRoom(dbmodel);
            }
        });

    }

    public void launchChatRoom(chatListModel dbmodel) {
        Intent intent = new Intent(context, chatRoom.class);
        intent.putExtra("userId", dbmodel.getUserId());
        intent.putExtra("photoUrl", dbmodel.getPhotoUrl());
        intent.putExtra("fullName", dbmodel.getFullName());
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return model.size();
    }

    public void loadKeys() throws NoSuchPaddingException, NoSuchAlgorithmException {
        AppCompatActivity activity = (AppCompatActivity) context;
        secretKey = Base64.getDecoder().decode(activity.getString(R.string.secretKey));
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView userName, onlineStatus, message;
        CardView card;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profilePicture);
            userName = itemView.findViewById(R.id.userName);
            onlineStatus = itemView.findViewById(R.id.onlineStatus);
            card = itemView.findViewById(R.id.card);
            message = itemView.findViewById(R.id.message);
        }
    }
}
