package com.example.bluetoothcarcontroller.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import com.example.bluetoothcarcontroller.MainActivity;
import com.example.bluetoothcontroler.R;

public class SensorFragment extends Fragment {

    private static final String DEFAULT_VALUE = "10";

    private TextView distText;
    private TextView connected;
    private TextView notConnected;

    private SwitchCompat switchSensor;
    private EditText distEditText;

    SharedPreferences prefs;

    public SensorFragment() {
        super(R.layout.sensor_fragment);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateInputs();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get data from shared prefrences
        switchSensor = view.findViewById(R.id.switchSensor);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String distValue = prefs.getString("dist", DEFAULT_VALUE);
        switchSensor.setChecked(prefs.getBoolean("sensorOn", false));

        connected = view.findViewById(R.id.connected);
        notConnected = view.findViewById(R.id.notConnected);

        // set visibility based on connection
        connected.setVisibility(MainActivity.isConnectedToBluetoothReceiver ? View.VISIBLE : View.INVISIBLE);
        notConnected.setVisibility(!MainActivity.isConnectedToBluetoothReceiver ? View.VISIBLE : View.INVISIBLE);

        // update input fields base on shared preferences
        distEditText = view.findViewById(R.id.distNumber);
        distEditText.setText(distValue);

        Button updateDataButton = view.findViewById(R.id.updateData);
        distText = view.findViewById(R.id.distText);

        // update the input field according to shared preferences
        updateInputs();

        switchSensor.setOnCheckedChangeListener((buttonView, isChecked) -> updateInputs());

        updateDataButton.setOnClickListener(v -> {

            // if not connected throw error
            // first check state of bluetooth receiver
            if(!MainActivity.isConnectedToBluetoothReceiver){
                // if bluetooth receiver connected -> try sending null message via .isConnected()
                if(!MainActivity.isConnected()){
                    MainActivity.showAlert(getContext(), "Error","No Bluetooth connection","OK", ()->{});
                }
            }else {

                // update car settings
                MainActivity.sendData(MainActivity.SENSOR_ON, 1, connected, notConnected);
                String distInput = distEditText.getText().toString();

                // make sure input is valid
                if (distInput.equals("")) {
                    distInput = DEFAULT_VALUE;
                }

                byte dist = (byte) Integer.parseInt(distInput);
                prefs.edit().putString("dist", distInput).apply();

                MainActivity.sendData(dist, 1, connected, notConnected);
                MainActivity.showAlert(getContext(), "Success","The data has been successfully updated","OK", ()->{});

            }
        });
    }

    void updateInputs()  {
        if(!switchSensor.isChecked()){

            distText.setVisibility(View.INVISIBLE);
            distEditText.setVisibility(View.INVISIBLE);
            prefs.edit().putBoolean("sensorOn", false).apply();

            new Thread(()-> MainActivity.sendData(MainActivity.SENSOR_OFF, 1, connected, notConnected)).start();

        }else{
            distText.setVisibility(View.VISIBLE);
            distEditText.setVisibility(View.VISIBLE);

            prefs.edit().putBoolean("sensorOn", true).apply();
        }
    }
}
