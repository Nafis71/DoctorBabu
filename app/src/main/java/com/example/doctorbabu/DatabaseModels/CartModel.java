package com.example.doctorbabu.DatabaseModels;

public class CartModel {
    String medicineId, medicineSheets,totalPrice;

    public CartModel() {
    }

    public String getMedicineId() {
        return medicineId;
    }

    public String getMedicineSheets() {
        return medicineSheets;
    }

    public String getTotalPrice() {
        return totalPrice;
    }
}
