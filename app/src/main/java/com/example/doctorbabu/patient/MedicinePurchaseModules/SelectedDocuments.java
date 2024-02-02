package com.example.doctorbabu.patient.MedicinePurchaseModules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SelectedDocuments {
    HashMap<String, String> documents = new HashMap<>();

    public HashMap<String, String> getDocuments(){
        return documents;
    }

    public void setDocuments(HashMap<String, String> documents) {
        this.documents = documents;
    }

    private SelectedDocuments(){}
    public static  SelectedDocuments instance = null;
    public static  SelectedDocuments getInstance(){
        if(instance == null){
            instance = new SelectedDocuments();
        }
        return instance;
    }
}
