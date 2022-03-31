package com.example.bluetoothcarcontroller.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bluetoothcarcontroller.Bluetooth.DeviceAdapter;
import com.example.bluetoothcarcontroller.MainActivity;
import com.example.bluetoothcarcontroller.Threads.ReceiveDataThread;
import com.example.bluetoothcontroler.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AutopilotFragment extends Fragment {
    TextView connected;
    TextView notConnected;
    Button actionButton;
    private static boolean isSearching = false;

    private final List<Integer> bluetoothInputStreamData = new ArrayList<>();

    public AutopilotFragment() {
        super(R.layout.autopilot_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean isConnected = MainActivity.isConnected();


        // init connect text
        connected = view.findViewById(R.id.connected);
        notConnected = view.findViewById(R.id.notConnected);

        actionButton = view.findViewById(R.id.actionButton);

        if (isSearching) {
            actionButton.setText(R.string.terminate);
        } else {
            actionButton.setText(R.string.start);
        }

        if (MainActivity.isConnectedToBluetoothReceiver) {
            MainActivity.sendData(MainActivity.STOP, 5, connected, notConnected);
        }


        actionButton.setOnClickListener(v -> {
            if (isConnected) {
                new Thread(() -> {
                        if (isSearching) {
                            MainActivity.sendData(MainActivity.AUTOMATIC_OFF, 5, connected, notConnected);
                            //MainActivity.sendData(MainActivity.STOP, 5, connected, notConnected);
                            actionButton.setText(R.string.start);
                        } else {
                            MainActivity.sendData(MainActivity.AUTOMATIC_ON, 5, connected, notConnected);
                            actionButton.setText(R.string.terminate);
                        }
                        isSearching = !isSearching;

                }).start();
            } else {
                MainActivity.showAlert(getContext(), "Error","No Bluetooth connection","OK", ()->{});
            }
        });

        connected.setVisibility(MainActivity.isConnectedToBluetoothReceiver ? View.VISIBLE : View.INVISIBLE);
        notConnected.setVisibility(!MainActivity.isConnectedToBluetoothReceiver ? View.VISIBLE : View.INVISIBLE);

        try {
            ReceiveDataThread receiveDataThread = new ReceiveDataThread(
                    DeviceAdapter.bluetoothSocket.getInputStream(), bluetoothInputStreamData,
                    requireActivity(), view.findViewById(R.id.canvas), connected, notConnected);
            receiveDataThread.start();
        } catch (Exception e) {
            // show disconnect
            connected.setVisibility(View.INVISIBLE);
            notConnected.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if(isSearching) {
            actionButton.setText(R.string.terminate);
        }else{
            actionButton.setText(R.string.start);
        }
    }
}
