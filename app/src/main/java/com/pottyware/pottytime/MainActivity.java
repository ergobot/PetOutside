package com.pottyware.pottytime;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.iid.InstanceID;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {


    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabase = database.getReference("pottyevent");
    DatabaseReference mDevicesReference = database.getReference("registereddevices");

    Button pottyMode;
    Button registerDevice;
    Button deleteDevice;

    FirebaseBackgroundService firebaseBackgroundService;
    boolean mBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            final String name = user.getDisplayName();
            final String email = user.getEmail();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            final String uid = user.getUid();

            mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        User value = dataSnapshot.getValue(User.class);
                        Log.d(TAG, "Value is: " + value);

                    } else {

                        writeNewUser(uid, name, email, new ArrayList<Device>());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            // registerdevice
            String iid = InstanceID.getInstance(getApplicationContext()).getId();
            String message = "Its potty time!; " + new Date().getTime();
            Device device = new Device(uid, message, iid);
            mDevicesReference.child(iid).setValue(device);

        } else {
            returnToLogin();
        }

        // Start the background Firebase activity
//        startService(new Intent(FirebaseBackgroundService.class.getName()));
        startService(new Intent(this, FirebaseBackgroundService.class));


        pottyMode = (Button) findViewById(R.id.pottymode);
        pottyMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent pottyMode = new Intent(MainActivity.this, PottyActivity.class);
                startActivity(pottyMode);
            }
        });

        registerDevice = (Button) findViewById(R.id.registerdevice);
        registerDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new MaterialDialog.Builder(MainActivity.this)
                        .title("Add a device")
                        .inputRangeRes(2, 20, R.color.color0)
                        .input(null, null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {


                                registerDevice(String.valueOf(input));

                            }
                        }).show();

            }
        });
        deleteDevice = (Button) findViewById(R.id.deletedevice);
        deleteDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // Name, email address, and profile photo Url
                    final String name = user.getDisplayName();
                    final String email = user.getEmail();

                    // The user's ID, unique to the Firebase project. Do NOT use this value to
                    // authenticate with your backend server, if you have one. Use
                    // FirebaseUser.getToken() instead.
                    final String uid = user.getUid();

                    final String iid = InstanceID.getInstance(getApplicationContext()).getId();

                    mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                User value = dataSnapshot.getValue(User.class);
                                Log.d(TAG, "Value is: " + value);

                                if (value.devices != null && value.devices.size() > 0) {
                                    ArrayList<String> deviceids = new ArrayList<String>();
                                    for (Device d : value.devices) {
                                        deviceids.add(d.iid);
                                    }
                                    showRemovalDialog(deviceids.toArray(new String[0]));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        TextView deviceId = (TextView) findViewById(R.id.deviceid);
        deviceId.setText(InstanceID.getInstance(getApplicationContext()).getId());


    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, FirebaseBackgroundService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            FirebaseBackgroundService.LocalBinder binder = (FirebaseBackgroundService.LocalBinder) service;
            firebaseBackgroundService = binder.getFirebaseBackgroundServiceInstance();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };



    private void showRemovalDialog(final String[] allDevices) {
        new MaterialDialog.Builder(MainActivity.this)
                .title("Remove device")
                .items(allDevices)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        for (Integer i : which) {
                            removeDevice(allDevices[i]);
                        }

                        /**
                         * If you use alwaysCallMultiChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected check box to actually be selected.
                         * See the limited multi choice dialog example in the sample project for details.
                         **/
                        return true;
                    }
                })
                .positiveText("Delete")
                .show();

    }

    private void getRegisterDevices() {

    }

    private void writeNewUser(String userId, String name, String email, ArrayList<Device> devices) {
        User user = new User(name, email, devices);

        mDatabase.child("users").child(userId).setValue(user);
    }

    private void returnToLogin() {
        Intent loginIntent = new Intent(MainActivity.this, GoogleSignInActivity.class);
        startActivity(loginIntent);
    }

    private void registerDevice(final String device) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            final String name = user.getDisplayName();
            final String email = user.getEmail();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            final String uid = user.getUid();

            final String iid = InstanceID.getInstance(getApplicationContext()).getId();

            mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        User value = dataSnapshot.getValue(User.class);
                        Log.d(TAG, "Value is: " + value);
                        Device registeredDevice = new Device(uid, "", device);
                        if (value.devices == null) {
                            value.devices = new ArrayList<Device>();
                            value.devices.add(registeredDevice);
                            mDatabase.child("users").child(uid).setValue(value);

                            Log.i("here", "here");
                            // here
                            stopService(new Intent(MainActivity.this, FirebaseBackgroundService.class));
                            startService(new Intent(MainActivity.this, FirebaseBackgroundService.class));
                        } else {
                            if (value.devices.contains(registeredDevice)) {
                                value.devices.add(registeredDevice);
                                mDatabase.child("users").child(uid).setValue(value);

                                Log.i("here", "here");
                                // here
                                stopService(new Intent(MainActivity.this, FirebaseBackgroundService.class));
                                startService(new Intent(MainActivity.this, FirebaseBackgroundService.class));
                            }
                        }


                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void removeDevice(final String device) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            final String name = user.getDisplayName();
            final String email = user.getEmail();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            final String uid = user.getUid();

            final String iid = InstanceID.getInstance(getApplicationContext()).getId();

            mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        User value = dataSnapshot.getValue(User.class);
                        Log.d(TAG, "Value is: " + value);
                        Device registeredDevice = new Device(uid, "", device);
                        if (value.devices != null) {

                            for (int i = 0; i < value.devices.size(); i++) {
                                if (value.devices.get(i).iid.equals(registeredDevice.iid)) {
//                                    value.devices.get(i).delete = true;
                                    value.devices.remove(i);
                                    mDatabase.child("users").child(uid).setValue(value);
//                                    firebaseBackgroundService.refresh();
                                }
                            }
//                            FirebaseBackgroundService firebaseBackgroundService;
                            if(mBound) {
//                                firebaseBackgroundService.removeAllListeners();
                            }
//                            stopService(new Intent(MainActivity.this, FirebaseBackgroundService.class));
//                            startService(new Intent(MainActivity.this, FirebaseBackgroundService.class));
                        }


                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

}
