package com.example.doctorbabu.Databases;

public class userHelper {
    String fullName, email ,phone,profileComplete="0", birthDate = "null", address = "null",photoUrl ="null",height="null",weight="null",gender="null";
    // profileComplete= 0 means not fully completed

    public userHelper(){}  //just for the firebase so that it can't throw any error



    public userHelper(String fullName, String email, String phone, String birthDate, String gender, String height, String weight, String address) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.address = address;
    }
    public String getBirthDate() {
        return birthDate;
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

    public String getProfileComplete() {
        return profileComplete;
    }

    public void setProfileComplete(String profileComplete) {
        this.profileComplete = profileComplete;
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

