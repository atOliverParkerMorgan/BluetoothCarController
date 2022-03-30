package com.example.bluetoothcarcontroller.Threads;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetoothcarcontroller.Bluetooth.DeviceAdapter;
import com.example.bluetoothcarcontroller.AutopilotCanvasView;
import com.example.bluetoothcarcontroller.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ReceiveDataThread extends Thread {
    private final InputStream inputStream;
    private final List<Integer> extractedData;
    private final Activity activity;
    private final AutopilotCanvasView autopilotCanvasView;
    private final TextView connected;
    private final TextView notConnected;

    private static final int canvasToCentimert = 4;


    public ReceiveDataThread(InputStream inputStream, List<Integer> extractedData, Activity activity,
                             AutopilotCanvasView autopilotCanvasView, TextView connected,
                             TextView notConnected) {
        this.inputStream = inputStream;
        this.extractedData = extractedData;
        this.activity = activity;
        this.autopilotCanvasView = autopilotCanvasView;
        this.connected = connected;
        this.notConnected = notConnected;
    }

    @Override
    public void run() {

        try {
            int angle = 89;

            while (DeviceAdapter.bluetoothSocket.isConnected()) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                if(inputStream.available()>0) {

                    int dist = inputStream.read();

                    dist *= canvasToCentimert;

                    angle++;

                    Log.d("DIST", String.valueOf(dist));
                    Log.d("ANGLE", String.valueOf(angle));

                    // calculating found point
                    // known: distance of the two points; angle; x2, y2 (cords of car)
                    // unknown: x, y (cords of point)
                    // substitution:
                    // x = x1-x2; y = y1-y2
                    // equations:

                    // dist**2 = ((x1-x2)**2+(y1-y2)**2)

                    // x = (dist**2-y1**2)**0.5
                    // y = tg(angle)x1

                    // solving for x

                    // x**2 = (dist**2 - tg(angle)**2 * x**2)
                    // x**2 + tg(angle)**2 * x**2 - dist**2 = 0
                    // x**2 * (1 + tg(angle)**2) - dist**2 = 0
                    // x = (+- (4 * dist**2 * (1 + tg(angle)**2)) ** 0.5 ) / (2*(1 + tg(angle)**2))

                    double a = 1 + Math.pow(Math.tan(Math.toRadians(angle)), 2);
                    double c = -Math.pow(dist, 2);
                    double D = -4 * a * c;

                    double x = (Math.sqrt(D)) / (2 * a);

                    // get second answer from (+-) if angle is in given quadrant
                    if (angle > 270 || angle < 90) x = -x;

                    double y = Math.tan(Math.toRadians(angle)) * x;

                    float x1 = (float) (x + autopilotCanvasView.mPosX);
                    float y1 = (float) (y + autopilotCanvasView.mPosY);

                    Log.d("Dist: ", String.valueOf(dist));

                    autopilotCanvasView.addPoint(new float[]{x1, y1});

                    if (!MainActivity.isConnected(connected, notConnected)) {
                        activity.runOnUiThread(() -> Toast.makeText(activity, "Error, connection lost", Toast.LENGTH_LONG).show());
                        break;
                    }
                    if(angle == 360) angle = 0;
                }

            }
            Log.d("BLUETOOTH DATA all: ", extractedData.toString());
        } catch (IOException e) {
            Log.d("Error: ", extractedData.toString());
            try {
                MainActivity.sendData(MainActivity.AUTOMATIC_OFF, 1);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            activity.runOnUiThread(()->Toast.makeText(activity, "Error, bluetooth not working", Toast.LENGTH_LONG).show());
        }
    }

    @Override
    public void interrupt() {

    }
}
