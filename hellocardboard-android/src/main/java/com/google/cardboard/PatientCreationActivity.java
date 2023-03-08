package com.google.cardboard;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Activité permettant la création et la modification des fiches patients
 */
public class PatientCreationActivity extends AppCompatActivity {

    Button validateButton;
    EditText textLastName;
    EditText textFirstName;
    EditText textBirthYear;
    EditText textComment;
    RadioGroup radioGroup;
    RadioButton button0;
    RadioButton button1;

    genreType genre;

    String patientFile = null;

    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_creation);
        genre = null;
        alertDialog = CreateAlertMessage();

        if (getIntent().hasExtra("patient")) patientFile = getIntent().getExtras().getString("patient");

        textLastName = findViewById(R.id.patientCreation_lastName);
        textFirstName = findViewById(R.id.patientCreation_firstName);
        textBirthYear = findViewById(R.id.patientCreation_age);
        textComment = findViewById(R.id.patientCreation_comment);

        radioGroup = findViewById(R.id.genreGroup);
        button0 = findViewById(R.id.radioButton0);
        button1 = findViewById(R.id.radioButton1);

        if (patientFile != null) {
            PatientData patientData = PatientData.getPatient(patientFile);
            textLastName.setText(patientData.lastName);
            textFirstName.setText(patientData.firstName);
            textBirthYear.setText(patientData.birthYear+"");
            textComment.setText(patientData.comment);
            genre = patientData.genre;
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

                String lastName = textLastName.getText().toString();
                String firstName = textFirstName.getText().toString();
                String birthyearString = textBirthYear.getText().toString();
                String comment = textComment.getText().toString();

                if (lastName.isEmpty() || firstName.isEmpty() || birthyearString.isEmpty() || genre == null) {
                    alertDialog.show();
                    return;
                }

                int birthYear = Integer.parseInt(birthyearString);

                Intent intent;

                PatientData patientData;
                if (patientFile == null) {
                    Log.d("patient file", "is null");
                    patientData = new PatientData(lastName, firstName,genre, birthYear);
                    patientData.SetComment(comment);
                    patientData.Save();
                    HomeActivity.SelectPatient(patientData);
                    intent = new Intent((Context) getBaseContext(), HomeActivity.class);
                }
                else {
                    Log.d("patient file", "is not null");
                    patientData = PatientData.getPatient(patientFile);
                    patientData.lastName = lastName;
                    patientData.firstName = firstName;
                    patientData.birthYear = birthYear;
                    if (genre != null) patientData.genre = genre;
                    patientData.SetComment(comment);
                    patientData.Update();
                    intent = new Intent((Context) getBaseContext(), FilesDisplayActivity.class);
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


    }

    AlertDialog CreateAlertMessage() {
        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
        builder.setMessage("Please fill all mandatory fields to create patient file")
                //.setCancelable(true)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        Log.d("dialog", "created");
        return builder.create();
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