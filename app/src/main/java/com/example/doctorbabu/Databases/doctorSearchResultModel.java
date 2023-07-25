package com.example.doctorbabu.Databases;

public class doctorSearchResultModel {
    String doctorNameAndId, department, profilePicture;

    public doctorSearchResultModel() {
    }

    public String getDoctorNameAndId() {
        return doctorNameAndId;
    }

    public void setDoctorNameAndId(String doctorNameAndId) {
        this.doctorNameAndId = doctorNameAndId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }
}
