package com.example.doctorbabu.Adapters;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.myViewHolder> {
    Context context;
    ArrayList<CartModel> model;
    SelectedCard selectedCard;
    MaterialCardView remove;
    RelativeLayout checkOutLayout;
    TextView totalPrice;
    double calculatedTotalPrice;
    ExecutorService medicineDataExecutor;
    ArrayList<String> itemPostion = new ArrayList<>();
    Dialog dialog;


    public CartAdapter(Context context, ArrayList<CartModel> model, SelectedCard selectedCard, MaterialCardView remove, RelativeLayout checkOutLayout, TextView totalPrice) {
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
        medicineDataExecutor = Executors.newSingleThreadExecutor();
        CartModel dbModel = model.get(position);
        holder.quantity.setText(dbModel.getQuantity());
        holder.medicinePrice.setText(dbModel.getTotalPrice());
        medicineDataExecutor.execute(new Runnable() {
            @Override
            public void run() {
                loadMedicineData(dbModel, holder);
            }
        });
        holder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.plus.setEnabled(false);
                int medicineQuantity = Integer.parseInt(holder.quantity.getText().toString());
                changePrice(dbModel, holder, medicineQuantity, true);
            }
        });
        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.minus.setEnabled(false);
                int medicineQuantity = Integer.parseInt(holder.quantity.getText().toString());
                changePrice(dbModel, holder, medicineQuantity, false);

            }
        });
        holder.circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cardSelection(holder, dbModel);
            }
        });
    }
    public void loadingScreen() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.cart_loading_screen);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void closeLoadingScreen() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.dismiss();
            }
        }, 400);
    }

    public void saveChangedQuantity(CartModel dbModel,int quantity,String updatedPrice){
        double totalPrice = Double.parseDouble(updatedPrice);
        totalPrice = Math.round(totalPrice);
        Firebase firebase = Firebase.getInstance();
        FirebaseUser user = firebase.getUserID();
        DatabaseReference reference = firebase.getDatabaseReference("medicineCart");
        reference.child(user.getUid()).child(dbModel.getMedicineId()).child("quantity").setValue(String.valueOf(quantity));
        reference.child(user.getUid()).child(dbModel.getMedicineId()).child("totalPrice").setValue(String.valueOf(totalPrice)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                closeLoadingScreen();
            }
        });
    }

    public void loadMedicineData(CartModel dbModel, CartAdapter.myViewHolder holder) {
        Firebase firebase = Firebase.getInstance();
        if (dbModel.getMedicineType().equalsIgnoreCase("tablet")) {
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
        } else if (dbModel.getMedicineType().equalsIgnoreCase("syrup")) {
            DatabaseReference medicineInformationReference = firebase.getDatabaseReference("syrupData");
            medicineInformationReference.child(dbModel.getMedicineId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Glide.with(context).load(String.valueOf(snapshot.child("syrupPicture").getValue())).into(holder.medicineImage);
                        holder.medicineName.setText(String.valueOf(snapshot.child("syrupName").getValue()));
                        holder.medicineBrand.setText(String.valueOf(snapshot.child("brandName").getValue()));
                        holder.medicineDosage.setText(String.valueOf(snapshot.child("bottleSize").getValue()));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        } else if(dbModel.getMedicineType().equalsIgnoreCase("herbalSyrup")){
            DatabaseReference medicineInformationReference = firebase.getDatabaseReference("herbalSyrupData");
            medicineInformationReference.child(dbModel.getMedicineId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Glide.with(context).load(String.valueOf(snapshot.child("syrupPicture").getValue())).into(holder.medicineImage);
                        holder.medicineName.setText(String.valueOf(snapshot.child("syrupName").getValue()));
                        holder.medicineBrand.setText(String.valueOf(snapshot.child("brandName").getValue()));
                        holder.medicineDosage.setText(String.valueOf(snapshot.child("bottleSize").getValue()));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    throw error.toException();
                }
            });
        }
        medicineDataExecutor.shutdown();
    }

    public void changePrice(CartModel dbModel, CartAdapter.myViewHolder holder, int medicineQuantity, boolean isPlus) {
        loadingScreen();
        int newMedicineQuantity;
        if (isPlus) {
            newMedicineQuantity = medicineQuantity + 1;
        } else {
            newMedicineQuantity = medicineQuantity - 1;
        }
        if (newMedicineQuantity != 0) {
            holder.quantity.setText(String.valueOf(newMedicineQuantity));
            Firebase firebase = Firebase.getInstance();
            if (dbModel.getMedicineType().equalsIgnoreCase("tablet")) {
                DatabaseReference medicineStripReference = firebase.getDatabaseReference("medicineData");
                medicineStripReference.child(dbModel.getMedicineId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            double perPiecePrice = Double.parseDouble(String.valueOf(snapshot.child("medicinePerPiecePrice").getValue()));
                            int sheetSize = Integer.parseInt(String.valueOf(snapshot.child("medicinePataSize").getValue()));
                            calculatePrice(holder, perPiecePrice, sheetSize, isPlus, medicineQuantity, dbModel,newMedicineQuantity);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        throw error.toException();
                    }
                });
            } else if(dbModel.getMedicineType().equalsIgnoreCase("herbalSyrup")){
                DatabaseReference medicineStripReference = firebase.getDatabaseReference("herbalSyrupData");
                medicineStripReference.child(dbModel.getMedicineId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            double unitPrice = Double.parseDouble(String.valueOf(snapshot.child("unitPrice").getValue()));
                            calculatePrice(holder, unitPrice, 0, isPlus, medicineQuantity, dbModel,newMedicineQuantity);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        throw error.toException();
                    }
                });
            } else {
                DatabaseReference medicineStripReference = firebase.getDatabaseReference("syrupData");
                medicineStripReference.child(dbModel.getMedicineId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            double unitPrice = Double.parseDouble(String.valueOf(snapshot.child("unitPrice").getValue()));
                            calculatePrice(holder, unitPrice, 0, isPlus, medicineQuantity, dbModel,newMedicineQuantity);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        throw error.toException();
                    }
                });
            }

        } else {
            holder.minus.setEnabled(true);
            closeLoadingScreen();
        }
    }

    public void calculatePrice(myViewHolder holder, double perPiecePrice, int sheetSize, boolean isPlus, int oldMedicineQuantity, CartModel dbModel,int newQuantity) {
        if (dbModel.getMedicineType().equalsIgnoreCase("tablet")) {
            double totalPrice = Integer.parseInt(holder.quantity.getText().toString()) * perPiecePrice * sheetSize;
            @SuppressLint("DefaultLocale")
            String formattedTotalPrice = String.format("%.2f", totalPrice);
            holder.medicinePrice.setText(formattedTotalPrice);
            saveChangedQuantity(dbModel,newQuantity,formattedTotalPrice);
            if (checkOutLayout.getVisibility() == View.VISIBLE && itemPostion.contains(String.valueOf(holder.getAdapterPosition()))) {
                if (isPlus) {
                    calculateTotalPlusPrice(holder, oldMedicineQuantity, perPiecePrice, sheetSize, dbModel,newQuantity);
                    saveChangedQuantity(dbModel,newQuantity,formattedTotalPrice);
                } else {
                    calculateTotalMinusPrice(holder, oldMedicineQuantity, perPiecePrice, sheetSize, dbModel,newQuantity);
                    saveChangedQuantity(dbModel,newQuantity,formattedTotalPrice);
                }
            } else {
                holder.minus.setEnabled(true);
                holder.plus.setEnabled(true);
                closeLoadingScreen();
            }
        } else {
            double totalPrice = Integer.parseInt(holder.quantity.getText().toString()) * perPiecePrice;  //here perPiece Price is unit price for syrups
            holder.medicinePrice.setText(String.valueOf(totalPrice));
            if (checkOutLayout.getVisibility() == View.VISIBLE && itemPostion.contains(String.valueOf(holder.getAdapterPosition()))) {
                if (isPlus) {
                    calculateTotalPlusPrice(holder, oldMedicineQuantity, perPiecePrice, sheetSize, dbModel,newQuantity);
                    saveChangedQuantity(dbModel,newQuantity,String.valueOf(totalPrice));
                } else {
                    calculateTotalMinusPrice(holder, oldMedicineQuantity, perPiecePrice, sheetSize, dbModel,newQuantity);
                    saveChangedQuantity(dbModel,newQuantity,String.valueOf(totalPrice));
                }
            } else {
                holder.minus.setEnabled(true);
                holder.plus.setEnabled(true);
                closeLoadingScreen();
            }
        }
    }

    public void calculateTotalPlusPrice(CartAdapter.myViewHolder holder, int oldMedicineQuantity, double perPiecePrice, int sheetSize, CartModel dbModel,int newQuantity) {
        if (dbModel.getMedicineType().equalsIgnoreCase("tablet")) {
            double medicinePrice = oldMedicineQuantity * perPiecePrice * sheetSize;
            calculatedTotalPrice = calculatedTotalPrice - medicinePrice;
            calculatedTotalPrice = Double.parseDouble(holder.medicinePrice.getText().toString()) + calculatedTotalPrice;
            totalPrice.setText(String.valueOf(Math.round(calculatedTotalPrice + 60.00)));
            holder.plus.setEnabled(true);
        } else {
            double medicinePrice = oldMedicineQuantity * perPiecePrice; //here perPiece Price is unit price for syrups
            calculatedTotalPrice = calculatedTotalPrice - medicinePrice;
            calculatedTotalPrice = Double.parseDouble(holder.medicinePrice.getText().toString()) + calculatedTotalPrice;
            totalPrice.setText(String.valueOf(Math.round(calculatedTotalPrice + 60.00)));
            holder.plus.setEnabled(true);
        }

    }

    public void calculateTotalMinusPrice(CartAdapter.myViewHolder holder, int oldMedicineQuantity, double perPiecePrice, int sheetSize, CartModel dbModel,int newQuantity) {
        if (dbModel.getMedicineType().equalsIgnoreCase("tablet")) {
            double medicinePrice = (oldMedicineQuantity) * perPiecePrice * sheetSize;
            calculatedTotalPrice = calculatedTotalPrice - medicinePrice;
            calculatedTotalPrice = calculatedTotalPrice + Double.parseDouble(holder.medicinePrice.getText().toString());
            totalPrice.setText(String.valueOf(Math.round(calculatedTotalPrice + 60.00)));
            holder.minus.setEnabled(true);

        } else {
            double medicinePrice = (oldMedicineQuantity) * perPiecePrice; //here perPiece Price is unit price for syrups
            calculatedTotalPrice = calculatedTotalPrice - medicinePrice;
            calculatedTotalPrice = calculatedTotalPrice + Double.parseDouble(holder.medicinePrice.getText().toString());
            totalPrice.setText(String.valueOf(Math.round(calculatedTotalPrice + 60.00)));
            holder.minus.setEnabled(true);

        }

    }

    public void cardSelection(CartAdapter.myViewHolder holder, CartModel dbModel) {
        if (selectedCard.cards.contains(dbModel.getMedicineId())) {
            selectedCard.cards.remove(dbModel.getMedicineId());
            itemPostion.remove(String.valueOf(holder.getAdapterPosition()));
            holder.circle.setImageResource(R.drawable.blank_circle);
            deductTotalPrice(holder);
            if (selectedCard.cards.size() == 0) {
                remove.setVisibility(View.GONE);
                checkOutLayout.setVisibility(View.GONE);
                calculatedTotalPrice = 0.00;
            }
        } else {
            itemPostion.add(String.valueOf(holder.getAdapterPosition()));
            selectedCard.cards.add(dbModel.getMedicineId());
            holder.circle.setImageResource(R.drawable.checkedcircle);
            addTotalPrice(holder);
            if (remove.getVisibility() == View.GONE && checkOutLayout.getVisibility() == View.GONE) {
                remove.setVisibility(View.VISIBLE);
                checkOutLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public void addTotalPrice(CartAdapter.myViewHolder holder) {
        calculatedTotalPrice = Double.parseDouble(holder.medicinePrice.getText().toString()) + calculatedTotalPrice;
        totalPrice.setText(String.valueOf(Math.round(calculatedTotalPrice + 60.00)));

    }

    public void deductTotalPrice(CartAdapter.myViewHolder holder) {
        calculatedTotalPrice = calculatedTotalPrice - Double.parseDouble(holder.medicinePrice.getText().toString());
        totalPrice.setText(String.valueOf(Math.round(calculatedTotalPrice + 60.00)));
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        ImageView medicineImage, plus, minus, circle;
        TextView medicineName, medicineDosage, medicineBrand, medicinePrice, quantity;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            medicineImage = itemView.findViewById(R.id.medicinePicture);
            medicineName = itemView.findViewById(R.id.medicineName);
            medicineDosage = itemView.findViewById(R.id.medicineDosage);
            medicineBrand = itemView.findViewById(R.id.medicineBrandName);
            medicinePrice = itemView.findViewById(R.id.medicinePrice);
            quantity = itemView.findViewById(R.id.quantity);
            plus = itemView.findViewById(R.id.plus);
            minus = itemView.findViewById(R.id.minus);
            circle = itemView.findViewById(R.id.circle);
        }
    }
    public void resetCalculatedPrice(){
        calculatedTotalPrice = 0.0;
    }
    public Double getCalculatedPrice(){
        return calculatedTotalPrice;
    }
}
