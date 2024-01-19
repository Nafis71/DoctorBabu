package com.example.doctorbabu.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doctorbabu.DatabaseModels.PdfModel;
import com.example.doctorbabu.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class ShopByPrescriptionAdapter extends RecyclerView.Adapter<ShopByPrescriptionAdapter.myViewHolder> {
    Context context;
    ArrayList<PdfModel> model;
    ArrayList<String> fileNames;
    TextView noteHeader;
    MaterialButton placeOrder;

    public ShopByPrescriptionAdapter(Context context, ArrayList<PdfModel> model,ArrayList<String> fileNames,TextView noteHeader,MaterialButton placeOrder) {
        this.context = context;
        this.model = model;
        this.fileNames = fileNames;
        this.noteHeader = noteHeader;
        this.placeOrder = placeOrder;
    }

    @NonNull
    @Override
    public ShopByPrescriptionAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_pdf_selection_layout,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopByPrescriptionAdapter.myViewHolder holder, @SuppressLint("RecyclerView") int position) {
        PdfModel dbModel = model.get(position);
        fileNames.add(dbModel.getFileName());
        holder.fileName.setText(dbModel.getFileName());
        if(dbModel.getFileType().equalsIgnoreCase("pdf")){
            holder.contentImage.setImageResource(R.drawable.pdf);
        }else{
            holder.contentImage.setImageResource(R.drawable.gallary);
        }
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View view) {
                model.remove(position);
                fileNames.remove(dbModel.getFileName());
                notifyDataSetChanged();
                if(model.size() == 0){
                    noteHeader.setVisibility(View.VISIBLE);
                    placeOrder.setVisibility(View.GONE);
                }
            }
        });
        placeOrder.setVisibility(View.VISIBLE);

    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView contentImage,remove;
        TextView fileName;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            contentImage = itemView.findViewById(R.id.pdfImage);
            fileName = itemView.findViewById(R.id.fileName);
            remove = itemView.findViewById(R.id.remove);
        }
    }
    @Override
    public int getItemCount() {
        return model.size();
    }
}
