package com.google.cardboard;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class PlaceholderActivity extends AppCompatActivity {

    //Communication du score à l'activité HomeActivity
    public static final String RESULT_SCORE = "RESULT_SCORE";

    private final String NAME = "Vertical_Subjective_Connection";

    private ArrayList<Float> mScore = new ArrayList<Float>();
    int nbMeasurements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);
        Log.d("Placeholder", "created");

        if (new Integer(getIntent().getExtras().getInt("nbMesures")) != null){
            nbMeasurements = getIntent().getExtras().getInt("nbMesures");
        } else {
           nbMeasurements = 3;
        }

        float min = -5f;
        float max = 5f;
        Random r = new Random();

        for (int i = 0; i < nbMeasurements; i++) {
            mScore.add(min + r.nextFloat() * (max - min));
        }

        endMeasure();
    }

    private void endMeasure() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mesure Terminée")
                .setMessage("Votre score est de :" + mScore)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.putExtra(RESULT_SCORE, mScore);  //request code
                        setResult(Activity.RESULT_OK, intent);  //result code
                        finish();
                    }
                })
                .create()
                .show();
    }
}