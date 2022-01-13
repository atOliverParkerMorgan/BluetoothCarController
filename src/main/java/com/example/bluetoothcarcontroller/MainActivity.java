package com.example.bluetoothcarcontroller;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.bluetoothcarcontroller.Bluetooth.BluetoothActivity;
import com.example.bluetoothcarcontroller.Bluetooth.Device;
import com.example.bluetoothcarcontroller.Bluetooth.DeviceAdapter;
import com.example.bluetoothcontroler.R;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Set;
import java.util.UUID;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {
    int SELECT_DEVICE_REQUEST_CODE = -1;
    public static final java.util.UUID UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        try {
            OutputStream adapterOutputStream = DeviceAdapter.getOutputStream();

            final OutputStream outputStream = null == adapterOutputStream ?getOutputStream(): adapterOutputStream;


            // left joystick
            JoystickView joystickRight = findViewById(R.id.joystick);
            joystickRight.setOnMoveListener((angle, strength) -> {
                if(outputStream!=null){
                    new Thread(()-> {
                        try {
                            outputStream.write(angle);
                        } catch (IOException e) {
                            exceptionToast();
                        }
                    }).start();
                }
            });

            ImageButton pair = findViewById(R.id.bluetooth_button);
            pair.setOnClickListener(view -> startActivity(new Intent(this, BluetoothActivity.class)));
        } catch (IOException e) {
            exceptionToast();
        }


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

    public static void showAlert(Context context, String message, String text){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(message);
        alert.setMessage(text);
        alert.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.cancel());
        alert.show();
    }

    public OutputStream getOutputStream() throws IOException {
        Set<BluetoothDevice> pairedDevice = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        for (BluetoothDevice bd : pairedDevice){
            if (bd.getName().equals("HC-05")) {
                BluetoothSocket bluetoothSocket = bd.createRfcommSocketToServiceRecord(UUID);
                bluetoothSocket.connect();
                return bluetoothSocket.getOutputStream();
            }
        }
        return null;

    }
    public void exceptionToast(){
            Toast.makeText(this,
                    "Oops, something went wrong with the bluetooth connection.",
                    Toast.LENGTH_LONG).show();
    }


}