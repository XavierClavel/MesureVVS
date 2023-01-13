package com.google.cardboard;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import android.os.Handler;

public class MyBluetoothService {
    private static final String Name = "MesureVVS";

    private static final String TAG = "BluetoothConnectionServ";

    private UUID MY_UUID = UUID.fromString("a0113482-fe2f-4ee4-bbbd-7f4346868e9b");

    private UUID deviceUUID;

    private Handler mHandler;

    private final BluetoothAdapter mBluetoothAdapter;

    private ConnectThread mConnectThread;

    private AcceptThread mAcceptThread;

    private ConnectedThread mConnectedThread;

    Context mContext;

    private AcceptThread mInsecureAcceptThread;

    ProgressDialog mProgressDialog;

    private BluetoothDevice mmDevice;

    public MyBluetoothService(Context paramContext, Handler parammHandler) {
        this.mContext = paramContext;
        this.mHandler = parammHandler;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    // Création du Thread de communication lorsque la connexion est établie
    private void connected(BluetoothSocket paramBluetoothSocket, BluetoothDevice paramBluetoothDevice) {
        Log.d(TAG, "connected: Starting.");
        this.mConnectedThread = new ConnectedThread(paramBluetoothSocket);
        this.mConnectedThread.start();
    }

    // Céation du Thread de connexion serveur (pour l'Ecran)
    public void startServer(UUID paramUUID) {
        Log.d(TAG, "startServer: Started");
        this.mProgressDialog = ProgressDialog.show(this.mContext, "Connecting Bluetooth", "Please Wait...", true);
        this.mAcceptThread = new AcceptThread(paramUUID);
        this.mAcceptThread.start();
    }

    // Création du Thread de connexion client (pour la Manette)
    public void startClient(BluetoothDevice paramBluetoothDevice, UUID paramUUID) {
        Log.d(TAG, "startClient: Started.");
        this.mProgressDialog = ProgressDialog.show(this.mContext, "Connecting Bluetooth", "Please Wait...", true);
        this.mConnectThread = new ConnectThread(paramBluetoothDevice, paramUUID);
        this.mConnectThread.start();
    }

    // Ecriture des octets d'information dans le canal connecté
    public void write(byte[] paramArrayOfbyte) {
        Log.d(TAG, "write: Write Called.");
        this.mConnectedThread.write(paramArrayOfbyte);
    }

    public class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(UUID paramUUID) {
            BluetoothServerSocket bluetoothServerSocket = null;
            try {
                bluetoothServerSocket = MyBluetoothService.this.mBluetoothAdapter.listenUsingRfcommWithServiceRecord("MesureVVS", paramUUID);
            } catch (IOException iOException) {
                Log.e(TAG, "Socket's listen() method failed", iOException);
            }
            this.mmServerSocket = bluetoothServerSocket;
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                this.mmServerSocket.close();
            } catch (IOException iOException) {
                Log.e(TAG, "Could not close the connect socket", iOException);
                return;
            }
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");
            BluetoothSocket tempSocket = null;
            while (true) {
                try {
                    Log.d(TAG, "run: RFCOM server socket start.....");
                    tempSocket = this.mmServerSocket.accept();
                    Log.d(TAG, "run: RFCOM server socket accepted connection.");
                } catch (IOException iOException) {
                    Log.e(TAG, "Socket's accept() method failed", iOException);
                    break;
                }
                if (tempSocket != null) {
                    MyBluetoothService myBluetoothService = MyBluetoothService.this;
                    myBluetoothService.connected(tempSocket, myBluetoothService.mmDevice);
                    try {
                        this.mmServerSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close the connect socket", e);
                    }
                    Log.i(TAG, "END mAcceptThread ");
                    break;
                }
            }
        }
    }

    // Thread de connexion
    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice param1BluetoothDevice, UUID param1UUID) {
            Log.d(TAG, "ConnectThread: started.");
            BluetoothSocket tmp = null;
            mmDevice = param1BluetoothDevice;
            deviceUUID = param1UUID;
            try {
                tmp = param1BluetoothDevice.createRfcommSocketToServiceRecord(param1UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's creat() method failed", e);
            }
            mmSocket = tmp;
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            Log.i(TAG, "RUN mConnectThread ");
            try {
                mmSocket.connect();
                Log.d(TAG, "Trying to connect to server socket");
            } catch (IOException connectException) {
                Log.e("BluetoothConnectServ", "Could not connect to the server socket", connectException);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e("BluetoothConnectServ", "Could not close the client socket", closeException);
                }
            }
            MyBluetoothService myBluetoothService = MyBluetoothService.this;
            myBluetoothService.connected(this.mmSocket, myBluetoothService.mmDevice);
        }
    }

    // Thread de communication
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;

        private final OutputStream mmOutStream;

        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket param1BluetoothSocket) {
            this.mmSocket = param1BluetoothSocket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            Log.d(TAG, "ConnectedThread: Starting.");
            try {
                MyBluetoothService.this.mProgressDialog.dismiss();
            } catch (NullPointerException nullPointerException) {
                nullPointerException.printStackTrace();
            }
            try {
                tmpIn = param1BluetoothSocket.getInputStream();
                tmpOut = param1BluetoothSocket.getOutputStream();
            } catch (IOException iOException) {
                iOException.printStackTrace();
            }
            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
        }

        public void cancel() {
            try {
                this.mmSocket.close();
                return;
            } catch (IOException iOException) {
                return;
            }
        }

        public void run() {
            byte[] arrayOfByte = new byte[1024];
            try {
                while (true) {
                    int i = this.mmInStream.read(arrayOfByte);
                    String str = new String(arrayOfByte, 0, i);
                    Intent intent = new Intent("RECEIVED");
                    MyBluetoothService.this.mContext.sendBroadcast(intent);
                    MyBluetoothService.this.mHandler.obtainMessage(0, i, -1, arrayOfByte).sendToTarget();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("InputStream: ");
                    stringBuilder.append(str);
                    Log.d(TAG, stringBuilder.toString());
                }
            } catch (IOException iOException) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("write: Error reading Input Stream. ");
                stringBuilder.append(iOException.getMessage());
                Log.e(TAG, stringBuilder.toString());
                return;
            }
        }

        public void write(byte[] param1ArrayOfbyte) {
            String str = new String(param1ArrayOfbyte, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("write: Writing to outputstream: ");
            stringBuilder.append(str);
            Log.d(TAG, stringBuilder.toString());
            try {
                this.mmOutStream.write(param1ArrayOfbyte);
                return;
            } catch (IOException iOException) {
                StringBuilder stringBuilder1 = new StringBuilder();
                stringBuilder1.append("write: Error writing to output stream. ");
                stringBuilder1.append(iOException.getMessage());
                Log.e(TAG, stringBuilder1.toString());
                return;
            }
        }
    }

    private static interface MessageConstants {
        public static final int MESSAGE_READ = 0;

        public static final int MESSAGE_TOAST = 2;

        public static final int MESSAGE_WRITE = 1;
    }
}
