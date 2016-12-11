package com.pottyware.pottytime;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Device {

    public String iid;
    public String uid;
    public String message;
    public boolean delete;

    public Device() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Device(String uid, String message, String iid) {
        this.iid = iid;
        this.uid = uid;
        this.message = message;
    }

}