package com.example.doctorbabu.Databases;

public class availableDoctorModel {
    String area,bmdc,dateofBirth,district,doctorId,doctorType,email,fullName,gender,nid,photoUrl,title,uId;
    float rating;

    public availableDoctorModel() {
    }

    public availableDoctorModel(String area, String bmdc, String dateofBirth, String district, String doctorId, String doctorType, String email, String fullName, String gender, String nid, String photoUrl, float rating, String title, String uId) {
        this.area = area;
        this.bmdc = bmdc;
        this.dateofBirth = dateofBirth;
        this.district = district;
        this.doctorId = doctorId;
        this.doctorType = doctorType;
        this.email = email;
        this.fullName = fullName;
        this.gender = gender;
        this.nid = nid;
        this.photoUrl = photoUrl;
        this.rating = rating;
        this.title = title;
        this.uId = uId;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getBmdc() {
        return bmdc;
    }

    public void setBmdc(String bmdc) {
        this.bmdc = bmdc;
    }

    public String getDateofBirth() {
        return dateofBirth;
    }

    public void setDateofBirth(String dateofBirth) {
        this.dateofBirth = dateofBirth;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorType() {
        return doctorType;
    }

    public void setDoctorType(String doctorType) {
        this.doctorType = doctorType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }
}
