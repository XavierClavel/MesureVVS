package com.google.cardboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class PatientCreationActivity extends AppCompatActivity {

    Button validateButton;
    EditText textName;
    EditText textAge;
    EditText textComment;
    RadioGroup radioGroup;

    genreType genre;

    //TODO : prevent user from validating incomplete profile
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_creation);

        textName = findViewById(R.id.patientCreation_name);
        textAge = findViewById(R.id.patientCreation_age);
        textComment = findViewById(R.id.patientCreation_comment);
        radioGroup = findViewById(R.id.genreGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButton = radioGroup.findViewById(i);
                boolean isChecked = radioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked) {
                    String value = radioButton.getText().toString();
                    ParseString(value);
                }
            }
        });
        validateButton = findViewById(R.id.patientCreation_done);
        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String patientName = textName.getText().toString();
                int age = Integer.parseInt(textAge.getText().toString());
                String comment = textComment.getText().toString();

                PatientData patientData = new PatientData(patientName, genre, age);
                patientData.SetComment(comment);

                Intent intent = new Intent((Context) getBaseContext(), HomeActivity.class);
                startActivity(intent);
            }
        });


    }

    void ParseString(String genreString) {
        switch (genreString) {
            case "M":
                genre = genreType.male;
            case "F" :
                genre = genreType.female;
        }
    }
}