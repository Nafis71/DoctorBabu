package com.example.doctorbabu.Databases;

public class prescriptionModel {
    String PrescribedBy,PrescribedTo,date,diagnosis;

    public String getPrescribedBy() {
        return PrescribedBy;
    }

    public void setPrescribedBy(String prescribedBy) {
        PrescribedBy = prescribedBy;
    }

    public String getPrescribedTo() {
        return PrescribedTo;
    }

    public void setPrescribedTo(String prescribedTo) {
        PrescribedTo = prescribedTo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public prescriptionModel(){}
}
