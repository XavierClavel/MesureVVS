package com.google.cardboard;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
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

    BluetoothAdapter bluetoothAdapter;

    Button mbuttonManette;
    Button mbuttonEcran;
    Button mbuttonOuvrir;
    Button mbuttonProtocole;
    SeekBar mseekMesures;
    ToggleButton mtoggleSimple;
    ToggleButton mtoggleDynamique;
    EditText mEditTextNom;
    TextView mtextMesures;
    //Button mbuttonTutoriel;

    int modeMesure;
    //la liste des paramètres des conditions des séries de mesures
    ArrayList<ParameterSeries> listeParametres = new ArrayList<ParameterSeries>();

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_home);

        // Initialisation des éléments graphiques
        mbuttonManette = findViewById(R.id.manette);
        //mbuttonTutoriel = findViewById(R.id.tutoriel);
        mbuttonEcran = findViewById(R.id.ecran);
        mbuttonOuvrir = findViewById(R.id.openusrdata);
        mbuttonProtocole = findViewById(R.id.protocole);
        mEditTextNom = findViewById(R.id.editT_nomPatient);
        mseekMesures = findViewById(R.id.sbNbMesures);
        mtextMesures = findViewById(R.id.tvNbMesures);
        mtoggleSimple = findViewById(R.id.tbSimple);
        mtoggleDynamique = findViewById(R.id.tbDynamique);

        mtextMesures.setText(String.valueOf(mseekMesures.getProgress()));
        mbuttonEcran.setEnabled(false);
        mbuttonOuvrir.setEnabled(false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listeParametres.add(new ParameterSeries(3,0,1,1,1));

        mbuttonEcran.setOnClickListener(enablebtListener);
        mbuttonManette.setOnClickListener(enablebtListener);
        //mbuttonTutoriel.setOnClickListener(enablebtListener);
        getRequiredPermissions();

        // Affiche la dernière mesure si le nom renseigné correspond à celui de la dernière mesure réalisée
        mbuttonOuvrir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String alertMessage;
                String currentScore = getSharedPreferences(SHARED_PREF_USER_INFO, MODE_PRIVATE).getString(SHARED_PREF_USER_INFO_SCORE, "Réaliser une mesure pour afficher le résultat");
                String currentName = getSharedPreferences(SHARED_PREF_USER_INFO, MODE_PRIVATE).getString(SHARED_PREF_USER_INFO_NAME, "Test");
                if (!currentName.equals(mEditTextNom.getText().toString())) {
                    alertMessage = "Réaliser une mesure pour afficher le résultat";
                } else {
                    alertMessage = currentScore;
                }
                new AlertDialog.Builder(HomeActivity.this)
                                .setTitle(mEditTextNom.getText().toString())
                                .setMessage(alertMessage)
                                .setCancelable(true).create().show();
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

        // Nom du patient, nécessaire pour activer les boutons Ecran et Ouvrir
        mEditTextNom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mbuttonEcran.setEnabled(!editable.toString().isEmpty());
                mbuttonOuvrir.setEnabled(!editable.toString().isEmpty());
            }
        });

        // Nombre de mesures, déterminé par le slider
        mseekMesures.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mtextMesures.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // VVS simple
        mtoggleSimple.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    modeMesure = 0;
                    mtoggleDynamique.setChecked(false);
                }
            }
        });

        // VVS dynamique
        mtoggleDynamique.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    modeMesure = 1;
                    mtoggleSimple.setChecked(false);
                    Log.i("ModeDynamique", "ENTREE DANS UNE MESURE DYNAMIQUE");
                }
            }
        });
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
            getSharedPreferences(SHARED_PREF_USER_INFO, MODE_PRIVATE)
                    .edit()
                    .putString(SHARED_PREF_USER_INFO_NAME, mEditTextNom.getText().toString())
                    .apply();
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

    // Listener pour le bouton Ecran, stock le nom du patient et démarre l'activit" VrActivity en renseignant le nombre de mesures et le mode de mesure
    private View.OnClickListener ecranListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getSharedPreferences(SHARED_PREF_USER_INFO, MODE_PRIVATE)
                    .edit()
                    .putString(SHARED_PREF_USER_INFO_NAME, mEditTextNom.getText().toString())
                    .apply();
            Intent intentEcran = new Intent((Context) getBaseContext(), VrActivity.class);
            intentEcran.putExtra("nbMesures", Integer.parseInt(mtextMesures.getText().toString()));
            intentEcran.putExtra("modeMesure", modeMesure);
            intentEcran.putExtra("parametres", listeParametres);
            startActivityForResult(intentEcran, REQUEST_CODE_ECRAN_ACTIVITY);
        }
    };

    /**
     * Gestion des résultats des activités VrActivity et l'activation du buetooth
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // L'activité VrActivity s'est terminée et renvoie les résultats de la mesure
        if (REQUEST_CODE_ECRAN_ACTIVITY == requestCode && RESULT_OK == resultCode && data != null) {
            Log.i(TAG, "Recieved result from Ecran");
            // Ajout des scores au fichier de préférences
            ArrayList<Float> arrayScore =(ArrayList<Float>)  data.getSerializableExtra(VrActivity.RESULT_SCORE);
            String score = arrayScore.stream().map(Object::toString).collect(Collectors.joining(", "));
            getSharedPreferences(SHARED_PREF_USER_INFO, MODE_PRIVATE)
                    .edit()
                    .putString(SHARED_PREF_USER_INFO_SCORE, score)
                    .apply();
        }
        // Le bluetooth a été activé
        else if (REQUEST_ENABLE_BT == requestCode && RESULT_OK == resultCode) {
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
}
