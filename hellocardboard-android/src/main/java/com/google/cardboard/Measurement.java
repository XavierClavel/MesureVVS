package com.google.cardboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.stream.Stream;

public class Measurement {
    public String date;
    public Boolean isSimpleVVS;
    public ArrayList<Float> valuesLeft;
    public ArrayList<Float> valuesRight;

    public String mean;
    public String variance;
    public String standardDeviation;

    public PatientData patient;
    public static PatientData lastPatient;
    public static String lastDate;
    public static Boolean lastVVSType = null;

    public static boolean isSameMeasurement;

    public Measurement(String date,Boolean isSimpleVVS, ArrayList<Float> valuesLeft, ArrayList<Float> valuesRight, PatientData patient) {
        this.date = date == null ? displayTime() : date;
        this.isSimpleVVS = isSimpleVVS;
        this.valuesLeft = valuesLeft;
        this.valuesRight = valuesRight;

        ArrayList<Float> fList = new ArrayList<>();
        for (Float f : valuesLeft) fList.add(f);
        for (Float f : valuesRight) fList.add(f);

        CalculateStats(fList);


        lastPatient = patient;
        lastDate = date;
        lastVVSType = isSimpleVVS;
    }

    public static boolean isIsSameMeasurement(PatientData patient, boolean VVSType) {
        return lastPatient == patient && lastDate.equals(displayTime()) && lastVVSType.equals(VVSType);
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
