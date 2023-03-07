package com.google.cardboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activité intermédiaire avant l'activité manette permettant d'établir la connexion bluetooth.
 * Permet de choisir entre les appareils appairés et la découverte de nouveaux appareils à proximité.
 */
public class ConnecteEcran extends AppCompatActivity {

    private static final String TAG = "DeviceListActivity";
    private boolean mEnableTouchEvents;
    private BluetoothAdapter mBtAdapter;

    Button btn_scan;
    Button btn_connect;
    Button btn_paired;

    ArrayList<String> macother = new ArrayList<String>();
    ArrayList<String> macpaired = new ArrayList<String>();
    ArrayList<String> nameother = new ArrayList<String>();
    ArrayList<String> namepaired = new ArrayList<String>();

    ArrayAdapter<String> mPairedArray;
    ArrayAdapter<String> mDiscoveredArray;

    AlertDialog mDiscoveredDialog;

    Spinner mDiscoveredSpinner;
    Spinner mPairedSpinner;

    BluetoothDevice selected_device;

    ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // ok
                    }
                }
            });

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_connecte_ecran);
        mEnableTouchEvents = true;
        setResult(0);

        // Initialisation des éléments graphiques
        btn_scan = findViewById(R.id.button_scan);
        btn_paired = findViewById(R.id.button_paired);
        btn_connect = findViewById(R.id.button_connect);
        btn_connect.setEnabled(false);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        // vérification que le bluetooth est disponible sur l'appareil
        if (mBtAdapter != null) {
            if (mBtAdapter.isEnabled()) {
                //connection avec l'appareil appairee s'il y en a un:
                Log.d("LogConnectEcran", "debutt");
                // Récupération des informations de connexion depuis les préférences partagées
                SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                String address = prefs.getString("device_address", null);
                try {
                    if (address != null) {
                        selected_device = mBtAdapter.getRemoteDevice(address);
                        // Connexion automatique à l'appareil Bluetooth précédemment appairé
                        // Utilisez les classes BluetoothSocket ou BluetoothGatt pour établir une connexion Bluetooth
                        if (mBtAdapter.isDiscovering()) {
                            mBtAdapter.cancelDiscovery();
                        }
                        Intent intent = new Intent((Context) getBaseContext(), Manette.class);
                        intent.putExtra("mac", selected_device.getAddress());
                        Toast.makeText(getBaseContext(), selected_device.getAddress(), Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    }
                } catch (Exception e) {
                    Log.d(TAG, "erreur lors de la récupération de l'adresse bluetooth enregistrée");
                }
                // Récupération des appareils appairés
                Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
                // Il existe au moins un appreil appairé
                if (pairedDevices.size() > 0) {
                    // Récupération des informations (nom + adresse MAC)
                    for (BluetoothDevice bluetoothDevice : pairedDevices) {
                        if (bluetoothDevice.getName() != null && !bluetoothDevice.getName().isEmpty()) {
                            namepaired.add(bluetoothDevice.getName());
                        } else {
                            namepaired.add(bluetoothDevice.getAddress());
                        }
                        macpaired.add(bluetoothDevice.getAddress());
                    }
                    mPairedArray = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, this.namepaired);
                    mPairedArray.setNotifyOnChange(true);
                    // Un click sur le bouton "paired devices" affiche la liste des noms des appareils appairés
                    btn_paired.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new AlertDialog.Builder(ConnecteEcran.this)
                                    .setTitle("Paired Devices")
                                    .setAdapter(mPairedArray, new DialogInterface.OnClickListener() {
                                        // Un click sur un appareil de la liste le sélectionne
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            selected_device = mBtAdapter.getRemoteDevice(macpaired.get(i));

                                            // On stock l'adresse MAC dans les préférences partagées
                                            String address = selected_device.getAddress();
                                            SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString("device_address", address);
                                            editor.apply();

                                            btn_connect.setEnabled(true);
                                            String txConnect = "Connect to " + selected_device.getName();
                                            btn_connect.setText(txConnect);
                                            dialogInterface.dismiss();
                                        }
                                    }).create().show();

                        }
                    });
                }
                mDiscoveredArray = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, this.nameother);
                mDiscoveredArray.setNotifyOnChange(true);
                IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                registerReceiver(mReceiver, intentFilter);
                // Un click sur le bouton "san for devices" réalise une découverte des appareils disponibles à proximité
                // et affiche la liste des appareils disponibles
                btn_scan.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View param1View) {
                        nameother.clear();
                        macother.clear();
                        btn_scan.setEnabled(false);
                        btn_scan.setText("Scanning...");
                        btn_paired.setEnabled(false);
                        Log.d("DeviceListActivity", "startDiscovery()");
                        Toast.makeText(getBaseContext(), "DISCOVERY_STARTED", Toast.LENGTH_SHORT).show();
                        mBtAdapter.startDiscovery();
                    }
                });
                // Connexion à l'appareil séléctionner parmis les appareils appairés ou découverts
                btn_connect.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View param1View) {
                        if (selected_device == null) {
                            Toast.makeText(getBaseContext(), "You need to select a device first", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            if (mBtAdapter.isDiscovering()) {
                                mBtAdapter.cancelDiscovery();
                            }
                            Intent intent = new Intent((Context) getBaseContext(), Manette.class);
                            intent.putExtra("mac", selected_device.getAddress());
                            Toast.makeText(getBaseContext(), selected_device.getAddress(), Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            } else {
                // Le bluetooth n'est pas activé, demande de l'activer
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                mActivityResultLauncher.launch(enableBtIntent);
            }
        }
    }

    // Récepteur broadcast gérant la découverte bluetooth
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Un nouvel appareil bluetooth a été découvert
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Stockage du nom et de l'adresse MAC
                if (bluetoothDevice.getName() != null && !bluetoothDevice.getName().isEmpty()) {
                    nameother.add(bluetoothDevice.getName());
                } else {
                    nameother.add(bluetoothDevice.getAddress());
                }
                    macother.add(bluetoothDevice.getAddress());
            }
            // Fin de la découverte bluetooth
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                btn_paired.setEnabled(true);
                btn_scan.setEnabled(true);
                btn_scan.setText(R.string.button_scan);
                // Si il y a au moins un appareil bluetooth découvert, affiche un AlertDialog contenant la liste des appareils
                if (nameother.size() > 0) {
                    new AlertDialog.Builder(ConnecteEcran.this)
                            .setTitle("Discovered Devices")
                            .setAdapter(mDiscoveredArray, new DialogInterface.OnClickListener() {
                                // Le click sur un appareil de la liste sélectionne l'appareil et renseigne son nom sur le bouton connect
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    selected_device = mBtAdapter.getRemoteDevice(macother.get(i));
                                    btn_connect.setEnabled(true);
                                    String txConnect = "Connect to " + selected_device.getName();
                                    btn_connect.setText(txConnect);
                                    dialogInterface.dismiss();
                                }
                            }).create().show();
                } else {
                    // Pas d'appareil découvert
                    Toast.makeText(ConnecteEcran.this, "No device found", Toast.LENGTH_SHORT);
                }
            }
        }
    };

    protected void onDestroy() {
        super.onDestroy();
        if (mBtAdapter != null)
        mBtAdapter.cancelDiscovery();
        unregisterReceiver(mReceiver);
    }

    protected void onResume() {
        super.onResume();
    }
}
