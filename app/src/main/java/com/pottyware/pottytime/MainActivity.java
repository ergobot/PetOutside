package com.pottyware.pottytime;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {



    Button pottyMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
}
