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
    private SwitchCompat switchSensor;
    private TextView distText;
    private EditText distEditText;
    private Button updateDataButton;
    SharedPreferences prefs;
    private static final String DEFAULT_VALUE = "10";

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

        switchSensor = view.findViewById(R.id.switchSensor);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        switchSensor.setChecked(prefs.getBoolean("sensorOn", false));

        distText = view.findViewById(R.id.distText);
        distEditText = view.findViewById(R.id.distNumber);
        updateDataButton = view.findViewById(R.id.updateData);

        updateInputs();

        switchSensor.setOnCheckedChangeListener((buttonView, isChecked) -> updateInputs());

        updateDataButton.setOnClickListener(v -> {
            try {
                MainActivity.sendData(MainActivity.SENSOR_ON, 1);
                String distInput = distEditText.getText().toString();
                if(distInput.equals("")) distInput = DEFAULT_VALUE;
                byte dist = (byte) Integer.parseInt(distInput);
                MainActivity.sendData(dist, 1);
            } catch (Exception ignore) {}

        });




    }

    void updateInputs()  {
        if(!switchSensor.isChecked()){
            distText.setVisibility(View.INVISIBLE);
            distEditText.setVisibility(View.INVISIBLE);
            updateDataButton.setVisibility(View.INVISIBLE);
            updateDataButton.setClickable(false);
            prefs.edit().putBoolean("sensorOn", false).apply();
            new Thread(()->{
                try {
                    MainActivity.sendData(MainActivity.SENSOR_OFF, 1);
                } catch (Exception ignore) {
            }}).start();
        }else{
            distText.setVisibility(View.VISIBLE);
            distEditText.setVisibility(View.VISIBLE);
            updateDataButton.setVisibility(View.VISIBLE);
            updateDataButton.setClickable(true);
            prefs.edit().putBoolean("sensorOn", true).apply();
        }
    }
}
