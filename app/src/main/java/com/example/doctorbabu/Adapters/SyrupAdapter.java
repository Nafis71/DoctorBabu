package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.syrupModel;
import com.example.doctorbabu.R;

import java.util.ArrayList;

public class SyrupAdapter extends RecyclerView.Adapter<SyrupAdapter.myViewHolder> {
    Context context;
    ArrayList<syrupModel> model;

    public SyrupAdapter(Context context, ArrayList<syrupModel> model) {
        this.context = context;
        this.model = model;
    }

    @NonNull
    @Override
    public SyrupAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_syrup_layout,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SyrupAdapter.myViewHolder holder, int position) {
        syrupModel dbModel = model.get(position);
        Glide.with(context).load(dbModel.getSyrupPicture()).into(holder.syrupImage);
        holder.syrupName.setText(dbModel.getSyrupName());
        holder.syrupBottleSize.setText(dbModel.getBottleSize());
        holder.syrupPrice.setText(dbModel.getUnitPrice());
    }
    public static class myViewHolder extends RecyclerView.ViewHolder{
        ImageView syrupImage;
        TextView syrupName,syrupPrice,syrupBottleSize;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            syrupImage = itemView.findViewById(R.id.syrupImage);
            syrupName =  itemView.findViewById(R.id.syrupName);
            syrupBottleSize = itemView.findViewById(R.id.syrupBottleSize);
            syrupPrice = itemView.findViewById(R.id.syrupPrice);
        }
    }

    @Override
    public int getItemCount() {
        return model.size();
    }
}
