package com.google.cardboard;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class PatientDataDisplay extends AppCompatActivity {
    TextView nameDisplay;
    TextView genreDisplay;
    TextView ageDisplay;

    TextView meanDisplay;
    TextView varianceDisplay;
    TextView standardDeviationDisplay;
    TextView commentDisplay;


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

        String patientName = getIntent().getExtras().getString("patient");

        if (patientName != null){
            Log.d("patient data", patientName);
            PatientData patientData = PatientData.getPatient(patientName);
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