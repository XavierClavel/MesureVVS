package com.google.cardboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    RadioButton button0;
    RadioButton button1;

    genreType genre;

    String patientFile = null;

    //TODO : prevent user from validating incomplete profile
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_creation);

        if (getIntent().hasExtra("patient")) patientFile = getIntent().getExtras().getString("patient");

        textName = findViewById(R.id.patientCreation_name);
        textAge = findViewById(R.id.patientCreation_age);
        textComment = findViewById(R.id.patientCreation_comment);

        radioGroup = findViewById(R.id.genreGroup);
        button0 = findViewById(R.id.radioButton0);
        button1 = findViewById(R.id.radioButton1);

        if (patientFile != null) {
            PatientData patientData = PatientData.getPatient(patientFile);
            textName.setText(patientData.patientName);
            textAge.setText(patientData.age+"");
            textComment.setText(patientData.comment);
            RadioButton genreButton = GenreToButton(patientData.genre);
            genreButton.setChecked(true);

        }


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

                Intent intent;

                PatientData patientData;
                if (patientFile == null) {
                    patientData = new PatientData(patientName, genre, age);
                    patientData.SetComment(comment);
                    patientData.Save();
                    intent = new Intent((Context) getBaseContext(), HomeActivity.class);
                }
                else {
                    patientData = PatientData.getPatient(patientFile);
                    patientData.patientName = patientName;
                    patientData.age = age;
                    patientData.genre = genre;
                    patientData.SetComment(comment);
                    patientData.Update();
                    intent = new Intent((Context) getBaseContext(), FilesDisplayActivity.class);
                }


                startActivity(intent);
            }
        });


    }

    void ParseString(String genreString) {
        Log.d("string parsed",genreString);
        if (genreString.equals("M")) genre = genreType.male;
        else if (genreString.equals("F")) genre = genreType.female;
        Log.d("genre enum", genre.name());
    }

    RadioButton GenreToButton(genreType genre) {
        switch (genre) {
            case male:
                return button0;

            case female:
                return button1;
        }
        return null;
    }
}