package com.google.cardboard;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Measurement {
    public String date;
    public Boolean isSimpleVVS;
    public ArrayList<Float> valuesLeft;
    public ArrayList<Float> valuesRight;
    public ArrayList<Float> values;

    public String mean;
    public String variance;
    public String standardDeviation;

    public PatientData patient;

    public static HashMap<Boolean,Measurement> VVSTypeToMeasurement = null;

    public static void StartMeasurementSeries() {
        VVSTypeToMeasurement = new HashMap<>();
    }
    public static void EndMeasurementSeries() {
        if (VVSTypeToMeasurement.containsKey(true)) VVSTypeToMeasurement.get(true).AddMeasurement();
        if (VVSTypeToMeasurement.containsKey(false)) VVSTypeToMeasurement.get(false).AddMeasurement();
        VVSTypeToMeasurement = null;
    }

    public static void AddMeasurementToSeries(boolean VVSType, ArrayList<Float> valuesLeft, ArrayList<Float> valuesRight) {
        if (VVSTypeToMeasurement.containsKey(VVSType)) VVSTypeToMeasurement.get(VVSType).AddValues(valuesLeft, valuesRight);
        else VVSTypeToMeasurement.put(VVSType,new Measurement(null,VVSType,valuesLeft, valuesRight, HomeActivity.selectedPatient));
    }

    public Measurement(String date,Boolean isSimpleVVS, ArrayList<Float> valuesLeft, ArrayList<Float> valuesRight, PatientData patient) {
        this.date = date == null ? displayTime() : date;
        this.isSimpleVVS = isSimpleVVS;
        this.valuesLeft = valuesLeft;
        this.valuesRight = valuesRight;

        SetValuesList();

        CalculateStats(values);
    }

    public void SetValuesList() {
        values = new ArrayList<>();
        for (Float f : valuesLeft) values.add(f);
        for (Float f : valuesRight) values.add(f);
    }

    public void AddValues(ArrayList<Float> valuesLeft, ArrayList<Float> valuesRight) {
        for (Float f : valuesLeft) {
            values.add(f);
            this.valuesLeft.add(f);
        }
        for (Float f : valuesRight) {
            values.add(f);
            this.valuesRight.add(f);
        }


        /*
        String measurementsFile = HomeActivity.selectedPatient.getMeasurementsFile();
        ArrayList<Measurement> values = XmlManager.ReadMeasurements(measurementsFile, HomeActivity.selectedPatient);

        Measurement measurement = values.get(values.size()-1);
        for (Float f : valuesLeft) measurement.valuesLeft.add(f);
        for (Float f : valuesRight) measurement.valuesRight.add(f);

        XmlManager.writeMeasurements(measurementsFile,values);

         */
    }

    public void AddMeasurement() {
        date = displayTime();

        String measurementsFile = HomeActivity.selectedPatient.getMeasurementsFile();
        ArrayList<Measurement> values = XmlManager.ReadMeasurements(measurementsFile, patient);
        values.add(this);
        XmlManager.writeMeasurements(measurementsFile,values);
    }

    static String displayTime() {
        String day = Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        String month = Integer.toString(Calendar.getInstance().get(Calendar.MONTH));
        String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
        return day + "/" + month + "/" + year;
    }

    void CalculateStats(ArrayList<Float> measurement) {
        float mean = 0f;
        for (Float value : measurement) {
            mean += value;
        }
        mean /= measurement.size();
        this.mean = Float.toString(mean);

        float variance = 0f;
        for (Float value : measurement) {
            variance +=  Math.pow(value-mean,2);
        }
        variance /= measurement.size();
        this.variance = Float.toString(variance);

        float standardDeviation = (float) Math.pow(variance,0.5f);
        this.standardDeviation = Float.toString(standardDeviation);
    }
}
