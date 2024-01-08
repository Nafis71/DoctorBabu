package com.example.doctorbabu.DatabaseModels;

public class MedicineSearchModel {
    String brandName,genericName,medicineDosage="0",medicineId, medicineName,medicinePicture,bottleSize="0",medicineType;

    public MedicineSearchModel() {
    }

    public String getMedicineType() {
        return medicineType;
    }

    public String getBottleSize() {
        return bottleSize;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getGenericName() {
        return genericName;
    }

    public String getMedicineDosage() {
        return medicineDosage;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getMedicinePicture() {
        return medicinePicture;
    }
}
