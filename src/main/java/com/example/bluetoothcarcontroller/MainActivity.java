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
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bluetoothcarcontroller.Bluetooth.BluetoothActivity;
import com.example.bluetoothcarcontroller.Bluetooth.DeviceAdapter;
import com.example.bluetoothcontroler.R;

import java.io.IOException;
import java.io.OutputStream;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {
    public static final java.util.UUID UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    int SELECT_DEVICE_REQUEST_CODE = -1;
    public static boolean isConnectedToBluetoothReceiver = false;
    public static BluetoothDevice connectedDevice = null;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        OutputStream outputStream = DeviceAdapter.getOutputStream();

        TextView connected = findViewById(R.id.connected);
        connected.setVisibility(isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);

        // left joystick
        JoystickView joystickRight = findViewById(R.id.joystick);
        joystickRight.setOnMoveListener((angle, strength) -> {

            if(outputStream!=null){
                try {
                    new Thread(() -> {

                        try {
                            StringBuilder output = new StringBuilder("a" + (angle + 90) + "s" + strength);
                            if(output.length()>10){
                                Toast.makeText(this,
                                        "Error, something went wrong with wrong.",
                                        Toast.LENGTH_LONG).show();
                            }
                            while(output.length()<10) {
                                output.append("!");
                            }

                            outputStream.write(output.toString().getBytes());


                        } catch (IOException e) {
                            Toast.makeText(this,
                                    "Oops, something went wrong with the bluetooth connection.",
                                    Toast.LENGTH_LONG).show();
                            isConnectedToBluetoothReceiver = false;
                        }

                    }).start();
                }catch (Exception e) {
                    Toast.makeText(this,
                            "Oops, something went wrong with the bluetooth connection.",
                            Toast.LENGTH_LONG).show();
                    isConnectedToBluetoothReceiver = false;

                }
            }
        });

        ImageButton pair = findViewById(R.id.bluetooth_button);
        pair.setOnClickListener(view -> startActivity(new Intent(this, BluetoothActivity.class)));


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

}