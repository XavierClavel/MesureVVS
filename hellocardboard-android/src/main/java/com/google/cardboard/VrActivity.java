/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cardboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * A Google Cardboard VR NDK sample application.
 *
 * <p>This is the main Activity for the sample application. It initializes a GLSurfaceView to allow
 * rendering.
 */
// TODO(b/184737638): Remove decorator once the AndroidX migration is completed.
@SuppressWarnings("deprecation")
public class VrActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
  static {
    System.loadLibrary("cardboard_jni");
  }

  private static final String TAG = VrActivity.class.getSimpleName();

  // Permission request codes
  private static final int PERMISSIONS_REQUEST_CODE = 2;

  // Opaque native pointer to the native CardboardApp instance.
  // This object is owned by the VrActivity instance and passed to the native methods.
  // Donc permet la communication entre cette activité java et le code C++
  private long nativeApp;
  //Angle de la barre verticale, communiqué par la code c++ via la méthode getAngle().
  private float mAngle;
  //Gestion de l'espace 3D OPENGl
  private GLSurfaceView glView;

  private final UUID MY_UUID = UUID.fromString("a0113482-fe2f-4ee4-bbbd-7f4346868e9b");

  //Communication du score à l'activité HomeActivity
  public static final String RESULT_SCORE = "RESULT_SCORE";

  private final String NAME = "Vertical_Subjective_Connection";

  private ArrayList<Float> mScore = new ArrayList<Float>();

  private List<String> mControlsArrayAdapter;



  private BroadcastReceiver Receiver = new BroadcastReceiver() {
    public void onReceive(Context param1Context, Intent param1Intent) {
      // ok
    }
  };

  Float[] alpha = new Float[1];

  BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

  HomeActivity.mHandler handler = new HomeActivity.mHandler();

  ArrayList<ParameterSeries> listeParametres;
  private int mode_mesure;
  private int mesure_restantes;
  private int nb_series_restantes;
  private int num_serie;
  private int sens_barre;
  private int sens_fond;
  private float vitesseFond;

  MyBluetoothService service;
  int tourne;
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


  @SuppressLint("ClickableViewAccessibility")
  @Override
  public void onCreate(Bundle savedInstance) {
    super.onCreate(savedInstance);
    Log.i("InputStream","ENtrée dans VrActivity");

    nativeApp = nativeOnCreate(getAssets());

    setContentView(R.layout.activity_vr);
    glView = findViewById(R.id.surface_view);
    glView.setEGLContextClientVersion(2);
    Renderer renderer = new Renderer();
    glView.setRenderer(renderer);
    glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    glView.setOnTouchListener(
        (v, event) -> {
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Signal a trigger event.
            glView.queueEvent(
                () -> {

                });
            return true;
          }
          return false;
        });

    Intent discoverableIntent =
            new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_SCAN_MODE,BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
    mActivityResultLauncher.launch(discoverableIntent);
    //Gestion du nombre de mesures à faire. En cas de non reception de la variable, 3 mesures.
    if (new Integer(getIntent().getExtras().getInt("nbMesures")) != null){
      mesure_restantes = getIntent().getExtras().getInt("nbMesures") - 1;
    } else {
      mesure_restantes = 2;
    }
    //Gestion du type de mesure (Dynamique ou Statique). En cas de non reception, VVS Statique.
    if (new Integer(getIntent().getExtras().getInt("modeMesure")) != null){
      mode_mesure = getIntent().getExtras().getInt("modeMesure");
    } else {
      mode_mesure = 0;
    }
    //Gestion des paramètres transmis. Si non reception, VVS statique 5 mesures droite droite
    if (new Integer(getIntent().getExtras().getInt("parametres")) != null){
      Log.d(TAG, "parametres non nuls");
      listeParametres = getIntent().getParcelableArrayListExtra("parametres");
      nb_series_restantes = listeParametres.size();
      Log.d(TAG, "nb de séries restantes : " + String.valueOf(nb_series_restantes));
      Log.d("VRactivity", "tag : " + TAG);
      num_serie = 0;
      ParameterSeries param_serie = listeParametres.get(num_serie);
      mode_mesure = param_serie.getMode();
      mesure_restantes = param_serie.getNbMesures();
      sens_barre = param_serie.getSensBarre();
      sens_fond = param_serie.getSensFond();
      vitesseFond = param_serie.getVitesseFond();
      nb_series_restantes--;
      mesure_restantes --;
    } else {
      Log.d(TAG, "parametres  nuls");
      mode_mesure = 0;
      mesure_restantes = 5;
      sens_barre = 0;
      sens_fond = 1;
      vitesseFond = 1;
    }
    Log.i("VRACTIVITY", " mode : " + String.valueOf(mode_mesure));

    //Gestion de la VR
    SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
    Sensor sensor = sensorManager.getDefaultSensor(9);
    SensorEventListener sensorEventListener = new SensorEventListener() {
      public void onAccuracyChanged(Sensor param1Sensor, int param1Int) {}

      public void onSensorChanged(SensorEvent param1SensorEvent) {
        float f1 = param1SensorEvent.values[0];
        float f2 = param1SensorEvent.values[1];
        float f3 = param1SensorEvent.values[2];
        Float[] arrayOfFloat = VrActivity.this.alpha;
        double d = ((float)Math.atan2(-f2, f1) * 180.0F);
        Double.isNaN(d);
        arrayOfFloat[0] = (float) (d / Math.PI);
      }
    };

    // Gestion de la connection Bluetooth avec la manette
    this.service = new MyBluetoothService((Context)this, mHandler);
    IntentFilter intentFilter = new IntentFilter("RECEIVED");
    registerReceiver(this.Receiver, intentFilter);
    this.service.startServer(this.MY_UUID);
    sensorManager.registerListener(sensorEventListener, sensor, 3);


    // TODO(b/139010241): Avoid that action and status bar are displayed when pressing settings
    // button.
    setImmersiveSticky();
    View decorView = getWindow().getDecorView();
    decorView.setOnSystemUiVisibilityChangeListener(
        (visibility) -> {
          if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
            setImmersiveSticky();
          }
        });

    // Forces screen to max brightness.
    WindowManager.LayoutParams layout = getWindow().getAttributes();
    layout.screenBrightness = 1.f;
    getWindow().setAttributes(layout);

    // Prevents screen from dimming/locking.
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
  }
  /*
  Gestion de la fin de la mesure, communication du score à HomeActivity
   */
  private void endMesure() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Mesure Terminée")
            .setMessage("Votre score est de :" + mScore)
            .setCancelable(false)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.putExtra(RESULT_SCORE, mScore);
                setResult(Activity.RESULT_OK, intent);
                finish();
              }
            })
            .create()
            .show();
  }

  /*
  Gestion de la communication avec la manette
  Byte_d = tourner à droite
  Byte_g = tourner à gauche
  Byte_dl = tourner lentement à droite
  Byte_gl = tourner lentement à gauche
  Byte_t = resultat d'une mesure, si il n'y a plus de mesure restantes, affichage des résultats
   */
  private final Handler mHandler = new Handler(Looper.getMainLooper()) {
    @Override
    public void handleMessage(Message param1Message) {
      super.handleMessage(param1Message);
      if (param1Message.what == 0) {
        String command = new String((byte[]) param1Message.obj, 0, param1Message.arg1);
        //La
        if (command.equals(new String(HomeActivity.byte_d))) {
          Log.i("InputStream","Received byte" + command);
          tourne = 1;
        }
        else if (command.equals(new String(HomeActivity.byte_dl))) {
          Log.i("InputStream","Received byte" + command);
          tourne = 2;
        }

        else if (command.equals(new String(HomeActivity.byte_g))) {
          Log.i("InputStream","Received byte" + command);
          tourne = -1;
        }
        else if (command.equals(new String(HomeActivity.byte_gl))) {
          Log.i("InputStream","Received byte" + command);
          tourne = -2;
        }

        else if (command.equals(new String(HomeActivity.byte_s))) {
          tourne = 0;
        }
        else if (command.equals(new String(HomeActivity.byte_t))) {
          mScore.add(mAngle); //TODO à changer pour avoir une liste de score pour chaque série...
          if (mesure_restantes > 0) {
            Log.d(TAG, "nb mesures restantes : " + String.valueOf(mesure_restantes + "  ; nb series restantes : " + nb_series_restantes));
            mesure_restantes --;
            tourne =  10;
          } else if (nb_series_restantes > 0) {
            nb_series_restantes --;
            num_serie ++;
            Log.d(TAG, "nb séries restantes : " + String.valueOf(nb_series_restantes));
            ParameterSeries param_serie = listeParametres.get(num_serie);
            mode_mesure = param_serie.getMode();
            mesure_restantes = param_serie.getNbMesures();
            sens_barre = param_serie.getSensBarre();
            sens_fond = param_serie.getSensFond();
            vitesseFond = param_serie.getVitesseFond();
            mesure_restantes --;
            Log.i(TAG, "nb mesures restantes: " + String.valueOf(mesure_restantes) + " ; mode: " + String.valueOf(mode_mesure) + " ; barre: " +
                    String.valueOf(sens_barre) + " ; fond: " + String.valueOf(sens_fond));
            tourne = 10;


          }else {
            String strScore = mScore.stream().map(Object::toString).collect(Collectors.joining(", "));
            VrActivity.this.service.write(strScore.getBytes(StandardCharsets.UTF_8));
            endMesure();
          }
        } else {
          Log.i("Ecran: HandleMessage", "unknown byte" + command + ", " + new String(HomeActivity.byte_g));
        }
      }
    }
  };

  @Override
  protected void onPause() {
    super.onPause();
    nativeOnPause(nativeApp);
    glView.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();

    // On Android P and below, checks for activity to READ_EXTERNAL_STORAGE. When it is not granted,
    // the application will request them. For Android Q and above, READ_EXTERNAL_STORAGE is optional
    // and scoped storage will be used instead. If it is provided (but not checked) and there are
    // device parameters saved in external storage those will be migrated to scoped storage.
    if (VERSION.SDK_INT < VERSION_CODES.Q && !isReadExternalStorageEnabled()) {
      requestPermissions();
      return;
    }

    glView.onResume();
    nativeOnResume(nativeApp);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    nativeOnDestroy(nativeApp);
    nativeApp = 0;
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
      setImmersiveSticky();
    }
  }

  private class Renderer implements GLSurfaceView.Renderer {
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
      nativeOnSurfaceCreated(nativeApp, mode_mesure);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
      nativeSetScreenParams(nativeApp, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
      nativeOnDrawFrame(nativeApp, tourne, sens_fond, mode_mesure, vitesseFond);
      //Log.i("InputStream","NATIVE DRAW ON FRAME OK");
      // Conversion de l'angle qui est en radians en degres
      mAngle = getAngle(nativeApp);
      mAngle = (float) (mAngle % Math.PI);
      mAngle = (float) ((mAngle*180.0)/Math.PI);
      if (mAngle > 90.0f){
        mAngle = mAngle - 180.0f;
      }
    mAngle = Math.round(mAngle * 100f) / 100f;

     }
  }

  /** Callback for when close button is pressed. */
  public void closeSample(View view) {
    Log.d(TAG, "Leaving VR sample");
    finish();
  }

  /** Callback for when settings_menu button is pressed. */
  public void showSettings(View view) {
    PopupMenu popup = new PopupMenu(this, view);
    MenuInflater inflater = popup.getMenuInflater();
    inflater.inflate(R.menu.settings_menu, popup.getMenu());
    popup.setOnMenuItemClickListener(this);
    popup.show();

  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    if (item.getItemId() == R.id.switch_viewer) {
      nativeSwitchViewer(nativeApp);
      return true;
    }
    return false;
  }

  /**
   * Checks for READ_EXTERNAL_STORAGE permission.
   *
   * @return whether the READ_EXTERNAL_STORAGE is already granted.
   */
  private boolean isReadExternalStorageEnabled() {
    return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        == PackageManager.PERMISSION_GRANTED;
  }

  /** Handles the requests for activity permission to READ_EXTERNAL_STORAGE. */
  private void requestPermissions() {
    final String[] permissions = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE};
    ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE);
  }

  /**
   * Callback for the result from requesting permissions.
   *
   * <p>When READ_EXTERNAL_STORAGE permission is not granted, the settings view will be launched
   * with a toast explaining why it is required.
   */
  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (!isReadExternalStorageEnabled()) {
      Toast.makeText(this, R.string.read_storage_permission, Toast.LENGTH_LONG).show();
      if (!ActivityCompat.shouldShowRequestPermissionRationale(
          this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
        // Permission denied with checking "Do not ask again". Note that in Android R "Do not ask
        // again" is not available anymore.
        launchPermissionsSettings();
      }
      finish();
    }
  }

  private void launchPermissionsSettings() {
    Intent intent = new Intent();
    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    intent.setData(Uri.fromParts("package", getPackageName(), null));
    startActivity(intent);
  }

  private void setImmersiveSticky() {
    getWindow()
        .getDecorView()
        .setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
  }

  //Méthodes qui communiquent avec le code C++

  private native long nativeOnCreate(AssetManager assetManager);

  private native void nativeOnDestroy(long nativeApp);

  private native void nativeOnSurfaceCreated(long nativeApp, int ModeMesure);

  private native void nativeOnDrawFrame(long nativeApp, int tourne, int sens_fond, int mode, float vitesseFond);

  private native void nativeOnPause(long nativeApp);

  private native void nativeOnResume(long nativeApp);

  private native void nativeSetScreenParams(long nativeApp, int width, int height);

  private native void nativeSwitchViewer(long nativeApp);

  private native float getAngle(long nativeApp);
}
