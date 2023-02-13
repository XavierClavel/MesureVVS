package com.google.cardboard;

import java.util.ArrayList;
import java.util.List;

public class SpinnerHandler {
    static List<String> patientNames = new ArrayList<>();
    static String[] patientList;

    public static void AddPatient(String name) {
        if (patientNames.isEmpty()) InitializePatientList();
        patientNames.add(name);
        patientList = (String[])patientNames.toArray();
    }


    static void InitializePatientList() {
        patientNames.add("New patient");
    }

    public static void RemovePatient(String name) {
        patientNames.remove(name);
        patientList = (String[])patientNames.toArray();
    }

    public static String[] GetPatientList() {
        if (patientNames.isEmpty()) InitializePatientList();
        return patientList;
    }
}
