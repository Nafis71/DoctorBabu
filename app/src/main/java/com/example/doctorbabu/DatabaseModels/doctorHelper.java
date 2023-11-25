package com.example.doctorbabu.DatabaseModels;

public class doctorHelper {
    String uId, doctorId, title, fullName, email, district, area, doctorType, gender, bmdc, nid, dateofBirth, photoUrl = "null", about = "null";
    int rating = 0, onlineStatus = 0;


    public String getDoctorId() {
        return doctorId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public int getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(int onlineStatus) {
        this.onlineStatus = onlineStatus;
    }


    public doctorHelper(String uId, String doctorId, String title, String fullName, String email, String district, String area, String doctorType, String gender, String bmdc, String nid, String dateofBirth) {
        this.uId = uId;
        this.doctorId = doctorId;
        this.title = title;
        this.fullName = fullName;
        this.email = email;
        this.district = district;
        this.area = area;
        this.doctorType = doctorType;
        this.gender = gender;
        this.bmdc = bmdc;
        this.nid = nid;
        this.dateofBirth = dateofBirth;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }



    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBmdc() {
        return bmdc;
    }

    public void setBmdc(String bmdc) {
        this.bmdc = bmdc;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
