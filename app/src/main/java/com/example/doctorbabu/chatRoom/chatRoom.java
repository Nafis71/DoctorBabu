package com.example.doctorbabu.chatRoom;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.Adapters.messageAdapter;
import com.example.doctorbabu.DatabaseModels.messageModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityChatRoomBinding;
import com.example.doctorbabu.encryption.AES;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class chatRoom extends AppCompatActivity {
    ActivityChatRoomBinding binding;
    ValueEventListener listener,onlineListener;
    String senderId, receiverId, receiverName, receiverPhoto,userType;
    byte[] secretKey;
    ArrayList<messageModel> list = new ArrayList<>();
    messageAdapter adapter;
    DatabaseReference reference,onlineReference;
    ExecutorService messageReader,userDataLoader;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        receiverId = getIntent().getStringExtra("userId");
        receiverName = getIntent().getStringExtra("fullName");
        receiverPhoto = getIntent().getStringExtra("photoUrl");
        preferences = getSharedPreferences("loginDetails",MODE_PRIVATE);
        userType = preferences.getString("loginAs","null");
        String storedSecretKey = getString(R.string.secretKey);
        secretKey = Base64.getDecoder().decode(storedSecretKey);
        messageReader = Executors.newSingleThreadExecutor();
        userDataLoader = Executors.newSingleThreadExecutor();
        messageReader.execute(new Runnable() {
            @Override
            public void run() {
                readMessages();
            }
        });
        userDataLoader.execute(new Runnable() {
            @Override
            public void run() {
                loadUserData();
            }
        });
        binding.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendMessage();
                } catch (NoSuchPaddingException | IllegalBlockSizeException |
                         NoSuchAlgorithmException | BadPaddingException | InvalidKeySpecException |
                         InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void loadUserData(){
        binding.userName.setText(receiverName);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(chatRoom.this).load(receiverPhoto).into(binding.profilePicture);
            }
        });
        Firebase firebase = Firebase.getInstance();
        if(userType.equalsIgnoreCase("patient")){
            onlineReference = firebase.getDatabaseReference("users");
        } else {
            onlineReference = firebase.getDatabaseReference("doctorInfo");
        }
        onlineListener = onlineReference.child(receiverId).child("onlineStatus").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String status = String.valueOf(snapshot.getValue());
                    if(Integer.parseInt(status) > 0)
                    {
                        binding.onlineStatus.setText("Online");
                    }
                    else
                    {
                        binding.onlineStatus.setText("Offline");
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void readMessages() {
        AES aes = new AES();
        Firebase firebase = Firebase.getInstance();
        if(userType.equalsIgnoreCase("patient")){
            FirebaseUser user = firebase.getUserID();
            senderId = user.getUid();
        } else {
            senderId = preferences.getString("doctorId","null");
        }
        reference = firebase.getDatabaseReference("chatRoom");
        binding.recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        listener = reference.child(senderId).child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                int count = 0;
                for (DataSnapshot snap : snapshot.getChildren()) {
                    messageModel model = snap.getValue(messageModel.class);
                    Log.w("Key", snap.getValue().toString());
                    assert model != null;
                    if (model.getReceiverId().equals(receiverId) && model.getSenderId().equals(senderId)) {
                        String decryptedMessage = null;
                        try {
                            decryptedMessage = aes.decryptionSender(model.getMessage(), secretKey);

                        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }

                        model.setMessage(decryptedMessage);
                        list.add(model);
                        if (model.getSeenStatus().equals("seen")) {
                            binding.seenStatus.setVisibility(View.VISIBLE);
                        } else {
                            binding.seenStatus.setVisibility(View.GONE);
                        }
                    } else if (model.getReceiverId().equals(senderId) && model.getSenderId().equals(receiverId)) {
                        String decryptedMessage = null;
                        try {
                            decryptedMessage = aes.decryption(model.getMessage(), model.getSecretKey(), secretKey);

                        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
                            throw new RuntimeException(e);
                        }

                        model.setMessage(decryptedMessage);
                        list.add(model);
                        DatabaseReference seenReference = firebase.getDatabaseReference("chatRoom");
                        seenReference.child(senderId).child(receiverId).child(Objects.requireNonNull(snap.getKey())).child("seenStatus").setValue("seen");
                        if (model.getSeenStatus().equals("seen")) {
                            binding.seenStatus.setVisibility(View.GONE);
                        } else {
                            binding.seenStatus.setVisibility(View.GONE);
                        }
                    }

                    adapter = new messageAdapter(chatRoom.this, list);
                    binding.recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    private void sendMessage() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        String message = binding.textBox.getText().toString().trim();
        AES aes = new AES();
        Firebase firebase = Firebase.getInstance();
        if (message.isEmpty()) {
            return;
        }
        String encryptedMessage = aes.encryption(message, secretKey);
        HashMap<String, String> hashMap = new HashMap<>();
        assert senderId != null;
        hashMap.put("receiverId", receiverId);
        hashMap.put("senderId", senderId);
        hashMap.put("message", encryptedMessage);
        hashMap.put("messageType", "text");
        hashMap.put("seenStatus", "unseen");
        DatabaseReference reference = firebase.getDatabaseReference("chatRoom");
        reference.child(senderId).child(receiverId).push().setValue(hashMap);
        reference.child(receiverId).child(senderId).push().setValue(hashMap);
        binding.textBox.setText(null);
    }

    public void onBackPressed() {
        reference.removeEventListener(listener);
        onlineReference.removeEventListener(onlineListener);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messageReader.shutdown();
        userDataLoader.shutdown();
    }
}