package com.example.doctorbabu.DatabaseModels;

public class AppointmentModel {
    String appointmentDate,appointmentHour,appointmentID,appointmentMinute,doctorID,patientID,timePeriod,broadcastCode;

    public AppointmentModel(){

    }

    public String getBroadcastCode() {
        return broadcastCode;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public String getAppointmentHour() {
        return appointmentHour;
    }

    public String getAppointmentID() {
        return appointmentID;
    }

    public String getAppointmentMinute() {
        return appointmentMinute;
    }

    public String getDoctorID() {
        return doctorID;
    }

    public String getPatientID() {
        return patientID;
    }

    public String getTimePeriod() {
        return timePeriod;
    }
}
