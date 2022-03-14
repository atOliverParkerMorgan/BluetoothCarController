package com.example.bluetoothcarcontroller.Threads;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetoothcarcontroller.Bluetooth.DeviceAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class receiveDataThread extends Thread {
    InputStream inputStream;
    List<Integer> extractedData;
    Context context;

    public receiveDataThread(InputStream inputStream, List<Integer> extractedData, Context context) {
        this.inputStream = inputStream;
        this.extractedData = extractedData;
        this.context = context;
    }

    @Override
    public void run() {

        try {
            while (DeviceAdapter.bluetoothSocket.isConnected()) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                if(inputStream.available()>0) {
                    byte[] data = new byte[inputStream.available()];
                    Log.d("BYTES RECEIVED", String.valueOf(inputStream.available()));
                    int Data = inputStream.read(data);
                    extractedData.add((int) Data);
                    Log.d("BLUETOOTH DATA: ", Arrays.toString(data));
                }
            }
            Log.d("BLUETOOTH DATA all: ", extractedData.toString());
        } catch (IOException e) {
            Log.d("Error: ", extractedData.toString());
            Toast.makeText(context, "Error, bluetooth not working", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void interrupt() {

    }
}
