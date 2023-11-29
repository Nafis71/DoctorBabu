package com.example.doctorbabu.DatabaseModels;

public class AppointmentModel {
    String appointmentHour, appointmentMinute,timePeriod,appointmentDate;
    private AppointmentModel(){

    }

    public static AppointmentModel instance = null;
    public static AppointmentModel getInstance(){
        if(instance == null){
            instance = new AppointmentModel();
        }
        return instance;
    }

    public String getAppointmentHour() {
        return appointmentHour;
    }

    public void setAppointmentHour(String appointmentHour) {
        this.appointmentHour = appointmentHour;
    }

    public String getAppointmentMinute() {
        return appointmentMinute;
    }

    public void setAppointmentMinute(String appointmentMinute) {
        this.appointmentMinute = appointmentMinute;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }
}
