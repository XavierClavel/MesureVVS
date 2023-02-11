package com.google.cardboard;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class PatientDataDisplay extends AppCompatActivity {
    TextView nameDisplay;
    TextView genreDisplay;
    TextView ageDisplay;

    TextView meanDisplay;
    TextView varianceDisplay;
    TextView standardDeviationDisplay;


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
    }

    /**
     * Displays patient data in the activity_patient_data_display_activity
     * @param patientData The patient info
     */
    void DisplayPatientData(PatientData patientData) {
        nameDisplay.setText(patientData.patientName);
        genreDisplay.setText(GetGenreString(patientData));
        ageDisplay.setText(patientData.age+"");

        meanDisplay.setText(patientData.mean.toString());
        varianceDisplay.setText(patientData.variance.toString());
        standardDeviationDisplay.setText(patientData.standardDeviation.toString());
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