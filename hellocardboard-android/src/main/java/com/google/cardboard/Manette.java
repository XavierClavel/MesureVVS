package com.google.cardboard;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.cardboard.MyBluetoothService;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Activité constituée d'un bouton gauche, un bouton droite et un bouton result.
 * Chaque bouton envoi une commande à l'appareil Ecran.
 */
public class Manette extends Activity {
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;

    private static final String TAG = "Manette";

    private String SERVICE_ID = "com.example.mesurevvs";

    private UUID MY_UUID = UUID.fromString("a0113482-fe2f-4ee4-bbbd-7f4346868e9b");

    private BroadcastReceiver Receiver = new BroadcastReceiver() {
        public void onReceive(Context param1Context, Intent param1Intent) {
            param1Intent.getAction();
            TextView textView = (TextView)Manette.this.findViewById(R.id.result_text);
            String strResult = "Results=" + Manette.this.result + "\n";
            textView.setText(strResult);
        }
    };

    BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

    private String mac;

    public static String result = "";

    private BluetoothDevice selected_device;

    private MyBluetoothService service;

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_manette);
        new Message();
        // Enregistrement des paramètres de la connexion bluetooth avec l'écran et attribution du rôle de Client bluetooth
        this.service = new MyBluetoothService((Context)this, this.mHandler);
        this.mac = getIntent().getExtras().getString("mac");
        this.selected_device = this.mBtAdapter.getRemoteDevice(this.mac);
        this.service.startClient(this.selected_device, this.MY_UUID);
        IntentFilter intentFilter = new IntentFilter("RECEIVED");
        registerReceiver(this.Receiver, intentFilter);
        // Appuyer sur le bouton droite envoi l'instruction de rotation et le relacher envoi l'instruction d'arrêter la rotation
        (findViewById(R.id.droite)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    Manette.this.service.write(HomeActivity.byte_d);
                } else if (action == MotionEvent.ACTION_UP) {
                    Manette.this.service.write(HomeActivity.byte_s);
                }
                return true;
            }
        });
        // Appuyer sur le bouton droite_lent envoi l'instruction de rotation et le relacher envoi l'instruction d'arrêter la rotation
        (findViewById(R.id.droite_lent)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    Manette.this.service.write(HomeActivity.byte_dl);
                } else if (action == MotionEvent.ACTION_UP) {
                    Manette.this.service.write(HomeActivity.byte_s);
                }
                return true;
            }
        });
        // Appuyer sur le bouton gauche envoi l'instruction de rotation et le relacher envoi l'instruction d'arrêter la rotation
        (findViewById(R.id.gauche)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    view.performClick();
                    Manette.this.service.write(HomeActivity.byte_g);
                } else if (action == MotionEvent.ACTION_UP) {
                    Manette.this.service.write(HomeActivity.byte_s);
                }
                return true;
            }
        });
        // Appuyer sur le bouton gauche_lent envoi l'instruction de rotation et le relacher envoi l'instruction d'arrêter la rotation
        (findViewById(R.id.gauche_lent)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    view.performClick();
                    Manette.this.service.write(HomeActivity.byte_gl);
                } else if (action == MotionEvent.ACTION_UP) {
                    Manette.this.service.write(HomeActivity.byte_s);
                }
                return true;
            }
        });
        // Le click sur le bouton result envoi l'instruction d'arrêter la mesure
        ((Button)findViewById(R.id.result)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View param1View) {
                Manette.this.service.write(HomeActivity.byte_t);
            }
        });
    }

    // Gestion de l'affichage du résultat final sur la manette
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message param1Message) {
            super.handleMessage(param1Message);
            if (param1Message.what == 0) {
                Manette.result = new String((byte[]) param1Message.obj, StandardCharsets.UTF_8);
                TextView textView = (TextView)Manette.this.findViewById(R.id.result_text);
                String strResult = Manette.result + "\n";
                textView.setText(strResult);
            }
        }
    };

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.Receiver);
    }
}
