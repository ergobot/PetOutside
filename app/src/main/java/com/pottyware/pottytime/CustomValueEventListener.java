package com.pottyware.pottytime;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by morbo on 12/10/16.
 */



public class CustomValueEventListener implements ValueEventListener {

    boolean initialized = false;

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
