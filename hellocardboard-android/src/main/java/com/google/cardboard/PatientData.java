package com.google.cardboard;

public class PatientData {
    public String patientName;
    public String measurementDate;
    public String filename;

    public PatientData(String patientName, String measurementDate, String filename) {
        this.patientName = patientName;
        this.measurementDate = measurementDate;
        this.filename = filename;
    }
}
