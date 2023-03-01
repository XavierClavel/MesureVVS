package com.google.cardboard;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Activité permettant l'affichage des données du patient
 */
public class PatientDataDisplay extends AppCompatActivity {
    TextView nameDisplay;
    TextView firstNameDisplay;
    TextView genreDisplay;
    TextView ageDisplay;
    TextView commentDisplay;

    Button editButton;
    Button deleteButton;

    LinearLayout layoutVVS_simple;
    LinearLayout layoutVVS_dynamique;

    String patientFile = null;

    AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_data_display);

        alertDialog = CreateAlertMessage();

        nameDisplay = findViewById(R.id.nameDisplay);
        firstNameDisplay = findViewById(R.id.firstNameDisplay);
        genreDisplay = findViewById(R.id.genreDisplay);
        ageDisplay = findViewById(R.id.ageDisplay);


        commentDisplay = findViewById(R.id.commentDisplay);

        layoutVVS_simple = findViewById(R.id.layoutVVS_simple);
        layoutVVS_dynamique = findViewById(R.id.layoutVVS_dynamique);

        editButton = findViewById(R.id.buttonEditFile);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), PatientCreationActivity.class);
                intent.putExtra("patient", patientFile);
                startActivity(intent);
            }
        });

        deleteButton = findViewById(R.id.buttonDeleteFile);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.show();
            }
        });

        patientFile = getIntent().getExtras().getString("patient");

        if (patientFile != null){
            Log.d("patient data", patientFile);
            PatientData patientData = PatientData.getPatient(patientFile);
            DisplayPatientData(patientData);

        }
        else {
            Log.d("patient data", "no patient");
        }

    }

    AlertDialog CreateAlertMessage() {
        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this patient file ? This operation is irreversible")
            //.setCancelable(true)
            .setPositiveButton("Delete file", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // Delete patient file
                    DeleteFile();
                }
            })
            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
        Log.d("dialog", "created");
        return builder.create();
    }

    void DeleteFile() {
        PatientData.getPatient(patientFile).Delete();
        Intent intent = new Intent(getBaseContext(), FilesDisplayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Displays patient data in the activity_patient_data_display_activity
     * @param patientData The patient info
     */
    void DisplayPatientData(PatientData patientData) {
        nameDisplay.setText(patientData.lastName);
        firstNameDisplay.setText(patientData.firstName);
        genreDisplay.setText(GetGenreString(patientData));

        ageDisplay.setText(patientData.age+"");

        Log.d("comment", patientData.comment);
        commentDisplay.setText(patientData.comment);

        ArrayList<Measurement> measurements = XmlManager.ReadMeasurements(patientData.getMeasurementsFile(), patientData);
        for (Measurement measurement : measurements) {
            DisplayMeasurementsSeries(measurement);
        }

    }

    void DisplayMeasurementsSeries(Measurement measurement) {
        String name = measurement.date;
        Boolean isSimpleVVS = measurement.isSimpleVVS;
        ArrayList<Float> values = measurement.valuesRight;
        Log.d("measurements", "displaying");
        LinearLayout measurementsDisplay = isSimpleVVS ? layoutVVS_simple : layoutVVS_dynamique;
        TextView nameDisplay = new TextView(this);
        nameDisplay.setText(name);
        nameDisplay.setTypeface(null, Typeface.BOLD);
        measurementsDisplay.addView(nameDisplay);

        TextView valueDisplay;
        /*
        for (float value : values) {
            valueDisplay = new TextView(this);
            //TODO : régler le nombres de décimales
            valueDisplay.setText(value+"");
            measurementsDisplay.addView(valueDisplay);
        }
         */

        valueDisplay = new TextView(this);
        valueDisplay.setText("Nombre de mesures avec une rotation à gauche : " + measurement.valuesLeft.size());
        measurementsDisplay.addView(valueDisplay);

        valueDisplay = new TextView(this);
        valueDisplay.setText("Nombre de mesures avec une rotation à droite : " + measurement.valuesRight.size());
        measurementsDisplay.addView(valueDisplay);

        valueDisplay = new TextView(this);
        valueDisplay.setText("Moyenne : " + measurement.mean);
        measurementsDisplay.addView(valueDisplay);

        valueDisplay = new TextView(this);
        valueDisplay.setText("Variance : " + measurement.variance);
        measurementsDisplay.addView(valueDisplay);

        valueDisplay = new TextView(this);
        valueDisplay.setText("Ecart-type : " + measurement.standardDeviation);
        measurementsDisplay.addView(valueDisplay);

    }

    public static String GetGenreString(PatientData patientData) {
        switch (patientData.genre) {
            case male :
                return "M";

            case female :
                return "F";
        }
        return "Error";
    }



}