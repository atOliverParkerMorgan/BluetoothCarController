package com.example.bluetoothcarcontroller.Bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.bluetoothcarcontroller.MainActivity;
import com.example.bluetoothcontroler.R;
import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends Activity {


    int REQUEST_ACCESS_COARSE_LOCATION = 1;
    BluetoothAdapter bluetoothAdapter;

    ArrayList<Device> devicesArrayList = new ArrayList<>();
    ListView devicesListView;
    @SuppressLint("StaticFieldLeak")
    public DeviceAdapter deviceAdapter;
    TextView searching;
    ProgressBar searchProgressbar;
    ImageButton backButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        locationPermissions();

        devicesListView = findViewById(R.id.devices);
        searching  = findViewById(R.id.textView);
        searchProgressbar = findViewById(R.id.progressBar);

        deviceAdapter = new DeviceAdapter(this, devicesArrayList);
        deviceAdapter.notifyDataSetChanged();
        devicesListView.setAdapter(deviceAdapter);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener((view)-> startActivity(new Intent(this, MainActivity.class)));

        getPairedDevices();

        //makeDiscoverable();
        new Thread(this::updatingDevices).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    public void getPairedDevices() {
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd: pairedDevice)
            devicesArrayList.add(new Device(bd, true, bd.getName().equals("HC-05")));
        deviceAdapter.notifyDataSetChanged();
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


    private void updatingDevices(){
        searching.setText(R.string.searching);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothAdapter.enable();
        BluetoothActivity.this.registerReceiver(myReceiver, intentFilter);
        // If we're already discovering, stop it
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        // Request discover from BluetoothAdapter
        bluetoothAdapter.startDiscovery();


    }

    private void locationPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            switch (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                    ((TextView) new AlertDialog.Builder(this)
                            .setTitle("Runtime Permissions up ahead")
                            .setMessage(Html.fromHtml("<p>To find nearby bluetooth devices please click \"Allow\" on the runtime permissions popup.</p>" +
                                    "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
                            .setNeutralButton("Okay", (dialog, which) -> {
                                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(BluetoothActivity.this,
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
    protected void onDestroy() {
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }
}