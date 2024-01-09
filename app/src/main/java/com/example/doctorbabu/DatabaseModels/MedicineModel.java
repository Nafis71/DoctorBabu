package com.example.doctorbabu.DatabaseModels;

public class MedicineModel {
    String administrationOfTheMedicine,alcoholEffect,brandName,drivingEffect,genericName,kidneyEffect,liverEffect,medicineDosage ="0",medicineId,medicineQuantity,
            medicineName,medicineType,medicinePataSize,medicinePerPiecePrice,medicinePicture,overdoseEffects,pregnancyAndLactation,sideEffect,storageCondition,bottleSize="0",description,unitPrice;

    public MedicineModel() {
    }

    public String getMedicineType() {
        return medicineType;
    }

    public String getBottleSize() {
        return bottleSize;
    }

    public String getDescription() {
        return description;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public String getMedicineQuantity() {
        return medicineQuantity;
    }

    public String getAdministrationOfTheMedicine() {
        return administrationOfTheMedicine;
    }

    public String getAlcoholEffect() {
        return alcoholEffect;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getDrivingEffect() {
        return drivingEffect;
    }

    public String getGenericName() {
        return genericName;
    }

    public String getKidneyEffect() {
        return kidneyEffect;
    }

    public String getLiverEffect() {
        return liverEffect;
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

    public String getMedicinePataSize() {
        return medicinePataSize;
    }

    public String getMedicinePerPiecePrice() {
        return medicinePerPiecePrice;
    }

    public String getMedicinePicture() {
        return medicinePicture;
    }

    public String getOverdoseEffects() {
        return overdoseEffects;
    }

    public String getPregnancyAndLactation() {
        return pregnancyAndLactation;
    }

    public String getSideEffect() {
        return sideEffect;
    }

    public String getStorageCondition() {
        return storageCondition;
    }
}
