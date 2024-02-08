package com.example.doctorbabu.chatRoom;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.Adapters.messageAdapter;
import com.example.doctorbabu.DatabaseModels.messageModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.example.doctorbabu.databinding.ActivityChatRoomBinding;
import com.example.doctorbabu.encryption.AES;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
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
    Uri imagePath;
    boolean isImage;

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
                if(isImage){
                    binding.progressCircular.setVisibility(View.VISIBLE);
                    sendImage();
                    return;
                }
                try {
                    sendMessage();
                } catch (NoSuchPaddingException | IllegalBlockSizeException |
                         NoSuchAlgorithmException | BadPaddingException | InvalidKeySpecException |
                         InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        binding.gallary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectPicture();
            }
        });
        binding.close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.imagePreview.setVisibility(View.GONE);
                imagePath = null;
                isImage = false;
            }
        });
    }

    public void selectPicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image files"), 102);
    }
    @SuppressLint("Range")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102 && data != null && data.getData() != null) {
            imagePath = data.getData();
            isImage = true;
            binding.imagePreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(imagePath).into(binding.selectedImage);
        }
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
            onlineReference = firebase.getDatabaseReference("doctorInfo");
        } else {
            onlineReference = firebase.getDatabaseReference("users");
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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new messageAdapter(chatRoom.this, list);
        listener = reference.child(senderId).child(receiverId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
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
                        DatabaseReference seenReference = firebase.getDatabaseReference("chatRoom");
                        seenReference.child(senderId).child(receiverId).child(Objects.requireNonNull(snap.getKey())).child("seenStatus").setValue("seen");
                        if (model.getSeenStatus().equals("seen")) {
                            binding.seenStatus.setVisibility(View.VISIBLE);
                        } else {
                            binding.seenStatus.setVisibility(View.GONE);
                        }
                    } else if (model.getReceiverId().equals(senderId) && model.getSenderId().equals(receiverId)) {
                        String decryptedMessage = null;
                        try {
                            decryptedMessage = aes.decryption(model.getMessage(), secretKey);

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
                }
                binding.recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }
    public void sendImage(){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference("messageImage");
        String imageId = uniqueId();
        storageReference.child(imageId).putFile(imagePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                getDownloadUrl(storageReference,imageId);
            }
        });
    }

    public String uniqueId(){
        return UUID.randomUUID().toString();
    }
    public void getDownloadUrl(StorageReference storageReference, String imageId) {
        storageReference.child(imageId).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            String imageLink;

            @Override
            public void onSuccess(Uri uri) {
                imageLink = uri.toString();
                try {
                    saveLinkToChatRoom(imageLink);
                } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void saveLinkToChatRoom(String imageLink) throws NoSuchPaddingException, NoSuchAlgorithmException {
        AES aes = new AES();
        Firebase firebase = Firebase.getInstance();
        String encryptedMessage = aes.encryption(imageLink, secretKey);
        Clock clock = Clock.systemDefaultZone();
        long milliSeconds=clock.millis();
        HashMap<String, Object> hashMap = new HashMap<>();
        assert senderId != null;
        hashMap.put("receiverId", receiverId);
        hashMap.put("senderId", senderId);
        hashMap.put("message", encryptedMessage);
        hashMap.put("messageType", "image");
        hashMap.put("seenStatus", "unseen");
        hashMap.put("time",milliSeconds);
        DatabaseReference reference = firebase.getDatabaseReference("chatRoom");
        reference.child(senderId).child(receiverId).push().setValue(hashMap);
        reference.child(receiverId).child(senderId).push().setValue(hashMap);
        isImage = false;
        binding.textBox.setEnabled(true);
        binding.progressCircular.setVisibility(View.GONE);
        binding.imagePreview.setVisibility(View.GONE);
        imagePath = null;
    }

    private void sendMessage() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        if(isImage){
            sendImage();
            return;
        }
        String message = binding.textBox.getText().toString().trim();
        AES aes = new AES();
        Firebase firebase = Firebase.getInstance();
        if (message.isEmpty()) {
            return;
        }
        String encryptedMessage = aes.encryption(message, secretKey);
        Clock clock = Clock.systemDefaultZone();
        long milliSeconds=clock.millis();
        HashMap<String, Object> hashMap = new HashMap<>();
        assert senderId != null;
        hashMap.put("receiverId", receiverId);
        hashMap.put("senderId", senderId);
        hashMap.put("message", encryptedMessage);
        hashMap.put("messageType", "text");
        hashMap.put("seenStatus", "unseen");
        hashMap.put("time",milliSeconds);
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