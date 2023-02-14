package com.google.cardboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PatientDataDisplay extends AppCompatActivity {
    TextView nameDisplay;
    TextView genreDisplay;
    TextView ageDisplay;

    TextView meanDisplay;
    TextView varianceDisplay;
    TextView standardDeviationDisplay;
    TextView commentDisplay;

    Button editButton;
    Button deleteButton;

    String patientFile = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_data_display);

        nameDisplay = findViewById(R.id.nameDisplay);
        genreDisplay = findViewById(R.id.genreDisplay);
        ageDisplay = findViewById(R.id.ageDisplay);

        meanDisplay = findViewById(R.id.meanDisplay);
        varianceDisplay = findViewById(R.id.varianceDisplay);
        standardDeviationDisplay = findViewById(R.id.standardDeviationDisplay);

        commentDisplay = findViewById(R.id.commentDisplay);

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
                PatientData.getPatient(patientFile).Delete();
                Intent intent = new Intent(getBaseContext(), FilesDisplayActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
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

    /**
     * Displays patient data in the activity_patient_data_display_activity
     * @param patientData The patient info
     */
    void DisplayPatientData(PatientData patientData) {
        nameDisplay.setText(patientData.patientName);
        genreDisplay.setText(GetGenreString(patientData));
        ageDisplay.setText(patientData.age+"");

        if (patientData.measurement != null) {
            meanDisplay.setText(patientData.mean.toString());
            varianceDisplay.setText(patientData.variance.toString());
            standardDeviationDisplay.setText(patientData.standardDeviation.toString());
        } else {
            meanDisplay.setText("-");
            varianceDisplay.setText("-");
            standardDeviationDisplay.setText("-");
        }

        Log.d("comment", patientData.comment);
        commentDisplay.setText(patientData.comment);
    }

    String GetGenreString(PatientData patientData) {
        switch (patientData.genre) {
            case male :
                return "M";

            case female :
                return "F";
        }
        return "Error";
    }
}