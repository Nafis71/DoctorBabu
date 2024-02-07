package com.example.doctorbabu.chatRoom;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


import com.example.doctorbabu.databinding.ActivityChatListBinding;

public class chatList extends AppCompatActivity {
    ActivityChatListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}