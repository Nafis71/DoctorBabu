package com.example.doctorbabu.Databases;

public class profileEditUserHelper { //I have made this class to update profile data without altering photo url in firebase.
    String address,age,email,fullName,gender,height,phone,weight;
    profileEditUserHelper(){}

    public profileEditUserHelper(String address, String age, String email, String fullName, String gender, String height, String phone, String weight) {
        this.address = address;
        this.age = age;
        this.email = email;
        this.fullName = fullName;
        this.gender = gender;
        this.height = height;
        this.phone = phone;
        this.weight = weight;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
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

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
