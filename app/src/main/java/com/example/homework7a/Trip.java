package com.example.homework7a;

/*
Assignment : Homework 7
Group : Group1_11
 */

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

public class Trip implements Serializable {
    public String title;

    @Override
    public boolean equals(@Nullable Object obj) {
        boolean result = false;
        if (obj == null || obj.getClass() != getClass()) {
            result = false;
        } else {
            Trip trip = (Trip) obj;
            if (this.creatorName.equals(trip.getCreatorName()) && this.tripPhotoUrl.equals(trip.getTripPhotoUrl())   && this.lat == trip.getLat()) {
                result = true;
            }
        }
        return result;
    }

    public String creatorName;
    public double lat, longi;
    public String tripPhotoUrl;
    public ArrayList<String> userList;
    public String creatorEmail;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    public String getTripPhotoUrl() {
        return tripPhotoUrl;
    }

    public void setTripPhotoUrl(String tripPhotoUrl) {
        this.tripPhotoUrl = tripPhotoUrl;
    }

    public ArrayList<String> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<String> userList) {
        this.userList = userList;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }
}

