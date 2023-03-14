package com.google.cardboard;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

/*
 * Activité principale, permet :
 *  - le choix du nom du patient
 *  - le choix du nombre de mesures
 *  - le choix du type de mesure (simple/dynamique)
 *  - le choix du rôle du téléphone (manette/VR)
 *  - l'accès à la dernière mesure
 */
public class HomeActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    static final String MESSAGE_B_RECEIVED = "RECEIVED";

    public static final String TAG = "HomeActivity";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_BT_PERMISSIONS = 2;
    private static final int REQUEST_CODE_ECRAN_ACTIVITY = 4;
    private static final int REQUEST_CODE_PROTOCOLE = 17;

    // Nom du fichier et des éléments permettant de stocker la dernière mesure (fiche patient)
    private static final String SHARED_PREF_USER_INFO = "SHARED_PREF_USER_INFO";
    private static final String SHARED_PREF_USER_INFO_NAME = "SHARED_PREF_USER_INFO_NAME";
    private static final String SHARED_PREF_USER_INFO_SCORE = "SHARED_PREF_USER_INFO_SCORE";

    // Octets envoyés par la manette pour la rotation gauche/droite et la fin de la mesure
    public static final byte[] byte_d = "d".getBytes();
    public static final byte[] byte_dl = "dl".getBytes();
    public static final byte[] byte_g = "g".getBytes();
    public static final byte[] byte_gl = "gl".getBytes();
    public static final byte[] byte_t = "t".getBytes();
    public static final byte[] byte_s = "s".getBytes();

    public static PatientData selectedPatient;

    BluetoothAdapter bluetoothAdapter;

    Button mbuttonManette;
    Button mbuttonEcran;
    Button mbuttonBluetoothReset;
    Button mbuttonOuvrir;
    Button mbuttonProtocole;
    SeekBar mseekMesures;
    ToggleButton mtoggleSimple;
    ToggleButton mtoggleDynamique;
    TextView mtextMesures;
    //Button mbuttonTutoriel;
    Button mbuttonSelectPatient;
    Button buttonExport;
    static TextView patientNameDisplay;
    static int nbMeasure = 5;

    int modeMesure;
    public static HomeActivity instance;

    SharedPreferences mPrefs;
    static SharedPreferences.Editor mEditor;

    static int patientId;
    AlertDialog noPatientDialog;

    public static void IncrementPatientId() {
        patientId++;
        mEditor.putInt("patientNumber", patientId);
        mEditor.commit();
    }

    public static void SelectPatient(PatientData patient) {
        selectedPatient = patient;
        patientNameDisplay.setText(selectedPatient.lastName + " " + selectedPatient.firstName);
    }
    //la liste des paramètres des conditions des séries de mesures
    ArrayList<ParameterSeries> listeParametres = new ArrayList<ParameterSeries>();

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_home);
        instance = this;
        //XmlManager.EraseIndex();
        XmlManager.ReadIndex();
        mPrefs = getSharedPreferences("label", 0);
        mEditor = mPrefs.edit();
        patientId = mPrefs.getInt("patientNumber", 0);

        // Récupération des références aux éléments graphiques
        mbuttonManette = findViewById(R.id.manette);
        //mbuttonTutoriel = findViewById(R.id.tutoriel);
        mbuttonEcran = findViewById(R.id.ecran);
        mbuttonBluetoothReset = findViewById(R.id.bluetooth_reset);
        mbuttonOuvrir = findViewById(R.id.openusrdata);
        mbuttonProtocole = findViewById(R.id.protocole);
        mtextMesures = findViewById(R.id.tvNbMesures);
        patientNameDisplay = findViewById(R.id.patientId);

        //mseekMesures.setProgress(nbMeasure);
        noPatientDialog = CreateNoPatientAlertMessage();

        if (selectedPatient != null) patientNameDisplay.setText(selectedPatient.lastName + " " + selectedPatient.firstName);

        buttonExport = findViewById(R.id.button_export);
        mbuttonSelectPatient = findViewById(R.id.placeholderButton); //to delete

        buttonExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CsvManager.WriteAllData();
            }
        });

        mbuttonSelectPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(),PatientSelectionActivity.class);
                startActivity(intent);
            }
        });

        //mbuttonEcran.setEnabled(false);
        //mbuttonOuvrir.setEnabled(false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listeParametres.add(new ParameterSeries(3,0,1,1,1));

        mbuttonEcran.setOnClickListener(enablebtListener);
        mbuttonManette.setOnClickListener(enablebtListener);
        //mbuttonTutoriel.setOnClickListener(enablebtListener);
        getRequiredPermissions();



        // Affiche la dernière mesure si le nom renseigné correspond à celui de la dernière mesure réalisée
        mbuttonBluetoothReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove("device_address");
                editor.apply();
                Toast.makeText(getApplicationContext(), "bluetooth settings reset", Toast.LENGTH_SHORT).show();
            }
        });


        // Affiche la dernière mesure si le nom renseigné correspond à celui de la dernière mesure réalisée
        mbuttonOuvrir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(),FilesDisplayActivity.class);
                startActivity(intent);
            }
        });

        // ouvre l'activité pour choisir le protocole
        mbuttonProtocole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ProtocoleActivity.class);
                startActivityForResult(intent,REQUEST_CODE_PROTOCOLE);
            }
        });


    }

    AlertDialog CreateAlertMessage() {
        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(0, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        builder.setMessage("Are you sure you want to delete this patient file ? This operation is irreversible")
                //.setCancelable(true)
                .setPositiveButton("Delete file", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Export Data
                        CsvManager.WriteAllData();
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        Log.d("dialog", "created");
        return builder.create();
    }

    // Listener affecté aux boutons Manette et Ecran tant que le bletooth n'est pas activé demandant de l'activer
    private View.OnClickListener enablebtListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(HomeActivity.this, "Les autorisations bluetooth sont nécessaires au fonctionnement de l'application", Toast.LENGTH_LONG);
            getRequiredPermissions();
        }
    };

    // Listener pour le bouton Manette, stock le nom du patient et démarre l'activité ConnecteEcran
    private View.OnClickListener manetteListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(HomeActivity.this, ConnecteEcran.class));
        }
    };
    /** @TODO FAIRE UNE activité tutoriel qui fonctionne
    /* Listener pour le bouton tutoriel
    private View.OnClickListener tutorielListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getSharedPreferences(SHARED_PREF_USER_INFO, MODE_PRIVATE)
                    .edit()
                    .putString(SHARED_PREF_USER_INFO_NAME, mEditTextNom.getText().toString())
                    .apply();
            startActivity(new Intent(HomeActivity.this, TutorialActivity.class));
        }
    };*/

    // Listener pour le bouton Ecran, stock le nom du patient et démarre l'activité VrActivity en renseignant le nombre de mesures et le mode de mesure
    private View.OnClickListener ecranListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (selectedPatient == null) {
                noPatientDialog.show();
                return;
            }
            getSharedPreferences(SHARED_PREF_USER_INFO, MODE_PRIVATE)
                    .edit()
                    .putString(SHARED_PREF_USER_INFO_NAME, selectedPatient.filename)
                    .apply();
            Intent intentEcran = new Intent((Context) getBaseContext(), VrActivity.class);
            intentEcran.putExtra("parametres", listeParametres);
            startActivityForResult(intentEcran, REQUEST_CODE_ECRAN_ACTIVITY);
            //ProtocoleActivity.DeleteProtocole();
        }
    };

    static ArrayList<Float> arrayScore;

    /**
     * Gestion des résultats des activités VrActivity et l'activation du buetooth
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Le bluetooth a été activé
        if (REQUEST_ENABLE_BT == requestCode && RESULT_OK == resultCode) {
            Log.d(TAG, "Bluetooth enabled");
            // Attribution des Listener aux boutons Manette et Ecran
            mbuttonManette.setOnClickListener(manetteListener);
            mbuttonEcran.setOnClickListener(ecranListener);
            //mbuttonTutoriel.setOnClickListener(tutorielListener);
        } else if ((requestCode == REQUEST_CODE_PROTOCOLE) && (RESULT_OK== resultCode) && (data != null)) {
            Log.i(TAG, "Received Result from Protocole");
            listeParametres = data.getParcelableArrayListExtra("data");
            Log.i(TAG, "nombres de séries prévues : " + String.valueOf(listeParametres.size()));
            Log.i(TAG, "sens du fond de la première série : " + String.valueOf(listeParametres.get(0).getSensFond()));
        } else {
            Log.i(TAG, "Unknown ActivityResult received");
        }
    }

    /**
     * Runtime permissions en fonction de l'API du téléphone et activation du bluetooth
     */
    private void getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.i(TAG, "Requesting 11+ API bluetooth Permissions");
            String[] requiredPermissions = new String[]{ Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE};
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_BT_PERMISSIONS);
        } else {
            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                Log.i(TAG, "Requesting enable bluetooth");
                startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), REQUEST_ENABLE_BT);
            } else {
                Log.i(TAG, "bluetooth already enabled");
                mbuttonManette.setOnClickListener(manetteListener);
                mbuttonEcran.setOnClickListener(ecranListener);
                //mbuttonTutoriel.setOnClickListener(tutorielListener);
            }
        }
    }

    /**
     * Vérifie que les permissions ont été accordées, si non, redemande
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BT_PERMISSIONS) {
            Log.i(TAG, "Received response for Bluetooth permissions request.");
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Bluetooth permissions granted.");
                mbuttonManette.setOnClickListener(manetteListener);
                mbuttonEcran.setOnClickListener(ecranListener);
                //mbuttonTutoriel.setOnClickListener(tutorielListener);
            } else {
                Log.i(TAG, "Bluetooth permissions denied.");
                Toast.makeText(this, "Les autorisations bluetooth sont nécessaires au fonctionnement de l'application", Toast.LENGTH_LONG);
                getRequiredPermissions();
            }
        }
    }


    private static interface MessageConstants {
        public static final int MESSAGE_READ = 0;

        public static final int MESSAGE_TOAST = 2;

        public static final int MESSAGE_WRITE = 1;
    }

    static class mHandler extends Handler {
        String command;
        @Override
        public void handleMessage(Message param1Message) {
            super.handleMessage(param1Message);
            if (param1Message.what == 0) {
                this.command = new String((byte[]) param1Message.obj);
            }
        }
    }

    AlertDialog CreateNoPatientAlertMessage() {
        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
        builder.setMessage("Please select a patient before starting a measurement")
                //.setCancelable(true)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        Log.d("dialog", "created");
        return builder.create();
    }
}
