package com.google.cardboard;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//TODO : export only selected patient
//TODO : choix raw data / data et tous patients/patient sélectionné
public class CsvManager {

    public static void WriteAllData() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                checkPermission();
                for (PatientData patientData : XmlManager.patientFiles) {
                    ArrayList<Measurement> measurements = XmlManager.ReadMeasurements(patientData.getMeasurementsFile(), patientData);
                    WriteCSV(patientData, measurements, true);
                    WriteCSV(patientData, measurements, false);
                }
                Toast.makeText(HomeActivity.instance, "All measurements saved to CSV", Toast.LENGTH_LONG).show();
            }
        };
        runnable.run();
    }

    public static void WriteCSV(PatientData patientData,ArrayList<Measurement> measurements, boolean raw) {
        checkPermission();
        String filename;
        filename = patientData.lastName + "_" + patientData.firstName;
        if (raw) filename += "_Raw";
        ArrayList<String[]> data;
        data = raw ? FormatRawData(patientData,measurements) : FormatData(patientData, measurements);

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


    static ArrayList<String[]> FormatRawData(PatientData patientData, ArrayList<Measurement> measurements) {
        int size = measurements.size();
        ArrayList<String[]> data = new ArrayList<>();
        ArrayList<String> line;
        String[] lineArray;

        line= new ArrayList<>();
        line.add("Date");
        for (int i = 0; i<size; i++) {
            if (measurements.get(i).valuesRight.size() != 0) line.add(measurements.get(i).date);
            if (measurements.get(i).valuesLeft.size() != 0) line.add(measurements.get(i).date);
        }
        lineArray = new String[line.size()];
        lineArray = line.toArray(lineArray);
        data.add(lineArray);


        line= new ArrayList<>();
        line.add("Nom");
        for (int i = 0; i<size; i++) {
            if (measurements.get(i).valuesRight.size() != 0) line.add(patientData.lastName);
            if (measurements.get(i).valuesLeft.size() != 0) line.add(patientData.lastName);
        }
        lineArray = new String[line.size()];
        line.toArray(lineArray);
        data.add(lineArray);

        line= new ArrayList<>();
        line.add("Prénom");
        for (int i = 0; i<size; i++) {

            if (measurements.get(i).valuesRight.size() != 0) line.add(patientData.firstName);
            if (measurements.get(i).valuesLeft.size() != 0) line.add(patientData.firstName);
        }
        lineArray = new String[line.size()];
        line.toArray(lineArray);
        data.add(lineArray);

        line= new ArrayList<>();
        line.add("Age");
        for (int i = 0; i<size; i++) {
            if (measurements.get(i).valuesRight.size() != 0) line.add(getPatientAgeAtMeasurement(measurements.get(i), patientData));
            if (measurements.get(i).valuesLeft.size() != 0) line.add(getPatientAgeAtMeasurement(measurements.get(i), patientData));
        }
        lineArray = new String[line.size()];
        line.toArray(lineArray);
        data.add(lineArray);

        line= new ArrayList<>();
        line.add("Genre");
        for (int i = 0; i<size; i++) {
            if (measurements.get(i).valuesRight.size() != 0) line.add(patientData.getGenreString());
            if (measurements.get(i).valuesLeft.size() != 0) line.add(patientData.getGenreString());
        }
        lineArray = new String[line.size()];
        line.toArray(lineArray);
        data.add(lineArray);

        line= new ArrayList<>();
        line.add("Type de VVS");
        for (int i = 0; i<size; i++) {
            if (measurements.get(i).valuesRight.size() != 0) line.add(measurements.get(i).isSimpleVVS ? "VVS Simple" : "VVS Dynamique");
            if (measurements.get(i).valuesLeft.size() != 0) line.add(measurements.get(i).isSimpleVVS ? "VVS Simple" : "VVS Dynamique");
        }
        lineArray = new String[line.size()];
        line.toArray(lineArray);
        data.add(lineArray);

        line= new ArrayList<>();
        line.add("Sens de rotation");
        for (int i = 0; i<size; i++) {
            if (measurements.get(i).valuesRight.size() != 0) line.add("Droite");
            if (measurements.get(i).valuesLeft.size() != 0) line.add("Gauche");
        }
        lineArray = new String[line.size()];
        line.toArray(lineArray);
        data.add(lineArray);


        int maxLength = getMaxLengthRightLeft(measurements);
        for (int j = 0; j < maxLength; j++) {
            line= new ArrayList<>();
            line.add(j == 0 ? "Valeurs" : null);
            for (int i = 0; i < size; i++) {
                Measurement measurement = measurements.get(i);
                if (measurements.get(i).valuesRight.size() != 0) {
                    if (j < measurement.valuesRight.size()) line.add(Float.toString(measurement.valuesRight.get(j)));
                    else line.add(null);
                }

                if (measurements.get(i).valuesLeft.size() != 0) {
                    if (j < measurement.valuesLeft.size()) line.add(Float.toString(measurement.valuesLeft.get(j)));
                    else line.add(null);
                }

            }
            lineArray = new String[line.size()];
            line.toArray(lineArray);
            data.add(lineArray);
        }

        return data;

    }

    static int getMaxLength(ArrayList<Measurement> measurements) {
        int max = 0;
        for (Measurement measurement : measurements) {
            if (measurement.values.size() > max) max = measurement.values.size();
        }
        return max;
    }

    static int getMaxLengthRightLeft(ArrayList<Measurement> measurements) {
        int max = 0;
        for (Measurement measurement : measurements) {
            if (measurement.valuesRight.size() > max) max = measurement.valuesRight.size();
            if (measurement.valuesLeft.size() > max) max = measurement.valuesLeft.size();
        }
        return max;
    }

    /**
     *
     * @param measurement
     * @return the age the patient was at the time of the measurement
     */
    static String getPatientAgeAtMeasurement(Measurement measurement,PatientData patient) {
        String measurementYear = measurement.date.split("/")[2];
        Integer ageAtMeasurement = Integer.parseInt(measurementYear) - patient.birthYear;
        return ageAtMeasurement.toString();
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
        line[0] = "Prénom";
        for (int i = 0; i<size; i++) {
            line[i+1] = patientData.firstName;
        }
        data.add(line);

        line = new String[size+1];
        line[0] = "Age";
        for (int i = 0; i<size; i++) {
            //line[i+1] = Float.toString(patientData.age);
            line[i+1] = getPatientAgeAtMeasurement(measurements.get(i), patientData);
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

        return data;
    }

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

        for (int j = 0; j < size; j++) {
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