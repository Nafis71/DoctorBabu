package com.example.doctorbabu.DatabaseModels;

public class doctorReviewModel {
    String comment, userId;
    int rating;

    public doctorReviewModel() {
    }

    public doctorReviewModel(String comment, String userId, int rating) {
        this.comment = comment;
        this.userId = userId;
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
