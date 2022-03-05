package com.example.bluetoothcarcontroller;


import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.bluetoothcarcontroller.Bluetooth.BluetoothActivity;
import com.example.bluetoothcarcontroller.Bluetooth.DeviceAdapter;
import com.example.bluetoothcontroler.R;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {
    public static final java.util.UUID UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    int SELECT_DEVICE_REQUEST_CODE = -1;
    private static final byte NULL_MESSAGE = -1;

    private static final byte STOP = 0;
    private static byte CURRENT_STATE = STOP;
    private static final byte FORWARD = 1;
    private static final byte BACKWARD = 2;
    private static final byte RIGHT_ROTATE_BACKWARDS = 3;
    private static final byte LEFT_ROTATE_BACKWARDS = 4;
    private static final byte RIGHT_ROTATE_FORWARDS = 5;
    private static final byte LEFT_ROTATE_FORWARDS = 6;
    private static final byte SENSOR_ON = 7;
    private static final byte SENSOR_OFF = 8;
    private static final byte AUTOMATIC_ON = 9;
    private static final byte AUTOMATIC_OFF = 10;


    private static boolean isSensorStateChange = false;
    private static boolean isSensorOn = false;

    private static boolean isSearchingAutomaticStateChange = false;
    private static boolean isSearchingAutomatic = false;


    public static boolean isConnectedToBluetoothReceiver = false;
    public static BluetoothDevice connectedDevice = null;

    private static Handler mainHandler;

    private static final int MAX_STRENGTH = 255;
    private static final int MIN_STRENGTH = 0;

    private static ExecutorService executorService;

    private static final float ONE_PERCENT_OF_STRENGTH = (float)(MAX_STRENGTH - MIN_STRENGTH) / 100;

    ImageButton sensor;
    ImageButton automatic;


    @Override
    protected void onResume() {
        super.onResume();
        TextView connected = findViewById(R.id.connected);
        TextView notConnected = findViewById(R.id.notConnected);
        isConnectedToBluetoothReceiver = isConnected();
        connected.setVisibility(isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);
        notConnected.setVisibility(!isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);

        sensor = findViewById(R.id.sensor_button);
        setImages();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init
        executorService = Executors.newSingleThreadExecutor();

        sensor = findViewById(R.id.sensor_button);
        ImageButton pair = findViewById(R.id.bluetooth_button);
        automatic = findViewById(R.id.autoamtic_button);

        // to update GUI from threads
        mainHandler = new Handler(this.getMainLooper());

        // make sure that app is always portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // is connected to HC-05
        isConnectedToBluetoothReceiver = isConnected();


        TextView connected = findViewById(R.id.connected);
        TextView notConnected = findViewById(R.id.notConnected);
        connected.setVisibility(isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);
        notConnected.setVisibility(!isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);

        setImages();


        // joystick logic
        JoystickView joystick = findViewById(R.id.joystick);
        joystick.setOnMoveListener((angle, strength) -> {
            if(DeviceAdapter.getOutputStream()!=null){
                executorService.submit(()-> sendDataByBluetooth(angle, strength, connected, notConnected));
            }
        });


        pair.setOnClickListener(view -> startActivity(new Intent(this, BluetoothActivity.class)));
        sensor.setOnClickListener(view -> {
           // sensor.setImageDrawable(R.drawable.);
            isSensorStateChange = true;
            isSensorOn = !isSensorOn;
            setImages();
            try {
                sendDataByBluetooth(-1, -1, connected, notConnected);
            }catch (Exception e){
                Toast.makeText(this, R.string.notConnected, Toast.LENGTH_LONG).show();
            }
        });
        automatic.setOnClickListener(view -> {
           // automatic.setImageDrawable(R.drawable.);
            isSearchingAutomatic = !isSearchingAutomatic;
            isSearchingAutomaticStateChange = true;
            setImages();
            try {
                sendDataByBluetooth(-1, -1, connected, notConnected);
                startActivity(new Intent(this, AnalyzeActivity.class));

            }catch (Exception e){
                Toast.makeText(this, R.string.notConnected, Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, AnalyzeActivity.class));
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        BluetoothDevice deviceToPair = null;
        if (data != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                deviceToPair = data.getParcelableExtra(
                        CompanionDeviceManager.EXTRA_DEVICE
                );
            }
        }
        if (requestCode == SELECT_DEVICE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {


                if (deviceToPair != null) {
                    deviceToPair.createBond();
                    // ... Continue interacting with the paired device.
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    public static void showAlert(Context context, String message, String text, Runnable action){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(message);
        alert.setMessage(text);
        alert.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            dialog.cancel();
            action.run();
        });

        alert.show();
    }

    public static boolean isConnected(){
        try {
            OutputStream outputStream = DeviceAdapter.getOutputStream();
            outputStream.write(NULL_MESSAGE);

            return true;

        }catch (Exception e){
            return false;
        }
    }

    public synchronized static void sendDataByBluetooth(int angle, int strength, TextView connected, TextView notConnected){
        try {
            byte normalizedStrength = (byte) (strength * ONE_PERCENT_OF_STRENGTH);


            if(isSensorStateChange){
                byte state = isSensorOn? SENSOR_ON: SENSOR_OFF;
                sendData(state, 5);
                isSensorStateChange = false;
            }
            else if(isSearchingAutomaticStateChange){
                byte state = isSearchingAutomatic? AUTOMATIC_ON: AUTOMATIC_OFF;
                sendData(state, 5);
                isSearchingAutomaticStateChange = false;
            }

            else if(strength<=10){
               if(CURRENT_STATE != STOP){
                   CURRENT_STATE = STOP;
                   sendData(STOP, 5);
               }

            }else if(angle>75 && angle < 115){
                if(CURRENT_STATE != FORWARD){
                    CURRENT_STATE = FORWARD;
                    sendData(FORWARD, 5);
                }else {
                    sendData(normalizedStrength, 1);
                }

            }else if(angle>255 && angle < 295) {
                if(CURRENT_STATE != BACKWARD){
                    CURRENT_STATE = BACKWARD;
                    sendData(BACKWARD, 5);
                }else {
                    sendData(normalizedStrength, 1);
                }
            }else {
                Log.d("STRENGTH: ", String.valueOf(strength));
                boolean goBackward = strength <= 40;
                byte fixedStrength = goBackward ? (byte) (MAX_STRENGTH - normalizedStrength/2) : normalizedStrength;


                if(angle < 80 || angle > 290){

                    // right
                    if(goBackward){
                        if(CURRENT_STATE!=RIGHT_ROTATE_BACKWARDS){
                            CURRENT_STATE = RIGHT_ROTATE_BACKWARDS;
                            sendData(RIGHT_ROTATE_BACKWARDS, 5);
                            return;
                        }
                    }else {
                        if(CURRENT_STATE!=RIGHT_ROTATE_FORWARDS){
                            CURRENT_STATE = RIGHT_ROTATE_FORWARDS;
                            sendData(RIGHT_ROTATE_FORWARDS, 5);
                            return;
                        }
                    }
                    sendData(fixedStrength, 1);

                }else {
                    // left
                    if(goBackward){
                        if(CURRENT_STATE!=LEFT_ROTATE_BACKWARDS){
                            CURRENT_STATE = LEFT_ROTATE_BACKWARDS;
                            sendData(LEFT_ROTATE_BACKWARDS, 5);
                            return;
                        }
                        sendData(fixedStrength, 1);
                    }else {
                        if(CURRENT_STATE!=LEFT_ROTATE_FORWARDS){
                            CURRENT_STATE = LEFT_ROTATE_FORWARDS;
                            sendData(LEFT_ROTATE_FORWARDS, 5);
                            return;
                        }
                        sendData((byte) (fixedStrength), 1);
                    }


                }
            }


        } catch (IOException e) {
            isConnectedToBluetoothReceiver = false;
            mainHandler.post(()->{
                connected.setVisibility(View.INVISIBLE);
                notConnected.setVisibility(View.VISIBLE);
            });

        }
    }

    static void sendData(byte data, int numberOfTimesToSendData) throws IOException{
        for (int i = 0; i < numberOfTimesToSendData; i++) {
            if(numberOfTimesToSendData>1) System.out.println(data);
            DeviceAdapter.getOutputStream().write(data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            DeviceAdapter.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setImages(){
        if(isSensorOn){
            sensor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_visibility_24));
        }else{
            sensor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_visibility_off_24));
        }
        if(isSearchingAutomatic){
            automatic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_filter_tilt_shift_24));
        }else {
            automatic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_control_camera_24));
        }

    }
}