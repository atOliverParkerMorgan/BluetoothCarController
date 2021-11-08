package com.example.bluetoothcarcontroller;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.bluetoothcarcontroller.Bluetooth.BluetoothActivity;
import com.example.bluetoothcarcontroller.Bluetooth.DeviceAdapter;
import com.example.bluetoothcontroler.R;
import java.io.IOException;
import java.io.OutputStream;
import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends AppCompatActivity {
    int SELECT_DEVICE_REQUEST_CODE = -1;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_landscape);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        OutputStream outputStream = DeviceAdapter.getOutputStream();

        // right joystick
        JoystickView joystickLeft = findViewById(R.id.joystickLeft);
        joystickLeft.setOnMoveListener((angle, strength) -> {

            if(outputStream!=null){
                try {
                   // Toast.makeText(this,"R: angle: "+angle+" | strength: "+strength, Toast.LENGTH_SHORT ).show();
                    Log.d("MOVING", "L: angle: "+angle+" | strength: "+strength);
                    outputStream.write(("L"+strength).getBytes());
                } catch (IOException e) {
                    Toast.makeText(this,
                            "Oops, something went wrong with the bluetooth connection.",
                            Toast.LENGTH_LONG).show();
                }
            }

        });

        // left joystick
        JoystickView joystickRight = findViewById(R.id.joystickRight);
        joystickRight.setOnMoveListener((angle, strength) -> {
            if(outputStream!=null){
                try {
                 //   Toast.makeText(this,"R: angle: "+angle+" | strength: "+strength, Toast.LENGTH_SHORT ).show();
                    outputStream.write(("R"+angle).getBytes());
                } catch (IOException e) {
                    Toast.makeText(this,
                            "Oops, something went wrong with the bluetooth connection.",
                            Toast.LENGTH_LONG).show();
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

    public static void showAlert(Context context, String message, String text){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(message);
        alert.setMessage(text);
        alert.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.cancel());
        alert.show();
    }

}