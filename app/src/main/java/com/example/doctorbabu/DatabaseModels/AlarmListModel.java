package com.example.doctorbabu.DatabaseModels;

public class AlarmListModel {
    String id = null,medicineName,alarmType,amPm;
    int broadcastCode,hour,minute,alarmStatus;
    public AlarmListModel(){}

    public void setAmPm(String amPm) {
        this.amPm = amPm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public int getBroadcastCode() {
        return broadcastCode;
    }

    public void setBroadcastCode(int broadcastCode) {
        this.broadcastCode = broadcastCode;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getAlarmStatus() {
        return alarmStatus;
    }

    public void setAlarmStatus(int alarmStatus) {
        this.alarmStatus = alarmStatus;
    }
}
