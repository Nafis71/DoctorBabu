package com.example.doctorbabu.Databases;

public class userHelper {
    String fullName, email ,phone, birthDate = "null", address = "null",photoUrl ="null",height="null",weight="null",gender="null",district="null",area="null";

    public userHelper(){}  //just for the firebase so that it can't throw any error



    public userHelper(String fullName, String email, String phone, String birthDate, String gender, String height, String weight, String district,String area) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.district = district;
        this.area = area;
    }
    public userHelper(String fullName, String email, String phone, String birthDate, String gender, String height, String weight, String address,String district,String area) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.district = district;
        this.area = area;
        this.address = address;
    }
    public String getBirthDate() {
        return birthDate;
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

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

