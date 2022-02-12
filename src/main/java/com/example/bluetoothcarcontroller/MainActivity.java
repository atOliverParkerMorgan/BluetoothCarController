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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.bluetoothcarcontroller.Bluetooth.BluetoothActivity;
import com.example.bluetoothcarcontroller.Bluetooth.DeviceAdapter;
import com.example.bluetoothcontroler.R;

import java.io.IOException;
import java.io.OutputStream;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {
    public static final java.util.UUID UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    int SELECT_DEVICE_REQUEST_CODE = -1;
    private static boolean isSensorOn = false;
    private static boolean isSearchingAutomatic = false;
    public static boolean isConnectedToBluetoothReceiver = false;
    public static BluetoothDevice connectedDevice = null;

    private final int MAX_STRENGTH = 255;
    private final int MIN_STRENGTH = 0;

    private final float ONE_PERCENT_OF_STRENGTH = (float)(MAX_STRENGTH - MIN_STRENGTH) / 100;

    @Override
    protected void onResume() {
        super.onResume();
        TextView connected = findViewById(R.id.connected);
        TextView notConnected = findViewById(R.id.notConnected);
        isConnectedToBluetoothReceiver = isConnected();
        connected.setVisibility(isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);
        notConnected.setVisibility(!isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);

        ImageButton sensor = findViewById(R.id.sensor_button);
        if(isSensorOn){
            sensor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_visibility_24));
        }else{
            sensor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_visibility_off_24));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton sensor = findViewById(R.id.sensor_button);
        ImageButton pair = findViewById(R.id.bluetooth_button);
        ImageButton automatic = findViewById(R.id.autoamtic_button);

        Handler mainHandler = new Handler(this.getMainLooper());

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        isConnectedToBluetoothReceiver = isConnected();

        OutputStream outputStream = DeviceAdapter.getOutputStream();

        TextView connected = findViewById(R.id.connected);
        TextView notConnected = findViewById(R.id.notConnected);

        connected.setVisibility(isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);
        notConnected.setVisibility(!isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);

        if(isSensorOn){
            sensor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_visibility_24));
        }else{
            sensor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_visibility_off_24));
        }

        // left joystick
        JoystickView joystickRight = findViewById(R.id.joystick);
        joystickRight.setOnMoveListener((angle, strength) -> {
           // Log.d("Angle: ", String.valueOf(angle));

            if(outputStream!=null){
                try {
                    new Thread(() -> {

                        try {
                            int normalizedStrength = (int) (strength * ONE_PERCENT_OF_STRENGTH);

                            StringBuilder output = new StringBuilder();
                            if(isSensorOn) output.append("E");
                            else output.append("N");
                            if(isSearchingAutomatic) output.append("A");
                            else output.append("N");
                            StringBuilder strengthString = new StringBuilder(String.valueOf(normalizedStrength));
                            while (strengthString.length()<3){
                                strengthString.append("X");
                            }
                            if(strength<5){
                                for (int i = 0; i < 4; i++) {
                                    output.append("S").append("XXX");
                                }

                            }else if(angle>75 && angle < 115){
                                for (int i = 0; i < 4; i++) {
                                    output.append("F").append(strengthString);
                                }

                            }else if(angle>255 && angle < 295) {
                                for (int i = 0; i < 4; i++) {
                                    output.append("B").append(strengthString);
                                }
                            }else {
                                char dir = normalizedStrength <= MAX_STRENGTH/2 ? 'B': 'F';
                                int fixedStrength = normalizedStrength < MAX_STRENGTH / 2 ? MAX_STRENGTH - normalizedStrength : normalizedStrength;
                                if(dir == 'F') fixedStrength -= 15*ONE_PERCENT_OF_STRENGTH;

                                StringBuilder fixedStrengthString = new StringBuilder(String.valueOf((int)(fixedStrength)));
                                while (fixedStrengthString.length()<3){
                                    fixedStrengthString.append("X");
                                }
                                if(angle < 80 || angle > 290){
                                    // right
                                    output.append(dir).append(fixedStrengthString).append("F").append(MAX_STRENGTH).append(dir).append(fixedStrengthString).append("F").append(MAX_STRENGTH);
                                }else if(angle > 80 || angle < 260){
                                    //left
                                    output.append("F").append(MAX_STRENGTH).append(dir).append(fixedStrengthString).append("F").append(MAX_STRENGTH).append(dir).append(fixedStrengthString);
                                }
                            }
                            Log.d("OUTPUT: ", output.toString());
                            Log.d("OUTPUT: ", String.valueOf(output.toString().length()));
                            outputStream.write(output.toString().getBytes());

                        } catch (IOException e) {
                            isConnectedToBluetoothReceiver = false;
                            mainHandler.post(()->{
                                connected.setVisibility(View.INVISIBLE);
                                notConnected.setVisibility(View.VISIBLE);
                            });

                        }

                    }).start();
                }catch (Exception e) {
                    isConnectedToBluetoothReceiver = false;
                    mainHandler.post(()->{
                        connected.setVisibility(View.INVISIBLE);
                        notConnected.setVisibility(View.VISIBLE);
                    });

                }
            }
        });

        pair.setOnClickListener(view -> startActivity(new Intent(this, BluetoothActivity.class)));
        sensor.setOnClickListener(view -> {
           // sensor.setImageDrawable(R.drawable.);
            isSensorOn = !isSensorOn;
            if(isSensorOn){
                sensor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_visibility_24));
            }else{
                sensor.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_baseline_visibility_off_24));
            }
        });
        automatic.setOnClickListener(view -> {
           // automatic.setImageDrawable(R.drawable.);
            isSearchingAutomatic = !isSearchingAutomatic;
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
            outputStream.write("NNSXXXSXXXSXXXSXXX!".getBytes());

            return true;
        }catch (Exception e){
            return false;
        }
    }

}