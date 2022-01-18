package com.example.bluetoothcarcontroller.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.bluetoothcarcontroller.MainActivity;
import com.example.bluetoothcontroler.R;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class DeviceAdapter extends ArrayAdapter <Device > {

    Context context;
    List<Device> devices;



    private static OutputStream outputStream;
    private static BluetoothSocket bluetoothSocket;




    // View lookup cache
    private static class ViewHolder {
        TextView deviceName;
        ImageView deviceImage;
        ProgressBar progressBar;
    }

    public DeviceAdapter(@NonNull Context context, ArrayList<Device> devices) {
        super(context, R.layout.item_device, devices);
        this.context = context;
        this.devices = devices;

    }

    private int lastPosition = -1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Device device = getItem(position);

        final View result;
        ViewHolder viewHolder = new ViewHolder();

        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_device, parent, false);
            viewHolder.deviceName = convertView.findViewById(R.id.device_name);
            viewHolder.progressBar = convertView.findViewById(R.id.pairProgressBar);
            viewHolder.deviceImage = convertView.findViewById(R.id.device_image);

            int image = R.drawable.ic_baseline_bluetooth_audio_24;
            if(device.hasBeenPair()){
                image = R.drawable.ic_baseline_settings_bluetooth_24;
            }
            viewHolder.deviceImage.setBackgroundResource(image);

            View finalConvertView = convertView;
            ViewHolder finalViewHolder = viewHolder;

            convertView.setOnClickListener((view) -> {
                finalViewHolder.progressBar.setVisibility(View.VISIBLE);

                if(!device.isHCO5()) {
                    notHC05();
                }else {
                    connect(device.getDevice(), finalViewHolder, finalConvertView);
                }

            });

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }
        Animation animation = AnimationUtils.loadAnimation(context, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        viewHolder.deviceName.setText(device.getDevice().getName());

        viewHolder.deviceImage.setTag(position);

        if(MainActivity.connectedDevice!=null&&bluetoothSocket!=null){
            if(MainActivity.connectedDevice.getName().equals(device.getDevice().getName())&&bluetoothSocket.isConnected()) {
                viewHolder.deviceImage.setBackgroundResource(R.drawable.ic_baseline_bluetooth_connected_24);
                convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.darker_grey));
                viewHolder.deviceName.setTextColor(ContextCompat.getColor(context, R.color.purple_500));
            }else setDefaultView(viewHolder, convertView);


        }else setDefaultView(viewHolder, convertView);

        // Return the completed view to render on screen
        return convertView;
    }

    public void setDefaultView(ViewHolder viewHolder, View convertView){
        viewHolder.deviceImage.setBackgroundResource(R.drawable.ic_baseline_settings_bluetooth_24);
        convertView.setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey));
        viewHolder.deviceName.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    public void connect(BluetoothDevice device, ViewHolder viewHolder, View finalConvertView) {
        Thread t = new Thread(()-> {

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MainActivity.UUID);
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();
                if(bluetoothSocket.isConnected()) {
                    ((Activity) context).runOnUiThread(() -> {
                        MainActivity.connectedDevice = device;
                        viewHolder.deviceImage.setBackgroundResource(R.drawable.ic_baseline_bluetooth_connected_24);
                        finalConvertView.setBackgroundColor(ContextCompat.getColor(context, R.color.darker_grey));
                        viewHolder.deviceName.setTextColor(ContextCompat.getColor(context, R.color.purple_500));
                        success();
                    });

                }else{
                    connectionError();
                }
            }catch (IOException e) {
               connectionError();
                e.printStackTrace();
            }finally {
                    viewHolder.progressBar.setVisibility(View.INVISIBLE);
            }
        });
        t.start();

    }

    public static OutputStream getOutputStream() {
        return outputStream;
    }

    public void notHC05(){
        MainActivity.showAlert(context, "Oops","The device you have chosen is not named HC-05. HC-05 is the bluetooth receiver that this app uses.", ()->{});
    }

    public void connectionError(){

        ((Activity) context).runOnUiThread(()-> MainActivity.showAlert(context, "Oops","Failed to connect to device", ()->{}));
    }
    public void success(){
        MainActivity.showAlert(context, "Success","You have successfully connected to the bluetooth device.", ()->{
            MainActivity.isConnectedToBluetoothReceiver = true;
            context.startActivity(new Intent(context, MainActivity.class));
        });
    }
}