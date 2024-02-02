package com.example.doctorbabu.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
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
import com.example.doctorbabu.DatabaseModels.diagnoseReportModel;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.MedicinePurchaseModules.MyDocumentViewer;
import com.example.doctorbabu.patient.MedicinePurchaseModules.SelectedDocuments;

import java.util.ArrayList;
import java.util.HashMap;

public class MyDocumentAdapter extends RecyclerView.Adapter<MyDocumentAdapter.myViewHolder> {
    Context context;
    ArrayList<diagnoseReportModel> model;

    public MyDocumentAdapter(Context context, ArrayList<diagnoseReportModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public MyDocumentAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_my_document,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyDocumentAdapter.myViewHolder holder, int position) {
        diagnoseReportModel dbModel = model.get(position);
        holder.fileName.setText(dbModel.getReportName());
        Glide.with(context).load(dbModel.getReportLink()).into(holder.gallaryImage);
        holder.gallaryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppCompatActivity activity = (AppCompatActivity)context;
                Intent intent = new Intent(context, MyDocumentViewer.class);
                intent.putExtra("imageUrl",dbModel.getReportLink());
                activity.startActivity(intent);
            }
        });
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,String> document = new HashMap<>();
                document.put("fileName", dbModel.getReportName());
                document.put("fileLink", dbModel.getReportLink());
                SelectedDocuments documents = SelectedDocuments.getInstance();
                documents.setDocuments(document);
                AppCompatActivity activity = (AppCompatActivity) context;
                Intent intent = new Intent();
                activity.setResult(Activity.RESULT_OK,intent);
                activity.finish();
            }
        });
    }

    public static class myViewHolder extends RecyclerView.ViewHolder{
        TextView fileName;
        ImageView gallaryImage;
        RelativeLayout card;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            gallaryImage = itemView.findViewById(R.id.gallaryImage);
            card = itemView.findViewById(R.id.card);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
