package com.example.doctorbabu.FirebaseDatabase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Firebase {
    protected FirebaseDatabase database;
    protected DatabaseReference reference;

    private Firebase() {
    }

    public static Firebase instance = null;

    public static Firebase getInstance() {
        if (instance == null) {
            instance = new Firebase();
        }
        return instance;
    }

    public DatabaseReference getDatabaseReference(String databaseDirectory) {
        database = FirebaseDatabase.getInstance("https://prescription-bf7c7-default-rtdb.asia-southeast1.firebasedatabase.app");
        return database.getReference(databaseDirectory);
    }

    public FirebaseUser getUserID() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return auth.getCurrentUser();
    }
}


