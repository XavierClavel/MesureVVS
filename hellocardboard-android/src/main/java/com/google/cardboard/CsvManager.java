package com.google.cardboard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CsvManager {

    public static void WriteAllData() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                checkPermission();
                for (PatientData patientData : XmlManager.patientFiles) {
                    ArrayList<Measurement> measurements = XmlManager.ReadMeasurements(patientData.getMeasurementsFile(), patientData);
                    WriteCSV(patientData, measurements);
                }
                Toast.makeText(HomeActivity.instance, "All measurements saved to CSV", Toast.LENGTH_LONG).show();
            }
        };
        runnable.run();
    }

    public static void WriteCSV(PatientData patientData,ArrayList<Measurement> measurements) {
        checkPermission();
        String filename = patientData.getMeasurementsFile();
        filename = patientData.lastName + "_" + patientData.firstName;
        ArrayList<String[]> data = FormatData(patientData, measurements);

        String csv;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            csv = HomeActivity.instance.getExternalFilesDir(Environment.DIRECTORY_DCIM) + "/" + filename + ".csv";
        }
        else
        {
            csv = Environment.getExternalStorageDirectory().toString() + "/" + filename + ".csv";
        }

        File file = new File(csv);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        //String csv = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename + ".csv");
        Log.d("path", csv);

        CSVWriter writer = null;
        try {
            writer = new CSVWriter(new FileWriter(csv), ',', CSVWriter.NO_QUOTE_CHARACTER);
            writer.writeAll(data); // data is adding to csv

            writer.close();
            //callRead();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    static ArrayList<String[]> FormatData(PatientData patientData, ArrayList<Measurement> measurements) {
        //date, nom, age, genre, type de vvs, nombre de mesures avec rotation à G et nombre de rot à D dans l'essai, moyenne, écart type, variance)
        int size = measurements.size();
        ArrayList<String[]> data = new ArrayList<>();
        String[] line = new String[size+1];
        line[0] = "Date";
        for (int i = 0; i<size; i++) {
            line[i+1] = measurements.get(i).date;
        }
        data.add(line);

        line = new String[size+1];
        line[0] = "Nom";
        for (int i = 0; i<size; i++) {
            line[i+1] = patientData.lastName;
        }
        data.add(line);

        line = new String[size+1];
        line[0] = "Age";
        for (int i = 0; i<size; i++) {
            line[i+1] = Float.toString(patientData.age);
        }
        data.add(line);

        line = new String[size+1];
        line[0] = "Genre";
        for (int i = 0; i<size; i++) {
            line[i+1] = patientData.getGenreString();
        }
        data.add(line);

        line = new String[size+1];
        line[0] = "Type de VVS";
        for (int i = 0; i<size; i++) {
            line[i+1] = measurements.get(i).isSimpleVVS ? "VVS Simple" : "VVS Dynamique";
        }
        data.add(line);

        line = new String[size+1];
        line[0] = "Nb mesures avec rotation à gauche";
        for (int i = 0; i<size; i++) {
            line[i+1] = Integer.toString(measurements.get(i).valuesLeft.size());
        }
        data.add(line);

        line = new String[size+1];
        line[0] = "Nb mesures avec rotation à droite";
        for (int i = 0; i<size; i++) {
            line[i+1] = Integer.toString(measurements.get(i).valuesRight.size());
        }
        data.add(line);

        line = new String[size+1];
        line[0] = "Moyenne";
        for (int i = 0; i<size; i++) {
            line[i+1] = measurements.get(i).mean;
        }
        data.add(line);

        line = new String[size+1];
        line[0] = "Ecart-type";
        for (int i = 0; i<size; i++) {
            line[i+1] = measurements.get(i).standardDeviation;
        }
        data.add(line);

        line = new String[size+1];
        line[0] = "Variance";
        for (int i = 0; i<size; i++) {
            line[i+1] = measurements.get(i).variance;
        }
        data.add(line);



        /*
        ArrayList<Measurement> staticVVS = new ArrayList<>();
        ArrayList<Measurement> dynamicVVS = new ArrayList<>();
        for (Measurement measurement : measurements) {
            if (measurement.isSimpleVVS) staticVVS.add(measurement);
            else dynamicVVS.add(measurement);
        }


        data.add(new String[]{
                "Nom", "Prénom", "Age", "Genre"});
        data.add(new String[]{
                patientData.lastName, patientData.firstName, Integer.toString(patientData.age), PatientDataDisplay.GetGenreString(patientData)});

        if (staticVVS.size() != 0) {
            data.add(new String[]{});
            data.add(new String[]{"VVS simple"});

            DisplayMeasurements(data, staticVVS);
        }

        if (dynamicVVS.size() != 0) {
            data.add(new String[]{});
            data.add(new String[]{"VVS dynamique"});

            DisplayMeasurements(data,dynamicVVS);
        }

         */

        return data;


    }
/*
    static void DisplayMeasurements(ArrayList<String[]> data, ArrayList<Measurement> measurements) {
        int size = measurements.size();
        if(size == 0) return;
        String[] line;

        line = new String[size+1];
        line[0] = null;
        for (int i = 0; i < size; i++) {
            line[i+1] = measurements.get(i).date;
        }
        data.add(line);

        for (int j = 0; j < getMaxLength(measurements); j++) {
            line = new String[size+1];
            line[0] = null;
            for (int i = 0; i < size; i++) {
                Measurement measurement = measurements.get(i);
                if (j < measurement.values.size()) line[i+1] = Float.toString(measurement.values.get(j));
                else line[i+1] = "";
            }
            data.add(line);
        }

        data.add(new String[]{});

        line = new String[size+1];
        line[0] = "Moyenne";
        for (int i = 0; i < size; i++) {
            line[i+1] = measurements.get(i).mean;
        }
        data.add(line);

        line = new String[size+1];
        line[0] = "Variance";
        for (int i = 0; i < size; i++) {
            line[i+1] = measurements.get(i).variance;
        }
        data.add(line);

        line = new String[size+1];
        line[0] = "Ecart type";
        for (int i = 0; i < size; i++) {
            line[i+1] = measurements.get(i).standardDeviation;
        }
        data.add(line);
    }

*/
    public static void checkPermission() {
        int permission1 = ContextCompat.checkSelfPermission(HomeActivity.instance, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // Check for permissions
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            Log.d("permission", "Requesting Permissions");

            // Request permissions
            ActivityCompat.requestPermissions(HomeActivity.instance,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 565);
        }

    }
}
