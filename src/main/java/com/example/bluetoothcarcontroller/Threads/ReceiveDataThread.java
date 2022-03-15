package com.example.bluetoothcarcontroller.Threads;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetoothcarcontroller.Bluetooth.DeviceAdapter;
import com.example.bluetoothcarcontroller.AutopilotCanvasView;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ReceiveDataThread extends Thread {
    InputStream inputStream;
    List<Integer> extractedData;
    Context context;
    AutopilotCanvasView autopilotCanvasView;

    public ReceiveDataThread(InputStream inputStream, List<Integer> extractedData, Context context, AutopilotCanvasView autopilotCanvasView) {
        this.inputStream = inputStream;
        this.extractedData = extractedData;
        this.context = context;
        this.autopilotCanvasView = autopilotCanvasView;
    }

    @Override
    public void run() {

        try {
            int angle = 0;
            while (DeviceAdapter.bluetoothSocket.isConnected()) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }

                if(inputStream.available()>0) {

                    int dist = inputStream.read();

                    Log.d("Dist:", String.valueOf(dist));
                    angle+=10;
                    // calculating found point
                    // known: distance of the two points; angle; x2, y2
                    // unknown: x, y of point
                    // substitution:
                    // x = x1-x2; y = y1-y2
                    // equations:


                    // dist**2 = ((x1-x2)**2+(y1-y2)**2)


                    // x = (dist**2-y1**2)**0.5
                    // y = tg(angle)x1

                    // x**2 = (dist**2 - tg(angle)**2 * x**2)
                    // x**2 + tg(angle)**2 * x**2 - dist**2 = 0
                    // x**2 * (1 + tg(angle)**2) - dist**2 = 0
                    // x = (+- (4 * dist**2 * (1 + tg(angle)**2)) ** 0.5 ) / (2*(1 + tg(angle)**2))

                    double a = 1 + Math.pow(Math.tan(Math.toRadians(angle)), 2);
                    double c = -Math.pow(dist, 2);
                    double D = -4 * a * c;

                    double x = ( Math.sqrt(D)) / (2 * a);

                    double y = Math.tan(Math.toRadians(angle)) * x;

                    float x1 = (float) (x + autopilotCanvasView.mPosX);
                    float y1 = (float) (y + autopilotCanvasView.mPosY);

                    Log.d("received: X; Y:", x1+"; "+y1);

                    autopilotCanvasView.addPoint(new float[]{x1, y1}, false);

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
