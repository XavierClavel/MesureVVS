package com.google.cardboard;

import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

enum genreType {male, female}


//TODO : update list when updating patient file


public class PatientData {
    public String lastName;
    public String firstName;
    public genreType genre;
    public int birthYear;
    public String comment = "No Comment";
    public int age;

    public String filename;
    public List<Float> measurement;

    public Float mean;
    public Float variance;
    public Float standardDeviation;

    String patientId;

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



    private void CalculateAge() {
        this.age = Calendar.getInstance().get(Calendar.YEAR) - this.birthYear;
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
     * @param lastName
     * @param genre
     * @param birthYear
     */
    public PatientData(String lastName, String firstName,genreType genre, int birthYear) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.genre = genre;
        this.birthYear = birthYear;

        //this.measurementDate = GetFileCreationDate();
        this.filename = AttributeFileName();

        CalculateAge();
        AddToList();
    }

    /**
     * Get reference to an already existing patient data
     * @param lastName
     * @param genre
     * @param birthYear
     * @param filename
     */
    public PatientData(String lastName, String firstName,genreType genre, int birthYear, String filename) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.genre = genre;
        this.birthYear = birthYear;
        this.filename = filename;

        CalculateAge();
        AddToList();
        XmlManager.patientFiles.add(this);

        patientId = filename.substring(7);
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
        patientId = HomeActivity.patientId+"";
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
        XmlManager.AddToIndex(this);
    }

    public void Update() {
        XmlManager.Write(this);
    }

    public void Delete() {
        XmlManager.RemoveFromIndex(this);
        XmlManager.Delete(this);
    }

    private void AddToList() {
        dictionaryNameToPatient.put(filename, this);
    }

    public static PatientData getPatient(String filename) {
        if (dictionaryNameToPatient.containsKey(filename)) return dictionaryNameToPatient.get(filename);
        return null;
    }

    public String getMeasurementsFile() {
        return "measurements" + patientId;
    }

}
