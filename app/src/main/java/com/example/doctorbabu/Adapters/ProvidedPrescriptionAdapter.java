package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.DatabaseModels.ProvidedPrescriptionModel;
import com.example.doctorbabu.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class ProvidedPrescriptionAdapter extends RecyclerView.Adapter<ProvidedPrescriptionAdapter.myViewHolder> {
    Context context;
    ArrayList<ProvidedPrescriptionModel> model;

    public ProvidedPrescriptionAdapter(Context context, ArrayList<ProvidedPrescriptionModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public ProvidedPrescriptionAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_provided_prescription,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProvidedPrescriptionAdapter.myViewHolder holder, int position) {
        ProvidedPrescriptionModel dbModel = model.get(position);
        holder.fileName.setText(dbModel.getFileName());
        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadPdf(dbModel);
            }
        });

    }

    public void downloadPdf(ProvidedPrescriptionModel dbModel){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(dbModel.getFileUrl()),"application/pdf");
        AppCompatActivity activity = (AppCompatActivity) context;
        activity.startActivity(intent);
    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        TextView fileName;
        MaterialCardView download;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            download = itemView.findViewById(R.id.download);
        }
    }
    @Override
    public int getItemCount() {
        return model.size();
    }
}
