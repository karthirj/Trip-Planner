package com.example.homework7a;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class Chat implements Serializable {
    public String message;
    public String creatorEmail;
    public String creatorName;
    public String msgImgUrl;

    public String getTrip_name() {
        return trip_name;
    }

    public void setTrip_name(String trip_name) {
        this.trip_name = trip_name;
    }

    public String trip_name;

    @ServerTimestamp
    private Date timestamp;

    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getMsgImgUrl() {
        return msgImgUrl;
    }

    public void setMsgImgUrl(String msgImgUrl) {
        this.msgImgUrl = msgImgUrl;
    }
}
