package com.google.cardboard;

import java.util.ArrayList;
import java.util.Calendar;

public class Measurement {
    public String name;
    public Boolean isSimpleVVS;
    public ArrayList<Float> values;

    public String mean;
    public String variance;
    public String standardDeviation;

    public Measurement(String name, Boolean isSimpleVVS, ArrayList<Float> values) {
        this.name = name;
        this.isSimpleVVS = isSimpleVVS;
        this.values = values;

        CalculateStats(values);
    }

    public void AddMeasurement() {
        name += displayTime();

        String measurementsFile = HomeActivity.selectedPatient.getMeasurementsFile();
        ArrayList<Measurement> values = XmlManager.ReadMeasurements(measurementsFile);
        values.add(this);
        XmlManager.writeMeasurements(measurementsFile,values);
    }

    String displayTime() {
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
