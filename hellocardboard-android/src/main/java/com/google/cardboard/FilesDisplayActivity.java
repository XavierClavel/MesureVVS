package com.google.cardboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FilesDisplayActivity extends AppCompatActivity {

    LinearLayout mainLayout;
    LinearLayout.LayoutParams dividerParam;
    LinearLayout.LayoutParams textParam;
    LinearLayout.LayoutParams buttonParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_display);

        mainLayout = findViewById(R.id.filesDisplayLayout);
        boolean topMostLayout = true;

        dividerParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5
        );

        textParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                25f
        );

        buttonParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                75f
        );

        for (PatientData patientData : XmlManager.patientFiles) {
            if (!topMostLayout) createDivider();
            topMostLayout = false;

            createLayout(patientData);
        }
    }

    void createDivider() {
        RelativeLayout divider = new RelativeLayout(FilesDisplayActivity.this);
        divider.setLayoutParams(dividerParam);
        divider.setBackgroundColor(Color.LTGRAY);
        mainLayout.addView(divider);
    }

    void createLayout(PatientData patientData) {

        //String date = patientData.measurementDate;
        String lastName = patientData.lastName;
        String firstName = patientData.firstName;

        RelativeLayout layout = new RelativeLayout(FilesDisplayActivity.this);
        layout.setPadding(20, 20, 20, 20);
        layout.setLayoutParams(textParam);

        LinearLayout layout1 = new LinearLayout(this);
        layout1.setOrientation(LinearLayout.HORIZONTAL);
        layout1.setLayoutParams(textParam);

        layout.addView(layout1);

        LinearLayout localLinLayout = new LinearLayout(FilesDisplayActivity.this);
        localLinLayout.setOrientation(LinearLayout.VERTICAL);
        localLinLayout.setLayoutParams(textParam);

        LinearLayout horizontal_layout1 = new LinearLayout(this);
        layout1.setOrientation(LinearLayout.HORIZONTAL);

        localLinLayout.addView(horizontal_layout1);

        TextView lastNameDisplay = new TextView(FilesDisplayActivity.this);
        lastNameDisplay.setText(lastName);
        lastNameDisplay.setTypeface(null, Typeface.BOLD);
        lastNameDisplay.setTextSize(20f);
        horizontal_layout1.addView(lastNameDisplay);

        TextView firstNameDisplay = new TextView(FilesDisplayActivity.this);
        firstNameDisplay.setText(" " + firstName);
        firstNameDisplay.setTextSize(20f);
        horizontal_layout1.addView(firstNameDisplay);

        /*
        TextView placeDisplay = new TextView(FilesDisplayActivity.this);

        placeDisplay.setText(date);
        localLinLayout.addView(placeDisplay);
         */

        layout1.addView(localLinLayout);

        Button button = new Button(this);
        //button.setLayoutParams(buttonParam);
        button.setText("Display");
        button.setMinimumWidth(500);
        button.setGravity(Gravity.CENTER);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), PatientDataDisplay.class);
                intent.putExtra("patient", patientData.filename);
                startActivity(intent);
            }
        });

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setLayoutParams(buttonParam);
        buttonLayout.setGravity(Gravity.RIGHT);
        buttonLayout.setMinimumWidth(500);

        layout1.addView(buttonLayout);
        buttonLayout.addView(button);

        mainLayout.addView(layout);

        /*
        TextView nbPointsDisplay = new TextView(FilesDisplayActivity.this);
        nbPointsDisplay.setText(nbPoints + " measurements");
        localLinLayout.addView(nbPointsDisplay);

        layout1.addView(localLinLayout);

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setLayoutParams(buttonParam);

        ImageButton displayButton = new ImageButton(FilesDisplayActivity.this);
        Log.d("button id", displayButton.getId() + "");
        displayButton.setBackgroundResource(R.drawable.border_on_click);
        displayButton.setBackgroundTintList(orange);
        displayButton.setImageResource(R.drawable.map);
        displayButton.setImageTintList(orange);
        displayButton.setMinimumWidth(200);
        buttonLayout.setGravity(Gravity.CENTER);

        //layout.addView(displayButton, paramsRight);
        layout1.addView(buttonLayout);
        buttonLayout.addView(displayButton);

        linLayout.addView(layout);

        ImageView separator = new ImageView(this);
        separator.setImageResource(R.drawable.line);
        separator.setPadding(150,0,150,0);
        separator.setImageTintList(orange);
        linLayout.addView(separator);

        bottomImageView = separator;

        buttonList.add(displayButton);
        displayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDeleting) {
                    linLayout.removeView(layout);
                    linLayout.removeView(separator);
                    Log.d("measurement removed", measurementSummary.date);
                    measurementSummaries.remove(measurementSummary);
                    File dir = MainActivity.instance.getFilesDir();
                    File file = new File(dir, measurementSummary.filename);
                    boolean deleted = file.delete();
                    Log.d("file deleted", deleted+"");
                    XmlManager.WriteHistory(measurementSummaries);
                }
                else {
                    List<TimestampedData> timestampedDataList = XmlManager.Read(measurementSummary.filename);
                    Log.d("file opened", measurementSummary.filename);

                    HistoryMapActivity.firstLocation = timestampedDataList.get(0).position;
                    HistoryHeatmapManager.data = timestampedDataList;

                    Intent intent = new Intent(HistoryActivity.this, HistoryMapActivity.class);
                    startActivity(intent);

                }

            }
        });

         */

    }
}