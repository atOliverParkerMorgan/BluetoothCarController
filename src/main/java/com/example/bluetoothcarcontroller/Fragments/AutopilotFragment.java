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

        actionButton = view.findViewById(R.id.actionButton);

        if (isSearching) {
            actionButton.setText(R.string.terminate);
        } else {
            actionButton.setText(R.string.start);
        }

        if (isConnected) {
            try {
                MainActivity.sendData(MainActivity.STOP, 5);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        actionButton.setOnClickListener(v -> {
            if (isConnected) {
                new Thread(() -> {
                    try {
                        if (isSearching) {
                            MainActivity.sendData(MainActivity.AUTOMATIC_OFF, 5);
                            actionButton.setText(R.string.searching);
                        } else {
                            MainActivity.sendData(MainActivity.AUTOMATIC_ON, 5);
                            actionButton.setText(R.string.terminate);
                        }
                        isSearching = !isSearching;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                Toast.makeText(getContext(), "Error, not connected", Toast.LENGTH_SHORT).show();
            }
        });

        // init connect text
        connected = view.findViewById(R.id.connected);
        notConnected = view.findViewById(R.id.notConnected);
        connected.setVisibility(MainActivity.isConnectedToBluetoothReceiver ? View.VISIBLE : View.INVISIBLE);
        notConnected.setVisibility(!MainActivity.isConnectedToBluetoothReceiver ? View.VISIBLE : View.INVISIBLE);

        try {
            ReceiveDataThread receiveDataThread = new ReceiveDataThread(
                    DeviceAdapter.bluetoothSocket.getInputStream(), bluetoothInputStreamData,
                    requireActivity(), view.findViewById(R.id.canvas), connected, notConnected);
            receiveDataThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            MainActivity.showAlert(getContext(), "Error", "You don't have a bluetooth connection with your car.", "Connect", () -> {
                MainActivity.switchFragment(savedInstanceState, BluetoothFragment.class, getContext());

            });
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
