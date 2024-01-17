package com.example.doctorbabu.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.DatabaseModels.PdfModel;
import com.example.doctorbabu.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.myViewHolder> {
    Context context;
    ArrayList<PdfModel> model;
    ArrayList<String> pdfNames;
    MaterialButton button;
    TextView fileName;

    public PdfAdapter(Context context, ArrayList<PdfModel> model,ArrayList<String> pdfNames,MaterialButton button,TextView fileName) {
        this.context = context;
        this.model = model;
        this.pdfNames = pdfNames;
        this.button = button;
        this.fileName = fileName;
    }

    @NonNull
    @Override
    public PdfAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_pdf_selection_layout,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfAdapter.myViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PdfModel dbModel = model.get(position);
        holder.fileName.setText(dbModel.getFileName());
        pdfNames.add(dbModel.getFileName());
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                model.remove(position);
                pdfNames.remove(dbModel.getFileName());
                if(model.size() ==0){
                    button.setVisibility(View.GONE);
                    fileName.setVisibility(View.VISIBLE);
                }
                notifyDataSetChanged();
            }
        });
        button.setVisibility(View.VISIBLE);
        fileName.setVisibility(View.GONE);

    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView remove;
        TextView fileName;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            remove = itemView.findViewById(R.id.remove);
            fileName = itemView.findViewById(R.id.fileName);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
