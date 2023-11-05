package com.example.doctorbabu.DatabaseModels;

public class doctorPastExperienceModel {

    String hospitalName, department, designation, joiningDate, leavingDate;

    public doctorPastExperienceModel() {
    }

    public doctorPastExperienceModel(String hospitalName, String department, String designation, String joiningDate, String leavingDate) {
        this.hospitalName = hospitalName;
        this.department = department;
        this.designation = designation;
        this.joiningDate = joiningDate;
        this.leavingDate = leavingDate;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(String joiningDate) {
        this.joiningDate = joiningDate;
    }

    public String getLeavingDate() {
        return leavingDate;
    }

    public void setLeavingDate(String leavingDate) {
        this.leavingDate = leavingDate;
    }
}
