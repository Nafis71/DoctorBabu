package com.example.doctorbabu.chatRoom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import com.example.doctorbabu.Adapters.chatListAdapter;
import com.example.doctorbabu.DatabaseModels.chatListModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.databinding.ActivityChatListBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class chatList extends AppCompatActivity {
    ActivityChatListBinding binding;
    ArrayList<chatListModel> list;
    chatListAdapter adapter;
    Firebase firebase;
    String userType,userId;
    SharedPreferences preferences;
    boolean isPatient;
    ExecutorService loadDataExecutor;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebase = Firebase.getInstance();
        user = firebase.getUserID();
        preferences =  getSharedPreferences("loginDetails",MODE_PRIVATE);
        userType = preferences.getString("loginAs","null");
        loadDataExecutor = Executors.newSingleThreadExecutor();
        loadDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadList();
            }
        });
    }

    public void loadList(){
        FirebaseUser user = firebase.getUserID();;
        binding.chatListRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        binding.chatListRecyclerView.setLayoutManager(linearLayoutManager);
        list = new ArrayList<>();
        if(userType.equalsIgnoreCase("patient")){
            userId = user.getUid();
            isPatient = true;
        } else{
            userId = preferences.getString("doctorId","null");
        }
        adapter = new chatListAdapter(this,list,userId);
        binding.chatListRecyclerView.setAdapter(adapter);
        binding.chatListRecyclerView.smoothScrollToPosition(adapter.getItemCount());
        DatabaseReference reference = firebase.getDatabaseReference("chatRoomRegister");
        reference.child(userId).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if(snapshot.exists())
                {
                    binding.noChatLayout.setVisibility(View.GONE);
                    binding.recyclerLayout.setVisibility(View.VISIBLE);
                    for(DataSnapshot snap : snapshot.getChildren())
                    {
                        String connectionId = String.valueOf(snap.child("connectionId").getValue());
                        addUser(connectionId,isPatient);
                    }
                }else{
                    adapter.notifyDataSetChanged();
                    binding.recyclerLayout.setVisibility(View.GONE);
                    binding.noChatLayout.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }
    public void addUser(String connectionId, boolean isPatient) {
        DatabaseReference userReference;
        if(isPatient){
            userReference = firebase.getDatabaseReference("doctorInfo");

        } else{
            userReference = firebase.getDatabaseReference("users");
        }
        userReference.child(connectionId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    {
                        chatListModel model = new chatListModel();
                        if(isPatient){
                            String fullName = String.valueOf(snapshot.child("title").getValue()) + String.valueOf(snapshot.child("fullName").getValue());
                            model.setFullName(fullName);
                            model.setUserId(String.valueOf(snapshot.child("doctorId").getValue()));
                        } else{
                            model.setFullName(String.valueOf(snapshot.child("fullName").getValue()));
                            model.setUserId(String.valueOf(snapshot.child("userId").getValue()));
                        }
                        model.setPhotoUrl(String.valueOf(snapshot.child("photoUrl").getValue()));
                        model.setOnlineStatus(Integer.parseInt(String.valueOf(snapshot.child("onlineStatus").getValue())));
                        list.add(model);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loadDataExecutor.shutdown();
    }
}