package com.example.bluetoothcarcontroller;


import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.bluetoothcarcontroller.Bluetooth.DeviceAdapter;
import com.example.bluetoothcarcontroller.Fragments.AutopilotFragment;
import com.example.bluetoothcarcontroller.Fragments.BluetoothFragment;
import com.example.bluetoothcarcontroller.Fragments.JoystickFragment;
import com.example.bluetoothcarcontroller.Fragments.SensorFragment;
import com.example.bluetoothcontroler.R;
import com.gauravk.bubblenavigation.BubbleNavigationConstraintView;

import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    public static final java.util.UUID UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    int SELECT_DEVICE_REQUEST_CODE = -1;
    private static final byte NULL_MESSAGE = -1;

    // Arduino car commands
    public static final byte STOP = 0;
    private static byte CURRENT_STATE = STOP;
    private static final byte FORWARD = 1;
    private static final byte BACKWARD = 2;
    private static final byte RIGHT_ROTATE_BACKWARDS = 3;
    private static final byte LEFT_ROTATE_BACKWARDS = 4;
    private static final byte RIGHT_ROTATE_FORWARDS = 5;
    private static final byte LEFT_ROTATE_FORWARDS = 6;
    public static final byte SENSOR_ON = 7;
    public static final byte SENSOR_OFF = 8;
    public static final byte AUTOMATIC_ON = 9;
    public static final byte AUTOMATIC_OFF = 10;


    public static boolean isConnectedToBluetoothReceiver = false;
    public static BluetoothDevice connectedDevice = null;

    private static Handler mainHandler;

    private static final int MAX_STRENGTH = 255;
    private static final int MIN_STRENGTH = 0;

    private static final int JOYSTICK_FRAGMENT_POSITION = 0;
    private static final int AUTOPILOT_FRAGMENT_POSITION = 1;
    private static final int SENSOR_FRAGMENT_POSITION = 2;
    public static final int BLUETOOTH_FRAGMENT_POSITION = 3;

    private int lastPosition = -1;


    private static final float ONE_PERCENT_OF_STRENGTH = (float)(MAX_STRENGTH - MIN_STRENGTH) / 100;

    private static FragmentManager fragmentManager;
    public static BubbleNavigationConstraintView bubbleNavigationConstraintView;


    @Override
    protected void onResume() {
        super.onResume();

        TextView connected = findViewById(R.id.connected);
        TextView notConnected = findViewById(R.id.notConnected);

        isConnectedToBluetoothReceiver = isConnected();

        sendData(STOP, 5, connected, notConnected);

        connected.setVisibility(isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);
        notConnected.setVisibility(!isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bubbleNavigationConstraintView = findViewById(R.id.bottom_navigation_constraint);

        // to update GUI from threads
        mainHandler = new Handler(this.getMainLooper());

        // make sure that app is always portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // is connected to HC-05
        isConnectedToBluetoothReceiver = isConnected();

        fragmentManager = getSupportFragmentManager();
        switchFragment(savedInstanceState, JoystickFragment.class, this);

        bubbleNavigationConstraintView.setNavigationChangeListener((view, position) -> {
            // turnoff auto
            if(lastPosition == AUTOPILOT_FRAGMENT_POSITION) {
                sendData(AUTOMATIC_OFF, 5, null, null);
            }
            //navigation changed, do something
            lastPosition = position;
            switch (position){
                case JOYSTICK_FRAGMENT_POSITION:
                    switchFragment(savedInstanceState, JoystickFragment.class, this);
                    break;
                case AUTOPILOT_FRAGMENT_POSITION:
                    switchFragment(savedInstanceState, AutopilotFragment.class, this);
                    break;
                case SENSOR_FRAGMENT_POSITION:
                    switchFragment(savedInstanceState, SensorFragment.class, this);
                    break;
                case BLUETOOTH_FRAGMENT_POSITION:
                    BluetoothAdapter.getDefaultAdapter().enable();
                    switchFragment(savedInstanceState, BluetoothFragment.class, this);
                    break;
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



    public static void showAlert(Context context, String message, String text, String conformationText, Runnable action){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(message);
        alert.setMessage(text);
        alert.setPositiveButton(conformationText, (dialog, which) -> {
            dialog.cancel();
            action.run();
        });

        alert.show();
    }

    public static boolean isConnected(){
        try {
            OutputStream outputStream = DeviceAdapter.getOutputStream();
            outputStream.write(NULL_MESSAGE);
            isConnectedToBluetoothReceiver = true;
            return true;

        }catch (Exception e){
            isConnectedToBluetoothReceiver = false;
            return false;

        }
    }

    public static boolean isConnected(TextView connected, TextView notConnected){
        try {
            OutputStream outputStream = DeviceAdapter.getOutputStream();
            outputStream.write(NULL_MESSAGE);
            isConnectedToBluetoothReceiver = true;

            return true;

        }catch (Exception e){
            isConnectedToBluetoothReceiver = false;
            mainHandler.post(()->{
                if(connected != null && notConnected != null) {
                    connected.setVisibility(View.INVISIBLE);
                    notConnected.setVisibility(View.VISIBLE);
                }
            });
            return false;
        }
    }

    /**
     * this function is used to send data from the in app joystick to the car
     * @param angle - angle of the joystick
     * @param strength - strength of the joystick
     * @param connected - @see sendData()
     * @param notConnected - @see sendData()
     */
    public synchronized static void sendDataByBluetooth(int angle, int strength, @Nullable TextView connected, @Nullable TextView notConnected){
            byte normalizedStrength = (byte) (strength * ONE_PERCENT_OF_STRENGTH);
            if(strength<=10){
               if(CURRENT_STATE != STOP){
                   CURRENT_STATE = STOP;
                   sendData(STOP, 5, connected, notConnected);
               }

            }else if(angle>75 && angle < 115){
                if(CURRENT_STATE != FORWARD){
                    CURRENT_STATE = FORWARD;
                    sendData(FORWARD, 5, connected, notConnected);
                }else {
                    sendData(normalizedStrength, 1, connected, notConnected);
                }

            }else if(angle>255 && angle < 295) {
                if(CURRENT_STATE != BACKWARD){
                    CURRENT_STATE = BACKWARD;
                    sendData(BACKWARD, 5, connected, notConnected);
                }else {
                    sendData(normalizedStrength, 1, connected, notConnected);
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
                            sendData(RIGHT_ROTATE_BACKWARDS, 5, connected, notConnected);
                            return;
                        }
                    }else {
                        if(CURRENT_STATE!=RIGHT_ROTATE_FORWARDS){
                            CURRENT_STATE = RIGHT_ROTATE_FORWARDS;
                            sendData(RIGHT_ROTATE_FORWARDS, 5, connected, notConnected);
                            return;
                        }
                    }

                }else {
                    // left
                    if(goBackward){
                        if(CURRENT_STATE!=LEFT_ROTATE_BACKWARDS){
                            CURRENT_STATE = LEFT_ROTATE_BACKWARDS;
                            sendData(LEFT_ROTATE_BACKWARDS, 5, connected, notConnected);
                            return;
                        }
                    }else {
                        if(CURRENT_STATE!=LEFT_ROTATE_FORWARDS){
                            CURRENT_STATE = LEFT_ROTATE_FORWARDS;
                            sendData(LEFT_ROTATE_FORWARDS, 5, connected, notConnected);
                            return;
                        }
                    }


                }
                sendData(fixedStrength, 1, connected, notConnected);
            }


    }

    /**
     * send data in bytes to car via bluetoothOutputStream
     *
     * @param data - data to send
     * @param numberOfTimesToSendData - number of times to send data
     * @param connected - a textView that the function uses to indicate whether the process was successful
     * @param notConnected - a textView that the function uses to indicate whether the process failed
    */
   public static void sendData(byte data, int numberOfTimesToSendData, @Nullable TextView connected, @Nullable TextView notConnected){

        if(isConnected()) {
            for (int i = 0; i < numberOfTimesToSendData; i++) {
                if (numberOfTimesToSendData > 1) System.out.println(data);
                try {
                    DeviceAdapter.getOutputStream().write(data);
                } catch (Exception e) {
                    isConnectedToBluetoothReceiver = false;
                    mainHandler.post(()->{
                        if(connected != null && notConnected != null) {
                            connected.setVisibility(View.INVISIBLE);
                            notConnected.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
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

    public static void switchFragment(Bundle savedInstanceState, Class<? extends Fragment> fragment, Context context){
        try {
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment, savedInstanceState)
                    .commit();
        }catch (Exception e){
            Toast.makeText(context, "Error, something went wrong", Toast.LENGTH_LONG).show();
        }
    }
}