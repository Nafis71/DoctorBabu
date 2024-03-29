package com.example.doctorbabu.DatabaseModels;

import android.net.Uri;

public class PdfModel {
    Uri uri;
    String fileName;
    String fileType;
    boolean fromMyDocuments;

    public boolean isFromMyDocuments() {
        return fromMyDocuments;
    }

    public void setFromMyDocuments(boolean fromMyDocuments) {
        this.fromMyDocuments = fromMyDocuments;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public PdfModel() {
    }
}
