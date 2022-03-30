package com.example.bluetoothcarcontroller.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.bluetoothcarcontroller.Bluetooth.Device;
import com.example.bluetoothcarcontroller.Bluetooth.DeviceAdapter;
import com.example.bluetoothcarcontroller.MainActivity;
import com.example.bluetoothcontroler.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BluetoothFragment extends Fragment {


    int REQUEST_ACCESS_COARSE_LOCATION = 1;
    BluetoothAdapter bluetoothAdapter;

    ArrayList<Device> devicesArrayList = new ArrayList<>();
    ListView devicesListView;

    public DeviceAdapter deviceAdapter;
    TextView searching;
    ProgressBar searchProgressbar;

    List<BluetoothDevice> shownDevices = new ArrayList<>();

    public BluetoothFragment(){
        super(R.layout.bluetooth_fragment);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(MainActivity.isConnected()) {
            try {
                MainActivity.sendData(MainActivity.STOP, 5);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        locationPermissions();

        devicesListView = view.findViewById(R.id.devices);
        searching  = view.findViewById(R.id.textView);
        searchProgressbar = view.findViewById(R.id.progressBar);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        MainActivity.isConnectedToBluetoothReceiver = MainActivity.isConnected();
        if( MainActivity.isConnectedToBluetoothReceiver) {
            try {
                MainActivity.sendData(MainActivity.STOP, 5);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        deviceAdapter = new DeviceAdapter(requireContext(), devicesArrayList);
        deviceAdapter.notifyDataSetChanged();
        devicesListView.setAdapter(deviceAdapter);

        searching.setText(R.string.searching);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        requireActivity().registerReceiver(myReceiver, intentFilter);
        bluetoothAdapter.startDiscovery();

        new Thread(()-> {
            while (true) {
                getPairedDevices();
            }
        }).start();

    }


    public void getPairedDevices() {
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd: pairedDevice) {
            final Device device = new Device(bd, true, bd.getName().equals("HC-05"));
            if(!shownDevices.contains(bd)){
                shownDevices.add(bd);
                requireActivity().runOnUiThread(() -> {
                    devicesArrayList.add(device);
                    deviceAdapter.notifyDataSetChanged();
                });
            }
        }
    }

    private final BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                boolean contains = false;
                for (Device d: devicesArrayList) {
                    if(d.getDevice().getName().equals(device.getName())){
                        contains = true;
                        break;
                    }
                }

                if(!contains&&device!=null&&device.getName()!=null) {
                    devicesArrayList.add(new Device(device, false, device.getName().equals("HC-05")));
                    deviceAdapter.notifyDataSetChanged();
                }else {
                    for (Device d: devicesArrayList) {
                        if(d.getDevice().getName().equals(device.getName())){
                            d.setHasNotBeenPair();
                        }
                    }
                }
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                MainActivity.connectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                searching.setText(R.string.nothingFound);
                searchProgressbar.setVisibility(View.INVISIBLE);
                //Done searching
            }

        }
    };

    private void locationPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            switch (ContextCompat.checkSelfPermission(requireActivity().getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                    ((TextView) new AlertDialog.Builder(getContext())
                            .setTitle("Runtime Permissions up ahead")
                            .setMessage(Html.fromHtml("<p>To find nearby bluetooth devices please click \"Allow\" on the runtime permissions popup.</p>" +
                                    "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
                            .setNeutralButton("Okay", (dialog, which) -> {
                                if (ContextCompat.checkSelfPermission(requireActivity().getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(requireActivity(),
                                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                            REQUEST_ACCESS_COARSE_LOCATION);
                                }
                            })
                            .show()
                            .findViewById(android.R.id.message))
                            .setMovementMethod(LinkMovementMethod.getInstance());       // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
                    break;
                case PackageManager.PERMISSION_GRANTED:
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        requireActivity().unregisterReceiver(myReceiver);
        super.onDestroy();
    }
}