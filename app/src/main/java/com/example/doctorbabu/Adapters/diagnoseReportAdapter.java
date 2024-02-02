package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.diagnoseReportModel;
import com.example.doctorbabu.R;
import com.example.doctorbabu.patient.MedicinePurchaseModules.MyDocumentViewer;

import java.util.ArrayList;

public class diagnoseReportAdapter extends RecyclerView.Adapter<diagnoseReportAdapter.myViewHolder> {

    Context context;
    ImageView delete;
    ArrayList<diagnoseReportModel> model;
    SelectedCard selectedCard;
    boolean isChecked;
    public diagnoseReportAdapter(Context context, ArrayList<diagnoseReportModel> model, SelectedCard selectedCard, ImageView delete){
        this.context = context;
        this.model = model;
        this.selectedCard = selectedCard;
        this.delete = delete;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_diagnose_reports,parent,false);
       return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        diagnoseReportModel dbmodel = model.get(position);
        holder.reportName.setText(dbmodel.getReportName());
        Glide.with(context).load(dbmodel.getReportLink()).into(holder.reportImage);
        holder.card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                longClickAction(holder,dbmodel);
                return true;
            }
        });
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isChecked){
                    singleClickAction(holder,dbmodel);
                }else{
                    AppCompatActivity activity = (AppCompatActivity) context;
                    Intent intent = new Intent(context, MyDocumentViewer.class);
                    intent.putExtra("imageUrl",dbmodel.getReportLink());
                    activity.startActivity(intent);
                }
            }
        });

    }

    public static class myViewHolder extends RecyclerView.ViewHolder{
        TextView reportName;
        ImageView reportImage,checkImage;
        CardView card;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            reportName = itemView.findViewById(R.id.reportName);
            reportImage = itemView.findViewById(R.id.profilePicture);
            card = itemView.findViewById(R.id.card);
            checkImage = itemView.findViewById(R.id.checkImage);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public void longClickAction(myViewHolder holder,diagnoseReportModel dbmodel){
        if(!isChecked){
            selectedCard.cards.add(dbmodel.getReportId());
            holder.checkImage.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            isChecked = true;
        }
    }

    public void singleClickAction(myViewHolder holder, diagnoseReportModel dbmodel){
        if(isChecked){
            if(selectedCard.cards.contains(dbmodel.getReportId())){
                selectedCard.cards.remove(dbmodel.getReportId());
                holder.checkImage.setVisibility(View.GONE);
            } else {
                selectedCard.cards.add(dbmodel.getReportId());
                holder.checkImage.setVisibility(View.VISIBLE);
            }
            int size = selectedCard.cards.size();
            if(size == 0){
                isChecked = false;
                delete.setVisibility(View.GONE);
            }

        }  //nothing for now

    }



}
