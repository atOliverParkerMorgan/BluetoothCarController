package com.example.bluetoothcarcontroller.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.bluetoothcarcontroller.Bluetooth.DeviceAdapter;
import com.example.bluetoothcarcontroller.MainActivity;
import com.example.bluetoothcontroler.R;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class JoystickFragment extends Fragment {

    TextView connected;
    TextView notConnected;

    private static ExecutorService executorService;

    public JoystickFragment() {
        super(R.layout.joystick_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(MainActivity.isConnected()) {
            try {
                MainActivity.sendData(MainActivity.STOP, 5);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // init
        executorService = Executors.newSingleThreadExecutor();

        // connected view
        connected = view.findViewById(R.id.connected);
        notConnected = view.findViewById(R.id.notConnected);
        connected.setVisibility(MainActivity.isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);
        notConnected.setVisibility(!MainActivity.isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);


        // joystick logic
        JoystickView joystick = view.findViewById(R.id.joystickController);
        joystick.setOnMoveListener((angle, strength) -> {
            if(DeviceAdapter.getOutputStream()!=null){
                executorService.submit(()-> MainActivity.sendDataByBluetooth(angle, strength, connected, notConnected));
            }
        });

    }
}