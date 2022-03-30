package com.example.bluetoothcarcontroller.Fragments;

import android.os.Bundle;
import android.view.View;
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

    private final List<Integer> bluetoothInputStreamData = new ArrayList<>();

    public AutopilotFragment() {
        super(R.layout.autopilot_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(MainActivity.isConnected()) {
            try {
                MainActivity.sendData(MainActivity.STOP, 5);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // init connect text
        connected = view.findViewById(R.id.connected);
        notConnected = view.findViewById(R.id.notConnected);
        connected.setVisibility(MainActivity.isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);
        notConnected.setVisibility(!MainActivity.isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);
        try {
            try {
                ReceiveDataThread receiveDataThread = new ReceiveDataThread(
                        DeviceAdapter.bluetoothSocket.getInputStream(), bluetoothInputStreamData,
                        requireActivity(), view.findViewById(R.id.canvas), connected, notConnected);
                receiveDataThread.start();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), R.string.notConnected, Toast.LENGTH_LONG).show();

            }

        }catch (Exception ignored){}




    }

}
