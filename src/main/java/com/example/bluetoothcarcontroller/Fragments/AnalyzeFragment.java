package com.example.bluetoothcarcontroller.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bluetoothcarcontroller.MainActivity;
import com.example.bluetoothcontroler.R;

public class AnalyzeFragment extends Fragment {
    TextView connected;
    TextView notConnected;

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
        }catch (Exception ignored){
            Toast.makeText(getContext(), R.string.notConnected, Toast.LENGTH_LONG).show();
        }



    }
}
