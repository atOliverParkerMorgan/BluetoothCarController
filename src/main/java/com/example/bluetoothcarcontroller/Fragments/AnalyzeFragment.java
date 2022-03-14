package com.example.bluetoothcarcontroller.Fragments;

import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bluetoothcarcontroller.Bluetooth.DeviceAdapter;
import com.example.bluetoothcarcontroller.MainActivity;
import com.example.bluetoothcontroler.R;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AnalyzeFragment extends Fragment {
    TextView connected;
    TextView notConnected;

    private final List<Integer> bluetoothInputStreamData = new ArrayList<>();

    public AnalyzeFragment() {
        super(R.layout.analyze_layout);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // init connect text
        connected = view.findViewById(R.id.connected);
        notConnected = view.findViewById(R.id.notConnected);
        connected.setVisibility(MainActivity.isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);
        notConnected.setVisibility(!MainActivity.isConnectedToBluetoothReceiver? View.VISIBLE: View.INVISIBLE);
        try {
            MainActivity.sendDataByBluetooth(-1, -1, connected, notConnected);
            startReceivingData();
        }catch (Exception ignored){
            Toast.makeText(getContext(), R.string.notConnected, Toast.LENGTH_LONG).show();
        }



    }

    Thread receiveThread =  new Thread(()-> {
        try {
            while (DeviceAdapter.bluetoothSocket.isConnected()) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                if(DeviceAdapter.bluetoothSocket.getInputStream().available()>0) {
                    byte[] data = new byte[DeviceAdapter.bluetoothSocket.getInputStream().available()];
                    Log.d("BYTES RECEIVED", String.valueOf(DeviceAdapter.bluetoothSocket.getInputStream().available()));
                    int Data = DeviceAdapter.bluetoothSocket.getInputStream().read(data);
                    bluetoothInputStreamData.add((int) Data);
                    Log.d("BLUETOOTH DATA: ", Arrays.toString(data));
                }
            }
            Log.d("BLUETOOTH DATA all: ", bluetoothInputStreamData.toString());
        } catch (IOException e) {

            requireActivity().runOnUiThread(()->Toast.makeText(getContext(), "Error, bluetooth not working", Toast.LENGTH_LONG).show());
        }
    });


    void startReceivingData(){
        receiveThread.start();
    }

    void closeReceivingData(){
        receiveThread.interrupt();
    }
}
