package com.example.doctorbabu.DatabaseModels;

public class diagnoseReportModel {
     String reportName,reportLink,reportId;
     public diagnoseReportModel(){};

     public static diagnoseReportModel instance = null;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public static diagnoseReportModel getInstance(){
         if(instance == null){
             instance = new diagnoseReportModel();
         }
         return instance;
     }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportLink() {
        return reportLink;
    }

    public void setReportLink(String reportLink) {
        this.reportLink = reportLink;
    }

    public static void setInstance(diagnoseReportModel instance) {
        diagnoseReportModel.instance = instance;
    }
}
