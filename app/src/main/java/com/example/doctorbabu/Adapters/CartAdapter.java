package com.example.doctorbabu.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doctorbabu.DatabaseModels.CartModel;
import com.example.doctorbabu.FirebaseDatabase.Firebase;
import com.example.doctorbabu.R;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.myViewHolder> {
    static Context context;
    ArrayList<CartModel> model;
    SelectedCard selectedCard;
    MaterialCardView remove;
    RelativeLayout checkOutLayout;
    TextView totalPrice;
    double calculatedTotalPrice;

    public CartAdapter(Context context, ArrayList<CartModel> model, SelectedCard selectedCard,MaterialCardView remove,RelativeLayout checkOutLayout,TextView totalPrice) {
        this.context = context;
        this.model = model;
        this.selectedCard = selectedCard;
        this.remove = remove;
        this.checkOutLayout = checkOutLayout;
        this.totalPrice = totalPrice;
    }

    @NonNull
    @Override
    public CartAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_row_design_cart_list, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.myViewHolder holder, int position) {
        CartModel dbModel = model.get(position);
        holder.medicineSheet.setText(dbModel.getMedicineSheets());
        holder.medicinePrice.setText(dbModel.getTotalPrice());
        loadMedicineData(dbModel, holder);
        holder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("Button Pressed?", "Yes");
                int medicineSheet = Integer.parseInt(holder.medicineSheet.getText().toString());
                changePrice(dbModel, holder, medicineSheet, true);
            }
        });
        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int medicineSheet = Integer.parseInt(holder.medicineSheet.getText().toString());
                changePrice(dbModel, holder, medicineSheet, false);
            }
        });
        holder.circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardSelection(holder, dbModel);
            }
        });
    }

    public void loadMedicineData(CartModel dbModel, CartAdapter.myViewHolder holder) {
        Firebase firebase = Firebase.getInstance();
        DatabaseReference medicineInformationReference = firebase.getDatabaseReference("medicineData");
        medicineInformationReference.child(dbModel.getMedicineId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Glide.with(context).load(String.valueOf(snapshot.child("medicinePicture").getValue())).into(holder.medicineImage);
                    holder.medicineName.setText(String.valueOf(snapshot.child("medicineName").getValue()));
                    holder.medicineBrand.setText(String.valueOf(snapshot.child("brandName").getValue()));
                    holder.medicineDosage.setText(String.valueOf(snapshot.child("medicineDosage").getValue()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw error.toException();
            }
        });
    }

    public void changePrice(CartModel dbModel, CartAdapter.myViewHolder holder, int medicineSheet, boolean isPlus) {
        int oldMedicineSheet = medicineSheet;
        if (isPlus) {
            medicineSheet = medicineSheet + 1;
        } else {
            medicineSheet = medicineSheet - 1;
        }
        if (medicineSheet != 0) {
            holder.medicineSheet.setText(String.valueOf(medicineSheet));
            Firebase firebase = Firebase.getInstance();
            DatabaseReference medicineStripReference = firebase.getDatabaseReference("medicineData");
            medicineStripReference.child(dbModel.getMedicineId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        double perPiecePrice = Double.parseDouble(String.valueOf(snapshot.child("medicinePerPiecePrice").getValue()));
                        int sheetSize = Integer.parseInt(String.valueOf(snapshot.child("medicinePataSize").getValue()));
                        calculatePrice(holder, perPiecePrice, sheetSize,isPlus,oldMedicineSheet);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        }
    }

    public void calculatePrice(myViewHolder holder, double perPiecePrice, int sheetSize,boolean isPlus,int oldMedicineSheet) {
        double totalPrice = Integer.parseInt(holder.medicineSheet.getText().toString()) * perPiecePrice * sheetSize;
        holder.medicinePrice.setText(String.valueOf(totalPrice));
        if(checkOutLayout.getVisibility() == View.VISIBLE){
            if(isPlus){
                calculateTotalPlusPrice(holder,oldMedicineSheet,perPiecePrice,sheetSize);
            } else {
                deductTotalPrice(holder);
            }
        }
    }

    public void cardSelection(CartAdapter.myViewHolder holder, CartModel dbModel) {
        if (selectedCard.cards.contains(dbModel.getMedicineId())) {
            selectedCard.cards.remove(dbModel.getMedicineId());
            holder.circle.setImageResource(R.drawable.blank_circle);
            deductTotalPrice(holder);
            if(selectedCard.cards.size() == 0){
                remove.setVisibility(View.GONE);
                checkOutLayout.setVisibility(View.GONE);
                calculatedTotalPrice = 0.00;
            }
        } else {
            selectedCard.cards.add(dbModel.getMedicineId());
            holder.circle.setImageResource(R.drawable.checkedcircle);
            addTotalPrice(holder);
            if(remove.getVisibility() == View.GONE && checkOutLayout.getVisibility() == View.GONE){
                remove.setVisibility(View.VISIBLE);
                checkOutLayout.setVisibility(View.VISIBLE);
            }

        }

    }

    public void calculateTotalPlusPrice(CartAdapter.myViewHolder holder,int oldMedicineSheet,double perPiecePrice, int sheetSize){
        double medicinePrice = oldMedicineSheet * perPiecePrice * sheetSize;
        calculatedTotalPrice = calculatedTotalPrice - medicinePrice;
        calculatedTotalPrice = Double.parseDouble(holder.medicinePrice.getText().toString()) + calculatedTotalPrice;
        totalPrice.setText(String.valueOf(calculatedTotalPrice + 60.00));
    }
    public void addTotalPrice(CartAdapter.myViewHolder holder){
        calculatedTotalPrice = Double.parseDouble(holder.medicinePrice.getText().toString()) + calculatedTotalPrice;
        totalPrice.setText(String.valueOf(calculatedTotalPrice + 60.00));

    }
    public void deductTotalPrice(CartAdapter.myViewHolder holder){
        calculatedTotalPrice = calculatedTotalPrice - Double.parseDouble(holder.medicinePrice.getText().toString());
        totalPrice.setText(String.valueOf(calculatedTotalPrice + 60.00));

    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView medicineImage, plus, minus, circle;
        TextView medicineName, medicineDosage, medicineBrand, medicinePrice, medicineSheet;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            medicineImage = itemView.findViewById(R.id.medicinePicture);
            medicineName = itemView.findViewById(R.id.medicineName);
            medicineDosage = itemView.findViewById(R.id.medicineDosage);
            medicineBrand = itemView.findViewById(R.id.medicineBrandName);
            medicinePrice = itemView.findViewById(R.id.medicinePrice);
            medicineSheet = itemView.findViewById(R.id.medicineSheet);
            plus = itemView.findViewById(R.id.plus);
            minus = itemView.findViewById(R.id.minus);
            circle = itemView.findViewById(R.id.circle);
        }
    }
}
