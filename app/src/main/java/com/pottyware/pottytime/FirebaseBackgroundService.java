package com.pottyware.pottytime;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

import static android.content.ContentValues.TAG;

//import com.firebase.client.DataSnapshot;
//import com.firebase.client.Firebase;
//import com.firebase.client.ValueEventListener;

public class FirebaseBackgroundService extends Service {

//    private Firebase f = new Firebase("https://somedemo.firebaseio-demo.com/");
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("pottyevent");
    DatabaseReference mDevicesReference = database.getReference("registereddevices");



    private ValueEventListener handler;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    ArrayList<ValueEventListener> deviceListeners = new ArrayList<ValueEventListener>();

    @Override
    public void onCreate() {
        super.onCreate();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            myRef.child("users").child(uid).child("broadcastdevices").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    GenericTypeIndicator<ArrayList<Device>> g = new GenericTypeIndicator<ArrayList<Device>>() {
                        @Override
                        public String toString() {
                            return super.toString();
                        }
                    };
                    ArrayList<Device> devices = dataSnapshot.getValue(g);

                    // remove the listeners
                    if (deviceListeners != null && deviceListeners.size() > 0) {
                        for (ValueEventListener oldListener : deviceListeners) {
                            myRef.removeEventListener(oldListener);
                        }
                    }
                    // create new listeners list
                    deviceListeners = new ArrayList<ValueEventListener>();
                    // add listeners
                    for (Device device : devices) {
                        ValueEventListener deviceEvent = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                String value = dataSnapshot.getValue(String.class);
                                Log.d(TAG, "Value is: " + value);
                                String message = value;
                                postNotif(message);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        };
                        deviceListeners.add(deviceEvent);
                        myRef.addValueEventListener(deviceEvent);
                        mDevicesReference.child(device.iid).child("device").child("message").addValueEventListener(deviceEvent);
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            myRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("message").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    String value = dataSnapshot.getValue(String.class);
                    Log.d(TAG, "Value is: " + value);
                    postNotif(value);

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
//        // Read from the database
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                // This method is called once with the initial value and again
//                // whenever data at this location is updated.
//                String value = dataSnapshot.getValue(String.class);
//                Log.d(TAG, "Value is: " + value);
//                postNotif(value);
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
//            }
//        });

//        handler = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot arg0) {
//                postNotif(arg0.getValue().toString());
//            }
//
//            @Override
//            public void onCancelled() {
//            }
//        };
//
//        f.addValueEventListener(handler);
        }
    }

    private void postNotif(String notifString) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        // build notification
        // the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.Builder(this)
                .setContentTitle("Potty Time")
                .setContentText("Open the door, its potty time")
                .setSmallIcon(R.drawable.ic_menu_send)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setStyle(new Notification.BigTextStyle().bigText("")).build();
        //  .addAction(R.drawable.line, "", pIntent).build();
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, n);
    }

}