package com.example.doctorbabu.DatabaseModels;

public class recentlyViewedDoctorModel {
    String doctorId, photoUrl;

    public recentlyViewedDoctorModel() {
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
