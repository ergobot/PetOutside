package com.pottyware.pottytime;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class User {

    public String username;
    public String email;

    public ArrayList<Device> devices;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, ArrayList<Device> devices) {
        this.devices = devices;
        this.username = username;
        this.email = email;
    }

}