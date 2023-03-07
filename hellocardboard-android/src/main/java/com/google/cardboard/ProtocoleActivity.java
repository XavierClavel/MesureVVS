package com.google.cardboard;



import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
        import android.widget.SeekBar;
        import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

import static java.lang.Integer.min;

public class ProtocoleActivity extends AppCompatActivity {

    public static final String TAG = "ProtocoleActivity";
    private LinearLayout containerLayout;
    private Button addSerieButton;
    private Button ValideButton;
    private int serieCount = 0;
    ArrayList<ParameterSeries> listeParametres = new ArrayList<ParameterSeries>();
    ArrayList<View> listeView = new ArrayList<View>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protocole);

        containerLayout = findViewById(R.id.containerLayout);
        addSerieButton = findViewById(R.id.addSerieButton);
        ValideButton = findViewById(R.id.ValideButton);
        //bouton ajouter série
        addSerieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSerie(5,0,0,1, 1F);
            }
        });
        //bouton valider
        ValideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listeParametres.size() ==0) {
                    ParameterSeries parameterSeries = new ParameterSeries(5,0,0,0,1);
                    listeParametres.add(parameterSeries);
                }
                // sauvegarde dans  les SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Convertion de la liste en chaîne de caractères
                String listString = listeParametres.get(0).toString();

                for (int i = 1; i < listeParametres.size(); i++) {
                    listString += "ll" + listeParametres.get(i).toString();
                }

                // Ajout de la chaîne de caractères aux SharedPreferences
                editor.putString("parameters_list", listString);
                editor.apply();



                Intent resultIntent = new Intent();
                for (int i = 0; i < listeParametres.size(); i++) {
                    ParameterSeries p = listeParametres.get(i);
                    Log.i(TAG, "envoie des paramètres suivants :\n" + String.valueOf(i) + " ; nb: " + String.valueOf(p.getNbMesures()) + " ; mode: " + String.valueOf(p.getMode()) + " ; barre: " +
                            String.valueOf(p.getSensBarre()) + " ; fond: " + String.valueOf(p.getSensFond()) + " ; vitessFond : " + String.valueOf(p.getVitesseFond()) + "\n");
                }
                resultIntent.putExtra("data", listeParametres);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        // Récupération des SharedPreferences

        Log.i(TAG, "récupération des paramètres enregistrés dans les préférences partagées");
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String listString = sharedPreferences.getString("parameters_list", "");
        ArrayList<ParameterSeries> listeParametresSauvegardes = new ArrayList<>();
        Log.d(TAG, "string contenue dans les pref part : " + listString + "    ");
        try {
            if (!listString.isEmpty()) {
                Log.d(TAG,"string non vide!");
                // Convertion de la chaîne de caractères en liste
                Log.d(TAG,"string non vide!");
                String[] listArray = listString.split("ll");
                Log.d(TAG,"string non vide!");
                Log.d(TAG, "string récupérée dans les préférences partagées :" );
                Log.d(TAG,"string non vide!");
                for (int i = 0; i < min(listArray.length, 10); i++) {
                    Log.d(TAG, "création du paramètre n° "+i);
                    ParameterSeries parameter = ParameterSeries.fromString(listArray[i]);
                    listeParametresSauvegardes.add(parameter);
                }
            }
        } catch (Exception e) {
            Log.d("TAG", "pb avec la récupération des paramètres sauvegardés");
        }
        if (listeParametresSauvegardes.size() ==0) {
            // Add one serie by default
            Log.d(TAG, "ajout d'une série");
            addSerie(15,0,0,1, 1F);
        } else {
            for (int i=0; i<listeParametresSauvegardes.size(); i++) {
                ParameterSeries parameterSeries = listeParametresSauvegardes.get(i);
                addSerie(parameterSeries.getNbMesures(),parameterSeries.getMode(),parameterSeries.getSensBarre(),parameterSeries.getSensFond(),parameterSeries.getVitesseFond());
            }
        }
    }

    private void addSerie(int nbMesures, int mode, int sensBarre, int sensFond, Float vitesseFond) {
        listeParametres.add(new ParameterSeries(nbMesures,mode,sensBarre,sensFond,vitesseFond));
        LayoutInflater inflater = LayoutInflater.from(this);
        View serieView = inflater.inflate(R.layout.serie_layout, containerLayout, false);
        listeView.add(serieView);
        containerLayout.addView(serieView, containerLayout.getChildCount() );

        TextView serieTitle = serieView.findViewById(R.id.serieTitle);
        SeekBar mesuresSeekBar = serieView.findViewById(R.id.mesuresSeekBar);
        Button barreButton = serieView.findViewById(R.id.barreButton);
        Button fondButton = serieView.findViewById(R.id.fondButton);
        Button modeButton = serieView.findViewById(R.id.modeButton);
        Button deleteButton = serieView.findViewById(R.id.deleteButton);
        TextView mtextMesures = findViewById(R.id.tvNbMesures);
        EditText champVitesseFond = findViewById(R.id.vitesseFond);
        Button validationVitesseFondButton = serieView.findViewById(R.id.validationVitesse);

        //initialisation des boutons aux bonnes valeurs
        mesuresSeekBar.setProgress(nbMesures);
        //TextView textMesure = (TextView) ((LinearLayout)(((LinearLayout) serieView).getChildAt(0))).getChildAt(2);
        mtextMesures.setText("nbMesures");
        if (mode ==1) {
            modeButton.setText("VVS Dynamique");
        } else {
            modeButton.setText("VVS Simple");
        }
        if (sensFond ==1) {
            fondButton.setText("droite");
        } else {
            fondButton.setText("gauche");
        }
        if (sensBarre ==0) {
            barreButton.setText("droite");
        } else {
            barreButton.setText("gauche");
        }
        champVitesseFond.setText(Float.toString(vitesseFond));



        serieCount++;
        serieTitle.setText("Série " + serieCount);
        //bouton pour supprimer la série
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getPositionById(serieView);
                listeParametres.remove(position);
                listeView.remove(position);
                containerLayout.removeView(serieView);
            }
        });
        // changement du nombre de mesures de la série
        mesuresSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int position = getPositionById(serieView);
                listeParametres.get(position).setnbMesures(progress);
                String nbMesures = String.valueOf(listeParametres.get(position).getNbMesures());
                Log.i(TAG, "changement du nb de mesures : " + nbMesures);
                TextView textMesure = (TextView) ((LinearLayout)(((LinearLayout) serieView).getChildAt(0))).getChildAt(2);
                textMesure.setText(nbMesures);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        // changement du mode de vvs
        modeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getPositionById(serieView);
                ParameterSeries para = listeParametres.get(position);
                if (para.getMode() ==0) {
                    para.setmode(1);
                    modeButton.setText("VVS Dynamique");
                } else {
                    para.setmode(0);
                    modeButton.setText("VVS Simple");
                }
                Log.i(TAG, "changement du mode : mode " + String.valueOf(para.getMode()));
            }
        });

        // changement du sens de rotation de la barre
        barreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getPositionById(serieView);
                ParameterSeries para = listeParametres.get(position);
                if (para.getSensBarre() ==0) {
                    para.setSensBarre(1);
                    barreButton.setText("gauche");
                } else {
                    para.setSensBarre(0);
                    barreButton.setText("droite");
                }
                Log.i(TAG, "changement du sens de la barre : sens de la barre " + String.valueOf(para.getSensBarre()));
            }
        });

        // changement du sens de rotation du fond
        fondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getPositionById(serieView);
                ParameterSeries para = listeParametres.get(position);
                if (para.getSensFond() ==-1) {
                    para.setsensFond(1);
                    fondButton.setText("droite");
                } else {
                    para.setsensFond(-1);
                    fondButton.setText("gauche");
                }
                Log.i(TAG, "changement du sens du fond : sens " + String.valueOf(para.getSensFond()));
            }
        });

        // changement de la vitesse du fond
        validationVitesseFondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = getPositionById(serieView);
                ParameterSeries para = listeParametres.get(position);//2.2.1.0
                EditText champVitesse = (EditText) ((LinearLayout) ((LinearLayout) ((LinearLayout)(((LinearLayout) serieView).getChildAt(2))).getChildAt(2)).getChildAt(1)).getChildAt(0);
                String s = String.valueOf(champVitesse.getText());
                try {
                    float sens_fond = Float.valueOf(s);
                    Log.d(TAG, "changement de la vitesse du fond : " + s);
                } catch (NumberFormatException e) {
                    Log.d(TAG, "tentative de changement de la vitesse du fond (ratée ): " + s);
                }
            }
        });

    }

    //obtenir le numéro de la série
    private int getPositionById(View serieview) {
        for (int i = 0; i < listeView.size(); i++) {
            if (serieview == null) {
                return -1;
            }
            if (serieview == listeView.get(i)) {
                return i;
            }
        }
        return -1;
    }

    //obtenir l'instance du textView contenu dans le linearlayout passée en paramètre:
    private TextView getTextView(LinearLayout serieView) {
        int childCount = serieView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = serieView.getChildAt(i);
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                return textView;
            }
        }
        return null;
    }
}