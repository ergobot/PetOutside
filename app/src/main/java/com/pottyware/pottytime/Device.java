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

    public boolean equals(Device device){

        if(device == null){
            return false;
        }
        if(device.iid == null || device.uid == null){
            return false;
        }
        if(this.iid == null || this.uid == null){
            return false;
        }
        if(device.iid.equals(this.iid) && device.uid.equals(this.uid)){
            return true;
        }
        return false;
    }

}