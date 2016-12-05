package com.pottyware.pottytime;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

                    if(dataSnapshot.exists()) {
                        User value = dataSnapshot.getValue(User.class);
                        Log.d(TAG, "Value is: " + value);

                    }else {

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
            Device device = new Device(uid,message,iid);
            mDevicesReference.child(iid).setValue(device);

        }else{
            returnToLogin();
        }

        // Start the background Firebase activity
//        startService(new Intent(FirebaseBackgroundService.class.getName()));
        startService(new Intent(this, FirebaseBackgroundService.class));


        pottyMode = (Button)findViewById(R.id.pottymode);
        pottyMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent pottyMode = new Intent(MainActivity.this, PottyActivity.class);
                startActivity(pottyMode);
            }
        });



    }

    private void writeNewUser(String userId, String name, String email, ArrayList<Device> devices) {
        User user = new User(name, email, devices);

        mDatabase.child("users").child(userId).setValue(user);
    }

    private void returnToLogin(){
        Intent loginIntent = new Intent(MainActivity.this, GoogleSignInActivity.class);
        startActivity(loginIntent);
    }

}
