package com.google.cardboard;

import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.RequiresPermission;

enum genreType {male, female}

public class PatientData {
    public String patientName;
    public genreType genre;
    public int age;
    public String comment = "No Comment";

    public String measurementDate;
    public String filename;
    public List<Float> measurement;

    public Float mean;
    public Float variance;
    public Float standardDeviation;

    RadioGroup patientList;
    static HashMap<String,PatientData> dictionaryNameToPatient = new HashMap<>();

    private void Calculate() {
        float mean = 0f;
        for (Float value : measurement) {
            mean += value;
        }
        mean /= measurement.size();
        this.mean = mean;

        float variance = 0f;
        for (Float value : measurement) {
            variance +=  Math.pow(value-mean,2);
        }
        variance /= measurement.size();
        this.variance = variance;
        this.standardDeviation = (float) Math.pow(standardDeviation,0.5f);
    }



    public void AddMeasurement(List<Float> measurement) {
        this.measurement = measurement;
        Calculate();
    }

    public void SetComment(String comment) {
        this.comment = comment;
    }


    //Add new client to database

    /**
     * Add new client to database and generate a new file
     * @param patientName
     * @param genre
     * @param age
     */
    public PatientData(String patientName, genreType genre, int age) {
        this.patientName = patientName;
        this.genre = genre;
        this.age = age;

        this.measurementDate = GetFileCreationDate();
        this.filename = AttributeFileName();

        AddToList();
    }

    /**
     * Get reference to an already existing patient data
     * @param patientName
     * @param genre
     * @param age
     * @param filename
     */
    public PatientData(String patientName, genreType genre, int age, String filename) {
        this.patientName = patientName;
        this.genre = genre;
        this.age = age;
        this.filename = filename;

        AddToList();
        XmlManager.patientFiles.add(this);
    }

    public String getGenreString() {
        switch (genre) {
            case male :
                return "M";

            case female:
                return "F";
        }
        return "Error";
    }

    /**
     * Generates a new unique filename for the patient
     * @return a unique filename
     */
    private String AttributeFileName() {
        String patientId = HomeActivity.patientId+"";
        String filename = "patient" + patientId;
        HomeActivity.IncrementPatientId();
        return filename;
    }

    private String GetFileCreationDate() {
        Date date = Calendar.getInstance().getTime();
        return date.toString();
    }

    public void Save() {
        XmlManager.Write(this);
        XmlManager.AddToHistory(this);
    }

    public void Update() {
        XmlManager.Write(this);
    }

    private void AddToList() {
        RadioGroup  patientList = HomeActivity.instance.findViewById(R.id.patientList);
        RadioButton patientButton = new RadioButton(HomeActivity.instance);
        patientButton.setText(patientName);
        patientList.addView(patientButton);

        dictionaryNameToPatient.put(filename, this);
    }

    public static PatientData getPatient(String filename) {
        if (dictionaryNameToPatient.containsKey(filename)) return dictionaryNameToPatient.get(filename);
        return null;
    }

}
