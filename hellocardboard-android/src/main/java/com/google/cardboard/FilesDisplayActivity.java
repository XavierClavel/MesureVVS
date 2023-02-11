package com.google.cardboard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class FilesDisplayActivity extends AppCompatActivity {

    LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_display);

        mainLayout = findViewById(R.id.filesDisplayLayout);

        for (PatientData patientData : XmlManager.patientFiles) {
            createLayout(patientData);
        }
    }

    void createLayout(PatientData measurementSummary) {

        LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                2f
        );

        LinearLayout.LayoutParams buttonParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                8f
        );

        String date = measurementSummary.measurementDate;
        String name = measurementSummary.patientName;

        RelativeLayout layout = new RelativeLayout(FilesDisplayActivity.this);
        layout.setPadding(20, 20, 20, 20);

        LinearLayout layout1 = new LinearLayout(this);
        layout1.setOrientation(LinearLayout.HORIZONTAL);

        layout.addView(layout1);

        LinearLayout localLinLayout = new LinearLayout(FilesDisplayActivity.this);
        localLinLayout.setOrientation(LinearLayout.VERTICAL);
        localLinLayout.setLayoutParams(textParam);

        TextView dateDisplay = new TextView(FilesDisplayActivity.this);
        dateDisplay.setText(name);
        dateDisplay.setTypeface(null, Typeface.BOLD);
        dateDisplay.setTextSize(20f);
        localLinLayout.addView(dateDisplay);

        TextView placeDisplay = new TextView(FilesDisplayActivity.this);
        placeDisplay.setText(date);
        localLinLayout.addView(placeDisplay);

        layout1.addView(localLinLayout);

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