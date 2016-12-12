package com.pottyware.pottytime;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class FirebaseBackgroundService extends Service {

    //    private Firebase f = new Firebase("https://somedemo.firebaseio-demo.com/");
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("pottyevent");
    DatabaseReference mDevicesReference = database.getReference("registereddevices");


    private ValueEventListener handler;

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public FirebaseBackgroundService getFirebaseBackgroundServiceInstance() {
            return FirebaseBackgroundService.this;
        }
    }


    HashMap<String, ValueEventListener> deviceListeners = new HashMap<>();
    String uid = "";
    ArrayList<Device> devices = new ArrayList<Device>();
    User user;

    public void refresh() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            myRef.child("users").child(uid).child("devices").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    GenericTypeIndicator<ArrayList<Device>> g = new GenericTypeIndicator<ArrayList<Device>>() {
                        @Override
                        public String toString() {
                            return super.toString();
                        }
                    };
                    devices = dataSnapshot.getValue(g);

                    // remove previous listeners
                    removeAllListeners();

                    // add listeners
                    if (devices != null && devices.size() > 0) {
                        for (int i = 0; i < devices.size(); i++) {
                            final int index = i;
                            final Device device = devices.get(i);


//                            if(device.delete){
//                                devices.remove(index);
//                                myRef.child("users").child(uid).child("devices").setValue(devices);
//                                mDevicesReference.removeEventListener(deviceListeners.get(i));
//                                deviceListeners.remove(i);
//                            }else {

                            ValueEventListener v = mDevicesReference.child(device.iid).child("message").addValueEventListener(new CustomValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String message = dataSnapshot.getValue(String.class);
                                    Log.d(TAG, "Value is: " + message);
                                    if (initialized) {
                                        postNotif(message);
                                    } else {
                                        initialized = true;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            deviceListeners.put(device.iid, v);

                        }

                    }
                }


//                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        refresh();
    }

    private void postNotif(String notifString) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        // ringtone
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        // build notification
        // the addAction re-use the same intent to keep the example short
        Notification n = new Notification.Builder(this)
                .setContentTitle("Potty Time")
                .setContentText("Open the door, its potty time")
                .setSmallIcon(R.drawable.ic_menu_send)
                .setSound(alarmSound)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setStyle(new Notification.BigTextStyle().bigText("")).build();
        //  .addAction(R.drawable.line, "", pIntent).build();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, n);
    }

    public void removeAllListeners() {
        // remove the listeners
        if (deviceListeners != null && deviceListeners.size() > 0) {
            for (Map.Entry<String, ValueEventListener> entry : deviceListeners.entrySet()) {
                mDevicesReference.child(entry.getKey()).child("message").removeEventListener(entry.getValue());

            }
            deviceListeners.clear();
        }
    }

//    @Override
//    public boolean stopService(Intent service){
//        boolean flag = super.stopService(service);
//
//        // remove the listeners
//        if (deviceListeners != null && deviceListeners.size() > 0) {
//            for (ValueEventListener oldListener : deviceListeners) {
//                myRef.removeEventListener(oldListener);
//            }
//        }
//
//        return flag;
//    }

}