package com.google.cardboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import java.util.HashMap;

public class PatientSelectionActivity extends AppCompatActivity {

    public static RadioGroup patientList;
    Button newPatientButton;
    HashMap<RadioButton,PatientData> radioButtonToPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_selection);
        patientList = findViewById(R.id.patientList);
        newPatientButton = findViewById(R.id.patientCreation);
        radioButtonToPatient = new HashMap();

        for (PatientData patientData : XmlManager.patientFiles) {
            RadioButton patientButton = new RadioButton(this);
            patientButton.setText(patientData.lastName + " " + patientData.firstName);
            patientList.addView(patientButton);
            radioButtonToPatient.put(patientButton,patientData);
            if (HomeActivity.selectedPatient != null && patientData.filename.equals(HomeActivity.selectedPatient.filename)) {
                Log.d("patient","selected");
                patientList.check(patientButton.getId());
            }
            else {
                Log.d("patient","not selected");
            }
        }

        newPatientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent((Context) getBaseContext(), PatientCreationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        patientList.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButton = radioGroup.findViewById(i);
                boolean isChecked = radioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked) {
                    HomeActivity.selectedPatient = radioButtonToPatient.get(radioButton);
                    Intent intent = new Intent(getBaseContext(), HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
    }
}