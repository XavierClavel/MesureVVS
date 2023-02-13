package com.google.cardboard;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class XmlManager {
    static String defaultFilename = "scan_data";

    public static List<Float> dataMemory = new ArrayList<>();
    static String xmlDebugFile;
    public static List<PatientData> patientFiles = new ArrayList<>();


    public static void Write(PatientData patientData) {
        dataMemory = patientData.measurement;
        Log.d("xml manager", "starting to write data");
        try {
            File dir = HomeActivity.instance.getFilesDir();
            File file = new File(dir, patientData.filename);
            boolean deleted = file.delete();
            Log.d("deleted", "" + deleted);
            FileOutputStream fos = HomeActivity.instance.openFileOutput(patientData.filename, Context.MODE_APPEND);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8"); //definit le fichier de sortie
            serializer.startDocument("UTF-8", true);
            writeData(serializer, patientData);  //reference variable or value variable
            serializer.endDocument();
            serializer.flush();
            fos.close();
            //Log.d("xml manager", "end of writing operation");


        } catch (Exception e) {
            Log.d("Xml manager", "failed to write data in xml file");
        }
    }

    public static void writeData(XmlSerializer serializer, PatientData patientData) {
        Log.d("xml manager", "starting to write");
        try {
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag("", "root");

                serializer.startTag("", "name");
                serializer.text(patientData.patientName);
                serializer.endTag("", "name");

                serializer.startTag("", "genre");
                serializer.text(patientData.genre.toString());
                serializer.endTag("", "genre");

                serializer.startTag("", "age");
                serializer.text(patientData.age + "");
                serializer.endTag("", "age");

                serializer.startTag("","comment");
                serializer.text(patientData.comment);
                serializer.endTag("", "comment");

                /*serializer.startTag("", "measurement");
                serializer.text(measurementValue + "");
                serializer.endTag("", "measurement");*/

            serializer.endTag("", "root");
            Log.d("xml manager", "successfully wrote data");
        } catch (Exception e) {
            Log.d("xml manager", "failed to write records on xml file");
        }

    }

    public static String getTimestamp() {
        Long tsLong = System.currentTimeMillis()/1000;
        return tsLong.toString();
    }

    private static String readRawData(String filename) {
        //String string = MainActivity.instance.getString(R.string.filename);

        FileInputStream fis;
        InputStreamReader isr;
        String data = "";
        try {
            fis = HomeActivity.instance.openFileInput(filename);

            isr = new InputStreamReader(fis);
            char[] inputBuffer = new char[fis.available()];
            isr.read(inputBuffer);
            data = new String(inputBuffer);

            Log.d("data", data);
            //Log.i(TAG, "Read data from file " + filename);
            isr.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static PatientData Read(String filename) {
        //List<TimestampedData> timestampedDataList = new ArrayList<>();
        Log.d("xml manager", "start reading data");
        //Read data string from file
        String data = readRawData(filename);

        //String data = xmlDebugFile;
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        NodeList items = null;
        Document dom;
        PatientData patientData = null;
        try {
            db = dbf.newDocumentBuilder();
            dom = db.parse(is);
            dom.getDocumentElement().normalize();
            //get all measurement tags
            items = dom.getElementsByTagName("name");
            String name = items.item(0).getTextContent();

            items = dom.getElementsByTagName("genre");
            genreType genre = genreType.valueOf(items.item(0).getTextContent());

            items = dom.getElementsByTagName("age");
            int age = Integer.parseInt(items.item(0).getTextContent());

            //items = dom.getElementsByTagName("measurement");
            //Log.d("xml manager", "nb of xml measurements = " + items.getLength());

            items = dom.getElementsByTagName("comment");
            String comment = items.item(0).getTextContent();

            patientData = new PatientData(name, genre, age, filename);
            patientData.SetComment(comment);


            Log.d("xml parser", "successfully read data");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return patientData;


    }

    public static void AddToHistory(PatientData patientData) {
        patientFiles.add(patientData);
        WriteHistory();
    }

    public static void EraseHistory() {
        try {
            File dir = HomeActivity.instance.getFilesDir();
            File file = new File(dir, "history");
            boolean deleted = file.delete();
            Log.d("deleted", "" + deleted);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ReadHistory() {


        File dir = HomeActivity.instance.getFilesDir();
        File file = new File(dir, "history");
        //boolean deleted = file.delete();
        List<PatientData> measurementSummaries = new ArrayList<>();

        Log.d("xml manager", "start reading data");
        //Read data string from file
        String data = readRawData("history");
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(data.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        NodeList items = null;
        Document dom;
        try {
            db = dbf.newDocumentBuilder();
            dom = db.parse(is);
            dom.getDocumentElement().normalize();
            //get all measurement tags
            items = dom.getElementsByTagName("measurement");
            Log.d("xml manager", "nb of xml measurements = " + items.getLength());
            for (int i=0; i<items.getLength(); i++){
                Element measure = (Element)items.item(i);
                //get timestamp
                String name = measure.getElementsByTagName("name").item(0).getTextContent();
                //String date = measure.getElementsByTagName("date").item(0).getTextContent();
                String filename = measure.getElementsByTagName("filename").item(0).getTextContent();

                PatientData patientData = Read(filename);

                measurementSummaries.add(patientData);
            }
            Log.d("xml parser", "successfully read data");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        patientFiles = measurementSummaries;
        return;
    }

    public static void WriteHistory() {
        Log.d("xml manager", "starting to write data");
        //printXML();
        try {
            File dir = HomeActivity.instance.getFilesDir();
            File file = new File(dir, "history");
            boolean deleted = file.delete();
            Log.d("deleted", "" + deleted);
            FileOutputStream fos = HomeActivity.instance.openFileOutput("history", Context.MODE_APPEND);
            XmlSerializer serializer = Xml.newSerializer();
            serializer.setOutput(fos, "UTF-8"); //definit le fichier de sortie
            serializer.startDocument("UTF-8", true);
            writeDataHistory(serializer, patientFiles);  //reference variable or value variable
            serializer.endDocument();
            serializer.flush();
            fos.close();
            Log.d("xml manager", "end of writing operation");
        } catch (Exception e) {
            Log.d("Xml manager", "failed to write data in xml file");
        }
    }

    public static void writeDataHistory(XmlSerializer serializer, List<PatientData> measurementSummaries) {
        try {
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.startTag("", "root");
            for (PatientData measurementSummary : measurementSummaries) {

                serializer.startTag("", "measurement");

                serializer.startTag("", "name");
                serializer.text(measurementSummary.patientName);
                serializer.endTag("", "name");

                //serializer.startTag("", "date");
                //serializer.text(measurementSummary.measurementDate);
                //serializer.endTag("", "date");

                serializer.startTag("","filename");
                serializer.text(measurementSummary.filename);
                serializer.endTag("", "filename");

                serializer.endTag("", "measurement");
            }
            serializer.endTag("", "root");
            Log.d("xml manager", "successfully wrote data");
        } catch (Exception e) {
            Log.d("xml manager", "failed to write records on xml file");
        }

    }


}
