package com.google.cardboard;

import android.os.Environment;
import android.widget.Toast;

import java.io.FileWriter;
import java.text.Normalizer;
import java.util.ArrayList;

public class CsvManager {

    public static void WriteAllData() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (PatientData patientData : XmlManager.patientFiles) {
                    ArrayList<Measurement> measurements = XmlManager.ReadMeasurements(patientData.getMeasurementsFile());
                    WriteCSV(patientData, measurements);
                }
                Toast.makeText(HomeActivity.instance, "All measurements saved to CSV", Toast.LENGTH_LONG).show();
            }
        };
        runnable.run();
    }

    public static void WriteCSV(PatientData patientData,ArrayList<Measurement> measurements) {
        String filename = patientData.getMeasurementsFile();
        ArrayList<String[]> data = FormatData(patientData, measurements);

        String csv = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename + ".csv");

        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(csv));
            writer.writeAll(data); // data is adding to csv

            writer.close();
            callRead();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    static ArrayList<String[]> FormatData(PatientData patientData, ArrayList<Measurement> measurements) {
        ArrayList<Measurement> staticVVS = new ArrayList<>();
        ArrayList<Measurement> dynamicVVS = new ArrayList<>();
        for (Measurement measurement : measurements) {
            if (measurement.isSimpleVVS) staticVVS.add(measurement);
            else dynamicVVS.add(measurement);
        }

        ArrayList<String[]> data = new ArrayList<>();
        data.add(new String[]{
                "Nom", "Prénom", "Age", "Genre"});
        data.add(new String[]{
                patientData.lastName, patientData.firstName, Integer.toString(patientData.age), PatientDataDisplay.GetGenreString(patientData)});

        data.add(new String[]{""});
        data.add(new String[]{"VVS simple"});

        DisplayMeasurements(data, staticVVS);

        data.add(new String[]{""});
        data.add(new String[]{"VVS dynamique"});

        DisplayMeasurements(data,dynamicVVS);

        return data;
    }

    static void DisplayMeasurements(ArrayList<String[]> data, ArrayList<Measurement> measurements) {
        int size = measurements.size();
        String[] line;

        line = new String[size];
        line[0] = "";
        for (int i = 0; i < size; i++) {
            line[i+1] = measurements.get(i).name;
        }
        data.add(line);

        for (int j = 0; j < getMaxLength(measurements); j++) {
            line = new String[size];
            line[0] = "";
            for (int i = 0; i < size; i++) {
                Measurement measurement = measurements.get(i);
                if (measurement.values.size() < j) line[i+1] = "";
                else line[i] = Float.toString(measurement.values.get(j));
                line[i+1] = measurements.get(i).name;
            }
            data.add(line);
        }

        data.add(new String[]{""});

        line = new String[size];
        line[0] = "Moyenne";
        for (int i = 0; i < size; i++) {
            line[i+1] = measurements.get(i).mean;
        }
        data.add(line);

        line = new String[size];
        line[0] = "Variance";
        for (int i = 0; i < size; i++) {
            line[i+1] = measurements.get(i).variance;
        }
        data.add(line);

        line = new String[size];
        line[0] = "Ecart type";
        for (int i = 0; i < size; i++) {
            line[i+1] = measurements.get(i).standardDeviation;
        }
        data.add(line);
    }

    static int getMaxLength(ArrayList<Measurement> measurements) {
        int max = 0;
        for (Measurement measurement : measurements) {
            if (measurement.values.size() > max) max = measurement.values.size();
        }
        return max;
    }
}
