package com.example.doctorbabu.DatabaseModels;

public class recentlyViewedDoctorModel {
    String doctorId, photoUrl;
    int onlineStatus;

    public recentlyViewedDoctorModel() {
    }

    public int getOnlineStatus() {
        return onlineStatus;
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
