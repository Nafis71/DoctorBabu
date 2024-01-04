package com.example.doctorbabu.DatabaseModels;

public class CartModel {
    String medicineId, quantity,totalPrice,medicineType;

    public CartModel() {
    }

    public String getMedicineType() {
        return medicineType;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getTotalPrice() {
        return totalPrice;
    }
}
