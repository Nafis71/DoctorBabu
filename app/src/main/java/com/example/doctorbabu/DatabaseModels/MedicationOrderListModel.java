package com.example.doctorbabu.DatabaseModels;

public class MedicationOrderListModel {
    String customerName,deliveryAddress,orderId,phoneNumber,productId,productQuantity,totalPrice,medicineType;
    long orderTime;
    int trackOrder;

    public String getMedicineType() {
        return medicineType;
    }

    public int getTrackOrder() {
        return trackOrder;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductQuantity() {
        return productQuantity;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public long getOrderTime() {
        return orderTime;
    }

    public String getCustomerName() {
        return customerName;
    }

    public MedicationOrderListModel() {
    }
}
