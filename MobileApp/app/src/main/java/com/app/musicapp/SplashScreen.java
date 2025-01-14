package com.app.musicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        // Delay for 3 seconds (3000 milliseconds) before transitioning to MainActivity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Intent to start MainActivity
                Intent intent = new Intent(SplashScreen.this, GoogleSignInActivity.class);
                startActivity(intent);
                finish();  // Finish the SplashActivity so the user can't return to it
            }
        }, 1000);
    }

}
