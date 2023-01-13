package com.google.cardboard;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Affiche le nom de l'application et passe à HomeActivity
 */
public class MainActivity extends AppCompatActivity {
    public static int SPLASH_TIME_OUT = 2000;

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_main);
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent((Context)MainActivity.this, HomeActivity.class);
                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
            }
        },SPLASH_TIME_OUT);
    }
}
