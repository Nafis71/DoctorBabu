package com.example.doctorbabu.DatabaseModels;

public class prescriptionMedicineModel {
    String medicineName,medicineDetails;

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getMedicineDetails() {
        return medicineDetails;
    }

    public void setMedicineDetails(String medicineDetails) {
        this.medicineDetails = medicineDetails;
    }

    public prescriptionMedicineModel() {
    }
}
